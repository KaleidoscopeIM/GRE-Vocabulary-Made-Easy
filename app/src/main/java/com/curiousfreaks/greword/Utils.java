package com.curiousfreaks.greword;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * Created by gasaini on 3/23/2018.
 */

public class Utils {

    private Context context;
    public TextToSpeech textToSpeech;
    public static wordDefinition word_of_the_day =null;


    public Utils(Context con)
    {
        context=con;
    }

    public void generate_word_of_the_day()
    {
        SharedPreferences sharedPreferences=context.getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        DatabaseHandler databaseHandler = new DatabaseHandler(context);

        int word_count = databaseHandler.getWordsCount();
        Log.d(MainActivity.TAG,"word count"+word_count);

        int word_id = generate_random(1,word_count);

        word_of_the_day = databaseHandler.getAWord(word_id,databaseHandler.getReadableDatabase());
        editor.putString("WORD_OF_DAY",word_of_the_day.getWord());
        editor.apply();
        editor.commit();
    }

    public int generate_random(int start, int end)
    {
        int num = ((new Random()).nextInt((end-start)+1))+start;
        Log.d(MainActivity.TAG,"A random number generated ..:"+num);
        return num;
    }

    public void set_notification_alarm(int hour, int minute, boolean showToast)
    {
        Intent alarm_intent = new Intent(context,notificationReceiver.class);
        alarm_intent.putExtra("RECEIVER_TYPE","ALARM_RECEIVER");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,1,alarm_intent,PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent); // cancel if alarm already exists
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,hour);
        calendar.set(Calendar.MINUTE,minute);
        if((new Date().getTime())>calendar.getTimeInMillis())
            calendar.add(Calendar.DATE,1);
        Log.d(MainActivity.TAG,"next alarm set to: "+calendar.getTimeInMillis()+"   ::");
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY,pendingIntent);
        //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),5000,pendingIntent);
        if(showToast)
            Toast.makeText(context,"Notification set at "+hour+":"+minute,Toast.LENGTH_SHORT).show();
    }
    public void cancel_alarm()
    {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent alarm_intent = new Intent(context,notificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,1,alarm_intent,0);
        alarmManager.cancel(pendingIntent);

    }
    public void initTextToSpeech()
    {
        textToSpeech=new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i!=TextToSpeech.ERROR)
                {
                    textToSpeech.setLanguage(Locale.ENGLISH);

                }
            }
        });
    }
    public void speekThis(String speech)
    {
        textToSpeech.setSpeechRate(0.7f);
        textToSpeech.speak(speech,TextToSpeech.QUEUE_FLUSH,null,null);
    }
    public SharedPreferences initAppPrefrences()
    {
        SharedPreferences sharedPreferences;
        sharedPreferences=context.getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt("ID",2000);
        editor.apply();
        return sharedPreferences;
    }

}
