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

class ResetPasswordActivity : AppCompatActivity() {

    lateinit var etResetOtp: EditText
    lateinit var etResetPassword: EditText
    lateinit var etResetPasswordConfirm: EditText
    lateinit var btnResetSubmit: Button

    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar

    lateinit var sharedPreferences: SharedPreferences
    val spFileName = "EatUp Preferences"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        etResetOtp = findViewById(R.id.etResetOtp)
        etResetPassword = findViewById(R.id.etResetPassword)
        etResetPasswordConfirm = findViewById(R.id.etResetPasswordConfirm)
        btnResetSubmit = findViewById(R.id.btnResetSubmit)

        btnResetSubmit.setOnClickListener {
            if (ConnectionManager().checkConnectivity(this@ResetPasswordActivity)) {
                val otp = etResetOtp.text.toString()
                val password = etResetPassword.text.toString()
                val passwordConfirm = etResetPasswordConfirm.text.toString()
                val mobileNumber = intent.extras?.get("mobileNumber").toString()

                if (otp.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
                    // fields are left blank
                    Toast.makeText(
                        this@ResetPasswordActivity,
                        "No fields should be left blank", Toast.LENGTH_LONG
                    ).show()
                } else if (mobileNumber.isEmpty()) {
                    Toast.makeText(
                        this@ResetPasswordActivity,
                        "Mobile number couldn't be found", Toast.LENGTH_LONG
                    ).show()
                } else if (password.length < 4) {
                    Toast.makeText(
                        this@ResetPasswordActivity,
                        "Minimum password length should be 4 characters!!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    if (password != passwordConfirm) {
                        Toast.makeText(
                            this@ResetPasswordActivity,
                            "Passwords entered don't match", Toast.LENGTH_LONG
                        ).show()
                    } else {
                        progressLayout = findViewById(R.id.progressLayoutReset)
                        progressLayout.visibility = View.VISIBLE
                        resetPassword(mobileNumber, password, otp)
                    }
                }
            } else {
                alertNoNet()
            }
        }


    }

    fun alertNoNet() {
        val dialog = AlertDialog.Builder(this@ResetPasswordActivity)
        dialog.setTitle("Error")
        dialog.setMessage("Internet Connection is Not Found")
        dialog.setPositiveButton("Open Settings") { _, _ ->
            val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
            startActivity(settingsIntent)
            ActivityCompat.finishAffinity(this@ResetPasswordActivity)
        }
        dialog.setNegativeButton("Exit") { _, _ ->
            ActivityCompat.finishAffinity(this@ResetPasswordActivity)
        }

        dialog.create()
        dialog.show()
    }

    fun resetPassword(mobileNumber: String, password: String, otp: String) {

        val queueRequests = Volley.newRequestQueue(this@ResetPasswordActivity)
        val url = "http://13.235.250.119/v2/reset_password/fetch_result/"
        val jsonParams = JSONObject()
        jsonParams.put("mobile_number", mobileNumber)
        jsonParams.put("password", password)
        jsonParams.put("otp", otp)


        val jsonObjectRequest =
            object : JsonObjectRequest(Request.Method.POST,
                url,
                jsonParams,
                Response.Listener {
                    try {
                        val dataObj = it.getJSONObject("data")
                        val success = dataObj.getBoolean("success")

                        if (success) {
                            Toast.makeText(
                                this@ResetPasswordActivity,
                                dataObj.getString("successMessage"),
                                Toast.LENGTH_LONG
                            ).show()

                            sharedPreferences =
                                getSharedPreferences(spFileName, Context.MODE_PRIVATE)
                            sharedPreferences.edit().clear().apply()
                            progressLayout.visibility = View.GONE
                            val intentLoginPage =
                                Intent(this@ResetPasswordActivity, LoginActivity::class.java)
                            intentLoginPage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intentLoginPage)
                            finish()

                        } else {
                            progressLayout.visibility = View.GONE
                            Toast.makeText(
                                this@ResetPasswordActivity,
                                dataObj.getString("successMessage"),
                                Toast.LENGTH_LONG
                            ).show()
                        }


                    } catch (e: Exception) {
                        progressLayout.visibility = View.GONE
                        Toast.makeText(
                            this@ResetPasswordActivity,
                            "Some unexpected error has occurred!!",
                            Toast.LENGTH_LONG
                        ).show()

                    }

                }, Response.ErrorListener {
                    progressLayout.visibility = View.GONE
                    Toast.makeText(
                        this@ResetPasswordActivity,
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
}
