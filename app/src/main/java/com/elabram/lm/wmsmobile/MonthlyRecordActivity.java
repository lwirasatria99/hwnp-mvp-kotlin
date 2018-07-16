package com.elabram.lm.wmsmobile;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.elabram.lm.wmsmobile.adapter.MonthlyAdapter;
import com.elabram.lm.wmsmobile.model.Monthly;
import com.elabram.lm.wmsmobile.rest.ApiClient;
import com.elabram.lm.wmsmobile.utilities.AppInfo;
import com.shawnlin.numberpicker.NumberPicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.elabram.lm.wmsmobile.utilities.AppInfo.PREFS_LOGIN;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.isOnline;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.mem_id;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.token;
import static com.elabram.lm.wmsmobile.utilities.AppInfo.user_email;

public class MonthlyRecordActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = MonthlyRecordActivity.class.getSimpleName();

    @BindView(R.id.linearMonth)
    RelativeLayout linearMonth;

    @BindView(R.id.tvDatePeriode)
    TextView tvDatePeriode;

//    @BindView(R.id.swiperefresh)
//    SwipeRefreshLayout swipeRefresh;

    @BindView(R.id.listview)
    ListView listView;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.tvNoData)
    TextView tvNoData;

    @BindView(R.id.relativeLoadMore)
    RelativeLayout relativeLoadMore;

    @BindView(R.id.rootView)
    LinearLayout rootView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.relativeEnabled)
    RelativeLayout rel_online;

    @BindView(R.id.relativeDisabled)
    RelativeLayout rel_offline;

    @BindView(R.id.buttonRefresh)
    ImageView buttonRefresh;

    private static final int MAX_YEAR = 2099;
    private static final String[] Months = new String[]{"January", "February",
            "March", "April", "May", "June", "July", "August", "September",
            "October", "November", "December"};

    private ArrayList<Monthly> monthlies;
    private MonthlyAdapter adapter;
    private String date_take;
    private String s_datefilter;
    private String lastDay;
    private String firstDay;
    private String realLastDay;
    private String formattedRealNumber;
    private String get_date_now;
    private String get_date_last;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthlyrecord);
        ButterKnife.bind(this);
        Fabric.with(this, new Crashlytics());
        Crashlytics.log(TAG + " "+user_email);

        // Toolbar
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
        // End

        getSharedUserDetail();
        monthlies = new ArrayList<>();
        adapter = new MonthlyAdapter(this, monthlies);
        listView.setAdapter(adapter);

        tvNoData.setVisibility(View.GONE);
        relativeLoadMore.setVisibility(View.GONE);

        setInitialDate();

        cekInternet();
        buttonRefresh.setOnClickListener(view -> cekInternet());

        linearMonth.setOnClickListener(view -> showDateDialog(getDatePeriode()));
    }

    private void cekInternet() {
        if (AppInfo.isOnline(this)) {
            rel_online.setVisibility(View.VISIBLE);
            showProgress();
            retrofitListMonthly();
            rel_offline.setVisibility(View.GONE);
        } else {
            rel_online.setVisibility(View.GONE);
            rel_offline.setVisibility(View.VISIBLE);
        }
    }

    private void getSharedUserDetail() {
        SharedPreferences preferences = getSharedPreferences(PREFS_LOGIN, Context.MODE_PRIVATE);
        token = preferences.getString("token", "");
        mem_id = preferences.getString("mem_id", "");
        user_email = preferences.getString("user_email", "");
    }

    private void retrofitListMonthly() {
        if (monthlies != null) {
            monthlies.clear();
            adapter.notifyDataSetChanged();
        }

        if (firstDay == null) {
            firstDay = get_date_now;
        }
        if (realLastDay == null) {
            realLastDay = get_date_last;
        }

        Call<ResponseBody> call = new ApiClient().getApiService().monthlyList(token, firstDay, realLastDay, mem_id);
        Log.e(TAG, "retrofitListMonthly: first " + firstDay);
        Log.e(TAG, "retrofitListMonthly: last " + realLastDay);
        Log.e(TAG, "retrofitListMonthly: mem_id " + mem_id);
        Log.e(TAG, "retrofitListMonthly: token " + token);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                hideProgress();
                try {
                    //noinspection ConstantConditions
                    String content = new String(response.body().bytes());
                    Log.e(TAG, "onResponse: Monthly List " + content);
                    parseJSON(content);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                hideProgress();
                Log.e(TAG, "onFailure: " + t.getCause());
            }
        });
    }

    private void parseJSON(String content) throws JSONException {
        JSONObject jsonObject = new JSONObject(content);
        String response_code = jsonObject.getString("response_code");
        switch (response_code) {
            case "401":
                String message = jsonObject.getString("message");
                Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
                snackbar.show();
                break;
            case "200":
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                if (jsonArray.length() == 0) {
                    tvNoData.setVisibility(View.VISIBLE);
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                    String att_id_token = jsonObject1.getString("att_id_token");
                    String att_date = jsonObject1.getString("att_date");
                    String att_day = jsonObject1.getString("att_day");
                    String time_in = jsonObject1.getString("time_in");
                    String time_out = jsonObject1.getString("time_out");
                    String project_name = jsonObject1.getString("project_name");
                    String description = jsonObject1.getString("description");
                    String location_first = jsonObject1.getString("location_first");
                    String location_last = jsonObject1.getString("location_last");
                    String total_working_hour = jsonObject1.getString("total_working_hour");

                    Monthly monthly = new Monthly();
                    monthly.setAttDate(att_date);
                    monthly.setAttDay(att_day);
                    monthly.setTimeIn(time_in);
                    monthly.setTimeOut(time_out);
                    monthly.setLocationFirst(location_first);
                    monthly.setLocationLast(location_last);
                    monthly.setTotalWorkingHour(total_working_hour);
                    monthlies.add(monthly);
                    listView.setAdapter(adapter);
                }
                break;
        }
    }

    private void setInitialDate() {
        DateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        Date date = new Date();
        String s_datenow = dateFormat.format(date);

        if (getDatePeriode().equals("null")) {
            tvDatePeriode.setText(s_datenow);
        }

        @SuppressLint("SimpleDateFormat") DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-01");
        Date date1 = new Date();

        Calendar calendar = Calendar.getInstance();

        String get_day_last = String.valueOf(calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        int month = calendar.get(Calendar.MONTH) + 1;
        String get_month = null;
        if (month < 10) {
            get_month = "0" + month;
        }

        String get_year = String.valueOf(calendar.get(Calendar.YEAR));
        get_date_last = get_year + "-" + get_month + "-" + get_day_last;
        Log.e(TAG, "setInitialDate: Last " + get_date_last);
        get_date_now = dateFormat1.format(date1);
        Log.e(TAG, "setInitialDate: First " + get_date_now);
    }

    public void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }

    public String getDatePeriode() {
        return tvDatePeriode.getText().toString();
    }

    private void showDateDialog(final String s_tvdateperiode) {
        String split_calendar[] = s_tvdateperiode.split("\\s+");
        String split_month = split_calendar[0];
        //String split_year = split_calendar[1];

        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ActionThemeDialog);
        LayoutInflater inflater = getLayoutInflater();

        @SuppressLint("InflateParams") final View dialogView = inflater.inflate(R.layout.dialog_month_picker, null);
        final NumberPicker monthPicker = dialogView.findViewById(R.id.picker_month);
        final NumberPicker yearPicker = dialogView.findViewById(R.id.picker_year);
        final ImageView imageCheck = dialogView.findViewById(R.id.imageCheck);
        final ImageView imageCross = dialogView.findViewById(R.id.imageCross);

        Calendar cal = Calendar.getInstance();

        Typeface type = Typeface.createFromAsset(getAssets(), "fonts/Ubuntu-B.ttf");
        monthPicker.setTypeface(type);
        yearPicker.setTypeface(type);

        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(Months.length - 1);
        monthPicker.setWrapSelectorWheel(true);
        monthPicker.setDisplayedValues(Months);
        //int month = cal.get(Calendar.MONTH);
        for (int i = 0; i < Months.length; i++) {
            if (Months[i].equalsIgnoreCase(split_month)) {
                monthPicker.setValue(i);
                break;
            }
        }
