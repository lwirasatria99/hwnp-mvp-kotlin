package com.elabram.lm.wmshwnp.login

import android.content.Context
import com.elabram.lm.wmshwnp.utilities.AppInfo.PREFS_LOGIN

class LoginRepository(private val mContext: Context) {

    fun putString(key: String, value: String) {
        val sPreferences = mContext.getSharedPreferences(PREFS_LOGIN, Context.MODE_PRIVATE)
        val editor = sPreferences.edit()
        editor.putString(key, value).apply()
    }

    fun putBoolean(key: String, value: Boolean) {
        val sPreferences = mContext.getSharedPreferences(PREFS_LOGIN, Context.MODE_PRIVATE)
        val editor = sPreferences.edit()
        editor.putBoolean(key, value).apply()
    }
}
