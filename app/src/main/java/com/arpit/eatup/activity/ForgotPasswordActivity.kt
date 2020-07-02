package com.arpit.eatup.activity

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.arpit.eatup.R
import com.arpit.eatup.util.ConnectionManager
import org.json.JSONObject
import java.lang.Exception

class ForgotPasswordActivity : AppCompatActivity() {

    lateinit var etForgotMobileNumber: EditText
    lateinit var etForgotEmail: EditText
    lateinit var btnForgotNext: Button

    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        etForgotMobileNumber = findViewById(R.id.etForgotMobileNumber)
        etForgotEmail = findViewById(R.id.etForgotEmail)
        btnForgotNext = findViewById(R.id.btnForgotNext)



        btnForgotNext.setOnClickListener {
            if (ConnectionManager().checkConnectivity(this@ForgotPasswordActivity)) {
                val mobileNumber = etForgotMobileNumber.text.toString()
                val email = etForgotEmail.text.toString()

                if (mobileNumber.isEmpty() || email.isEmpty()) {
                    // fields are left blank
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        "No field should be left blank",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (mobileNumber.length < 10) {
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        "Minimum mobile number length should be 10 digits!!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    progressLayout = findViewById(R.id.progressLayoutPass)
                    progressLayout.visibility = View.VISIBLE
                    forgotPassword(mobileNumber, email)
                }
            } else {
                alertNoNet()
            }
        }

    }

    fun alertNoNet() {
        val dialog = AlertDialog.Builder(this@ForgotPasswordActivity)
        dialog.setTitle("Error")
        dialog.setMessage("Internet Connection is Not Found")
        dialog.setPositiveButton("Open Settings") { _, _ ->
            val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
            startActivity(settingsIntent)
            ActivityCompat.finishAffinity(this@ForgotPasswordActivity)
        }
        dialog.setNegativeButton("Exit") { _, _ ->
            ActivityCompat.finishAffinity(this@ForgotPasswordActivity)
        }

        dialog.create()
        dialog.show()
    }

    fun forgotPassword(mobileNumber: String, email: String) {

        val queueRequests = Volley.newRequestQueue(this@ForgotPasswordActivity)
        val jsonParams = JSONObject()
        jsonParams.put("mobile_number", mobileNumber)
        jsonParams.put("email", email)
        val url = "http://13.235.250.119/v2/forgot_password/fetch_result/"

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST,
            url,
            jsonParams,
            Response.Listener {

                try {
                    val dataObj = it.getJSONObject("data")
                    val success = dataObj.getBoolean("success")

                    if (success) {

                        val firstTry = dataObj.getBoolean("first_try")

                        if (firstTry) {
                            Toast.makeText(
                                this@ForgotPasswordActivity,
                                "Use OTP sent to you which is valid for 24 hours",
                                Toast.LENGTH_LONG
                            ).show()

                        } else {
                            Toast.makeText(
                                this@ForgotPasswordActivity,
                                "Use OTP sent to you in last 24 hours",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        // launching ResetPasswordActivity
                        progressLayout.visibility = View.GONE
                        val intentReset =
                            Intent(this@ForgotPasswordActivity, ResetPasswordActivity::class.java)
                        intentReset.putExtra("mobileNumber", mobileNumber)
                        startActivity(intentReset)
                        finish()

                    } else {
                        progressLayout.visibility = View.GONE
                        Toast.makeText(
                            this@ForgotPasswordActivity,
                            "OTP dispatch failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }


                } catch (e: Exception) {
                    progressLayout.visibility = View.GONE
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        "Some unexpected error has occurred!!",
                        Toast.LENGTH_LONG
                    ).show()
                }


            }, Response.ErrorListener {
                progressLayout.visibility = View.GONE
                Toast.makeText(
                    this@ForgotPasswordActivity,
                    "Volley Error $it",
                    Toast.LENGTH_LONG
                ).show()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-type"] = "application/json"
                headers["token"] = "your token"

                return headers
            }
        }

        queueRequests.add(jsonObjectRequest)

    }
}
