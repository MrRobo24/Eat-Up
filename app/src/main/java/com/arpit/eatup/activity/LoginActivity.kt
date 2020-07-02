package com.arpit.eatup.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.arpit.eatup.R
import com.arpit.eatup.util.ConnectionManager
import org.json.JSONObject
import java.lang.Exception

class LoginActivity : AppCompatActivity() {

    lateinit var etMobileNumber: EditText
    lateinit var etPassword: EditText
    lateinit var btnLogin: Button
    lateinit var txtForgotPassword: TextView
    lateinit var txtRegister: TextView

    lateinit var intentLogin: Intent

    lateinit var sharedPreferences: SharedPreferences
    val spFileName = "EatUp Preferences"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        // getting the shared preferences
        sharedPreferences = getSharedPreferences(spFileName, Context.MODE_PRIVATE)
        // checking if the user is logged in already or not from last session
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        // intent to start the main activity
        intentLogin = Intent(this@LoginActivity, MainActivity::class.java)

        if (isLoggedIn) {
            startActivity(intentLogin)
            finish()
        }


        etMobileNumber = findViewById(R.id.etMobileNumber)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        txtForgotPassword = findViewById(R.id.txtForgotPassword)
        txtRegister = findViewById(R.id.txtRegister)


        btnLogin.setOnClickListener {
            if (ConnectionManager().checkConnectivity(this@LoginActivity)) {
                val mobileNumber: String = etMobileNumber.text.toString()
                val password: String = etPassword.text.toString()

                // if some field is left blank
                if (mobileNumber.isEmpty() || password.isEmpty()) {
                    Toast.makeText(
                        this@LoginActivity,
                        "No field should be left blank",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    // min password length is four
                    when {
                        password.length < 4 -> {
                            Toast.makeText(
                                this@LoginActivity,
                                "Minimum password length should be 4 characters!!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        mobileNumber.length < 10 -> {
                            Toast.makeText(
                                this@LoginActivity,
                                "Minimum mobile number length should be 10 digits!!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> { // everything is fine to submit

                            login(mobileNumber, password)

                        }
                    }
                }

            } else {
                alertNoNet()
            }

        }




        txtRegister.setOnClickListener {
            if (ConnectionManager().checkConnectivity(this@LoginActivity)) {
                val intentRegister = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intentRegister)
            } else {
                alertNoNet()
            }

        }

        txtForgotPassword.setOnClickListener {
            if (ConnectionManager().checkConnectivity(this@LoginActivity)) {
                val intentForgot = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
                startActivity(intentForgot)
            } else {
                alertNoNet()
            }
        }


    }

    fun alertNoNet() {
        val dialog = AlertDialog.Builder(this@LoginActivity)
        dialog.setTitle("Error")
        dialog.setMessage("Internet Connection is Not Found")
        dialog.setPositiveButton("Open Settings") { _, _ ->
            val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
            startActivity(settingsIntent)
            ActivityCompat.finishAffinity(this@LoginActivity)
        }
        dialog.setNegativeButton("Exit") { _, _ ->
            ActivityCompat.finishAffinity(this@LoginActivity)
        }

        dialog.create()
        dialog.show()
    }

    fun login(mobileNumber: String, password: String) {
        val queueRequests = Volley.newRequestQueue(this@LoginActivity)
        val url = "http://13.235.250.119/v2/login/fetch_result/"
        val jsonParams = JSONObject()
        jsonParams.put("mobile_number", mobileNumber)
        jsonParams.put("password", password)

        val jsonObjectRequest =
            object : JsonObjectRequest(Request.Method.POST, url, jsonParams, Response.Listener {

                try {
                    //fetch success boolean
                    val dataObject = it.getJSONObject("data")
                    val success = dataObject.getBoolean("success")
                    if (success) {
                        // login credentials were authenticated
                        val userJsonObj = dataObject.getJSONObject("data")


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

                        startActivity(intentLogin)
                        finish()


                    } else {
                        //  login credentials couldn't be authenticated
                        val errorMessage = it.getString("errorMessage")
                        Toast.makeText(
                            this@LoginActivity,
                            errorMessage,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } catch (e: Exception) {

                    Toast.makeText(
                        this@LoginActivity,
                        "Mobile Number/Password couldn't be verified!!",
                        Toast.LENGTH_LONG
                    ).show()
                }


            }, Response.ErrorListener {
                Toast.makeText(
                    this@LoginActivity,
                    "Volley Error!! $it",
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

        //sending the request
        queueRequests.add(jsonObjectRequest)
    }

}
