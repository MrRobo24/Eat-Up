package com.arpit.eatup.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

import com.arpit.eatup.R
import com.arpit.eatup.activity.MainActivity
import com.arpit.eatup.activity.OrderSuccessful
import com.arpit.eatup.adapter.CartRecyclerAdapter
import com.arpit.eatup.database.OrderEntity
import com.arpit.eatup.database.RestaurantDatabase
import com.arpit.eatup.model.Dish
import com.arpit.eatup.util.ConnectionManager
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.util.concurrent.CopyOnWriteArrayList


class CartFragment : Fragment() {


    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: CartRecyclerAdapter
    lateinit var recyclerCart: RecyclerView
    lateinit var btnPlaceOrder: Button
    lateinit var txtOrderingFrom: TextView

    var sharedPreferences: SharedPreferences? = null
    val spFileName = "EatUp Preferences"
    var resName: String? = ""
    var resId: String? = ""
    var totalCost = 0

    var dishList = arrayListOf<Dish>()
    var entityList = arrayListOf<OrderEntity>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_cart, container, false)
        (activity as AppCompatActivity).supportActionBar?.title = "My Cart"
        (activity as MainActivity).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        progressLayout = view.findViewById(R.id.progressLayoutCart)
        progressBar = view.findViewById(R.id.progressBarCart)
        progressLayout.visibility = View.VISIBLE

        btnPlaceOrder = view.findViewById(R.id.btnPlaceOrder)
        txtOrderingFrom = view.findViewById(R.id.txtOrderingFrom)

        //fetching orders from database
        val fetchOrders = DBAsyncTask(context as Context, 1).execute()
        val fetched = fetchOrders.get()

        if (fetched) {
            //Toast.makeText(activity as Context, "Entered", Toast.LENGTH_LONG).show()
            progressLayout.visibility = View.GONE
            var counter = 1
            totalCost = 0
            resId = arguments?.getString("resId")
            resName = arguments?.getString("resName")

            txtOrderingFrom.text = "Ordering From: " + resName
            for (item in entityList) {
                val currDish = Dish(counter++, item.id, item.name, item.cost, item.resId)
                totalCost += currDish.cost.toInt()
                dishList.add(currDish)
            }

            recyclerCart = view.findViewById(R.id.recyclerCart)
            layoutManager = LinearLayoutManager(activity as Context)
            recyclerAdapter = CartRecyclerAdapter(activity as Context, dishList)

            recyclerCart.adapter = recyclerAdapter
            recyclerCart.layoutManager = layoutManager

            btnPlaceOrder.text = "Place Order (Total: Rs. $totalCost)"

            // nuking current order table
            val nukeAll = DBAsyncTask(context as Context, 2).execute()
            nukeAll.get()

            btnPlaceOrder.setOnClickListener {
                if (ConnectionManager().checkConnectivity(activity as Context)) {
                    progressLayout.visibility = View.VISIBLE
                    placeOrder()
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

            }


        } else {
            Toast.makeText(activity as Context, "Order Fetch Failed!!", Toast.LENGTH_LONG).show()
        }

        return view
    }

    private fun placeOrder() {
        sharedPreferences = activity?.getSharedPreferences(spFileName, Context.MODE_PRIVATE)
        val queueRequests = Volley.newRequestQueue(activity as Context)
        val url = "http://13.235.250.119/v2/place_order/fetch_result/"
        val jsonParams = JSONObject()
        jsonParams.put("user_id", sharedPreferences?.getString("userId", ""))
        jsonParams.put("restaurant_id", resId)
        jsonParams.put("total_cost", totalCost)

        val food = JSONArray()

        for (dish in dishList) {
            food.put(JSONObject().put("food_item_id", dish.id))
        }
        jsonParams.put("food", food)


        val jsonObjectRequest =
            object : JsonObjectRequest(Request.Method.POST, url, jsonParams,

                Response.Listener {

                    try {
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")

                        if (success) {
                            progressLayout.visibility = View.GONE
                            val intent = Intent(activity, OrderSuccessful::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                        } else {
                            progressLayout.visibility = View.GONE
                            Toast.makeText(
                                activity as Context,
                                "Order Failed!!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (e: Exception) {
                        progressLayout.visibility = View.GONE
                        Toast.makeText(
                            activity as Context,
                            "Some unexpected error has occurred!!",
                            Toast.LENGTH_LONG
                        ).show()
                    }


                }, Response.ErrorListener {
                    progressLayout.visibility = View.GONE
                    Toast.makeText(
                        activity as Context,
                        "Volley error $it",
                        Toast.LENGTH_LONG
                    ).show()
                }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "your token"

                    return headers
                }
            }


        queueRequests.add(jsonObjectRequest)

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
                    // to fetch all the orders in a global list
                    entityList = db.orderDao().getAllOrders() as ArrayList<OrderEntity>
                    db.close()

                    return entityList.isNotEmpty()
                }

                2 -> {
                    // nuke it all bro!
                    db.orderDao().nukeTable()
                    return true
                }
            }

            return false
        }
    }

}
