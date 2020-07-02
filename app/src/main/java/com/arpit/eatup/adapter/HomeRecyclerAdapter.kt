package com.arpit.eatup.adapter

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.arpit.eatup.R
import com.arpit.eatup.activity.MainActivity
import com.arpit.eatup.database.RestaurantDatabase
import com.arpit.eatup.database.RestaurantEntity
import com.arpit.eatup.fragment.RestaurantMenuFragment
import com.arpit.eatup.model.Restaurant
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_favourite.*


class HomeRecyclerAdapter(
    val context: Context,
    val itemList: ArrayList<Restaurant>,
    val usage: Int
) :
    RecyclerView.Adapter<HomeRecyclerAdapter.HomeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_home_single_row, parent, false)

        return HomeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {

        holder.txtRestaurantName.text = itemList[position].resName
        holder.txtRestaurantPrice.text = "Rs. " + itemList[position].resPrice + " / person"
        holder.txtRestaurantRating.text = itemList[position].resRating


        // using Glide for better performance
        Glide.with(context)
            .load(itemList[position].resImageUrl)
            .placeholder(R.drawable.def_logo)
            .into(holder.imgRestaurant)


        val currItem = itemList[position]
        val restaurantEntity = RestaurantEntity(
            currItem.resId,
            currItem.resName,
            currItem.resRating,
            currItem.resPrice,
            currItem.resImageUrl
        )

        var checkFav = DBAsyncTask(context, restaurantEntity, 3).execute()
        var isFav = checkFav.get()

        // Setting view of fav button
        if (!isFav) {
            holder.btnRestaurantFavourite.setBackgroundResource(R.drawable.ic_favorite_border_black_30dp)
        } else {
            holder.btnRestaurantFavourite.setBackgroundResource(R.drawable.ic_favorite_black_30dp)
        }

        holder.btnRestaurantFavourite.setOnClickListener {

            checkFav = DBAsyncTask(context, restaurantEntity, 3).execute()
            isFav = checkFav.get()

            if (isFav) {
                // TODO REMOVE FROM FAV DATABASE
                val removeRes = DBAsyncTask(context, restaurantEntity, 2).execute()
                val removed = removeRes.get()

                if (removed) {
                    it.setBackgroundResource(R.drawable.ic_favorite_border_black_30dp)
                    if (usage == 2) {
                        itemList.removeAt(position)
                        notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(context, "Favourite Removal Failed!!", Toast.LENGTH_LONG).show()
                }

            } else {

                val addRes = DBAsyncTask(context, restaurantEntity, 1).execute()
                val added = addRes.get()

                if (added) {
                    it.setBackgroundResource(R.drawable.ic_favorite_black_30dp)
                } else {
                    Toast.makeText(context, "Favourite Addition Failed!!", Toast.LENGTH_LONG).show()
                }
            }

        }


        holder.cvContent.setOnClickListener {

            val args = Bundle()
            args.putString("resId", itemList[position].resId)
            args.putString("resName", itemList[position].resName)
            val newFrag = RestaurantMenuFragment()
            newFrag.arguments = args
            val activity: MainActivity = it.context as MainActivity
            activity.supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.frame,
                    newFrag
                )
                .addToBackStack("restaurantMenu")

                .commit()
            activity.supportActionBar?.title = itemList[position].resName
        }

    }

    class HomeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cvContent: CardView = view.findViewById(R.id.cvContent)
        val imgRestaurant: ImageView = view.findViewById(R.id.imgRestaurant)
        val txtRestaurantName: TextView = view.findViewById(R.id.txtRestaurantName)
        val txtRestaurantPrice: TextView = view.findViewById(R.id.txtRestaurantPrice)
        val txtRestaurantRating: TextView = view.findViewById(R.id.txtRestaurantRating)
        val btnRestaurantFavourite: ImageButton = view.findViewById(R.id.btnRestaurantFavourite)
    }

    class DBAsyncTask(val context: Context, val restaurantEntity: RestaurantEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {


        val db =
            Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurant-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {

            when (mode) {
                1 -> {
                    // for insertion
                    db.restaurantDao().insertRestaurant(restaurantEntity)
                    db.close()
                    return true
                }

                2 -> {
                    // for deletion
                    db.restaurantDao().deleteRestaurant(restaurantEntity)
                    db.close()
                    return true
                }

                3 -> {
                    // for getting restaurant by resId
                    val restaurant: RestaurantEntity? =
                        db.restaurantDao().getRestaurantById(restaurantEntity.res_id.toString())
                    db.close()

                    return restaurant != null
                }
            }



            return false
        }

    }

}