//        monthPicker.setValue();

        monthPicker.setFormatter(value -> {
            // TODO Auto-generated method stub
            return Months[value];
        });

        int yearMin = 1994;
        int year = cal.get(Calendar.YEAR);
        yearPicker.setMinValue(yearMin);
        yearPicker.setMaxValue(MAX_YEAR);
        yearPicker.setValue(year);

        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        imageCheck.setOnClickListener(view -> findDataMonthly(monthPicker, yearPicker, alertDialog));

        imageCross.setOnClickListener(view -> alertDialog.dismiss());

    }

    private void findDataMonthly(NumberPicker monthPicker, NumberPicker yearPicker, AlertDialog alertDialog) {
        onDateSet(null, yearPicker.getValue(), monthPicker.getValue(), 0);
        Log.e(TAG, "onClick Date Take: " + date_take);
        int realMonthNumber = monthPicker.getValue() + 1;
        Log.e(TAG, "findDataMonthly: Month " + realMonthNumber);

        // Date Convert
        SimpleDateFormat inputFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-01", Locale.getDefault());
        Date dateFormat = null;
        try {
            dateFormat = inputFormat.parse(date_take);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        firstDay = outputFormat.format(dateFormat);
        Log.e(TAG, "findDataMonthly: source date " + firstDay);
        // End

        // Get The Last Day
        @SuppressLint("SimpleDateFormat") SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date convertedDate = null;
        try {
            convertedDate = inFormat.parse(firstDay);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(convertedDate);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        lastDay = String.valueOf(c.getActualMaximum(Calendar.DAY_OF_MONTH));
        if (realMonthNumber < 10) {
            formattedRealNumber = "0" + realMonthNumber;
        }
        realLastDay = yearPicker.getValue() + "-" + formattedRealNumber + "-" + lastDay;
        Log.e(TAG, "findDataMonthly: LastDay" + realLastDay);

        if (isOnline(this)) {
            retrofitListMonthly();
        } else {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
        }

        alertDialog.dismiss();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
        int month = monthOfYear + 1;

        String formattedMonth = "" + month;

        if (month < 10) {
            formattedMonth = "0" + month;
        }

        date_take = formattedMonth + " " + year;
        if (!date_take.isEmpty()) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat origin = new SimpleDateFormat("MM yyyy");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat destination = new SimpleDateFormat("MMMM yyyy");
            Date newFormat = null;
            try {
                newFormat = origin.parse(date_take);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            date_take = destination.format(newFormat);
            tvDatePeriode.setText(date_take);
        }

    }
}
