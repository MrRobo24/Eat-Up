package com.arpit.eatup.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.arpit.eatup.R
import com.arpit.eatup.model.Dish
import kotlinx.android.synthetic.main.recycler_cart_single_row.view.*

class CartRecyclerAdapter(val context: Context, val itemList: List<Dish>) :
    RecyclerView.Adapter<CartRecyclerAdapter.CartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_cart_single_row, parent, false)

        return CartViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val currItem = itemList[position]

        holder.txtDishName.text = currItem.name.toString()
        holder.txtDishCost.text = currItem.cost.toString()
    }

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtDishName: TextView = view.findViewById(R.id.txtDishName)
        val txtDishCost: TextView = view.findViewById(R.id.txtDishCost)
    }

}