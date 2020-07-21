package com.curiousfreaks.greword;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TimePicker;
import android.widget.Toast;

public class manageNotifications extends AppCompatActivity {
    Button test_notification_button;
    TimePicker timePicker;
    Button set_time_button;
    CheckBox notification_check_box;
    int hour,minute;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor prefEditor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_notifications);

        test_notification_button = findViewById(R.id.trigger_notification);
        timePicker = findViewById(R.id.timePicker1);
        set_time_button = findViewById(R.id.set_time);
        notification_check_box = findViewById(R.id.notification_check_box);

        sharedPreferences=getApplicationContext().getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE);
        prefEditor=sharedPreferences.edit();

        notification_check_box.setChecked(sharedPreferences.getBoolean("NOTIFICATION_CHECK",false));


        if(sharedPreferences.getInt("STORED_HOUR",-1)==-1)
            hour = 9;
        else
            hour = sharedPreferences.getInt("STORED_HOUR",-1);

        if(sharedPreferences.getInt("STORED_MINUTE",-1)==-1)
            minute = 0;
        else
            minute= sharedPreferences.getInt("STORED_MINUTE",-1);

            /*timePicker.setHour(hour);
            timePicker.setMinute(minute);*/
            setTimePickerHour(timePicker,hour);
            setTimePickerMinute(timePicker,minute);

        if(!(sharedPreferences.getBoolean("NOTIFICATION_CHECK",false)))
            notification_check_box.setChecked(false);
        else
            notification_check_box.setChecked(true);

        notification_check_box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Utils utils = new Utils(getApplicationContext());
                if(b)
                {
                    Toast.makeText(getApplicationContext(),"Notifications enabled",Toast.LENGTH_SHORT).show();
                    utils.set_notification_alarm(getTimePickerHour(timePicker),getTimePickerMinutes(timePicker),true);
                    prefEditor.putBoolean("NOTIFICATION_CHECK",b);

                }else
                {
                    Toast.makeText(getApplicationContext(),"Notifications canceled",Toast.LENGTH_SHORT).show();
                    utils.cancel_alarm();
                    prefEditor.putBoolean("NOTIFICATION_CHECK",b);
                }
                prefEditor.apply();
                prefEditor.commit();
            }
        });

        set_time_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefEditor.putInt("STORED_HOUR",getTimePickerHour(timePicker));
                prefEditor.putInt("STORED_MINUTE",getTimePickerMinutes(timePicker));
                prefEditor.apply();
                prefEditor.commit();
                /*Toast.makeText(getApplicationContext(),"Notification timing set to: "+sharedPreferences.getInt("STORED_HOUR",-1)+"."
                        +sharedPreferences.getInt("STORED_MINUTE",-1),Toast.LENGTH_SHORT).show();*/

                Utils utils = new Utils(getApplicationContext());
                utils.set_notification_alarm(getTimePickerHour(timePicker),getTimePickerMinutes(timePicker),true);

            }
        });

        test_notification_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //build_notification();
            }
        });
    }

    public void build_notification() {

        Utils utils = new Utils(getApplicationContext());
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE);
        if (Utils.word_of_the_day == null) {
            utils.generate_word_of_the_day();
        }

        if ((sharedPreferences.getString("WORD_OF_DAY", "").equals(Utils.word_of_the_day.getWord())) || (sharedPreferences.getString("WORD_OF_DAY", "").equals(""))) {
            utils.generate_word_of_the_day();
        }

        wordDefinition word_of_the_day = Utils.word_of_the_day;
        if(word_of_the_day!=null)
        {
            Log.d(MainActivity.TAG,"word of the day ...:"+word_of_the_day.getWord()+" notification id:"+word_of_the_day.getId());
        }

        int NOTIFICATION_ID = (int)word_of_the_day.getId();
        String channel_id = "CF_NOTIFICATION_CHANNEL";
        CharSequence channel_name = "CF NOTIFICATION";

        Intent receiver_intent = new Intent(getApplicationContext(),notificationReceiver.class);
        receiver_intent.setAction("GOT_IT");
        receiver_intent.putExtra("NOTIFICATION_ID",NOTIFICATION_ID);
        receiver_intent.putExtra("RECEIVER_TYPE","NOTIFICATION_ACTION");
        receiver_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingReceiverIntent = PendingIntent.getBroadcast(getApplicationContext(),0,receiver_intent,PendingIntent.FLAG_CANCEL_CURRENT);

        Intent open_intent = new Intent(getApplicationContext(),WordDetails.class);
        open_intent.putExtra("NOTIFICATION_ID",NOTIFICATION_ID);
        open_intent.putExtra("ACTIVITY_TYPE","NOTIFICATION_OPEN");
        open_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent open_pending_intent = PendingIntent.getActivity(getApplicationContext(),0,open_intent,PendingIntent.FLAG_CANCEL_CURRENT);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),R.mipmap.logo_round);
        NotificationCompat.Builder notify_builder;
        //NotificationManagerCompat notificationManagerCompat =  NotificationManagerCompat.from(this);;
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(channel_id,channel_name,NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notify_builder = new NotificationCompat.Builder(this,channel_id)
                .setSmallIcon(R.mipmap.coffee)
                .setLargeIcon(icon)
                .setContentTitle("Word of the day: "+word_of_the_day.getWord())
                .setContentText(word_of_the_day.getMeaning())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(open_pending_intent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(word_of_the_day.getMeaning()+" :\""+word_of_the_day.getSentence()+"\""))
                .addAction(R.mipmap.check,"got it?",pendingReceiverIntent)
                .setAutoCancel(true);

        //notificationManagerCompat.notify(NOTIFICATION_ID,notify_builder.build());
        notificationManager.notify(NOTIFICATION_ID,notify_builder.build());
        Log.d(MainActivity.TAG,"notification triggered for id :"+NOTIFICATION_ID);
    }

    public int getTimePickerHour(TimePicker tp)
    {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
        {
            return tp.getHour();
        }else
        {
            return tp.getCurrentHour();
        }
    }

    public int getTimePickerMinutes(TimePicker tp)
    {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
        {
            return tp.getMinute();
        }else
        {
            return tp.getCurrentMinute();
        }
    }
    public void setTimePickerHour(TimePicker tp, int hour)
    {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
        {
            tp.setHour(hour);
        }else
        {
            tp.setCurrentHour(hour);
        }
    }
    public void setTimePickerMinute(TimePicker tp, int mins)
    {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
        {
            tp.setMinute(mins);
        }else
        {
            tp.setCurrentMinute(mins);
        }
    }
}
