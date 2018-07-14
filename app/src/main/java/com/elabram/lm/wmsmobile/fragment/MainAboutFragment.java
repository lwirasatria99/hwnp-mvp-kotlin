package com.elabram.lm.wmsmobile.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elabram.lm.wmsmobile.R;
import com.elabram.lm.wmsmobile.rest.ApiClient;
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

/**
 * Class :
 * Created by lalu.mahendra on 09-Mar-18.
 */

public class MainAboutFragment extends Fragment {

    private static final String TAG = MainAboutFragment.class.getSimpleName();
    private Activity mActivity;
    private String versionName;

    @BindView(R.id.ivLogoClient)
    ImageView ivLogoClient;

    @BindView(R.id.rootView)
    RelativeLayout rootView;

    public MainAboutFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_about2, container, false);
        ButterKnife.bind(this, view);

        String wmsDescription = "" +
                "<p>WMS Mobile is brought to you by Elabram Systems, a company providing outsourcing and recruitment solutions, covering all level of expertise in Engineering and Telecommunication industry across Asia Pacific.</p>" +
                "<p>WMS Mobile is an integrated employee self-service solution that helps employees stay connected to their company’s information at any time, from anywhere.<p/>" +
                "<p>All information and features are the same as in WMS website but we give you the efficiency for checking, inputting and approving Timesheet, Overtime, Travel Request and Claim whenever and wherever you are.</p>";

        TextView tvAbout = view.findViewById(R.id.tvDescription);
        tvAbout.setText(Html.fromHtml(wmsDescription));

        TextView tvVersion = view.findViewById(R.id.tvVersion);
        getVersionInfo();
        tvVersion.setText("Version "+versionName);

//        SharedPreferences preferences = mActivity.getSharedPreferences("LOGO", 0);
//        String urlImage = preferences.getString("url", "");
//        if (!urlImage.equals("https://elabram.com/hris/")) {
//            Picasso.with(mActivity)
//                    .load(urlImage)
//                    .fit()
//                    .into(ivLogoClient);
//        }
        retrofitListClient();

        return view;
    }


    private void retrofitListClient() {
        Call<ResponseBody> call = new ApiClient().getApiService().listLogo(token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String mResponse = new String(response.body().bytes());
//                    Log.e(TAG, "onResponse: " + mResponse);
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
                            if (!urlImage.equals("https://elabram.com/hris/")) {
                                Picasso.with(mActivity)
                                        .load(urlImage)
                                        .fit()
                                        .into(ivLogoClient);
                            }


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

    private void getVersionInfo() {
        //int versionCode = -1;
        try {
            PackageInfo packageInfo = mActivity.getPackageManager()
                    .getPackageInfo(mActivity.getPackageName(), 0);
            versionName = packageInfo.versionName;
            //Log.e(TAG, "getVersionInfo: "+versionName );
            //versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

}
