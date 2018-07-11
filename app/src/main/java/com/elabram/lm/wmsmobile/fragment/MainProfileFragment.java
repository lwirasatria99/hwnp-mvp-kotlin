package com.elabram.lm.wmsmobile.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elabram.lm.wmsmobile.LoginActivity;
import com.elabram.lm.wmsmobile.R;
import com.elabram.lm.wmsmobile.rest.ApiClient;
import com.elabram.lm.wmsmobile.utilities.WordUtils;
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

import static com.elabram.lm.wmsmobile.utilities.AppInfo.PREFS_LOGIN;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.token;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.mem_nip;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.mem_address;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.mem_mobile;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.mem_phone;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.mem_image;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.position;


/**
 * Created by lalu.mahendra on 09-Mar-18.
 */

public class MainProfileFragment extends Fragment {

    @BindView(R.id.tvName)
    TextView tvName;
    @BindView(R.id.tvNIK)
    TextView tvNIK;
    @BindView(R.id.tvEmail)
    TextView tvEmail;
    @BindView(R.id.tvMobile)
    TextView tvMobile;
    @BindView(R.id.tvTelephone)
    TextView tvTelephone;
    @BindView(R.id.tvPosition)
    TextView tvPosition;
    @BindView(R.id.tvAuthority)
    TextView tvAuthority;
    @BindView(R.id.ivProfil)
    ImageView ivProfile;
    @BindView(R.id.buttonLogout)
    RelativeLayout buttonLogout;

    @BindView(R.id.rootView)
    RelativeLayout rootView;

    private Activity mActivity;
    private AlertDialog dialogLogout;

    private static final String TAG = MainProfileFragment.class.getSimpleName();
    private String user_image;
    private String user_fullname;
    private String user_position;
    private String user_email;
    private String user_phone;
    private String user_mobile;
    private String user_address;
    private ProgressDialog progressDialog;


    public MainProfileFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profil2, container, false);
        ButterKnife.bind(this, view);

        progressDialog = new ProgressDialog(mActivity, R.style.AppThemeLoading);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Please Wait...");

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getSharedUserDetail();
        setDetailsForm();
        buttonLogout.setOnClickListener(view1 -> dialogOpenLogout());
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

    private void retrofitProfile() {
        showDialog();

        Call<ResponseBody> profileCall = new ApiClient().getApiService().loadProfileParams(token);
        profileCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                dismissDialog();
                try {
                    //noinspection ConstantConditions
                    String responseContent = new String(response.body().bytes());
                    JSONObject jsonObject = new JSONObject(responseContent);

                    String message = jsonObject.getString("message");
                    String response_code = jsonObject.getString("response_code");

                    if (response_code.equals("200")) {
                        Log.e(TAG, "onResponse: " + message);
                        JSONObject jsonObject1 = jsonObject.getJSONObject("data");

                        user_image = jsonObject1.getString("user_image");
                        user_fullname = jsonObject1.getString("user_fullname");
                        user_position = jsonObject1.getString("user_position");
                        user_email = jsonObject1.getString("user_email");
                        user_phone = jsonObject1.getString("user_phone");
                        user_mobile = jsonObject1.getString("user_mobile");
                        user_address = jsonObject1.getString("user_address");
                        Log.e(TAG, "onResponse: Username " + user_fullname);

                    } else {
                        Log.e(TAG, "onResponse: " + message);
                        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
                dismissDialog();
            }
        });
    }

    private void dialogOpenLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.dialog_delete, null);

        TextView buttonNo = view.findViewById(R.id.No);
        TextView buttonYes = view.findViewById(R.id.Yes);
        TextView tvMessage = view.findViewById(R.id.tvMessage);

        builder.setView(view);
        dialogLogout = builder.create();
        //noinspection ConstantConditions
        dialogLogout.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogLogout.show();

        String s_logout = "Are you sure you want to logout ?";
        tvMessage.setText(s_logout);

        buttonNo.setOnClickListener(view1 -> dialogLogout.cancel());

        buttonYes.setOnClickListener(view12 -> logout());
    }

    private void logout() {
        // Clear SharedPreferences
        SharedPreferences settings = mActivity.getSharedPreferences(PREFS_LOGIN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();

        // Clear Activity And Fragment
        dialogLogout.dismiss();
        assert getFragmentManager() != null;
        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        Intent intent = new Intent(mActivity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    private void setDetailsForm() {
        tvName.setText(WordUtils.capitalize(user_fullname));
        tvAuthority.setText(position);

        tvNIK.setText(mem_nip);
        tvPosition.setText(position);
        tvEmail.setText(user_email);
        tvMobile.setText(mem_mobile);
        tvTelephone.setText(mem_phone);

        // Profile Picture
        if (user_image == null) {
            ivProfile.setImageResource(R.drawable.man);
        } else {
            Picasso.with(mActivity)
                    .load(user_image)
                    .fit()
                    .into(ivProfile);
        }

        // ID
        if (mem_nip.length() == 0) {
            tvNIK.setText("-");
        }

        // Position
        if (position.length() == 0) {
            tvPosition.setText("-");
        }

        // Email
        if (user_email.length() == 0) {
            tvEmail.setText("-");
        }

        // Mobile
        if (mem_mobile.length() == 0) {
            tvMobile.setText("-");
        }

        // Telephone
        if (mem_phone.length() == 0) {
            tvTelephone.setText("-");
        }
    }

    public void getSharedUserDetail() {
        SharedPreferences preferences = mActivity.getSharedPreferences(PREFS_LOGIN, Context.MODE_PRIVATE);
        token = preferences.getString("token", "");
        mem_nip = preferences.getString("mem_nip", "");
        mem_mobile = preferences.getString("mem_mobile", "");
        mem_phone = preferences.getString("mem_phone", "");
        mem_image = preferences.getString("mem_image", "");
        mem_address = preferences.getString("mem_address", "");
        position = preferences.getString("position", "");
        user_fullname = preferences.getString("name", "");
        user_email = preferences.getString("email", "");
    }

}
