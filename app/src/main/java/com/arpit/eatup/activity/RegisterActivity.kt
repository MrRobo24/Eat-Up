package com.arpit.eatup.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.arpit.eatup.R
import com.arpit.eatup.util.ConnectionManager
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    lateinit var etRegName: EditText
    lateinit var etRegEmail: EditText
    lateinit var etRegMobileNumber: EditText
    lateinit var etRegDeliveryAddress: EditText
    lateinit var etRegPassword: EditText
    lateinit var etRegConfirmPassword: EditText
    lateinit var btnSignup: Button
    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar

    lateinit var intentLogin: Intent

    lateinit var sharedPreferences: SharedPreferences
    val spFileName = "EatUp Preferences"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        progressLayout = findViewById(R.id.progressLayoutReg)

        // getting the shared preferences
        sharedPreferences = getSharedPreferences(spFileName, Context.MODE_PRIVATE)

        etRegName = findViewById(R.id.etRegName)
        etRegEmail = findViewById(R.id.etRegEmail)
        etRegMobileNumber = findViewById(R.id.etRegMobileNumber)
        etRegDeliveryAddress = findViewById(R.id.etRegDeliveryAddress)
        etRegPassword = findViewById(R.id.etRegPassword)
        etRegConfirmPassword = findViewById(R.id.etRegConfirmPassword)
        btnSignup = findViewById(R.id.btnSignup)


        btnSignup.setOnClickListener {
            if (ConnectionManager().checkConnectivity(this@RegisterActivity)) {
                val name = etRegName.text.toString()
                val email = etRegEmail.text.toString()
                val mobileNumber = etRegMobileNumber.text.toString()
                val deliveryAddress = etRegDeliveryAddress.text.toString()
                val password = etRegPassword.text.toString()
                val confirmPassword = etRegConfirmPassword.text.toString()

                // if some field is left blank
                if (name.isEmpty() ||
                    email.isEmpty() ||
                    mobileNumber.isEmpty() ||
                    deliveryAddress.isEmpty() ||
                    password.isEmpty() ||
                    confirmPassword.isEmpty()
                ) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "No field should be left blank",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    // fields are filled
                    if (name.length < 4) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Minimum 4 characters expected in a name",
                            Toast.LENGTH_LONG
                        ).show()
                    } else if (mobileNumber.length != 10) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Invalid mobile number",
                            Toast.LENGTH_LONG
                        ).show()
                    } else if (password.length < 4) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Minimum 4 characters expected in a password",
                            Toast.LENGTH_LONG
                        ).show()
                    } else if (password != confirmPassword) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Confirm Password do not match",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        progressLayout.visibility = View.VISIBLE
                        // everything is fine
                        signup(name, email, mobileNumber, deliveryAddress, password)
                    }
                }
            } else {
                alertNoNet()
            }

        }


    }

    fun alertNoNet() {
        val dialog = AlertDialog.Builder(this@RegisterActivity)
        dialog.setTitle("Error")
        dialog.setMessage("Internet Connection is Not Found")
        dialog.setPositiveButton("Open Settings") { _, _ ->
            val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
            startActivity(settingsIntent)
            ActivityCompat.finishAffinity(this@RegisterActivity)
        }
        dialog.setNegativeButton("Exit") { _, _ ->
            ActivityCompat.finishAffinity(this@RegisterActivity)
        }

        dialog.create()
        dialog.show()
    }

    fun signup(
        name: String,
        email: String,
        mobileNumber: String,
        deliveryAddress: String,
        password: String
    ) {
        val queueRequests = Volley.newRequestQueue(this@RegisterActivity)
        val url = "http://13.235.250.119/v2/register/fetch_result/"
        val jsonParams = JSONObject()
        jsonParams.put("name", name)
        jsonParams.put("mobile_number", mobileNumber)
        jsonParams.put("password", password)
        jsonParams.put("address", deliveryAddress)
        jsonParams.put("email", email)

        val jsonRequest =
            object : JsonObjectRequest(Request.Method.POST, url, jsonParams, Response.Listener {

                try {
                    val dataObject = it.getJSONObject("data")
                    val success = dataObject.getBoolean("success")
                    if (success) {
                        progressLayout.visibility = View.GONE
                        Toast.makeText(
                            this@RegisterActivity,
                            "Welcome to EatUp mobile app!!",
                            Toast.LENGTH_LONG
                        ).show()
                        // registration was successful
                        val userJsonObj = dataObject.getJSONObject("data")
                        //saving user info
                        sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
                        sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
                        sharedPreferences.edit()
                            .putString("userId", userJsonObj.getString("user_id")).apply()
                        sharedPreferences.edit().putString("name", userJsonObj.getString("name"))
                            .apply()
                        sharedPreferences.edit().putString("email", userJsonObj.getString("email"))
                            .apply()
                        sharedPreferences.edit()
                            .putString("mobileNumber", userJsonObj.getString("mobile_number"))
                            .apply()
                        sharedPreferences.edit()
                            .putString("address", userJsonObj.getString("address")).apply()

                        // intent to start the main activity
                        intentLogin = Intent(this@RegisterActivity, MainActivity::class.java)
                        intentLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intentLogin)
                        finish()

                    } else {
                        progressLayout.visibility = View.GONE
                        Toast.makeText(
                            this@RegisterActivity,
                            "Registration failed $success",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } catch (e: Exception) {
                    progressLayout.visibility = View.GONE
                    Toast.makeText(
                        this@RegisterActivity,
                        "Some unexpected error occurred!!",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }, Response.ErrorListener {
                progressLayout.visibility = View.GONE

                Toast.makeText(
                    this@RegisterActivity,
                    "Volley Error $it",
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

        queueRequests.add(jsonRequest)

    }


}
