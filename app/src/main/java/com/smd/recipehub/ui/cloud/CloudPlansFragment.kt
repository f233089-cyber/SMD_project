package com.smd.recipehub.ui.cloud

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.smd.recipehub.R
import com.smd.recipehub.data.firebase.CloudMealPlan
import com.smd.recipehub.data.firebase.FirestoreRepository
import kotlinx.coroutines.launch

class CloudPlansFragment : Fragment() {
    private val repository = FirestoreRepository()
    private lateinit var adapter: CloudPlanAdapter
    private lateinit var emptyText: TextView
    private lateinit var progress: ProgressBar
    private var listener: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_cloud_plans, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        emptyText = view.findViewById(R.id.cloudEmptyText)
        progress = view.findViewById(R.id.cloudProgress)
        adapter = CloudPlanAdapter(onEdit = ::showPlanDialog, onDelete = ::confirmDelete)
        view.findViewById<RecyclerView>(R.id.cloudRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CloudPlansFragment.adapter
        }
        view.findViewById<Button>(R.id.addCloudPlanButton).setOnClickListener { showPlanDialog(null) }
        startListening()
    }

    override fun onDestroyView() {
        listener?.remove()
        listener = null
        super.onDestroyView()
    }

    private fun startListening() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        progress.isVisible = true
        listener = repository.listenMealPlans(
            uid = uid,
            onChange = { plans ->
                progress.isVisible = false
                adapter.submitList(plans)
                emptyText.isVisible = plans.isEmpty()
            },
            onError = { error ->
                progress.isVisible = false
                Toast.makeText(requireContext(), error.localizedMessage ?: "Firestore error", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun showPlanDialog(existing: CloudMealPlan?) {
        val view = layoutInflater.inflate(R.layout.dialog_cloud_plan, null)
        val titleInput = view.findViewById<EditText>(R.id.cloudTitleInput)
        val dateInput = view.findViewById<EditText>(R.id.cloudDateInput)
        val noteInput = view.findViewById<EditText>(R.id.cloudNoteInput)

        titleInput.setText(existing?.title.orEmpty())
        dateInput.setText(existing?.targetDate.orEmpty())
        noteInput.setText(existing?.note.orEmpty())

        AlertDialog.Builder(requireContext())
            .setTitle(if (existing == null) "Add cloud plan" else "Edit cloud plan")
            .setView(view)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton
                val title = titleInput.text.toString().trim()
                if (title.isBlank()) {
                    Toast.makeText(requireContext(), "Title required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                lifecycleScope.launch {
                    runCatching {
                        if (existing == null) {
                            repository.addPlan(uid, title, dateInput.text.toString(), noteInput.text.toString())
                        } else {
                            repository.updatePlan(
                                existing.copy(
                                    title = title,
                                    targetDate = dateInput.text.toString(),
                                    note = noteInput.text.toString()
                                )
                            )
                        }
                    }.onFailure {
                        Toast.makeText(requireContext(), it.localizedMessage ?: "Save failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .show()
    }

    private fun confirmDelete(plan: CloudMealPlan) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete cloud plan")
            .setMessage("Delete ${plan.title} from Firestore?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    runCatching { repository.deletePlan(plan.id) }
                        .onFailure { Toast.makeText(requireContext(), it.localizedMessage ?: "Delete failed", Toast.LENGTH_SHORT).show() }
                }
            }
            .show()
    }
}
