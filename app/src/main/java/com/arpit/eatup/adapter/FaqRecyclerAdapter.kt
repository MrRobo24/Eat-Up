package com.arpit.eatup.adapter

import android.content.Context
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.arpit.eatup.R
import com.arpit.eatup.model.QA
import kotlinx.android.synthetic.main.recycler_faq_single_row.view.*

class FaqRecyclerAdapter(val context: Context, val itemList: ArrayList<QA>) :
    RecyclerView.Adapter<FaqRecyclerAdapter.FaqViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_faq_single_row, parent, false)

        return FaqRecyclerAdapter.FaqViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        val currItem = itemList[position]

        holder.txtQuestion.text = "Q."+(position + 1)+". "+currItem.question.toString()
        holder.txtAnswer.text = "A."+(position + 1)+". "+currItem.answer.toString()
    }

    class FaqViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtQuestion: TextView = view.findViewById(R.id.txtQuestion)
        val txtAnswer: TextView = view.findViewById(R.id.txtAnswer)
    }
}