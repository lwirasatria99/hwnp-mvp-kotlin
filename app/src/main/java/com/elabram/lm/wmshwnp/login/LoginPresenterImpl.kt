package com.elabram.lm.wmshwnp.login

import android.util.Log

import com.elabram.lm.wmshwnp.rest.ApiClient
import com.elabram.lm.wmshwnp.rest.ApiClientLogin

import org.json.JSONException
import org.json.JSONObject

import java.io.IOException

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import com.elabram.lm.wmshwnp.utilities.AppInfo.PREFS_LOGGED

class LoginPresenterImpl (private var view: LoginView?,
                                              private val loginRepository: LoginRepository) : LoginContract.Presenter {

    override fun retrofitLogin() {
        view!!.showDialog()
        val call = ApiClientLogin().apiService.login(
                view!!.textUser,
                view!!.textPass,
                "1",
                view!!.imei)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                view!!.dismissDialog()
                try {
                    if (response.body() != null) {

                        val responseContent = response.body()!!.string()
                        Log.e(TAG, "onResponse: Login -> $responseContent")

                        try {
                            val jsonObject = JSONObject(responseContent)
                            val message = jsonObject.getString("message")

                            if (message.equals("success", ignoreCase = true)) {
                                // data
                                val jsonData = jsonObject.getJSONObject("data")
                                val token = jsonData.getString("token")
                                val userId = jsonData.getString("user_id")
                                val userName = jsonData.getString("user_name")
                                val userEmail = jsonData.getString("user_email")
                                val userType = jsonData.getString("user_type")
                                // user_data
                                val jsonUserData = jsonData.getJSONObject("user_data")
                                val userdataName = jsonUserData.getString("name")
                                val memNip = jsonUserData.getString("mem_nip")
                                val memId = jsonUserData.getString("mem_id")
                                val memMobile = jsonUserData.getString("mem_mobile")
                                val memPhone = jsonUserData.getString("mem_phone")
                                val memAddress = jsonUserData.getString("mem_address")
                                val memImage = jsonUserData.getString("mem_image")
                                val position = jsonUserData.getString("position")

                                loginRepository.putBoolean(PREFS_LOGGED, true)
                                loginRepository.putString("email", view!!.textUser)
                                loginRepository.putString("password", view!!.textPass)
                                loginRepository.putString("user_id", userId)
                                loginRepository.putString("user_name", userName)
                                loginRepository.putString("user_email", userEmail)
                                loginRepository.putString("user_type", userType)
                                loginRepository.putString("token", token)

                                loginRepository.putString("name", userdataName)
                                loginRepository.putString("mem_id", memId)
                                loginRepository.putString("mem_nip", memNip)
                                loginRepository.putString("mem_mobile", memMobile)
                                loginRepository.putString("mem_phone", memPhone)
                                loginRepository.putString("mem_image", memImage)
                                loginRepository.putString("mem_address", memAddress)
                                loginRepository.putString("position", position)

                                view!!.gotoCheckinV1Activity()
                            } else {
                                loginRepository.putBoolean(PREFS_LOGGED, false)
                                view!!.showSnackbar(message)
                            }

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message)
                view!!.showSnackbar("Check your network / please try again")
                view!!.dismissDialog()
            }
        })
    }

    override fun retrofitCheckVersion() {
        val call = ApiClient().apiService.checkVersion()
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    if (response.body() != null) {

                        val mResponse = String(response.body()!!.bytes())
                        //Log.e(TAG, "onResponse: CheckVersion " + mResponse);
                        val jsonObject = JSONObject(mResponse)
                        val responseCode = jsonObject.getString("response_code")
                        val message = jsonObject.getString("message")

                        when (responseCode) {
                            "401" -> view!!.showSnackbar(message)
                            "200" -> {
                                val jsonArray = jsonObject.getJSONArray("data")
                                for (i in 0 until jsonArray.length()) {
                                    val jsonObject1 = jsonArray.getJSONObject(i)
                                    val sVersion = jsonObject1.getString("version")
                                    if (sVersion != view!!.version) {
                                        view!!.dialogCheckVersion()
                                    }
                                }
                            }
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e(TAG, "onFailure: Version " + t.message)
            }
        })
    }

    override fun onDestroy() {
        view = null
    }

    override fun login() {
        retrofitLogin()
    }

    companion object {

        private val TAG = LoginPresenterImpl::class.java.simpleName
    }

}
