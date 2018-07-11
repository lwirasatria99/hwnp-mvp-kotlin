package com.elabram.lm.wmsmobile.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elabram.lm.wmsmobile.CheckinActivity;
import com.elabram.lm.wmsmobile.R;
import com.squareup.picasso.Picasso;

import static com.elabram.lm.wmsmobile.utilities.AppInfo.PREFS_LOGIN;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.mem_image;

/**
 * Class : Home Fragment
 * Created by lalu.mahendra on 09-Mar-18.
 */

public class MainHomeFragment extends Fragment {

    private Activity mActivity;
    private String fullname;
    private String fullemail;
    private String user_image;

    public MainHomeFragment() {
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

    private void getSharedUserDetail() {
        SharedPreferences preferences = mActivity.getSharedPreferences(PREFS_LOGIN, Context.MODE_PRIVATE);
        fullname = preferences.getString("name", "");
        fullemail = preferences.getString("user_email", "");
        mem_image = preferences.getString("mem_image", "");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getSharedUserDetail();
        View view = inflater.inflate(R.layout.fragment_home2, container, false);

        TextView tvUsername = view.findViewById(R.id.tvName);
        TextView tvUseremail = view.findViewById(R.id.tvAuthority);
        ImageView ivUserpic = view.findViewById(R.id.ivProfil);

        if (mem_image.isEmpty()) {
            ivUserpic.setImageResource(R.drawable.man);
        } else {
            Picasso.with(mActivity)
                    .load(mem_image)
                    .fit()
                    .into(ivUserpic);
        }
        tvUsername.setText(fullname);
        tvUseremail.setText(fullemail);

//        RelativeLayout buttonOvertime = view.findViewById(R.id.buttonOvertime);
//        RelativeLayout buttonTimesheet = view.findViewById(R.id.buttonTimesheet);
//        RelativeLayout buttonTravel = view.findViewById(R.id.buttonTravel);
//        RelativeLayout buttonClaim = view.findViewById(R.id.buttonClaim);
//        RelativeLayout buttonMonthlyReport = view.findViewById(R.id.buttonMonthlyReport);
        RelativeLayout buttonCheckin = view.findViewById(R.id.buttonCheckin);

//        if (!role.equals("2")) {
//            buttonMonthlyReport.setVisibility(View.GONE);
//            buttonCheckin.setVisibility(View.GONE);
//        }

//        buttonOvertime.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                clickOvertime();
//            }
//        });
//
//        buttonTimesheet.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                clickTimesheet();
//            }
//        });
//
//        buttonTravel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                clickTravel();
//            }
//        });
//
//        buttonClaim.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                clickClaim();
//            }
//        });
//
//        buttonMonthlyReport.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                clickMonthlyReport();
//            }
//        });

        buttonCheckin.setOnClickListener(view1 -> startActivity(new Intent(mActivity, CheckinActivity.class)));

        return view;
    }

//    private void clickOvertime() {
//        startActivity(new Intent(mActivity, OvertimeActivity2.class));
//    }
//
//    private void clickTimesheet() {
//        startActivity(new Intent(mActivity, TimesheetActivity.class));
//    }
//
//    private void clickTravel() {
//        startActivity(new Intent(mActivity, TravelActivity.class));
//    }
//
//    private void clickClaim() {
//        startActivity(new Intent(mActivity, ClaimActivity.class));
//    }
//
//    private void clickMonthlyReport() {
//        startActivity(new Intent(mActivity, MonthlyReportActivity.class));
//    }

}
