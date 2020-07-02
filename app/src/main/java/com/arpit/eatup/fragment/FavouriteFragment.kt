package com.arpit.eatup.fragment

import android.app.AlertDialog
import android.content.AbstractThreadedSyncAdapter
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room

import com.arpit.eatup.R
import com.arpit.eatup.activity.MainActivity
import com.arpit.eatup.adapter.HomeRecyclerAdapter
import com.arpit.eatup.database.RestaurantDatabase
import com.arpit.eatup.database.RestaurantEntity
import com.arpit.eatup.model.Restaurant
import com.arpit.eatup.util.ConnectionManager
import java.lang.Exception


class FavouriteFragment : Fragment() {


    var resEntityList = arrayListOf<RestaurantEntity>()
    var resList = arrayListOf<Restaurant>()
    lateinit var recyclerFavourite: RecyclerView
    lateinit var recyclerAdapter: HomeRecyclerAdapter
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var rlFavCover: RelativeLayout


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_favourite, container, false)

        (activity as MainActivity).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        rlFavCover = view.findViewById(R.id.rlFavCover)

        try {
            val getResList = DBAsyncTask(activity as Context, 1).execute()
            getResList.get()

            if (resEntityList.size != 0) {

                for (res in resEntityList) {
                    resList.add(
                        Restaurant(
                            res.res_id,
                            res.resName,
                            res.resRating,
                            res.resPrice,
                            res.resImageUrl
                        )
                    )
                }
                if (ConnectionManager().checkConnectivity(activity as Context)) {
                    recyclerFavourite = view.findViewById(R.id.recyclerFavourite)
                    recyclerAdapter = HomeRecyclerAdapter(activity as Context, resList, 2)
                    layoutManager = LinearLayoutManager(activity as Context)

                    recyclerFavourite.adapter = recyclerAdapter
                    recyclerFavourite.layoutManager = layoutManager
                } else {
                    val dialog = AlertDialog.Builder(activity as Context)
                    dialog.setTitle("Error")
                    dialog.setMessage("Internet Connection is Not Found")
                    dialog.setPositiveButton("Open Settings") { _, _ ->
                        val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                        startActivity(settingsIntent)
                        ActivityCompat.finishAffinity(activity as MainActivity)
                    }
                    dialog.setNegativeButton("Exit") { _, _ ->
                        ActivityCompat.finishAffinity(activity as MainActivity)
                    }

                    dialog.create()
                    dialog.show()
                }


            } else {
                rlFavCover.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Toast.makeText(
                activity as Context,
                "Some unexpected error has occurred!!",
                Toast.LENGTH_LONG
            ).show()
            rlFavCover.visibility = View.VISIBLE
        }




        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        resList.clear()
        resEntityList.clear()
    }

    inner class DBAsyncTask(val context: Context, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {

        val db = Room.databaseBuilder(
            context,
            RestaurantDatabase::class.java,
            "restaurant-db"
        ).build()

        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {
                1 -> {
                    //fetch all favourites
                    resEntityList =
                        db.restaurantDao().getAllRestaurants() as ArrayList<RestaurantEntity>
                    return true
                }
            }
            return false
        }

    }

}
