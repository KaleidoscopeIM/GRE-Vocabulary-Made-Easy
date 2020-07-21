package com.curiousfreaks.greword;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "Curious Freaks";
    Button allWords, flashCards, myWordsList, speechRecognize, newWordEntry, buyCoffee, barron333, reportIssue, manageNotification;
    private AdView adView;
    ProgressBar progressBar;
    TextView progressText;
    /*public int fileSize = 0;
    final String wordDataURL = "https://s3.ap-south-1.amazonaws.com/grewords1/WordsList.json";
    final String imageDataURL = "https://s3.ap-south-1.amazonaws.com/grewords1/DataMap.json";
    final String barron333URL = "https://s3.ap-south-1.amazonaws.com/grewords1/Barron333.json";*/
    private final int VOICE_SEARCH_REQ=1;
    public DatabaseHandler databaseHandler;
    public SQLiteDatabase database;
    public String allWordsJson;
    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor prefEditor;
    Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);

        allWords = findViewById(R.id.allwords);
        flashCards = findViewById(R.id.flashcards);
        myWordsList = findViewById(R.id.mywordslist);
        progressBar=findViewById(R.id.downloadProgressBar);
        progressText=findViewById(R.id.downloadProgressText);
        speechRecognize=findViewById(R.id.speechRecognize);
        newWordEntry=findViewById(R.id.newWordEntry);
        buyCoffee=findViewById(R.id.buyCoffee);
        barron333=findViewById(R.id.barron333);
        reportIssue = findViewById(R.id.reportAnIssue);
        manageNotification = findViewById(R.id.manageNotifications);

        //new downloadData().execute(wordDataURL,"SHOW_PROGRESS","REFRESH_DB");
        //new downloadData().execute(imageDataURL,"NO_PROGRESS","NO_REFRESH_DB");
        //new downloadData().execute(barron333URL,"NO_PROGRESS","NO_REFRESH_DB");
        databaseHandler = new DatabaseHandler(getApplicationContext());
        database = databaseHandler.getReadableDatabase();
        utils =new Utils(getApplicationContext());

        //dHandler.refreshDB();
        //initialize_d();
        sharedPreferences=getApplicationContext().getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE);
        prefEditor=sharedPreferences.edit();
        String status = sharedPreferences.getString("INITIALIZE_CODE","FAILED");
        if(status.equals("FAILED"))
        {
            allWordsJson = databaseHandler.getJsonStingUsingAssetManager(getAssets(),"WordsList.json");
            new initialize_data().execute(allWordsJson);
            utils.set_notification_alarm(9,0, false);
            prefEditor.putBoolean("SYNONYMS",false);
            prefEditor.putBoolean("ANTONYMS",false);
            prefEditor.putBoolean("NOTIFICATION_CHECK",true);
            prefEditor.putFloat("VOICE_MODULATION",0.7f);
            prefEditor.putBoolean("PURCHASE",false);
            prefEditor.apply();
            prefEditor.commit();
        }
        MobileAds.initialize(this, "ca-app-pub-1290738907415765~3668862095");
        adView =findViewById(R.id.adView);
        if(sharedPreferences.getBoolean("PURCHASE",false)) {
            adView.setVisibility(View.GONE);
        }
        else
        {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }

        allWords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Animation anim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.button_main_layout);
                view.startAnimation(anim);*/
                Intent intent = new Intent(MainActivity.this, CommonAllWords.class);
                //intent.putExtra("ACTIVITY_TYPE","ALL_WORDS");
                prefEditor.putString("ACTIVITY_TYPE","ALL_WORDS");
                prefEditor.commit();
                startActivity(intent);
            }
        });

        flashCards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FlashCards.class);
                startActivity(intent);

            }
        });

        myWordsList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CommonAllWords.class);
                //intent.putExtra("ACTIVITY_TYPE","MY_FAV_WORDS");
                prefEditor.putString("ACTIVITY_TYPE","MY_FAV_WORDS");
                prefEditor.commit();
                startActivity(intent);
            }
        });
        speechRecognize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
                try {
                    startActivityForResult(intent, VOICE_SEARCH_REQ);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Speech not supported or google voice search is disabled", Toast.LENGTH_LONG).show();
                }
            }
        });
        newWordEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(getApplicationContext(),NewWordEntry.class);
                startActivity(i);
            }
        });
        buyCoffee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BuyCoffee.class);
                startActivity(intent);

            }
        });
        barron333.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CommonAllWords.class);
                //intent.putExtra("ACTIVITY_TYPE","BARRON_333");
                prefEditor.putString("ACTIVITY_TYPE","BARRON_333");
                prefEditor.commit();
                startActivity(intent);

            }
        });
        reportIssue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater li = LayoutInflater.from(MainActivity.this);
                View reportView = li.inflate(R.layout.report_issue,null);
                AlertDialog.Builder dialog_builder = new AlertDialog.Builder(MainActivity.this);

                final TextView report_word = reportView.findViewById(R.id.report_word);
                final EditText report_addition_info = reportView.findViewById(R.id.report_addition_info);
                final Button report_send_button = reportView.findViewById(R.id.report_send_button);

                report_word.setText("Would you like to report an issue ?");
                report_addition_info.setHint("Please provide details here..");
                dialog_builder
                        .setView(reportView)
                        .setCancelable(true);
                final AlertDialog report_dialog = dialog_builder.create();
                report_send_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String str_addition_info = report_addition_info.getText().toString();
                        Intent email = new Intent(Intent.ACTION_SEND);
                        email.putExtra(Intent.EXTRA_EMAIL,new String[]{"thecuriousfreak007@gmail.com"});
                        email.putExtra(Intent.EXTRA_SUBJECT,"Important alert message..issue reported in a word");
                        email.putExtra(Intent.EXTRA_TEXT,"Word(optional): "+"     Feedback: "+str_addition_info);
                        email.setType("plain/text");
                        startActivity(Intent.createChooser(email,"Choose an Email client:"));
                        report_dialog.dismiss();
                    }
                });
                report_dialog.show();
            }
        });
        manageNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(getApplicationContext(),manageNotifications.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case VOICE_SEARCH_REQ:{
                if(resultCode==RESULT_OK && data!=null)
                {
                    ArrayList<String> speech=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Intent i=new Intent(MainActivity.this,WordDetails.class);
                    i.putStringArrayListExtra("VOICE_SEARCH_RESULT",speech);
                    i.putExtra("ACTIVITY_TYPE","VOICE_SEARCH");
                    startActivity(i);
                }
            }
        }

    }

    public class initialize_data extends AsyncTask<String, String, String>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(progressBar.getVisibility()==View.INVISIBLE)
            {
                progressBar.setVisibility(View.VISIBLE);
                progressText.setVisibility(View.VISIBLE);
                //downloadBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_OUT);
                progressBar.setMax(100);
                progressBar.setProgress(0);
            }
        }
        @Override
        protected String doInBackground(String... strings) {
            try {
                JSONObject jRootObject = new JSONObject(strings[0]);
                JSONArray jArray= jRootObject.optJSONArray("words");
                for(int i=0;i<jArray.length();++i) {
                    JSONObject jObject = jArray.getJSONObject(i);
                    Long id=Long.parseLong(jObject.getString("ID"));
                    String word=jObject.getString("WORD");
                    String type=jObject.getString("TYPE");
                    String meaning=jObject.getString("MEANING");
                    String sentense=jObject.getString("SENTENCE");
                    String synonyms=jObject.getString("SYNONYMS");
                    String antonyms=jObject.getString("ANTONYMS");
                    String link=jObject.getString("LINK");
                    String attr1=jObject.getString("ATTR1");
                    String attr2=jObject.getString("ATTR2");
                    int progress = ((i*100)/jArray.length());
                    publishProgress(Integer.toString(progress),Long.toString(id),word,type,meaning,sentense,synonyms,antonyms,link,attr1,attr2);
                    Thread.sleep(1);
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }
            return "";
        }
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            databaseHandler.refreshOneWord(database,Long.parseLong(values[1]),values[2],values[3],values[4],values[5],values[6],values[7],values[8],values[9],values[10]);
            progressBar.setProgress(Integer.parseInt(values[0]));
            progressText.setText("Initializing data files.. "+values[0]+"%");
        }
        @Override
        protected void onPostExecute(String s) {
            if(progressBar.getVisibility()==View.VISIBLE) {
                progressBar.setVisibility(View.GONE);
                progressText.setVisibility(View.GONE);
            }
            if(s.equals("-1")) {
                prefEditor.putString("INITIALIZE_CODE","FAILED");
                prefEditor.commit();
                Toast.makeText(getApplicationContext(),"Error in initializing data from server!!",Toast.LENGTH_SHORT).show();
            }
            else {
                prefEditor.putString("INITIALIZE_CODE","SUCCESS");
                prefEditor.commit();
                Toast.makeText(getApplicationContext(),"Initialization complete!!",Toast.LENGTH_SHORT).show();
            }
            database.close();
            super.onPostExecute(s);
        }

        @Override
        protected void onCancelled(String s) {
            Toast.makeText(getApplicationContext(),"Initialization cancelled!!",Toast.LENGTH_SHORT).show();
            prefEditor.putString("INITIALIZE_CODE","FAILED");
            prefEditor.commit();
            super.onCancelled(s);
        }
    }

    /*public class downloadData extends AsyncTask<String,Integer,String>
    {
        @Override
        protected String doInBackground(String... strings) {
            try {

                URL url = new URL(strings[0]);
                HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
                connection.connect();
                String fileName=Uri.parse(strings[0]).getLastPathSegment();
                File jsonFile = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);

                if(!(connection.getResponseCode()==HttpsURLConnection.HTTP_OK))
                    return "200";

                long fetchSize = connection.getContentLength();

                if (!jsonFile.exists()) {
                    Log.v(MainActivity.TAG, "not exists created one.." + getFilesDir() + "/wordList.json");
                    jsonFile.createNewFile();
                } else {
                   if(jsonFile.length()==fetchSize) {
                        return "0";
                    }else{
                        jsonFile.delete();
                        jsonFile.createNewFile();
                    }
                }
                InputStream inputStream = new BufferedInputStream(url.openStream());
                FileOutputStream fos = new FileOutputStream(jsonFile);
                byte data[] = new byte[1024];
                int readBytes,progressFrom,progressTo;

                while ((readBytes = inputStream.read(data)) != -1) {
                    progressFrom=(int)(fileSize/fetchSize)*100;
                    fileSize = fileSize + readBytes;
                    fos.write(data, 0, readBytes);
                    progressTo=(int)(fileSize/fetchSize)*100;
                    if(strings[1].equals("SHOW_PROGRESS")) {
                        for (int i = progressFrom; i <= progressTo; ++i) {
                            publishProgress(i);
                           // Thread.sleep(1);
                        }
                    }
                }
                inputStream.close();
                fos.close();
                return strings[2];

            }catch (Exception e)
            {
                e.printStackTrace();
                return "-1";
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(downloadBar.getVisibility()==View.VISIBLE)
                downloadBar.setVisibility(View.INVISIBLE);
            if(downloadProgressText.getVisibility()==View.VISIBLE)
                downloadProgressText.setVisibility(View.INVISIBLE);
            if(s.equals("-1")) {
                Toast.makeText(getApplicationContext(),"Error in downloading data from server!!",Toast.LENGTH_SHORT).show();
            }
            if(s.equals("REFRESH_DB"))
            {
                DatabaseHandler dHandler = new DatabaseHandler(getApplicationContext());
                //dHandler.refreshDB(getApplicationContext());

            }
            Log.v(MainActivity.TAG, "Exit Thread execution" );
            Log.v(MainActivity.TAG, "Exit startDownloadWith Refresh DB" );
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if(downloadBar.getVisibility()==View.INVISIBLE)
            {
                downloadBar.setVisibility(View.VISIBLE);
                downloadProgressText.setVisibility(View.VISIBLE);
                //downloadBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_OUT);
                downloadBar.setMax(100);
                downloadBar.setProgress(0);
            }
            downloadBar.setProgress(values[0]);
            downloadProgressText.setText("Downloading data files.. "+values[0]+"%");
            super.onProgressUpdate(values);
        }
    }*/
}
