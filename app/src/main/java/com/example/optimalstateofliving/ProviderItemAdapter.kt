package com.example.optimalstateofliving

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
/*
class ProviderItemAdapter(
    private val items: List<String>,
    private val onDeleteClick: (String) -> Unit,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ProviderItemAdapter.ProviderViewHolder>() {

    inner class ProviderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItemName: TextView = itemView.findViewById(R.id.tvItemName)
        val imgDeleteIcon: ImageView = itemView.findViewById(R.id.imgDeleteIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProviderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_provider, parent, false)
        return ProviderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProviderViewHolder, position: Int) {
        val item = items[position]
        holder.tvItemName.text = item

        // عند الضغط على العنصر
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

        // عند الضغط على أيقونة الحذف
        holder.imgDeleteIcon.setOnClickListener {
            onDeleteClick(item)
        }
    }

    override fun getItemCount(): Int = items.size
}*/
class ProviderItemAdapter(
    private val clientList: MutableList<String>,
    private val onDeleteClick: (String) -> Unit,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ProviderItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_provider, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val clientName = clientList[position]
        holder.tvClientName.text = clientName

        // Handle delete button click
        holder.imgDeleteIcon.setOnClickListener {
            onDeleteClick(clientName)
        }

        holder.itemView.setOnClickListener {
            onItemClick(clientName)
        }
    }

    override fun getItemCount(): Int = clientList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvClientName: TextView = itemView.findViewById(R.id.tvItemName)
        val imgDeleteIcon: ImageView = itemView.findViewById(R.id.imgDeleteIcon)
    }
}

