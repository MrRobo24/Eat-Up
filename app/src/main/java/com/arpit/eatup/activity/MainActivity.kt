package com.arpit.eatup.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.arpit.eatup.R
import com.arpit.eatup.fragment.*
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    lateinit var drawerLayout: DrawerLayout
    lateinit var coordinatorLayout: CoordinatorLayout
    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    lateinit var frameLayout: FrameLayout
    lateinit var navigationView: NavigationView
    lateinit var txtDrawerUserName: TextView
    lateinit var txtDrawerMobileNumber: TextView
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    var previousMenuItem: MenuItem? = null
    var savedMenuItem: MenuItem? = null

    lateinit var sharedPreferences: SharedPreferences
    val spFileName = "EatUp Preferences"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)







        sharedPreferences = getSharedPreferences(spFileName, Context.MODE_PRIVATE)

        drawerLayout = findViewById(R.id.drawerLayout)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        toolbar = findViewById(R.id.toolbar)
        frameLayout = findViewById(R.id.frame)
        navigationView = findViewById(R.id.navigationView)


        val header: View = navigationView.getHeaderView(0)
        txtDrawerUserName = header.findViewById(R.id.txtDrawerUserName)
        txtDrawerMobileNumber = header.findViewById(R.id.txtDrawerMobileNumber)

        txtDrawerUserName.text = sharedPreferences.getString("name", "User Name")
        txtDrawerMobileNumber.text = sharedPreferences.getString("mobileNumber", "Mobile Number")

        setUpToolbar()

        // Remove hamburger

        actionBarDrawerToggle = ActionBarDrawerToggle(
            this@MainActivity,
            drawerLayout,
            R.string.open_drawer,
            R.string.close_drawer
        )

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        openHome()


        navigationView.setNavigationItemSelectedListener {

            if (previousMenuItem != null) {
                // un-checking the previously clicked menu item
                savedMenuItem = previousMenuItem
                previousMenuItem?.isChecked = false
            }

            it.isCheckable = true
            it.isChecked = true
            previousMenuItem = it

            when (it.itemId) {
                R.id.home -> {
                    openHome()
                    drawerLayout.closeDrawers()
                }

                R.id.myProfile -> {

                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame,
                            ProfileFragment()
                        )
                        //.addToBackStack("myProfile")
                        .commit()
                    supportActionBar?.title = "My Profile"
                    drawerLayout.closeDrawers()
                }

                R.id.favRestaurants -> {

                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame,
                            FavouriteFragment()
                        )
                        //.addToBackStack("favRestaurants")
                        .commit()
                    supportActionBar?.title = "Favourite Restaurants"
                    drawerLayout.closeDrawers()

                }

                R.id.orderHistory -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame,
                            HistoryFragment()
                        )
                        //.addToBackStack("orderHistory")
                        .commit()
                    supportActionBar?.title = "Order History"
                    drawerLayout.closeDrawers()

                }

                R.id.faqs -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame,
                            FaqFragment()
                        )
                        //.addToBackStack("orderHistory")
                        .commit()
                    supportActionBar?.title = "FAQs"
                    drawerLayout.closeDrawers()
                }

                R.id.logout -> {
                    logoutAlert()
                }
            }



            return@setNavigationItemSelectedListener true
        }


    }


    fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Toolbar Title"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            val frag = supportFragmentManager.findFragmentById(R.id.frame)
            when (frag) {
                is RestaurantMenuFragment -> onBackPressed()
                is CartFragment -> onBackPressed()
                else -> {
                    drawerLayout.openDrawer(GravityCompat.START)
                }
            }
//            drawerLayout.openDrawer(GravityCompat.START)
//            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    fun openHome() {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.frame,
                HomeFragment()
            )
            .addToBackStack("home")
            .commit()

        supportActionBar?.title = "All Restaurants"
        navigationView.setCheckedItem(R.id.home)
        savedMenuItem = navigationView.checkedItem
    }

    override fun onBackPressed() {
        val frag = supportFragmentManager.findFragmentById(R.id.frame)

        when (frag) {
            is CartFragment -> super.onBackPressed()

            !is HomeFragment -> openHome()

            else -> finish()
        }
    }

    fun logoutAlert() {
        val dialog = AlertDialog.Builder(this@MainActivity)
        dialog.setTitle("Confirmation")
        dialog.setMessage("Are you sure you want to log out?")
        dialog.setPositiveButton("Yes") { _, _ ->
            sharedPreferences.edit().clear().apply()
            val intentLoginPage = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intentLoginPage)
            finish()
        }
        dialog.setNegativeButton("No") { _, _ ->
            previousMenuItem?.isChecked = false
            savedMenuItem?.isCheckable = true
            savedMenuItem?.isChecked = true
            drawerLayout.closeDrawers()

        }

        dialog.create()
        dialog.show()
    }
}
