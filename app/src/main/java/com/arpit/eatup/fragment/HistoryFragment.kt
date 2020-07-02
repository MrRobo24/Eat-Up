package com.arpit.eatup.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

import com.arpit.eatup.R
import com.arpit.eatup.activity.MainActivity
import com.arpit.eatup.adapter.HistoryRecyclerAdapter
import com.arpit.eatup.model.Dish
import com.arpit.eatup.model.Order
import com.arpit.eatup.util.ConnectionManager
import org.json.JSONObject
import java.lang.Exception
import java.util.zip.CheckedOutputStream

class HistoryFragment : Fragment() {

    lateinit var recyclerHistory: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: HistoryRecyclerAdapter

    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar

    lateinit var rlHistoryCover: RelativeLayout
    lateinit var txtPrevOrders: TextView

    var sharedPreferences: SharedPreferences? = null
    val spFileName = "EatUp Preferences"
    var userId: String? = ""
    var orderList = arrayListOf<Order>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        (activity as MainActivity).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        progressLayout = view.findViewById(R.id.progressLayoutHistory)
        progressBar = view.findViewById(R.id.progressBarHistory)
        progressLayout.visibility = View.VISIBLE
        rlHistoryCover = view.findViewById(R.id.rlHistoryCover)
        txtPrevOrders = view.findViewById(R.id.txtPrevOrders)

        sharedPreferences = activity?.getSharedPreferences(spFileName, Context.MODE_PRIVATE)
        userId = sharedPreferences?.getString("userId", "")

        if (ConnectionManager().checkConnectivity(activity as Context)) {
            val queueRequests = Volley.newRequestQueue(activity as Context)
            val url = "http://13.235.250.119/v2/orders/fetch_result/$userId"

            val jsonObjectRequest =
                object : JsonObjectRequest(Request.Method.GET, url, null,
                    Response.Listener {
                        try {
                            val dataMain = it.getJSONObject("data")
                            val success = dataMain.getBoolean("success")

                            if (success) {
                                progressLayout.visibility = View.GONE

                                val dataArr = dataMain.getJSONArray("data")

                                if (dataArr.length() < 1) {
                                    txtPrevOrders.visibility = View.GONE
                                    rlHistoryCover.visibility = View.VISIBLE
                                } else {
                                    for (i in 0 until dataArr.length()) {
                                        val currOrderObj = dataArr[i] as JSONObject
                                        val id = currOrderObj.getString("order_id")
                                        val resName = currOrderObj.getString("restaurant_name")
                                        val totalCost = currOrderObj.getString("total_cost")
                                        val date = currOrderObj.getString("order_placed_at")
                                        val foodJsonArr = currOrderObj.getJSONArray("food_items")
                                        val food = arrayListOf<Dish>()
                                        var counter = 1
                                        for (j in 0 until foodJsonArr.length()) {
                                            val currFood = foodJsonArr[j] as JSONObject
                                            val foodId = currFood.getString("food_item_id")
                                            val foodName = currFood.getString("name")
                                            val foodCost = currFood.getString("cost")
                                            food.add(
                                                Dish(
                                                    counter++,
                                                    foodId,
                                                    foodName,
                                                    foodCost,
                                                    ""
                                                )
                                            )
                                        }

                                        orderList.add(Order(id, resName, totalCost, date, food))
                                    }

                                    recyclerAdapter =
                                        HistoryRecyclerAdapter(activity as Context, orderList)
                                    recyclerHistory = view.findViewById(R.id.recyclerHistory)
                                    layoutManager = LinearLayoutManager(activity as Context)
                                    recyclerHistory.adapter = recyclerAdapter
                                    recyclerHistory.layoutManager = layoutManager

                                }

                            } else {
                                txtPrevOrders.visibility = View.GONE
                                rlHistoryCover.visibility = View.VISIBLE
                            }

                        } catch (e: Exception) {
                            Toast.makeText(
                                activity as Context,
                                "Some unexpected error has occurred!! $e",
                                Toast.LENGTH_LONG
                            ).show()
                            txtPrevOrders.visibility = View.GONE
                            rlHistoryCover.visibility = View.VISIBLE
                        }


                    }, Response.ErrorListener {

                        Toast.makeText(
                            activity as Context,
                            "Volley Error $it",
                            Toast.LENGTH_LONG
                        ).show()
                        txtPrevOrders.visibility = View.GONE
                        rlHistoryCover.visibility = View.VISIBLE

                    }) {

                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "your token"

                        return headers
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
        orderList.clear()
    }

}
