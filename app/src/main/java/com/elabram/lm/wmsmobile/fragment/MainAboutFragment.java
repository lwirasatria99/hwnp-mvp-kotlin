package com.elabram.lm.wmsmobile.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.elabram.lm.wmsmobile.R;

/**
 * Class :
 * Created by lalu.mahendra on 09-Mar-18.
 */

public class MainAboutFragment extends Fragment {

    private Activity mActivity;
    private String versionName;

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

        String wmsDescription = "" +
                "<p>WMS Mobile is brought to you by Elabram Systems, logo_indosat company providing outsourcing and recruitment solutions, covering all level of expertise in Engineering and Telecommunication industry across Asia Pacific.</p>" +
                "<p>WMS Mobile is an integrated employee self-service solution that helps employees stay connected to their company’s information at any time, from anywhere.<p/>" +
                "<p>All information and features are the same as in WMS website but we give you the efficiency for checking, inputting and approving Timesheet, Overtime, Travel Request and Claim whenever and wherever you are.</p>";

        TextView tvAbout = view.findViewById(R.id.tvDescription);
        tvAbout.setText(Html.fromHtml(wmsDescription));

        TextView tvVersion = view.findViewById(R.id.tvVersion);
        getVersionInfo();
        tvVersion.setText("Version "+versionName);

        return view;
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
