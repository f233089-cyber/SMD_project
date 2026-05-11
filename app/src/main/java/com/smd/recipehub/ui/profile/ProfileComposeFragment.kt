package com.smd.recipehub.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.smd.recipehub.data.firebase.FirestoreRepository
import com.smd.recipehub.data.local.SavedMealDao
import com.smd.recipehub.ui.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileComposeFragment : Fragment() {
    private val firestoreRepository = FirestoreRepository()
    private val localCount = mutableStateOf(0)
    private val cloudCount = mutableStateOf(0)
    private val photoUri = mutableStateOf<String?>(null)
    private var userListener: ListenerRegistration? = null
    private var plansListener: ListenerRegistration? = null

    private val photoPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri ?: return@registerForActivityResult
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@registerForActivityResult
        photoUri.value = uri.toString()
        lifecycleScope.launch {
            runCatching { firestoreRepository.updateUserPhoto(uid, uri.toString()) }
                .onFailure { Toast.makeText(requireContext(), it.localizedMessage ?: "Photo update failed", Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ProfileScreen(
                    email = currentUser?.email.orEmpty(),
                    uid = currentUser?.uid.orEmpty(),
                    localCount = localCount.value,
                    cloudPlanCount = cloudCount.value,
                    photoUri = photoUri.value,
                    onPickPhoto = {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onLogout = { (activity as? MainActivity)?.logout() }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadLocalCount()
        listenCloudData()
    }

    override fun onDestroyView() {
        userListener?.remove()
        plansListener?.remove()
        userListener = null
        plansListener = null
        super.onDestroyView()
    }

    private fun loadLocalCount() {
        lifecycleScope.launch {
            localCount.value = withContext(Dispatchers.IO) { SavedMealDao(requireContext()).countSavedMeals() }
        }
    }

    private fun listenCloudData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        userListener = firestoreRepository.listenUser(
            uid = uid,
            onChange = { user -> photoUri.value = user?.photoUri },
            onError = { Toast.makeText(requireContext(), it.localizedMessage ?: "User listener error", Toast.LENGTH_SHORT).show() }
        )
        plansListener = firestoreRepository.listenMealPlans(
            uid = uid,
            onChange = { plans -> cloudCount.value = plans.size },
            onError = { Toast.makeText(requireContext(), it.localizedMessage ?: "Plans listener error", Toast.LENGTH_SHORT).show() }
        )
    }
}
