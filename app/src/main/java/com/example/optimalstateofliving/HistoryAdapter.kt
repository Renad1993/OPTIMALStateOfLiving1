package com.example.optimalstateofliving

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private val historyList: List<HistoryItem>) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyItem = historyList[position]
        holder.checkTimeTextView.text = historyItem.checkTime
        holder.statusNameTextView.text = historyItem.statusName
        holder.colorView.setCardBackgroundColor(historyItem.statusColor)
    }

    override fun getItemCount(): Int = historyList.size

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkTimeTextView: TextView = itemView.findViewById(R.id.checkTimeTextView)
        val statusNameTextView: TextView = itemView.findViewById(R.id.statusNameTextView)
        val colorView: CardView = itemView.findViewById(R.id.colorView)
    }
}
