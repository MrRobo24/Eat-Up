package com.arpit.eatup.adapter

import android.content.Context
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arpit.eatup.R
import com.arpit.eatup.model.Order
import kotlinx.android.synthetic.main.recycler_history_single_row.view.*

class HistoryRecyclerAdapter(val context: Context, val itemList: List<Order>) :
    RecyclerView.Adapter<HistoryRecyclerAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_history_single_row, parent, false)

        return HistoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val currItem = itemList[position]

        holder.txtHistoryResName.text = currItem.resName
        holder.txtHistoryDate.text = currItem.date.split(" ")[0]
        holder.txtTotalCost.text = "Rs. " + currItem.total_cost

        val recyclerChildAdapter = CartRecyclerAdapter(context, currItem.food)
        val childLayoutManager = LinearLayoutManager(context)
        holder.recyclerHistoryChild.adapter = recyclerChildAdapter
        holder.recyclerHistoryChild.layoutManager = childLayoutManager

    }


    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtHistoryResName: TextView = view.findViewById(R.id.txtHistoryResName)
        val txtHistoryDate: TextView = view.findViewById(R.id.txtHistoryDate)
        val txtTotalCost: TextView = view.findViewById(R.id.txtTotalCost)
        val recyclerHistoryChild: RecyclerView = view.findViewById(R.id.recyclerHistoryChild)
    }
}