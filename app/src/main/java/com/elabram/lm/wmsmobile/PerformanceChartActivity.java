package com.elabram.lm.wmsmobile;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elabram.lm.wmsmobile.model.PerformanceModel;
import com.elabram.lm.wmsmobile.rest.ApiClient;
import com.elabram.lm.wmsmobile.utilities.DecimalRemover;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.shawnlin.numberpicker.NumberPicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

import static com.elabram.lm.wmsmobile.utilities.AppInfo.token;

public class PerformanceChartActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    private static final int MAX_YEAR = 2099;
    private static final String TAG = PerformanceChartActivity.class.getSimpleName();

    @BindView(R.id.chart1)
    BarChart mChart;

    @BindView(R.id.iv_back)
    ImageView ivBack;

    @BindView(R.id.rootView)
    RelativeLayout rootView;

    @BindView(R.id.tv_year)
    TextView tv_year;

    private Disposable disposable;
    private ArrayList<PerformanceModel> pModels = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        ButterKnife.bind(this);

        ivBack.setOnClickListener(view -> finish());

        tv_year.setOnClickListener(view -> showDateDialog());

        setCurrentYear();

        retrofitChart();

    }

    private void setCurrentYear() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        tv_year.setText(String.valueOf(year));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null)
            disposable.dispose();
    }

    private String getTextYear() {
        return tv_year.getText().toString();
    }

    private void retrofitChart() {
        Observable<ResponseBody> call = new ApiClient().getApiService().readChart(getParamsChart());
        call.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            String mResponse = responseBody.string();
                            JSONObject jsonObject = new JSONObject(mResponse);
                            Log.e(TAG, "onNext: " + jsonObject);
                            String response_code = jsonObject.getString("response_code");
                            switch (response_code) {
                                case "401":
                                    String message = jsonObject.getString("message");
                                    Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
                                    break;
                                case "200":
                                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                                        String s_month = jsonObject1.getString("month");
                                        String s_latetime = jsonObject1.getString("latetime");
                                        String s_ontime = jsonObject1.getString("ontime");

                                        PerformanceModel pModel = new PerformanceModel();
                                        pModel.setMonth(s_month);
                                        pModel.setLatetime(s_latetime);
                                        pModel.setOntime(s_ontime);
                                        pModels.add(pModel);
                                    }

                                    break;
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(PerformanceChartActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                        setChart();
                    }
                });
    }

    private HashMap<String, String> getParamsChart() {
        HashMap<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("isYear", getTextYear());
        Log.e(TAG, "getParamsChart: " + params);
        return params;
    }

    private void setChart() {
        mChart.setOnChartValueSelectedListener(this);
        mChart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(40);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);
        mChart.setScaleEnabled(false);

        mChart.setDrawGridBackground(false);
        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(false);
        mChart.setHighlightFullBarEnabled(false);

        mChart.getXAxis().setDrawGridLines(false);
        mChart.getAxisLeft().setDrawGridLines(false);

        // change the position of the y-labels
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setAxisMaximum(31);
        leftAxis.setAxisMinimum(0); // this replaces setStartAtZero(true)
        leftAxis.setLabelCount(31);
        mChart.getAxisRight().setEnabled(false);

        ArrayList<String> xAxisLabel = new ArrayList<>();
        xAxisLabel.add("Jan");
        xAxisLabel.add("Feb");
        xAxisLabel.add("Mar");
        xAxisLabel.add("Apr");
        xAxisLabel.add("May");
        xAxisLabel.add("Jun");
        xAxisLabel.add("Jul");
        xAxisLabel.add("Aug");
        xAxisLabel.add("Sep");
        xAxisLabel.add("Oct");
        xAxisLabel.add("Nov");
        xAxisLabel.add("Dec");

        XAxis xLabels = mChart.getXAxis();
        xLabels.setLabelCount(11);
        xLabels.setPosition(XAxis.XAxisPosition.BOTTOM);
        mChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisLabel));

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setFormSize(8f);
        l.setFormToTextSpace(4f);
        l.setXEntrySpace(6f);

        // for dummy
        //final int min = 1;
        //final int max = 12;

        ArrayList<BarEntry> yVals1 = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            // for dummy
            //final int random1 = new Random().nextInt((max - min) + 1) + min;
            //final int random2 = new Random().nextInt((max - min) + 1) + min;

            int i_latetime = Integer.parseInt(pModels.get(i).getLatetime());
            int i_ontime = Integer.parseInt(pModels.get(i).getOntime());

            yVals1.add(new BarEntry(
                    i,
                    new float[]{(float) i_latetime, (float) i_ontime},
                    getResources().getDrawable(R.drawable.star)));
        }

        BarDataSet barDataSet;

        if (mChart.getData() != null && mChart.getData().getDataSetCount() > 0) {
            barDataSet = (BarDataSet) mChart.getData().getDataSetByIndex(0);
            barDataSet.setValues(yVals1);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            barDataSet = new BarDataSet(yVals1, "");
            barDataSet.setDrawIcons(false);
            barDataSet.setColors(getColors());
            barDataSet.setStackLabels(new String[]{"Late", "On Time"});

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(barDataSet);

            // value inside bar
            BarData barData = new BarData(dataSets);
            barData.setValueTextColor(Color.WHITE);
            barData.setValueFormatter(new DecimalRemover());
            barData.setValueTextSize(11);

            mChart.setData(barData);
        }

        mChart.setFitBars(true);
        mChart.invalidate();
    }

    private void showDateDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ActionThemeDialog);
        LayoutInflater inflater = getLayoutInflater();

        @SuppressLint("InflateParams") final View dialogView = inflater.inflate(R.layout.dialog_year_picker, null);
        final NumberPicker yearPicker = dialogView.findViewById(R.id.picker_year);
        final RelativeLayout relativeCancel = dialogView.findViewById(R.id.relative_cancel);
        final RelativeLayout relativeConfirm = dialogView.findViewById(R.id.relative_confirm);

        Typeface type = Typeface.createFromAsset(getAssets(), "fonts/Ubuntu-B.ttf");
        yearPicker.setTypeface(type);

        Calendar cal = Calendar.getInstance();
        int yearMin = 1994;
        int yearNow = cal.get(Calendar.YEAR);

        yearPicker.setMinValue(yearMin);
        yearPicker.setMaxValue(MAX_YEAR);
        yearPicker.setValue(yearNow);

        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        relativeConfirm.setOnClickListener(view -> setYear(yearPicker, alertDialog));

        relativeCancel.setOnClickListener(view -> alertDialog.cancel());
    }

    private void setYear(NumberPicker yearPicker, AlertDialog alertDialog) {
        String mYear = String.valueOf(yearPicker.getValue());
        tv_year.setText(mYear);
        alertDialog.dismiss();

        mChart.clear();
        pModels.clear();

        retrofitChart();
        mChart.animateY(3000);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        BarEntry entry = (BarEntry) e;

        if (entry.getYVals() != null)
            Log.i("VAL SELECTED", "Value: " + entry.getYVals()[h.getStackIndex()]);
        else
            Log.i("VAL SELECTED", "Value: " + entry.getY());
    }

    @Override
    public void onNothingSelected() {

    }

    private int[] getColors() {

        int stacksize = 2;

        // have as many colors as stack-values per entry
        int[] colors = new int[stacksize];
        colors[0] = getResources().getColor(R.color.red);
        colors[1] = getResources().getColor(R.color.green_grab);

        return colors;
    }
}
