package com.arpit.eatup.adapter

import android.content.Context
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.arpit.eatup.R
import com.arpit.eatup.database.OrderEntity
import com.arpit.eatup.database.RestaurantDatabase
import com.arpit.eatup.model.Dish


class RestaurantMenuRecyclerAdapter(
    val context: Context,
    val dishInfoList: ArrayList<Dish>,
    val btnGoToCart: Button,
    val recyclerMenu: RecyclerView
) :
    RecyclerView.Adapter<RestaurantMenuRecyclerAdapter.RestaurantMenuViewHolder>() {

    val accentColor = ContextCompat.getColor(
        context,
        R.color.colorAccent
    )
    val primaryColor = ContextCompat.getColor(
        context,
        R.color.colorPrimary
    )
    val whiteColor = ContextCompat.getColor(
        context,
        R.color.colorWhite
    )


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantMenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_restaurant_menu_single_row, parent, false)

        return RestaurantMenuViewHolder(view)
    }

    override fun getItemCount(): Int {

        return dishInfoList.size
    }

    override fun onBindViewHolder(holder: RestaurantMenuViewHolder, position: Int) {

        val currItem = dishInfoList[position]
        holder.txtResMenuSrl.text = currItem.srl.toString()
        holder.txtResMenuName.text = currItem.name.toString()
        holder.txtResMenuPrice.text = "Rs. " + currItem.cost.toString()

        println(currItem.id + "UNIQUE ID")

        val orderEntity =
            OrderEntity(
                currItem.id,
                currItem.name,
                currItem.cost,
                currItem.resId
            )

        var checkIfOrdered = DBAsyncTask(context, orderEntity, 4).execute()
        var ordered = checkIfOrdered.get()

        if (ordered) {
            holder.btnResMenuAdd.setBackgroundColor(accentColor)
            holder.btnResMenuAdd.text = "REMOVE"
            holder.btnResMenuAdd.setTextColor(primaryColor)

        } else {
            holder.btnResMenuAdd.setBackgroundColor(primaryColor)
            holder.btnResMenuAdd.text = "ADD"
            holder.btnResMenuAdd.setTextColor(whiteColor)
        }


        // deal with adding to cart
        holder.btnResMenuAdd.setOnClickListener {

            checkIfOrdered = DBAsyncTask(context, orderEntity, 4).execute()
            ordered = checkIfOrdered.get()

            if (ordered) {
                //remove from orders
                val deleteOrder = DBAsyncTask(context, orderEntity, 5).execute()
                deleteOrder.get()

                holder.btnResMenuAdd.setBackgroundColor(primaryColor)
                holder.btnResMenuAdd.text = "ADD"
                holder.btnResMenuAdd.setTextColor(whiteColor)


            } else {
                //add to orders
                val insertOrder = DBAsyncTask(context, orderEntity, 1).execute()
                insertOrder.get()

                holder.btnResMenuAdd.setBackgroundColor(accentColor)
                holder.btnResMenuAdd.text = "REMOVE"
                holder.btnResMenuAdd.setTextColor(primaryColor)
            }


            val tableEmptyCheck = DBAsyncTask(context, orderEntity, 2).execute()
            val emptyCheck = tableEmptyCheck.get()

            println("EMPTY: " + emptyCheck)

            if (emptyCheck) {
                val dpRatio = context.resources.displayMetrics.density
                val pixelForDp = 50 * dpRatio.toInt()

                val paramsOfRecycler = recyclerMenu.layoutParams as FrameLayout.LayoutParams
                paramsOfRecycler.setMargins(0, 0, 0, pixelForDp)
                btnGoToCart.visibility = View.VISIBLE
            } else {
                val paramsOfRecycler = recyclerMenu.layoutParams as FrameLayout.LayoutParams
                paramsOfRecycler.setMargins(0, 0, 0, 0)
                btnGoToCart.visibility = View.GONE
            }

        }
    }

    class RestaurantMenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtResMenuSrl: TextView = view.findViewById(R.id.txtResMenuSrl)
        val txtResMenuName: TextView = view.findViewById(R.id.txtResMenuName)
        val txtResMenuPrice: TextView = view.findViewById(R.id.txtResMenuPrice)
        val btnResMenuAdd: Button = view.findViewById(R.id.btnResMenuAdd)
    }

    class DBAsyncTask(val context: Context, val orderEntity: OrderEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {

        val db =
            Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurant-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {

            when (mode) {
                1 -> {
                    // for insertion
                    db.orderDao().insertOrder(orderEntity)
                    db.close()
                    return true
                }

                2 -> {
                    // to check if the table is empty
                    val orders: List<OrderEntity> = db.orderDao().getAllOrders()
                    db.close()

                    return orders.isNotEmpty()
                }

                3 -> {
                    //to nuke the table
                    db.orderDao().nukeTable()
                    return true
                }

                4 -> {
                    //to check if an item is ordered or not
                    val order = db.orderDao().getOrderById(orderEntity.id)
                    db.close()
                    return order != null
                }

                5 -> {
                    //to delete a specific order from table
                    db.orderDao().deleteOrder(orderEntity)
                    db.close()
                    return true
                }

            }

            return false
        }

    }
}