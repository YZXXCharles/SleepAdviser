package com.santiagoapps.sleepadviser.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.*;

import com.santiagoapps.sleepadviser.R;
import com.santiagoapps.sleepadviser.data.model.Session;
import com.santiagoapps.sleepadviser.data.repo.SessionRepo;
import com.santiagoapps.sleepadviser.helpers.DBHelper;
import com.santiagoapps.sleepadviser.helpers.DateHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;


public class CreateRecord extends AppCompatActivity {

    private final static String TAG = "Dormie (" + CreateRecord.class.getSimpleName() + ") ";

    private Toolbar toolbar;

    //components
    private TextView tvSleepTime,tvWakeTime, tvWakeDate ,tvDuration;
    private Button btnCancel, btnAdd;
    private TimePickerDialog mTimePicker;
    private DatePickerDialog mDatePicker;
    private CardView card1, card2, card3;

    private Session session;
    private SessionRepo sessionRepo;
    private String sleep_time;
    private String wake_time;

    SimpleDateFormat sdf = Session.SLEEP_DATE_FORMAT;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_record);
        setToolbar();

        session = new Session();
        sessionRepo = new SessionRepo();

        //textView for sleeping time
        tvSleepTime = (TextView)findViewById(R.id.tvSleepTime);
        tvSleepTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSleepingTime();
            }
        });


        //textView for waking time
        tvWakeTime = (TextView)findViewById(R.id.tvWakeupTime);
        tvWakeTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setWakeUpTime();
            }
        });


        //textView for sleep session date
        tvWakeDate = (TextView)findViewById(R.id.tvWakeDate);
        Date now = new Date();
        tvWakeDate.setText(DateHelper.getMonthDay(now));
        tvWakeDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDate();
            }
        });


        tvDuration = (TextView) findViewById(R.id.tvDuration);

        //Buttons
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addOnClick();
            }
        });

        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sessionRepo.resetSessionTable();
                backToDashboard();
            }
        });

        setSleepingTime();
        setSleepQuality();

    }

    private void setSleepQuality(){
        card1 = (CardView)findViewById(R.id.card1);
        card1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                card2.setCardBackgroundColor(getResources().getColor(R.color.app_color));
                card3.setCardBackgroundColor(getResources().getColor(R.color.app_color));
                card1.setCardBackgroundColor(getResources().getColor(R.color.blue_dark1));

            }
        });

        card2 = (CardView)findViewById(R.id.card2);
        card2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                card2.setCardBackgroundColor(getResources().getColor(R.color.blue_dark1));
                card1.setCardBackgroundColor(getResources().getColor(R.color.app_color));
                card3.setCardBackgroundColor(getResources().getColor(R.color.app_color));

            }
        });

        card3 = (CardView)findViewById(R.id.card3);
        card3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                card3.setCardBackgroundColor(getResources().getColor(R.color.blue_dark1));
                card2.setCardBackgroundColor(getResources().getColor(R.color.app_color));
                card1.setCardBackgroundColor(getResources().getColor(R.color.app_color));

            }
        });
    }

    private void setDate(){
        Calendar mCurrent = Calendar.getInstance();
        int year = mCurrent.get(Calendar.YEAR);
        int month = mCurrent.get(Calendar.MONTH);
        int day = mCurrent.get(Calendar.DAY_OF_MONTH);

        mDatePicker = new DatePickerDialog(CreateRecord.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                String date1 = year + "-" + (month+1) + "-" + day + " " + sleep_time;
                String date2 = year + "-" + (month+1) + "-" + day + " " + wake_time;
                Log.d(TAG, "Sleep time: " + date1 + "\nWake up time: " + date2);

                Calendar temp = Calendar.getInstance();
                temp.setTime(DateHelper.stringToDate(date1));

                // If sleeping time is PM = treat
                // it as the day before waking time
                if((temp.get(Calendar.AM_PM)) == Calendar.PM){
                    temp.add(Calendar.DAY_OF_YEAR, -1);
                    date1 = year + "-" + (month+1) + "-" + (day-1) + " " + sleep_time;
                }


                session.setSleep_date(DateHelper.stringToDate(date1));
                session.setWake_date(DateHelper.stringToDate(date2));
                session.setSleep_duration(session.getSleep_duration());


                tvDuration.setText(session.getSleep_duration());
                tvWakeDate.setText(DateHelper.getMonthDay(session.getWake_date()));


            }
        }, year, month, day);

        mDatePicker.setTitle("Select sleep date");
        mDatePicker.show();
    }


    private void setSleepingTime(){
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = 0;
        mTimePicker = new TimePickerDialog(CreateRecord.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                String AM_PM;
                String minute;
                int hour = selectedHour;

                if(selectedMinute < 10){
                    minute = "0" + selectedMinute;
                } else {
                    minute = "" + selectedMinute;
                }

                if(selectedHour < 12){
                    AM_PM = "AM";
                } else{
                    hour = selectedHour - 12;
                    AM_PM = "PM";
                }

                tvSleepTime.setText( hour + ":" + minute + " " + AM_PM );
                sleep_time = hour + ":" + minute + " " + AM_PM;
                setWakeUpTime();

            }
        }, hour, minute, false);
        mTimePicker.setTitle("Select Sleeping Time");
        mTimePicker.show();
}

    private void setWakeUpTime(){
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);

        mTimePicker = new TimePickerDialog(CreateRecord.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                String AM_PM;
                String minute;
                int hour = selectedHour;

                if(selectedMinute < 10){
                    minute = "0" + selectedMinute;
                } else {
                    minute = "" + selectedMinute;
                }

                if(selectedHour < 12){
                    AM_PM = "AM";
                } else{
                    hour = selectedHour - 12;
                    AM_PM = "PM";
                }

                tvWakeTime.setText( hour + ":" + minute + " " + AM_PM );
                wake_time = hour + ":" + minute + " " + AM_PM;
                setDate();

            }
        }, hour, minute, false);

        mTimePicker.setTitle("Select Wake Time");
        mTimePicker.show();
    }


    private void setToolbar(){
        toolbar = (Toolbar)findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Create sleep record");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToDashboard();
            }
        });
    }

    private void backToDashboard(){
        startActivity(new Intent(CreateRecord.this, NavigationMain.class));
        finish();
    }

    private void addOnClick(){
        Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();
        Log.d(TAG, DateHelper.dateToString(session.getSleep_date()));

        sessionRepo.insertSession(session);
    }
}