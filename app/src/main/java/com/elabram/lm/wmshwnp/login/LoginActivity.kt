package com.elabram.lm.wmshwnp.login

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import butterknife.ButterKnife
import com.crashlytics.android.Crashlytics
import com.elabram.lm.wmshwnp.R
import com.elabram.lm.wmshwnp.checkin.CheckinV1Activity
import com.elabram.lm.wmshwnp.utilities.AppInfo
import com.elabram.lm.wmshwnp.utilities.AppInfo.*
import com.google.android.material.snackbar.Snackbar
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_login2.*

class LoginActivity : AppCompatActivity(), LoginView {

    override val textUser: String
        get() = etUsername.text.toString()

    override val textPass: String
        get() = etPassword.text.toString()

    override val version: String
        get() = versionInfo!!

    override val imei: String
        get() = deviceId!!

    private var presenter: LoginPresenterImpl? = null
    private val mActivity = this@LoginActivity
    private var progressDialog: ProgressDialog? = null

    private val TAG = LoginActivity::class.java.simpleName
    private var adVersion: AlertDialog? = null

    private val versionInfo: String?
        get() {
            var versionName: String? = null
            //int versionCode = -1;
            //Log.e(TAG, "getVersionInfo: "+versionName );
            //versionCode = packageInfo.versionCode;
            try {
                val packageInfo = packageManager.getPackageInfo(packageName, 0)
                versionName = packageInfo.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            return versionName
        }

    private val deviceId: String?
        @SuppressLint("HardwareIds")
        get() {
            val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                    Toast.makeText(this, "Please allow the all permission to generate your account for login", Toast.LENGTH_SHORT).show()
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), 333)
                }

                return null
            }
            return telephonyManager.deviceId.trim { it <= ' ' }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)
        ButterKnife.bind(this)
        Fabric.with(this, Crashlytics())

        // Init
        progressDialog = ProgressDialog(this@LoginActivity, R.style.AppThemeLoading)
        progressDialog!!.isIndeterminate = true
        progressDialog!!.setMessage("Authenticating...")

        val loginRepository = LoginRepository(this)
        presenter = LoginPresenterImpl(this, loginRepository)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        // Method
        checkingPermission()
        presenter!!.retrofitCheckVersion()
        buttonLogin!!.setOnClickListener { login() }
    }

    private fun dismissAlert() {
        if (adVersion != null && adVersion!!.isShowing) {
            adVersion!!.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter!!.onDestroy()
        dismissAlert()
    }

    override fun dialogCheckVersion() {
        val builder = AlertDialog.Builder(this@LoginActivity)
        @SuppressLint("InflateParams") val view = layoutInflater.inflate(R.layout.dialog_version, null)

        val tvUpdate = view.findViewById<TextView>(R.id.tvUpdate)
        val tvNoThanks = view.findViewById<TextView>(R.id.tvNoThanks)

        builder.setView(view)
        adVersion = builder.create()

        adVersion!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        adVersion!!.setCancelable(false)

        if (!isFinishing)
            adVersion!!.show()

        // Go To Playstore
        tvUpdate.setOnClickListener {
            val appPackageName = packageName // getPackageName() from Context or Activity object
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
            } catch (anfe: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
            }
        }

        // Exit the apps
        tvNoThanks.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 21) {
                dismissAlert()
                finishAndRemoveTask()
            } else {
                dismissAlert()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    finishAffinity()
                } else {
                    ActivityCompat.finishAffinity(this)
                }
            }
        }

    }

    override fun showToast(message: String) {
        Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun login() {
        if (validate()) {
            if (isOnline(this)) {
                checkingPermission()
                presenter!!.login()
            } else {
                showToast("Check your internet connection")
            }
        }
    }

    private fun checkingPermission() {
        val permissionCode = 15
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE)

        if (!AppInfo.hasPermissions(mActivity, *permissions)) {
            ActivityCompat.requestPermissions(mActivity, permissions, permissionCode)
        }
    }

    override fun onResume() {
        super.onResume()
        checkingPermission()

        val preferences = getSharedPreferences(PREFS_LOGIN, 0)
        val loggin = preferences.getBoolean(PREFS_LOGGED, false)
        if (loggin) {
            gotoCheckinV1Activity()
        }

    }

    override fun showDialog() {
        if (progressDialog != null) {
            progressDialog!!.show()
        }
    }

    override fun dismissDialog() {
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
    }

//    override fun getTextUser(): String {
//        return etUsername!!.text.toString()
//    }
//
//    override fun getTextPass(): String {
//        return etPassword!!.text.toString()
//    }
//
//    override fun getImei(): String? {
//        return deviceId
//    }
//
//    override fun getVersion(): String? {
//        return versionInfo
//    }

    override fun showSnackbar(message: String) {
        Snackbar.make(containerMain!!, message, Snackbar.LENGTH_LONG).show()
    }

    override fun gotoCheckinV1Activity() {
        dismissDialog()
        startActivity(Intent(mActivity, CheckinV1Activity::class.java))
        this.finish()
    }

    private fun validate(): Boolean {
        var valid = true

        if (textUser.isEmpty()) {
            etUsername!!.error = "email is required"
            valid = false
        }

        if (textPass.isEmpty()) {
            etPassword!!.error = "password is required"
            valid = false
        }

        return valid
    }
}
