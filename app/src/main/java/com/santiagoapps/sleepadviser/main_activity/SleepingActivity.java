package com.santiagoapps.sleepadviser.main_activity;

import android.app.Dialog;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import com.santiagoapps.sleepadviser.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SleepingActivity extends AppCompatActivity {
    Dialog myDialog;
    TextView txtTime;
    TextView txtAm;
    public static Camera cam;
    private static final String TAG = "SleepAdviser";
    Boolean isOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleeping);
        myDialog = new Dialog(SleepingActivity.this);

        txtTime = (TextView)findViewById(R.id.txtTime);
        txtAm = (TextView)findViewById(R.id.txtAm);



        setUpAlarm();

    }

    public void setUpAlarm(){
        txtTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView txtClose;
                CardView btnEnter;
                final TimePicker timePicker;
                myDialog.setContentView(R.layout.dialog_alarm);

                timePicker = myDialog.findViewById(R.id.timePicker);


                btnEnter = myDialog.findViewById(R.id.btnEnter);
                btnEnter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Get time
                        String AM_PM;
                        int hour = timePicker.getHour();
                        int min = timePicker.getMinute();

                        String minute;
                        if(timePicker.getMinute() < 10){
                            minute = "0" + timePicker.getMinute();
                        } else {
                            minute = "" + timePicker.getMinute();
                        }

                        if(hour < 12){
                            AM_PM = "AM";
                        } else{
                            hour = timePicker.getHour() - 12;
                            AM_PM = "PM";
                        }

                        txtTime.setText(hour + ":" + minute);
                        txtAm.setText(AM_PM);






                        myDialog.dismiss();
                    }
                });

                txtClose = myDialog.findViewById(R.id.txtClose);
                txtClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        myDialog.dismiss();
                    }
                });

                myDialog.show();
            }
        });
    }


    public void wakeUp(View v){
        //TODO: for time record (Sleep time)

        //for dialogBox
        TextView txtClose;
        final TextView flashDesc;
        CardView btnWake;
        CardView btnFlash;
        myDialog.setContentView(R.layout.dialog_unlocked);

        txtClose = (TextView) myDialog.findViewById(R.id.txtClose);
        flashDesc = (TextView) myDialog.findViewById(R.id.flashDesc);
        btnWake = (CardView) myDialog.findViewById(R.id.btnWake);
        btnFlash = (CardView) myDialog.findViewById(R.id.btnFlash);

        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if flash is open
                if(isOpen){
                    cam.stopPreview();
                    cam.release();
                    cam = null;

                    flashDesc.setText("OPEN FLASHLIGHT");
                    isOpen = false;

                    Log.d(TAG, "FLASHLIGHT IS OFF!");
                }
                myDialog.dismiss();
            }
        });

        btnFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!isOpen){
                    cam = Camera.open();
                    Camera.Parameters p = cam.getParameters();
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    cam.setParameters(p);
                    cam.startPreview();

                    flashDesc.setText("CLOSE FLASHLIGHT");
                    isOpen = true;

                    Log.d(TAG, "FLASHLIGHT IS ON!");
                }else  {
                    cam.stopPreview();
                    cam.release();
                    cam = null;

                    flashDesc.setText("OPEN FLASHLIGHT");
                    isOpen = false;

                    Log.d(TAG, "FLASHLIGHT IS OFF!");
                }

            }
        });

        myDialog.show();


    }
}
