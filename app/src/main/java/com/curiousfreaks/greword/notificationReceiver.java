package com.curiousfreaks.greword;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

public class notificationReceiver extends BroadcastReceiver {

    String RECEIVER_TYPE;
    int NOTIFICATION_ID;
    DatabaseHandler dbHandler;
    SQLiteDatabase db;
    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getExtras()!=null && intent.getExtras().containsKey("RECEIVER_TYPE")){
            RECEIVER_TYPE=intent.getExtras().getString("RECEIVER_TYPE");
        }else
            return;

        if(RECEIVER_TYPE.equals("NOTIFICATION_ACTION")) {

            NOTIFICATION_ID = intent.getIntExtra("NOTIFICATION_ID", -1);
            if (NOTIFICATION_ID == -1) {
                Toast.makeText(context, "Error in learned words", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d(MainActivity.TAG, ">>>>> Notification broadcast received ...notification id: " + NOTIFICATION_ID);

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            notificationManagerCompat.cancel(NOTIFICATION_ID);

            dbHandler = new DatabaseHandler(context);
            db = dbHandler.getReadableDatabase();
            wordDefinition aWord = dbHandler.getAWord(NOTIFICATION_ID, db);
            Log.d(MainActivity.TAG, ">>>>> WOrd id: " + aWord.getWord());

            dbHandler.updateLearntOrBookmarked(db, aWord, "YES", null);
            Toast.makeText(context, "Congrats!! you have learnt word of the day: " + aWord.getWord(), Toast.LENGTH_SHORT).show();

            db.close();
        }
        if(RECEIVER_TYPE.equals("ALARM_RECEIVER"))
        {
            Utils utils = new Utils(context);
            SharedPreferences sharedPreferences = context.getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE);
            //if (Utils.word_of_the_day == null) {
                utils.generate_word_of_the_day();
            //}
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

            Intent receiver_intent = new Intent(context,notificationReceiver.class);
            receiver_intent.setAction("GOT_IT");
            receiver_intent.putExtra("NOTIFICATION_ID",NOTIFICATION_ID);
            receiver_intent.putExtra("RECEIVER_TYPE","NOTIFICATION_ACTION");
            receiver_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingReceiverIntent = PendingIntent.getBroadcast(context,0,receiver_intent,PendingIntent.FLAG_CANCEL_CURRENT);

            Intent open_intent = new Intent(context,WordDetails.class);
            open_intent.putExtra("NOTIFICATION_ID",NOTIFICATION_ID);
            open_intent.putExtra("ACTIVITY_TYPE","NOTIFICATION_OPEN");
            open_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent open_pending_intent = PendingIntent.getActivity(context,0,open_intent,PendingIntent.FLAG_CANCEL_CURRENT);

            Bitmap icon = BitmapFactory.decodeResource(context.getResources(),R.mipmap.logo_round);
            NotificationCompat.Builder notify_builder;
            //NotificationManagerCompat notificationManagerCompat =  NotificationManagerCompat.from(this);;
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                NotificationChannel notificationChannel = new NotificationChannel(channel_id,channel_name,NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            notify_builder = new NotificationCompat.Builder(context,channel_id)
                    .setSmallIcon(R.mipmap.coffee)
                    .setLargeIcon(icon)
                    .setContentTitle("Word of the day: "+word_of_the_day.getWord())
                    .setContentText(word_of_the_day.getMeaning())
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(open_pending_intent)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(word_of_the_day.getMeaning()+" :\""+word_of_the_day.getSentence()+"\""))
                    .addAction(R.mipmap.check,"got it?",pendingReceiverIntent)
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL);

            //notificationManagerCompat.notify(NOTIFICATION_ID,notify_builder.build());
            notificationManager.notify(NOTIFICATION_ID,notify_builder.build());
            Log.d(MainActivity.TAG,"notification triggered for id :"+NOTIFICATION_ID);

        }
    }
}
