package com.smd.recipehub.ui.cloud

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smd.recipehub.R
import com.smd.recipehub.data.firebase.CloudMealPlan

class CloudPlanAdapter(
    private val onEdit: (CloudMealPlan) -> Unit,
    private val onDelete: (CloudMealPlan) -> Unit
) : RecyclerView.Adapter<CloudPlanAdapter.CloudPlanViewHolder>() {
    private val plans = mutableListOf<CloudMealPlan>()

    fun submitList(newPlans: List<CloudMealPlan>) {
        plans.clear()
        plans.addAll(newPlans)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CloudPlanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cloud_plan, parent, false)
        return CloudPlanViewHolder(view)
    }

    override fun getItemCount(): Int = plans.size

    override fun onBindViewHolder(holder: CloudPlanViewHolder, position: Int) {
        holder.bind(plans[position])
    }

    inner class CloudPlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.cloudPlanTitle)
        private val meta: TextView = itemView.findViewById(R.id.cloudPlanMeta)
        private val note: TextView = itemView.findViewById(R.id.cloudPlanNote)
        private val editButton: Button = itemView.findViewById(R.id.editCloudPlanButton)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteCloudPlanButton)

        fun bind(plan: CloudMealPlan) {
            title.text = plan.title
            meta.text = "Target date: ${plan.targetDate.ifBlank { "Not set" }}"
            note.text = plan.note
            editButton.setOnClickListener { onEdit(plan) }
            deleteButton.setOnClickListener { onDelete(plan) }
        }
    }
}
