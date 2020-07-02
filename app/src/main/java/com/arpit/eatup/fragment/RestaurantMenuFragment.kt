package com.arpit.eatup.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.arpit.eatup.R
import com.arpit.eatup.activity.MainActivity
import com.arpit.eatup.adapter.RestaurantMenuRecyclerAdapter
import com.arpit.eatup.database.OrderEntity
import com.arpit.eatup.database.RestaurantDatabase
import com.arpit.eatup.model.Dish
import com.arpit.eatup.util.ConnectionManager
import java.lang.Exception

class RestaurantMenuFragment : Fragment() {


    val dishInfoList = arrayListOf<Dish>()
    lateinit var allOrders: List<OrderEntity>
    var resName: String = ""


    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: RestaurantMenuRecyclerAdapter
    lateinit var recyclerResMenu: RecyclerView
    lateinit var btnGoToCart: Button

    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar
    lateinit var coverResMenu: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_restaurant_menu, container, false)

        (activity as MainActivity).drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)

        progressLayout = view.findViewById(R.id.progressLayoutResMenu)
        progressBar = view.findViewById(R.id.progressBarResMenu)
        btnGoToCart = view.findViewById(R.id.btnGoToCart)
        coverResMenu = view.findViewById(R.id.coverResMenu)
        //progressLayout.visibility = View.VISIBLE


        resName = arguments?.get("resName").toString()
        setUpToolbar()

        (activity as MainActivity).actionBarDrawerToggle.isDrawerIndicatorEnabled = false

        if (ConnectionManager().checkConnectivity(activity as Context)) {
            val queueRequests = Volley.newRequestQueue(activity as Context)
            val resId = arguments?.get("resId").toString()
            println("RES ID: " + resId)

            val url = "http://13.235.250.119/v2/restaurants/fetch_result/$resId/"

            val jsonObjectRequest =
                object : JsonObjectRequest(Request.Method.GET,
                    url,
                    null,
                    Response.Listener {
                        val dataObj = it.getJSONObject("data")
                        val success = dataObj.getBoolean("success")

                        progressLayout.visibility = View.GONE

                        try {
                            if (success) {
                                val jsonObjArr = dataObj.getJSONArray("data")

                                for (i in 0 until jsonObjArr.length()) {
                                    val dishObj = jsonObjArr.getJSONObject(i)
                                    val dishId = dishObj.getString("id")
                                    val dishName = dishObj.getString("name")
                                    val dishCost = dishObj.getString("cost_for_one")
                                    println("CHECK: $dishId $dishName $dishCost $resId")

                                    val dishData: Dish =
                                        Dish(i + 1, dishId, dishName, dishCost, resId)
                                    dishInfoList.add(dishData)
                                }

                                //println("ARPIT: " + dishInfoList.size)

                                recyclerResMenu = view.findViewById(R.id.recyclerResMenu)
                                layoutManager = LinearLayoutManager(activity as Context)
                                recyclerAdapter =
                                    RestaurantMenuRecyclerAdapter(
                                        activity as Context,
                                        dishInfoList,
                                        btnGoToCart,
                                        recyclerResMenu
                                    )

                                recyclerResMenu.adapter = recyclerAdapter
                                recyclerResMenu.layoutManager = layoutManager

                                recyclerResMenu.addItemDecoration(
                                    DividerItemDecoration(
                                        recyclerResMenu.context,
                                        (layoutManager as LinearLayoutManager).orientation
                                    )
                                )

                            } else {
                                Toast.makeText(
                                    activity as Context,
                                    "Some unexpected error has occurred!!",
                                    Toast.LENGTH_LONG
                                ).show()
                                coverResMenu.visibility = View.VISIBLE
                            }

                        } catch (e: Exception) {
                            Toast.makeText(
                                activity as Context,
                                "Some unexpected error has occurred!!",
                                Toast.LENGTH_LONG
                            ).show()
                            coverResMenu.visibility = View.VISIBLE
                        }


                    }, Response.ErrorListener {
                        Toast.makeText(
                            activity as Context,
                            "Volley Error $it",
                            Toast.LENGTH_LONG
                        ).show()
                        coverResMenu.visibility = View.VISIBLE
                    }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "your token"

                        return headers
                    }
                }


            btnGoToCart.setOnClickListener {

                val tableEmptyCheck = DBAsyncTask(
                    context as Context,
                    2
                ).execute()
                val emptyCheck = tableEmptyCheck.get()


                if (emptyCheck) {

                    val args = Bundle()
                    args.putString("resName", resName)
                    args.putString("resId", resId)
                    val newFrag = CartFragment()
                    newFrag.arguments = args

                    val activity: MainActivity = it.context as MainActivity
                    activity.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame, newFrag)
                        .addToBackStack("resMenuFragment")
                        .commit()

                } else {
                    Toast.makeText(
                        activity as Context,
                        "No dish has been selected to order!!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }


            queueRequests.add(jsonObjectRequest)
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

    override fun onDestroyView() {
        super.onDestroyView()
        dishInfoList.clear()
    }


    fun setUpToolbar() {
        (activity as AppCompatActivity).supportActionBar?.title = resName
        (activity as AppCompatActivity).supportActionBar?.setHomeButtonEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
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

                6 -> {
                    //to get all orders
                    allOrders = db.orderDao().getAllOrders()
                    db.close()

                    return allOrders.isNotEmpty()
                }

            }
            return false
        }

    }

}