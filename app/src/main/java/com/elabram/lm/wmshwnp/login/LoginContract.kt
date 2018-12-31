package com.elabram.lm.wmshwnp.login

interface LoginContract {

    interface Presenter {
        fun retrofitLogin()

        fun retrofitCheckVersion()

        fun onDestroy()

        fun login()
    }
}
