package com.smd.recipehub.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun ensureUserProfile(uid: String, email: String?, displayName: String?, photoUri: String?) {
        val user = mapOf(
            "uid" to uid,
            "email" to email.orEmpty(),
            "displayName" to displayName.orEmpty(),
            "photoUri" to photoUri,
            "updatedAt" to System.currentTimeMillis()
        )
        db.collection(COL_USERS).document(uid).set(user, SetOptions.merge()).await()
    }

    suspend fun updateUserPhoto(uid: String, uri: String) {
        db.collection(COL_USERS).document(uid)
            .set(mapOf("photoUri" to uri, "updatedAt" to System.currentTimeMillis()), SetOptions.merge())
            .await()
    }

    fun listenUser(uid: String, onChange: (AppUser?) -> Unit, onError: (Exception) -> Unit): ListenerRegistration {
        return db.collection(COL_USERS).document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                onChange(snapshot?.toObject(AppUser::class.java))
            }
    }

    fun listenMealPlans(uid: String, onChange: (List<CloudMealPlan>) -> Unit, onError: (Exception) -> Unit): ListenerRegistration {
        return db.collection(COL_MEAL_PLANS)
            .whereEqualTo("ownerId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val plans = snapshot?.documents
                    ?.mapNotNull { it.toObject(CloudMealPlan::class.java) }
                    ?.sortedByDescending { it.updatedAt }
                    .orEmpty()
                onChange(plans)
            }
    }

    suspend fun addPlan(uid: String, title: String, targetDate: String, note: String) {
        val doc = db.collection(COL_MEAL_PLANS).document()
        val now = System.currentTimeMillis()
        val plan = CloudMealPlan(
            id = doc.id,
            ownerId = uid,
            title = title.trim(),
            targetDate = targetDate.trim(),
            note = note.trim(),
            updatedAt = now
        )
        doc.set(plan).await()

        val starterItem = CloudMealItem(
            id = "starter",
            planId = doc.id,
            mealTitle = "Add your first planned recipe",
            checked = false,
            updatedAt = now
        )
        doc.collection(COL_ITEMS).document(starterItem.id).set(starterItem).await()
    }

    suspend fun updatePlan(plan: CloudMealPlan) {
        val updated = plan.copy(updatedAt = System.currentTimeMillis())
        db.collection(COL_MEAL_PLANS).document(plan.id).set(updated).await()
    }

    suspend fun deletePlan(planId: String) {
        db.collection(COL_MEAL_PLANS).document(planId).delete().await()
    }

    fun listenPlanItems(planId: String, onChange: (List<CloudMealItem>) -> Unit, onError: (Exception) -> Unit): ListenerRegistration {
        return db.collection(COL_MEAL_PLANS).document(planId).collection(COL_ITEMS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents
                    ?.mapNotNull { it.toObject(CloudMealItem::class.java) }
                    ?.sortedByDescending { it.updatedAt }
                    .orEmpty()
                onChange(items)
            }
    }

    companion object {
        const val COL_USERS = "users"
        const val COL_MEAL_PLANS = "meal_plans"
        const val COL_ITEMS = "items"
    }
}
