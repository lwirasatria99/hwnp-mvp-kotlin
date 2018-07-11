package com.elabram.lm.wmsmobile;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.elabram.lm.wmsmobile.utilities.AppInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.elabram.lm.wmsmobile.utilities.AppInfo.PREFS_LOGGED;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.PREFS_LOGIN;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.PRE_URL;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.etUsername)
    EditText etUsername;
    @BindView(R.id.etPassword)
    EditText etPassword;
    @BindView(R.id.containerMain)
    RelativeLayout containerMain;
    @BindView(R.id.buttonLogin)
    Button buttonLogin;

    private Activity mActivity = LoginActivity.this;
    private String TAG = LoginActivity.class.getSimpleName();
    private ProgressDialog progressDialog;
    private AppInfo info = new AppInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        checkingPermission();

        progressDialog = new ProgressDialog(LoginActivity.this, R.style.AppThemeLoading);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");

        buttonLogin.setOnClickListener(view -> login());
    }

    private void login() {
        if (validate()) {
            volleyLogin();
        }
    }

    private void checkingPermission() {
        int PERMISSION_ALL = 15;
        String[] PERMISSIONS = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        if (!AppInfo.hasPermissions(mActivity, PERMISSIONS)) {
            ActivityCompat.requestPermissions(mActivity, PERMISSIONS, PERMISSION_ALL);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences(PREFS_LOGIN, 0);
        boolean loggin = preferences.getBoolean(PREFS_LOGGED, false);
        Log.e(TAG, "onResume: Loggin " + loggin);
        if (loggin)
        {
            showMainMenu();
        }
//        else
//        {
//            dismissVersion();
//            retrofitCheckVersion();
//        }

    }

    private void showMainMenu()
    {
        dismissDialog();
        startActivity(new Intent(mActivity, MainActivity.class));
        this.finish();
    }

    private void showDialog() {
        if (progressDialog != null) {
            progressDialog.show();
        }
    }

    private void dismissDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void volleyLogin() {
        showDialog();

        String API_LOGIN = PRE_URL + "/login";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, API_LOGIN, response -> {
            Log.e(TAG, "onResponse: VolleyLogin " + response);
            dismissDialog();

            SharedPreferences sPreferences = getSharedPreferences(PREFS_LOGIN, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sPreferences.edit();
            try {
                JSONObject jsonObject = new JSONObject(response);
                String message = jsonObject.getString("message");

                if (message.equalsIgnoreCase("success")) {

                    JSONObject jsonData = jsonObject.getJSONObject("data");

                    String token = jsonData.getString("token");
                    String user_id = jsonData.getString("user_id");
                    String user_name = jsonData.getString("user_name");
                    String user_email = jsonData.getString("user_email");
                    String user_type = jsonData.getString("user_type");

                    JSONObject jsonUserData = jsonData.getJSONObject("user_data");
                    String userdata_name = jsonUserData.getString("name");
                    String mem_nip = jsonUserData.getString(getString(R.string.nip));
                    String mem_mobile = jsonUserData.getString(getString(R.string.mobile));
                    String mem_phone = jsonUserData.getString(getString(R.string.phone));
                    String mem_address = jsonUserData.getString(getString(R.string.address));
                    String mem_image = jsonUserData.getString("mem_image");
                    String position = jsonUserData.getString(getString(R.string.position));


                    editor.putBoolean(AppInfo.PREFS_LOGGED, true);
                    editor.putString("email", getTextUser());
                    editor.putString("password", getTextPass());
                    editor.putString("user_id", user_id);
                    editor.putString("user_name", user_name);
                    editor.putString("user_email", user_email);
                    editor.putString("user_type", user_type);
                    editor.putString("token", token);

                    editor.putString("name", userdata_name);
                    editor.putString(getString(R.string.nip), mem_nip);
                    editor.putString(getString(R.string.mobile), mem_mobile);
                    editor.putString(getString(R.string.phone), mem_phone);
                    editor.putString("mem_image", mem_image);
                    editor.putString(getString(R.string.address), mem_address);
                    editor.putString(getString(R.string.position), position);

                    editor.apply();

                    showMainMenu();
                } else {
                    Snackbar snackbar = Snackbar.make(containerMain, message, Snackbar.LENGTH_LONG);
                    snackbar.show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            info.errorResponse(error, mActivity);
            dismissDialog();
        })

        {

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams()
            {
                HashMap<String, String> params = new HashMap<>();
                params.put("mem_email", getTextUser());
                params.put("mem_password", getTextPass());
                return params;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(mActivity);
        requestQueue.add(stringRequest);
    }

    private String getTextUser() {
        return etUsername.getText().toString();
    }

    private String getTextPass() {
        return etPassword.getText().toString();
    }

    private boolean validate() {
        boolean valid = true;

        if (getTextUser().length() <= 0) {
            etUsername.setError("email is required");
            valid = false;
        }

        if (getTextPass().length() <= 0) {
            etPassword.setError("password is required");
            valid = false;
        }

        return valid;
    }
}
