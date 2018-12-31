package com.elabram.lm.wmshwnp.login

interface LoginView {

    val textUser: String

    val textPass: String

    val imei: String

    val version: String

    fun showSnackbar(message: String)

    fun gotoCheckinV1Activity()

    fun dialogCheckVersion()

    fun showToast(message: String)

    fun showDialog()

    fun dismissDialog()

}
