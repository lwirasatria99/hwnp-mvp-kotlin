package com.elabram.lm.wmsmobile.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.elabram.lm.wmsmobile.R;
import com.elabram.lm.wmsmobile.model.Monthly;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by lalu.mahendra on 16-May-18.
 */

public class MonthlyAdapter extends BaseAdapter {

    private static final String TAG = MonthlyAdapter.class.getSimpleName();
    private Activity mActivity;
    private List<Monthly> monthlies;
    private LayoutInflater inflater;

    static class ViewHolder {
        @BindView(R.id.date)
        TextView date;

        @BindView(R.id.day)
        TextView day;

        @BindView(R.id.first_clock)
        TextView first_clock;

        @BindView(R.id.first_location)
        TextView first_location;

        @BindView(R.id.last_clock)
        TextView last_clock;

        @BindView(R.id.last_location)
        TextView last_location;

        @BindView(R.id.total_hours)
        TextView total_hours;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

    }

    public MonthlyAdapter(Activity mActivity, List<Monthly> monthlies) {
        this.mActivity = mActivity;
        this.monthlies = monthlies;
    }

    @Override
    public int getCount() {
        return monthlies.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if (inflater == null)
            inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            assert inflater != null;
            view = inflater.inflate(R.layout.row_monthlyrecord, viewGroup, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        Monthly monthly = monthlies.get(i);

        /*
          Parsing
          Format
          setText
         */
        String source_date = monthly.getAttDate();
        //Log.e(TAG, "getView: sourceDate "+source_date);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat read = new SimpleDateFormat("yyyy-MM-dd");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat writeTo = new SimpleDateFormat("dd");
        Date date = null;
        try {
            date = read.parse(source_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formatted_date = writeTo.format(date);
        if (!formatted_date.isEmpty()) {
            //Log.e(TAG, "getView: formattedDate "+formatted_date );
            holder.date.setText(formatted_date);
        }
        // End

        // Substring day 3 letter
        String source_day = monthly.getAttDay();
        String daySubs = source_day.substring(0, 3);
        holder.day.setText(daySubs);
        // end

        @SuppressLint("SimpleDateFormat") SimpleDateFormat readClock = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat writeClock = new SimpleDateFormat("HH:mm");
        String sourceFirstClock = monthly.getTimeIn();
        if (!sourceFirstClock.isEmpty()) {
            Date dateClock = null;
            try {
                dateClock = readClock.parse(sourceFirstClock);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String formattedFirstClock = writeClock.format(dateClock);
            holder.first_clock.setText(formattedFirstClock);
        } else {
            holder.first_clock.setText("-");
        }

        String sourceLastClock = monthly.getTimeOut();
        if (!sourceLastClock.isEmpty()) {
            Date dateClock = null;
            try {
                dateClock = readClock.parse(sourceLastClock);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String formattedLastClock = writeClock.format(dateClock);
            holder.last_clock.setText(formattedLastClock);
        } else {
            holder.last_clock.setText("-");
        }

        if (!monthly.getLocationFirst().isEmpty()) {
            holder.first_location.setText(monthly.getLocationFirst());
        } else {
            holder.first_location.setText("-");
        }

        // Last Location
        if (!monthly.getLocationLast().isEmpty()) {
            holder.last_location.setText(monthly.getLocationLast());
        } else {
            holder.last_location.setText("-");
        }

        // TotalHour
        String sourceHour = monthly.getTotalWorkingHour();
        if (!sourceLastClock.isEmpty()) {
            String replaceTime = sourceHour.replace(" hour ", ".")
                    .replace(" minute", "");
            holder.total_hours.setText(replaceTime);
        } else {
            holder.total_hours.setText("-");
        }

        return view;
    }
}
