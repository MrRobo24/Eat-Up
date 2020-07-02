package com.arpit.eatup.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.arpit.eatup.R
import com.arpit.eatup.activity.MainActivity
import com.arpit.eatup.adapter.HomeRecyclerAdapter
import com.arpit.eatup.database.OrderEntity
import com.arpit.eatup.database.RestaurantDatabase
import com.arpit.eatup.model.Restaurant
import com.arpit.eatup.util.ConnectionManager
import java.util.*
import kotlin.collections.HashMap


class HomeFragment : Fragment() {

    val resInfoList = arrayListOf<Restaurant>()
    lateinit var recyclerHome: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: HomeRecyclerAdapter

    lateinit var coverHome: RelativeLayout

    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar
    lateinit var dialog: AlertDialog.Builder

    var costLowHighComparator = Comparator<Restaurant> { res1, res2 ->
        if (res1.resPrice.compareTo(res2.resPrice, true) == 0) {
            res1.resName.compareTo(res2.resName, true)
        } else {
            res1.resPrice.compareTo(res2.resPrice, true)
        }
    }

    var ratingLowHighComparator = kotlin.Comparator<Restaurant> { res1, res2 ->
        if (res1.resRating.compareTo(res2.resRating, true) == 0) {
            res1.resName.compareTo(res2.resName, true)
        } else {
            res1.resRating.compareTo(res2.resRating, true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        setHasOptionsMenu(true)

        (activity as MainActivity).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)



        coverHome = view.findViewById(R.id.coverHome)
        progressLayout = view.findViewById(R.id.progressLayout)
        progressBar = view.findViewById(R.id.progressBar)
        progressLayout.visibility = View.VISIBLE

        if (ConnectionManager().checkConnectivity(activity as Context)) {
            (activity as AppCompatActivity).supportActionBar?.title = "All Restaurants"

            val queueRequests = Volley.newRequestQueue(activity as Context)
            val url = "http://13.235.250.119/v2/restaurants/fetch_result/"

            val jsonObjectRequest =
                object : JsonObjectRequest(
                    Method.GET,
                    url,
                    null,
                    Response.Listener {
                        try {

                            progressLayout.visibility = View.GONE

                            val dataObject = it.getJSONObject("data")
                            val success = dataObject.getBoolean("success")

                            if (success) {

                                val jsonObjArr = dataObject.getJSONArray("data")

                                for (i in 0 until jsonObjArr.length()) {

                                    val resJsonObject = jsonObjArr.getJSONObject(i)
                                    val resObject = Restaurant(
                                        resJsonObject.getString("id"),
                                        resJsonObject.getString("name"),
                                        resJsonObject.getString("rating"),
                                        resJsonObject.getString("cost_for_one"),
                                        resJsonObject.getString("image_url")
                                    )

                                    resInfoList.add(resObject)
                                }

                                recyclerHome = view.findViewById(R.id.recyclerHome)
                                layoutManager = LinearLayoutManager(activity)
                                recyclerAdapter =
                                    HomeRecyclerAdapter(activity as Context, resInfoList, 1)

                                recyclerHome.adapter = recyclerAdapter
                                recyclerHome.layoutManager = layoutManager

//                            recyclerHome.addItemDecoration(
//                                DividerItemDecoration(
//                                    recyclerHome.context,
//                                    (layoutManager as LinearLayoutManager).orientation
//                                )
//                            )

                            } else {
                                Toast.makeText(
                                    activity as Context,
                                    "Some unexpected error has occurred!!",
                                    Toast.LENGTH_LONG
                                ).show()

                                coverHome.visibility = View.VISIBLE
                            }

                        } catch (e: Exception) {
                            Toast.makeText(
                                activity as Context,
                                "Some unexpected error has occurred!!",
                                Toast.LENGTH_LONG
                            ).show()

                            coverHome.visibility = View.VISIBLE
                        }
                    }, Response.ErrorListener {

                        Toast.makeText(
                            activity as Context,
                            "Volley Error $it",
                            Toast.LENGTH_LONG
                        ).show()

                        coverHome.visibility = View.VISIBLE

                    }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "your token"

                        return headers
                    }
                }

            queueRequests.add(jsonObjectRequest)

            refreshOrder()
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


        return view
    }

    override fun onResume() {
        super.onResume()
        coverHome.visibility = View.GONE
        (activity as MainActivity).actionBarDrawerToggle.isDrawerIndicatorEnabled = true
        //setUpToolbar()
        refreshOrder()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        resInfoList.clear()
    }

    fun refreshOrder() {

        val tableEmptyCheck = DBAsyncTask(
            context as Context,
            2
        ).execute()
        val emptyCheck = tableEmptyCheck.get()

        if (emptyCheck) {
            val nukeItAll = DBAsyncTask(
                context as Context,
                3
            ).execute()

            val nuked = nukeItAll.get()

            if (!nuked) {
                Toast.makeText(
                    activity as Context,
                    "Order went into delirium!!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId
        if (id == R.id.action_sort) {
            sortAlert()
        }

        return super.onOptionsItemSelected(item)
    }

    fun sortAlert() {
        dialog = AlertDialog.Builder(activity as Context)
        dialog.setTitle("Sort By?")
        val items = arrayOf<String>("Cost (Low to High)", "Cost (High to Low)", "Rating")
        var checkedItem = -1

        dialog.setSingleChoiceItems(items, checkedItem) { dialog, which ->
            when (which) {
                0 -> costLowToHigh()
                1 -> costHighToLow()
                2 -> ratingHighToLow()
            }

            dialog.dismiss()
        }

        dialog.create()
        dialog.show()
    }


    fun costLowToHigh() {
        Collections.sort(resInfoList, costLowHighComparator)
        recyclerAdapter.notifyDataSetChanged()
    }

    fun costHighToLow() {
        Collections.sort(resInfoList, costLowHighComparator)
        resInfoList.reverse()
        recyclerAdapter.notifyDataSetChanged()
    }

    fun ratingHighToLow() {
        Collections.sort(resInfoList, ratingLowHighComparator)
        resInfoList.reverse()
        recyclerAdapter.notifyDataSetChanged()
    }


    inner class DBAsyncTask(val context: Context, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {

        val db =
            Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurant-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {
                2 -> {
                    // to check if the table is empty
                    val orders: List<OrderEntity> = db.orderDao().getAllOrders()
                    db.close()

                    return orders.isNotEmpty()
                }

                3 -> {
                    //to nuke the table
                    db.orderDao().nukeTable()
                    db.close()
                    return true
                }

            }
            return false
        }

    }

}
