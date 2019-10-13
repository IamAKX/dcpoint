package com.akashapplications.dcpoint;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.akashapplications.dcpoint.utils.API;
import com.akashapplications.dcpoint.utils.LocalPreference;
import com.akashapplications.dcpoint.utils.RequestQueueSingleton;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.daasuu.bl.BubbleLayout;
import com.marozzi.roundbutton.RoundButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import devs.mulham.horizontalcalendar.utils.Utils;

public class Home extends AppCompatActivity {


    TextView currentDayOfWeek, currentDate, startTime, endTime, breakStartTime, breakEndTime, worktimeLog, totalKms;
    Spinner welleSpinner;
    ImageView startTimeCheck, endTimeCheck, breakStartTimeCheck, breakEndTimeCheck;
    BubbleLayout startTimeBubble, endTimeBubble, breakStartTimeBubble, breakEndTimeBubble;
    EditText kfzNumber, tourNumber, scannerNO, startKMs, endKMs;
    RoundButton updateBtn;
    LocalPreference localPreference;
    String currentLanguage = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        localPreference = new LocalPreference(getBaseContext());
        super.onCreate(savedInstanceState);
        currentLanguage = localPreference.getLanguage();
        setLocale(currentLanguage);
        setContentView(R.layout.activity_home);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        initComponents();
        /* starts before 1 month from now */
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -3);

        /* ends after 1 month from now */
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 3);


        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(this, R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(5)
                .configure()
                .showTopText(false)
                .sizeTopText(14)
                .sizeMiddleText(18)
                .sizeBottomText(14)
                .end()
                .build();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                String selectedDateStr = DateFormat.format("yyyy-MM-dd", date).toString();
                Log.i("checking", String.valueOf(Utils.isSameDate(date, Calendar.getInstance())));
                worktimeLog.setText("");
                kfzNumber.setText("");
                tourNumber.setText("");

                if (!Utils.isSameDate(date, Calendar.getInstance())) {
                    new GetRecord(selectedDateStr).execute();
                    updateBtn.setVisibility(View.GONE);
                    worktimeLog.setVisibility(View.VISIBLE);
                    Toast.makeText(getBaseContext(), "Loading data, please wait...", Toast.LENGTH_SHORT).show();
                } else {
                    updateBtn.setVisibility(View.VISIBLE);
                    worktimeLog.setVisibility(View.GONE);
                }
            }

        });


        Date today = new Date();
        currentDayOfWeek.setText(DateFormat.format("EEEE", today).toString());
        currentDate.setText(DateFormat.format("dd MMMM, yyyy", today).toString());

        initWelleSpinner();

        initBubbleClickListner();

        scannerNO.setText(localPreference.getScanner());
        kfzNumber.setText(localPreference.getKFZ());
        tourNumber.setText(localPreference.getTour());

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateInput()) {
                    updateBtn.startAnimation();
                    String totalHours = "";
                    String totalWorkingHours = "";
                    String totalBreakHours = "";

                    localPreference.setKFZ(kfzNumber.getText().toString());
                    localPreference.setTour(tourNumber.getText().toString());
                    localPreference.setScanner(scannerNO.getText().toString());

//                    if (breakStartTime.getText().toString().equals("__:__") && breakEndTime.getText().toString().equals("__:__")) {
                        long diff = calculateTimeDifference(startTime.getText().toString(), "HH:mm", endTime.getText().toString(), "HH:mm");
                        long totalTime = diff;
                        diff = diff / (1000 * 60 * 60);
                        int breakHours = 0;

                        if(diff <= 6)
                        {
                            breakStartTime.setText("__:__");
                            breakEndTime.setText("__:__");
                            breakHours = 0;
                        }
                        else
                            if(diff > 6 && diff <=8)
                            {
                                breakStartTime.setText("14:00");
                                breakEndTime.setText("14:30");
                                breakHours = 30;
                            }
                            else
                                if(diff > 8)
                                {
                                    breakStartTime.setText("14:00");
                                    breakEndTime.setText("15:00");
                                    breakHours = 60;
                                }

                        totalHours = getFormatedTime(totalTime);
                        totalWorkingHours = getFormatedTime(totalTime - (breakHours * 60 * 1000));
                        totalBreakHours = getFormatedTime(breakHours * 60 * 1000);

                        Log.e("checking", "total : " + totalHours);
                        Log.e("checking", "working : " + totalWorkingHours);
                        Log.e("checking", "break : " + totalBreakHours);

                        JSONObject req = new JSONObject();
                        try {
                            req.put("name", localPreference.getName());
                            req.put("email", localPreference.getEmail());
                            req.put("scanner_number", scannerNO.getText().toString());
                            req.put("kfz_number", kfzNumber.getText().toString());
                            req.put("tour_number", tourNumber.getText().toString());
                            req.put("start_time", startTime.getText().toString());
                            req.put("end_time", endTime.getText().toString());
                            req.put("break_start_time", breakStartTime.getText().toString());
                            req.put("break_end_time", breakEndTime.getText().toString());
                            req.put("break_hours", totalBreakHours);
                            req.put("working_hours", totalWorkingHours);
                            req.put("total_hours", totalHours);
                            req.put("welle", "345".charAt(localPreference.getWelle()) + "");
                            req.put("recordDate", DateFormat.format("yyyy-MM-dd", new Date()).toString());
                            req.put("start_km", Double.parseDouble(startKMs.getText().toString()));
                            req.put("end_km", Double.parseDouble(endKMs.getText().toString()));
                            req.put("total_km", (Double.parseDouble(endKMs.getText().toString()) - Double.parseDouble(startKMs.getText().toString())));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        showAlertToBreakTime(breakHours, req);
//                    }
//                    else {
//                        long total = calculateTimeDifference(startTime.getText().toString(), "HH:mm", endTime.getText().toString(), "HH:mm");
//                        long breakTime = calculateTimeDifference(breakStartTime.getText().toString(), "HH:mm", breakEndTime.getText().toString(), "HH:mm");
//                        long working = total - breakTime;
//
//                        totalHours = getFormatedTime(total);
//                        totalWorkingHours = getFormatedTime(working);
//                        totalBreakHours = getFormatedTime(breakTime);
//
//                        Log.e("checking", "total : " + totalHours);
//                        Log.e("checking", "working : " + totalWorkingHours);
//                        Log.e("checking", "break : " + totalBreakHours);
//
//                        JSONObject req = new JSONObject();
//                        try {
//                            req.put("name", localPreference.getName());
//                            req.put("email", localPreference.getEmail());
//                            req.put("kfz_number", kfzNumber.getText().toString());
//                            req.put("scanner_number", scannerNO.getText().toString());
//                            req.put("tour_number", tourNumber.getText().toString());
//                            req.put("start_time", startTime.getText().toString());
//                            req.put("end_time", endTime.getText().toString());
//                            req.put("break_start_time", breakStartTime.getText().toString());
//                            req.put("break_end_time", breakEndTime.getText().toString());
//                            req.put("break_hours", totalBreakHours);
//                            req.put("working_hours", totalWorkingHours);
//                            req.put("total_hours", totalHours);
//                            req.put("welle", "345".charAt(localPreference.getWelle()) + "");
//                            req.put("recordDate", DateFormat.format("yyyy-MM-dd", new Date()).toString());
//                            req.put("start_km", Double.parseDouble(startKMs.getText().toString()));
//                            req.put("end_km", Double.parseDouble(endKMs.getText().toString()));
//                            req.put("total_km", (Double.parseDouble(endKMs.getText().toString()) - Double.parseDouble(startKMs.getText().toString())));
//
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//                        new AddRecord(req).execute();
//                    }


                }
            }
        });


        startKMs.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(startKMs.getText().toString().length() > 0 && endKMs.getText().toString().length() > 0)
                {
                    calculateTotalDistance();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        endKMs.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(startKMs.getText().toString().length() > 0 && endKMs.getText().toString().length() > 0)
                {
                    calculateTotalDistance();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                localPreference.logOut();
                startActivity(new Intent(getBaseContext(), Login.class));
                finish();
            }
        });



        findViewById(R.id.translate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentLanguage = (currentLanguage.equals("de") ? "en" : "de");
                localPreference.setLanguage(currentLanguage);
                setLocale(currentLanguage);
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
    }

    private void calculateTotalDistance() {
        double s = Double.parseDouble(startKMs.getText().toString());
        double e = Double.parseDouble(endKMs.getText().toString());
        double t = e - s;
        totalKms.setText(String.format("%.2f", t));
    }

    private String getFormatedTime(long diff) {
        long totalseconds = diff / 1000;

        long hours = totalseconds / 3600;
        totalseconds %= 3600;
        long minutes = totalseconds / 60;
        long sec = totalseconds % 60;
        return String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", sec);
    }

    private void showAlertToBreakTime(int breakHours, final JSONObject req) {
        if(breakHours == 0)
        {
            new AddRecord(req).execute();
            return;
        }
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);

        // Setting Dialog Title
        alertDialog.setTitle(getResources().getString(R.string.alertTitle));

        long diff = calculateTimeDifference(startTime.getText().toString(), "HH:mm", endTime.getText().toString(), "HH:mm");

        // Setting Dialog Message
        alertDialog.setMessage(getResources().getString(R.string.alert1) + breakHours + getResources().getString(R.string.alert2));


        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                // Write your code here to invoke YES event
                new AddRecord(req).execute();
                dialog.dismiss();
            }
        });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to invoke NO event
                updateBtn.revertAnimation();
                dialog.dismiss();

            }
        });

        alertDialog.setCancelable(false);

        alertDialog.show();
    }

    private boolean validateInput() {
        if (kfzNumber.getText().toString().length() == 0) {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toast4), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (tourNumber.getText().toString().length() == 0) {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toast5), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (scannerNO.getText().toString().length() == 0) {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toast6), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (startKMs.getText().toString().length() == 0) {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toast10), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (endKMs.getText().toString().length() == 0) {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toast11), Toast.LENGTH_SHORT).show();
            return false;
        }

        if(Double.parseDouble(endKMs.getText().toString()) <= Double.parseDouble(startKMs.getText().toString()))
        {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toast12), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (breakStartTime.getText().toString().equals("__:__") && !breakEndTime.getText().toString().equals("__:__")) {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toast7), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!breakStartTime.getText().toString().equals("__:__") && breakEndTime.getText().toString().equals("__:__")) {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toast8), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (endTime.getText().toString().equals("__:__")) {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toast9), Toast.LENGTH_SHORT).show();
            return false;
        }

        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        String time1 = startTime.getText().toString();
        String time2 = endTime.getText().toString();
        try {
            Date date1 = format.parse(time1);
            Date date2 = format.parse(time2);
            long difference = date2.getTime() - date1.getTime();
            if(difference <= 0 )
            {
                Toast.makeText(getBaseContext(), getResources().getString(R.string.toast13), Toast.LENGTH_SHORT).show();
                return false;
            }

        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toast14), Toast.LENGTH_SHORT).show();
            return false;
        }



        return true;
    }

    private void initBubbleClickListner() {
        startTimeBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                int mHour = c.get(Calendar.HOUR_OF_DAY);
                int mMinute = c.get(Calendar.MINUTE);
                TimePickerDialog dialog =
                        new TimePickerDialog(Home.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                startTime.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
                                startTimeCheck.setBackgroundResource(R.drawable.circle_done);
                            }
                        }, mHour, mMinute, true);

                dialog.show();
            }
        });

        breakStartTimeBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                int mHour = c.get(Calendar.HOUR_OF_DAY);
                int mMinute = c.get(Calendar.MINUTE);
                TimePickerDialog dialog =
                        new TimePickerDialog(Home.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                breakStartTime.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
                                breakStartTimeCheck.setBackgroundResource(R.drawable.circle_done);
                            }
                        }, mHour, mMinute, true);
                dialog.show();
            }
        });

        breakEndTimeBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                int mHour = c.get(Calendar.HOUR_OF_DAY);
                int mMinute = c.get(Calendar.MINUTE);
                TimePickerDialog dialog =
                        new TimePickerDialog(Home.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                breakEndTime.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
                                breakEndTimeCheck.setBackgroundResource(R.drawable.circle_done);
                            }
                        }, mHour, mMinute, true);
                dialog.show();
            }
        });

        endTimeBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                int mHour = c.get(Calendar.HOUR_OF_DAY);
                int mMinute = c.get(Calendar.MINUTE);
                TimePickerDialog dialog =
                        new TimePickerDialog(Home.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                if (hourOfDay > 22 || (hourOfDay >= 22 && minute >= 30)) {
                                    hourOfDay = 22;
                                    minute = 30;
                                }
                                endTime.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
                                endTimeCheck.setBackgroundResource(R.drawable.circle_done);
                            }
                        }, mHour, mMinute, true);
                dialog.show();
            }
        });
    }

    private void initWelleSpinner() {
        final String[] welleValue = {"3", "4", "5"};
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, welleValue);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        welleSpinner.setAdapter(dataAdapter);

        welleSpinner.setSelection(localPreference.getWelle());
        welleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                localPreference.setWelle(i);
                setStartTimeAutomaticaly(welleValue[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void setStartTimeAutomaticaly(String n) {
        String sTime = "";
        if (!currentDayOfWeek.getText().toString().equalsIgnoreCase("Saturday")) {
            switch (n) {
                case "3":
                    sTime = "09:40";
                    break;
                case "4":
                    sTime = "10:00";
                    break;
                case "5":
                    sTime = "10:20";
                    break;
            }
        } else {
            switch (n) {
                case "3":
                    sTime = "06:40";
                    break;
                case "4":
                    sTime = "07:00";
                    break;
                case "5":
                    sTime = "07:20";
                    break;
            }
        }
        startTime.setText(sTime);
        startTimeCheck.setBackgroundResource(R.drawable.circle_done);
    }

    private void initComponents() {
        currentDayOfWeek = findViewById(R.id.dayOfWeek);
        currentDate = findViewById(R.id.currentDate);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        breakStartTime = findViewById(R.id.startBreakTime);
        breakEndTime = findViewById(R.id.endBreakTime);

        welleSpinner = findViewById(R.id.welle_spinner);

        startTimeCheck = findViewById(R.id.startTimeCheck);
        endTimeCheck = findViewById(R.id.endTimeCheck);
        breakStartTimeCheck = findViewById(R.id.startBreakTimeCheck);
        breakEndTimeCheck = findViewById(R.id.endBreakTimeCheck);

        startTimeBubble = findViewById(R.id.startTimeBubble);
        endTimeBubble = findViewById(R.id.endTimeBubble);
        breakStartTimeBubble = findViewById(R.id.startBreakTimeBubble);
        breakEndTimeBubble = findViewById(R.id.endBreakTimeBubble);

        kfzNumber = findViewById(R.id.kfzNumber);
        tourNumber = findViewById(R.id.tourNumber);
        scannerNO = findViewById(R.id.scannernumber);

        updateBtn = findViewById(R.id.updateBtn);

        worktimeLog = findViewById(R.id.worktimeLog);

        totalKms = findViewById(R.id.totalKM);
        startKMs = findViewById(R.id.startKM);
        endKMs = findViewById(R.id.endKM);
    }

    long calculateTimeDifference(String sTime, String sFormat, String eTime, String eFormat) {
        long diff = 0;

        try {
            SimpleDateFormat sf = new SimpleDateFormat(sFormat);
            SimpleDateFormat ef = new SimpleDateFormat(eFormat);
            Date d1 = sf.parse(sTime);
            Date d2 = ef.parse(eTime);

            diff = d2.getTime() - d1.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return diff;
    }

    private class AddRecord extends AsyncTask<Void, Void, Void> {

        JSONObject req;

        public AddRecord(JSONObject req) {
            this.req = req;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, API.ADD_RECORD, req,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Toast.makeText(getBaseContext(), getResources().getString(R.string.msg_update), Toast.LENGTH_LONG).show();

                            updateBtn.revertAnimation();


                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkResponse networkResponse = error.networkResponse;
                    String data = new String(networkResponse.data);

                    Log.e("checking", data);
                    updateBtn.revertAnimation();
                    try {
                        JSONObject response = new JSONObject(data);
                        if (response.has("msg"))
                            Toast.makeText(getBaseContext(), response.getString("msg"), Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getBaseContext(), response.getString("Failed"), Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });

            jsonObjectRequest.setShouldCache(false);
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));
            RequestQueue requestQueue = RequestQueueSingleton.getInstance(getBaseContext())
                    .getRequestQueue();
            requestQueue.getCache().clear();
            requestQueue.add(jsonObjectRequest);
            return null;
        }
    }

    private class GetRecord extends AsyncTask<Void, Void, Void> {

        String selectedDate;

        public GetRecord(String selectedDate) {
            this.selectedDate = selectedDate;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            JSONObject req = new JSONObject();
            try {
                req.put("email", localPreference.getEmail());
                req.put("recordDate", selectedDate);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, API.VIEW_RECORD, req,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                if (response.has("msg"))
                                    Toast.makeText(getBaseContext(), response.getString("msg"), Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(getBaseContext(), response.getString("Success"), Toast.LENGTH_SHORT).show();

                                if (response.has("record")) {
                                    response = response.getJSONObject("record");
                                    if (response.has("kfz_number"))
                                        kfzNumber.setText(response.getString("kfz_number"));

                                    if (response.has("tour_number"))
                                        tourNumber.setText(response.getString("tour_number"));

                                    if (response.has("start_time"))
                                        startTime.setText(response.getString("start_time"));
                                    if (response.has("end_time"))
                                        endTime.setText(response.getString("end_time"));

                                    if (response.has("break_start_time"))
                                        breakStartTime.setText(response.getString("break_start_time"));
                                    if (response.has("break_end_time"))
                                        breakEndTime.setText(response.getString("break_end_time"));

                                    String log = "";
                                    if (response.has("working_hours"))
                                        log += "Working hours - " + getFormatedHours(response.getString("working_hours")) + "\n";

                                    if (response.has("break_hours"))
                                        log += "Break hours - " + getFormatedHours(response.getString("break_hours")) + "\n";

                                    if (response.has("total_hours"))
                                        log += "Total hours - " + getFormatedHours(response.getString("total_hours")) + "\n";

                                    worktimeLog.setText(log);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkResponse networkResponse = error.networkResponse;
                    String data = new String(networkResponse.data);

                    Log.e("checking", data);
                    updateBtn.revertAnimation();
                    try {
                        JSONObject response = new JSONObject(data);
                        if (response.has("msg"))
                            Toast.makeText(getBaseContext(), response.getString("msg"), Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getBaseContext(), response.getString("Failed"), Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });

            jsonObjectRequest.setShouldCache(false);
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));
            RequestQueue requestQueue = RequestQueueSingleton.getInstance(getBaseContext())
                    .getRequestQueue();
            requestQueue.getCache().clear();
            requestQueue.add(jsonObjectRequest);
            return null;
        }
    }

    private String getFormatedHours(String t) {
        t = t.replaceFirst(":", "Hours ");
        t = t.replaceFirst(":", "Minutes ");
        return t + "Seconds";
    }

    public void setLocale(String localeName) {

        Locale myLocale = new Locale(localeName);
        Locale.setDefault(myLocale);

        Configuration configuration = new Configuration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(myLocale);
        }
        else
            configuration.locale = myLocale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());

    }
}
