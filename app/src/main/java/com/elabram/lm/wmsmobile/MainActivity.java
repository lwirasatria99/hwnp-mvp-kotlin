package com.elabram.lm.wmsmobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RelativeLayout;

import com.crashlytics.android.Crashlytics;
import com.elabram.lm.wmsmobile.fragment.MainAboutFragment;
import com.elabram.lm.wmsmobile.fragment.MainHomeFragment;
import com.elabram.lm.wmsmobile.fragment.MainProfileFragment;
import com.elabram.lm.wmsmobile.rest.ApiClient;
import com.elabram.lm.wmsmobile.utilities.AppInfo;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.elabram.lm.wmsmobile.utilities.AppInfo.token;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.user_email;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.navigation)
    BottomNavigationView navigation;

    @BindView(R.id.container)
    RelativeLayout rootView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        getSharedUser();
        Crashlytics.log(TAG + " " + user_email);

        // Check Internet Connection
//        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        assert cm != null;
//        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        NetworkInfo data_provider = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//        if ((wifi != null & data_provider != null) && wifi.isConnected() | data_provider.isConnected()) {
//            Log.e(TAG, "onCreate: Connected");
//        } else {
//            Log.e(TAG, "onCreate: No connection");
//        }

        if (AppInfo.isOnline(this)) {
            Log.e(TAG, "onCreate: from App Connected");
        } else {
            Log.e(TAG, "onCreate: from App No Connection");
        }

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_home);
    }

    private void getSharedUser() {
        SharedPreferences preferences = getSharedPreferences(AppInfo.PREFS_LOGIN, Context.MODE_PRIVATE);
        user_email = preferences.getString("user_email", "");

    }

    BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        Fragment fragment;
        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragment = new MainHomeFragment();
                replaceFragment(fragment);
                return true;
            case R.id.navigation_dashboard:
                fragment = new MainAboutFragment();
                replaceFragment(fragment);
                return true;
            case R.id.navigation_notifications:
                fragment = new MainProfileFragment();
                replaceFragment(fragment);
                return true;
        }
        return false;
    };

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameContainer, fragment);
        transaction.commit();
    }

    private void retrofitListClient() {
        Call<ResponseBody> call = new ApiClient().getApiService().listLogo(token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String mResponse = new String(response.body().bytes());
                    Log.e(TAG, "onResponse: " + mResponse);
                    JSONObject jsonObject = new JSONObject(mResponse);

                    String response_code = jsonObject.getString("response_code");
                    switch (response_code) {
                        case "401":
                            String message = jsonObject.getString("message");
                            Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
                            snackbar.show();
                            break;
                        case "200":
                            JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                            String urlImage = jsonObject1.getString("cus_logo");
                            Log.e(TAG, "onResponse: urlImage " + urlImage);
//                            SharedPreferences preferences = getSharedPreferences("LOGO", 0);
//                            SharedPreferences.Editor editor = preferences.edit();
//                            editor.putString("url", urlImage);
//                            editor.apply();
//                            if (!urlImage.equals("https://elabram.com/hris/")) {
//                                Picasso.with(mActivity)
//                                        .load(urlImage)
//                                        .fit()
//                                        .into(ivLogoClient);
//                            }


                            break;
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getCause());
            }
        });
    }


}
