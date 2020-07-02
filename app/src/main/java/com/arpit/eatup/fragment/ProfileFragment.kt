package com.arpit.eatup.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

import com.arpit.eatup.R
import com.arpit.eatup.activity.MainActivity
import com.arpit.eatup.adapter.HomeRecyclerAdapter
import com.arpit.eatup.model.Restaurant

class ProfileFragment : Fragment() {


    lateinit var txtProfileName: TextView
    lateinit var txtProfileMobileNumber: TextView
    lateinit var txtProfileEmail: TextView
    lateinit var txtProfileAddress: TextView
    lateinit var cvProfileFragment: CardView

    var sharedPreferences: SharedPreferences? = null
    val spFileName = "EatUp Preferences"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        (activity as MainActivity).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

        txtProfileName = view.findViewById(R.id.txtProfileName)
        txtProfileMobileNumber = view.findViewById(R.id.txtProfileMobileNumber)
        txtProfileEmail = view.findViewById(R.id.txtProfileEmail)
        txtProfileAddress = view.findViewById(R.id.txtProfileAddress)
        cvProfileFragment = view.findViewById(R.id.cvProfileFragment)

        sharedPreferences =  activity?.getSharedPreferences(spFileName, Context.MODE_PRIVATE)




        txtProfileName.text = sharedPreferences?.getString("name", "User Name").toString()
        txtProfileMobileNumber.text = sharedPreferences?.getString("mobileNumber", "Mobile Number").toString()
        txtProfileEmail.text = sharedPreferences?.getString("email", "Email").toString()
        txtProfileAddress.text = sharedPreferences?.getString("address", "Delivery Address").toString()

        

        return view
    }

}