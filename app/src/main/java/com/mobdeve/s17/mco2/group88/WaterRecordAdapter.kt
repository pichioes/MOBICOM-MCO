// WaterRecordAdapter.kt

package com.mobdeve.s17.mco2.group88

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.TextView

class WaterRecordAdapter(private val waterRecords: List<WaterRecord>) :
    RecyclerView.Adapter<WaterRecordAdapter.WaterRecordViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WaterRecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.record_item, parent, false)
        return WaterRecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WaterRecordViewHolder, position: Int) {
        val record = waterRecords[position]
        holder.timeTextView.text = record.time
        holder.amountTextView.text = "${record.amount} ml"
    }

    override fun getItemCount(): Int = waterRecords.size

    class WaterRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
    }
}
