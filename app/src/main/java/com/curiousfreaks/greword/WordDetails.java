package com.curiousfreaks.greword;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by gasaini on 3/8/2018.
 */

public class WordDetails extends AppCompatActivity implements imageRecyclerAdapter.imageClickInterface{

    TextView word,type,meaning,sentence,synonym,antonym;
    ImageView speak,bookmark,share;
    Button next,previous;
    DatabaseHandler dbHandler;
    wordDefinition aWord = null;
    String ACTIVITY_TYPE="";           //             /*homeURL="https://s3.ap-south-1.amazonaws.com/grewords1/images";*/
    List<wordDefinition> wordsList;
    TextToSpeech textToSpeech;
    boolean isTTSInitialized = false;
    LinearLayout imageLayout;
    RecyclerView imageRecyclerView;
    imageRecyclerAdapter imageAdapter;
    List<imageDefinition> imagesList;
    //downloadWordImages downloadImageTask;
    //static boolean showSynonyms=false,showAntonyms=false;
    public String jsonString = "";
    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor prefEditor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(this.getIntent().getExtras()!=null && this.getIntent().getExtras().containsKey("ACTIVITY_TYPE")){
            ACTIVITY_TYPE=getIntent().getExtras().getString("ACTIVITY_TYPE");
        }

        textToSpeech=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i == TextToSpeech.SUCCESS)
                {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                    isTTSInitialized = true;
                }

            }
        });

        dbHandler=new DatabaseHandler(getApplicationContext());
        wordsList=new ArrayList<>();

        sharedPreferences=getApplicationContext().getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE);
        prefEditor=sharedPreferences.edit();

        setContentView(R.layout.word_details);
        word=findViewById(R.id.word);
        speak=findViewById(R.id.speakWord);
        bookmark=findViewById(R.id.bookmarkWord);
        type=findViewById(R.id.type);
        meaning=findViewById(R.id.meaning);
        sentence=findViewById(R.id.sentence);
        synonym=findViewById(R.id.synonym);
        antonym=findViewById(R.id.antonym);
        previous=findViewById(R.id.previousWord);
        next=findViewById(R.id.nextWord);
        imageLayout=findViewById(R.id.wordImagesLayout);
        imageRecyclerView=findViewById(R.id.imagesRecycler);
        share=findViewById(R.id.shareAWord);

        imagesList=new ArrayList<>();
        imageAdapter=new imageRecyclerAdapter(imagesList);
        imageAdapter.setImageClickListner(this);
        RecyclerView.LayoutManager lm=new LinearLayoutManager(getApplicationContext(), LinearLayout.HORIZONTAL,false);
        imageRecyclerView.setLayoutManager(lm);
        imageRecyclerView.setAdapter(imageAdapter);
        //downloadImageTask=new downloadWordImages();


        findAWord();
        initDisplay();

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index=wordsList.indexOf(aWord);
                index--;
                aWord=wordsList.get(index);
                if(next.getVisibility()==View.INVISIBLE)
                    next.setVisibility(View.VISIBLE);
                if(index==0) {
                    previous.setVisibility(View.INVISIBLE);
                }
                initDisplay();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index=wordsList.indexOf(aWord);
                ++index;
                aWord=wordsList.get(index);
                if(previous.getVisibility()==View.INVISIBLE)
                    previous.setVisibility(View.VISIBLE);
                if(index==(wordsList.size()-1))
                {
                    next.setVisibility(View.INVISIBLE);

                }
                initDisplay();
            }
        });
        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //textToSpeech.setSpeechRate(0.7f);
                String speech;
                if (!aWord.getAttr1().equals(""))
                    speech = aWord.getWord() + "." + aWord.getAttr1();
                else
                    speech = aWord.getWord() + "." + aWord.getMeaning();
                //textToSpeech.speak(speech, TextToSpeech.QUEUE_FLUSH, null, null);
                speakIt(speech);
            }
        });
        bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseHandler dbHandler=new DatabaseHandler(getApplicationContext());
                SQLiteDatabase db = dbHandler.getWritableDatabase();
                if(aWord.getBookmarked().equals("YES"))
                {
                    bookmark.setImageResource(R.mipmap.outline_star_green);
                    aWord.setBookmarked("NO");
                    dbHandler.updateLearntOrBookmarked(db,aWord,"null","NO");
                }else
                {
                    bookmark.setImageResource(R.mipmap.yellow_star);
                    aWord.setBookmarked("YES");
                    dbHandler.updateLearntOrBookmarked(db,aWord,"null","YES");
                }
                db.close();
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String body="Word: "+aWord.getWord()+"\n"+
                        "Meaning: "+aWord.getMeaning()+"\n"+
                        "\""+aWord.getSentence()+"\""
                        ;

                intent.putExtra(Intent.EXTRA_SUBJECT,"Curious Freaks");
                intent.putExtra(Intent.EXTRA_STREAM,R.mipmap.logo_rect);
                intent.putExtra(Intent.EXTRA_TEXT,body);
                startActivity(Intent.createChooser(intent,"Share using.."));
            }
        });

    }

    public void findAWord()
    {
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        if(ACTIVITY_TYPE.equals("NOTIFICATION_OPEN")) {
            wordsList=dbHandler.getAllWords(db);
            int notification_id = getIntent().getExtras().getInt("NOTIFICATION_ID"); // it would be great if I can get WORD here instead of id
            Iterator itr = wordsList.iterator();
            wordDefinition foundWord;
            while(itr.hasNext())
            {
                foundWord = (wordDefinition) itr.next();
                if (foundWord.getId() == notification_id) {
                    aWord=foundWord;
                    return;
                }
            }
            db.close();
            return;
        }
        if(ACTIVITY_TYPE.equals("VOICE_SEARCH")) {

            wordsList=dbHandler.getAllWords(db);
            wordDefinition foundWord;
            ArrayList<String> voiceRecognizedWords=getIntent().getStringArrayListExtra("VOICE_SEARCH_RESULT");
            for(String word:voiceRecognizedWords)
            {
                Iterator itr = wordsList.iterator();
                while(itr.hasNext())
                {
                    foundWord = (wordDefinition) itr.next();
                    if (foundWord.getWord().equalsIgnoreCase(word)) {
                        aWord=foundWord;
                        return;
                    }
                }
            }
            return;
        }
        //getting details if flow comes from commonAllWord activity
        ACTIVITY_TYPE = sharedPreferences.getString("ACTIVITY_TYPE","");
        if(ACTIVITY_TYPE.equals("ALL_WORDS")){
            wordsList = dbHandler.getAllWords(db);
        }
        if(ACTIVITY_TYPE.equals("MY_FAV_WORDS")){
            wordsList = dbHandler.getSelectedWords("SELECT * FROM "+DatabaseHandler.TABLE_NAME+" WHERE BOOKMARKED = \"YES\"",db);
        }
        if(ACTIVITY_TYPE.equals("BARRON_333")){
            wordsList = dbHandler.getBarron333(getAssets(),db);
        }
        if (wordsList.isEmpty())
        {
            return;
        }
        long uniqueId = (int) getIntent().getExtras().getLong("uniqueId");
        Iterator itr = wordsList.iterator();
        while (itr.hasNext()) {
            aWord = (wordDefinition) itr.next();
            if (aWord.getId() == uniqueId) {
                return;
            }
            }

        db.close();
    }

    public void initDisplay()
    {
        if(aWord==null)
        {
            Toast.makeText(getApplicationContext(),"Couldn't find word in library.. try again!!",Toast.LENGTH_SHORT).show();
            Intent intent=new Intent();
            intent.putExtra("STATUS",-2);
            setResult(RESULT_CANCELED,intent);
            finish();
            return;
        }
        if(ACTIVITY_TYPE.equals("VOICE_SEARCH")) {
            previous.setVisibility(View.INVISIBLE);
            next.setVisibility(View.INVISIBLE);
            String speech;
            if (!aWord.getAttr1().equals(""))
                speech = aWord.getWord() + ".\n" + aWord.getAttr1();
            else
                speech = aWord.getWord() + ".\n" + aWord.getMeaning();

            speakIt(speech);
        }
        if(ACTIVITY_TYPE.equals("NOTIFICATION_OPEN")){
            previous.setVisibility(View.INVISIBLE);
            next.setVisibility(View.INVISIBLE);

        }else{
            if(wordsList.indexOf(aWord)==0)
            {
                previous.setVisibility(View.INVISIBLE);
            }
            if(wordsList.indexOf(aWord)==(wordsList.size()-1))
            {
                next.setVisibility(View.INVISIBLE);
            }
        }

        word.setText(aWord.getWord());

        if(!aWord.getType().equals(""))
            type.setText(aWord.getType());
        else
            type.setText("");

        if(!aWord.getMeaning().equals(""))
            meaning.setText(aWord.getMeaning());
        else
            meaning.setText("");

        if(!aWord.getSentence().equals(""))
            sentence.setText("\""+aWord.getSentence()+"\"");
        else
            sentence.setText("");

        /*if(sharedPreferences.getBoolean("SYNONYMS",false))
            synonym.setVisibility(View.VISIBLE);
        if(sharedPreferences.getBoolean("ANTONYMS",false))
            antonym.setVisibility(View.VISIBLE);*/

        smartDisplaySynonymsAntonyms();

        if(aWord.getBookmarked().equals("YES"))
        {
            bookmark.setImageResource(R.mipmap.yellow_star);
        }else
        {
            bookmark.setImageResource(R.mipmap.outline_star_green);
        }

        initImages();

    }

    public void initImages()
    {
        /*if(downloadImageTask.getStatus()==AsyncTask.Status.RUNNING)
        {
            downloadImageTask.cancel(true);
        }*/
        if(!imagesList.isEmpty())
            imagesList.clear();
        if(jsonString.equals(""))
            //jsonString = fetchDataMapJsonString();
            jsonString = dbHandler.getJsonStingUsingAssetManager(getAssets(),"DataMap.json");
        try {
            JSONObject jRootObject = new JSONObject(jsonString);
            JSONArray jArray= jRootObject.optJSONArray("FDATA");
            for(int i=0;i<jArray.length();++i) {
                JSONObject jsonObject=jArray.getJSONObject(i);
                if(aWord.getId()==jsonObject.getLong("MAPID")){
                    Iterator<String> itr=jsonObject.keys();
                    while(itr.hasNext())
                    {
                        String key=itr.next();
                        if(!key.equals("MAPID")){

                            InputStream imageStream = getAssets().open("images/"+jsonObject.getString(key));
                            Bitmap image = BitmapFactory.decodeStream(imageStream);
                            imageDefinition aImageDef = new imageDefinition(image);
                            aImageDef.setId(aWord.getId());
                            aImageDef.setName(jsonObject.getString(key));
                            imagesList.add(aImageDef);
                            imageAdapter.notifyDataSetChanged();
                            /*
                            File imageFile = new File(getApplicationContext().getExternalCacheDir(), jsonObject.getString(key) + ".jpg");
                            if(imageFile.exists() || imageFile.length() != 0L)
                            {
                                imageDefinition aImage=new imageDefinition(aWord.getId(),BitmapFactory.decodeFile(imageFile.getAbsolutePath()));
                                imagesList.add(aImage);
                                imageAdapter.notifyDataSetChanged();
                            }else
                            {
                                new downloadWordImages().execute(jsonObject.getString(key));
                            }
                            */

                        }
                    }
                    break;
                }
            }

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        imageAdapter.notifyDataSetChanged();

    }
    public void speakIt(String speech1)
    {
        final String speech = speech1;
        final float voice_mod = sharedPreferences.getFloat("VOICE_MODULATION",0.7f);
        Thread th = new Thread(){
            @Override
            public void run() {
                super.run();
                int waitCount=0;
                while(!isTTSInitialized){
                    try{
                        if(waitCount==10)
                            break;
                        Thread.sleep(1000);
                        waitCount++;
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                if(isTTSInitialized){
                    try{
                        textToSpeech.setSpeechRate(voice_mod);
                        textToSpeech.speak(speech, TextToSpeech.QUEUE_FLUSH, null, null);
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),"TTS initialization in progress",Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    //Toast.makeText(getApplicationContext(),"TTS initialization error",Toast.LENGTH_SHORT).show();
                }
            }
        };
        th.start();
    }

    @Override
    protected void onPause() {
       // if (textToSpeech != null) {
        if (textToSpeech.isSpeaking()){
            textToSpeech.stop();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi=getMenuInflater();
        mi.inflate(R.menu.word_details_menu,menu);
        menu.findItem(R.id.id_synonymCheck).setChecked(sharedPreferences.getBoolean("SYNONYMS",false));
        menu.findItem(R.id.id_antonymCheck).setChecked(sharedPreferences.getBoolean("ANTONYMS",false));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.id_synonymCheck)
        {
            boolean checked = item.isChecked();
            if (checked)
            {
                prefEditor.putBoolean("SYNONYMS",false);
                item.setChecked(false);
            }
            else {
                prefEditor.putBoolean("SYNONYMS",true);
                item.setChecked(true);
            }
            prefEditor.apply();
            prefEditor.commit();
            smartDisplaySynonymsAntonyms();
        }

        if(item.getItemId() == R.id.id_antonymCheck)
        {
            if (item.isChecked())
            {
                prefEditor.putBoolean("ANTONYMS",false);
                item.setChecked(false);
            }
            else {
                prefEditor.putBoolean("ANTONYMS",true);
                item.setChecked(true);
            }
            prefEditor.apply();
            prefEditor.commit();
            smartDisplaySynonymsAntonyms();
        }
       if(item.getItemId() == R.id.id_voice_modulation)
       {
           LayoutInflater li = LayoutInflater.from(this);
           View voice_mod_view = li.inflate(R.layout.void_modulation,null);
           AlertDialog.Builder diallog_builder = new AlertDialog.Builder(this);

           final SeekBar seekBar = voice_mod_view.findViewById(R.id.id_seekBar);
           final TextView voice_mod_text = voice_mod_view.findViewById(R.id.id_voice_modulation_text);
           final Button set_voice_mod_button = voice_mod_view.findViewById(R.id.id_set_voice_modulation);

           float saved_progress = sharedPreferences.getFloat("VOICE_MODULATION",0.7f);

           seekBar.setProgress((int)(saved_progress*10));
           voice_mod_text.setText(saved_progress+"f");

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    Log.d(MainActivity.TAG,"seeking to : "+Integer.toString(i));
                    float fl_val = (float) i/10;
                    voice_mod_text.setText(Float.toString(fl_val)+"f");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

           diallog_builder
                   .setView(voice_mod_view)
                   .setCancelable(true);

           final AlertDialog voice_mod_dialog = diallog_builder.create();

           set_voice_mod_button.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   float mod_value = Float.parseFloat((String)voice_mod_text.getText());
                   Toast.makeText(getApplicationContext(),"Voice modulation set to: "+Float.toString(mod_value)+"f",Toast.LENGTH_SHORT).show();
                   prefEditor.putFloat("VOICE_MODULATION",mod_value);
                   prefEditor.apply();
                   prefEditor.commit();
                   voice_mod_dialog.dismiss();
               }
           });

           voice_mod_dialog.show();
       }

        if(item.getItemId()==R.id.reportWordIssue)
        {
            LayoutInflater li = LayoutInflater.from(this);
            View reportView = li.inflate(R.layout.report_issue,null);
            AlertDialog.Builder diallog_builder = new AlertDialog.Builder(this);

            final TextView report_word = reportView.findViewById(R.id.report_word);
            //final EditText report_email_id =reportView.findViewById(R.id.report_email_id);
            final EditText report_addition_info = reportView.findViewById(R.id.report_addition_info);
            final Button report_send_button = reportView.findViewById(R.id.report_send_button);

            report_word.setText("Would you like to report an issue in " + aWord.getWord()+"?");
            diallog_builder
                    .setView(reportView)
                    .setCancelable(true);

            final AlertDialog report_dialog = diallog_builder.create();

            report_send_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String str_addition_info = report_addition_info.getText().toString();
                    Intent email = new Intent(Intent.ACTION_SEND);
                    email.putExtra(Intent.EXTRA_EMAIL,new String[]{"thecuriousfreak007@gmail.com"});
                    email.putExtra(Intent.EXTRA_SUBJECT,"Important alert message..issue reported in word "+aWord.getWord());
                    email.putExtra(Intent.EXTRA_TEXT,"Word: " +aWord.getId()+" #"+aWord.getId()+" "+
                            "Addition Information :" +
                            ""+str_addition_info);
                    email.setType("plain/text");
                    startActivity(Intent.createChooser(email,"Choose an Email client:"));

                    report_dialog.dismiss();
                    //Toast.makeText(getApplicationContext(),"Thanks you so much for your feedback!!",Toast.LENGTH_SHORT).show();

                }
            });


            report_dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void smartDisplaySynonymsAntonyms()
    {
        boolean showSynonyms = sharedPreferences.getBoolean("SYNONYMS",false);
        boolean showAntonyms = sharedPreferences.getBoolean("ANTONYMS",false);
        if(!aWord.getSynonyms().equals("") && showSynonyms) {
            synonym.setVisibility(View.VISIBLE);
            synonym.setText("Synonyms - " + aWord.getSynonyms());
            if(!aWord.getAntonyms().equals("") && showAntonyms){
                antonym.setVisibility(View.VISIBLE);
                antonym.setText("Antonyms - "+aWord.getAntonyms());
            }
            else {
                if(antonym.getVisibility()==View.VISIBLE){
                    antonym.setVisibility(View.INVISIBLE);
                }
            }
        }
        else {
            if (!aWord.getAntonyms().equals("") && showAntonyms) {
                synonym.setVisibility(View.VISIBLE);
                synonym.setText("Antonyms - " + aWord.getAntonyms()); // if synonyms is not marked visible then show antonyms in place of synonyms text box
                if(antonym.getVisibility() == View.VISIBLE)
                {
                    antonym.setVisibility(View.INVISIBLE);
                }
            }else {
                if (synonym.getVisibility() == View.VISIBLE)
                {
                    synonym.setVisibility(View.INVISIBLE);
                }

            }
        }
        /*if(!aWord.getAntonyms().equals(""))
            antonym.setText("Antonyms - "+aWord.getAntonyms());
        else
            antonym.setText("");*/
    }

    public String fetchDataMapJsonString()
    {
        String jsonStr="";
        File dataMapFile;
        try {

            dataMapFile=new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"DataMap.json");
            if(!dataMapFile.exists() || (dataMapFile.length()==0L))
                return "";
            BufferedReader br = new BufferedReader(new FileReader(dataMapFile.getAbsolutePath()));
            String line;
            while ((line = br.readLine()) != null) {
                jsonStr += line;
            }
            br.close();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return jsonStr;
    }

    @Override
    public void OnImageClick(List<imageDefinition> imagesList) {

        AlertDialog.Builder dialog_builder = new AlertDialog.Builder(this);
        //dialog_builder.setTitle(aWord.getWord()+": "+aWord.getMeaning());
        LayoutInflater inflator = LayoutInflater.from(this);
        View dialog_layout = inflator.inflate(R.layout.image_dialog,null);
        LinearLayout image_liner_layout = dialog_layout.findViewById(R.id.image_dialog_linear_layout);
        final TextView textView = dialog_layout.findViewById(R.id.dialog_word_details);
        textView.setText(aWord.getWord()+": "+aWord.getMeaning());

        int imgMaxWidth = (int)(getResources().getDisplayMetrics().widthPixels*0.80);

        for (imageDefinition imgDef: imagesList)
        {
            ImageView aImage= new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout
                    .LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            aImage.setPadding(15,15,15,15);

            aImage.setLayoutParams(params);
            Bitmap tempBitmap = getScaledBitmap(imgDef.getBitmap(),imgMaxWidth);

            aImage.setImageBitmap(tempBitmap);
            image_liner_layout.addView(aImage);
        }

        dialog_builder.setView(dialog_layout);

        AlertDialog dialog = dialog_builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    public Bitmap getScaledBitmap(Bitmap originalBitmap, float scaledWidth)
    {
        Bitmap scaledBitmap=null;
        if(originalBitmap!=null) {
            int originalWidth = originalBitmap.getWidth();
            int originalHeight = originalBitmap.getHeight();
            float widthRatio=scaledWidth/originalWidth;

            int finalWidth=(int)Math.floor(originalWidth*widthRatio);
            int finalHeight=(int)Math.floor(originalHeight*widthRatio);

            scaledBitmap=Bitmap.createScaledBitmap(originalBitmap,finalWidth,finalHeight,true);

        }

        return scaledBitmap;
    }

    @Override
    public void onBackPressed() {
        if(!(ACTIVITY_TYPE.equals("VOICE_SEARCH") || ACTIVITY_TYPE.equals("NOTIFICATION_OPEN"))) {
            //prefEditor.putString("WORD_DETAIL_RETURN", "ALL_WORDS");
            prefEditor.putBoolean("REFRESH_LIST", true);
            prefEditor.apply();
            prefEditor.commit();
        }
        super.onBackPressed();
    }

    /* private class downloadWordImages extends AsyncTask<String,Bitmap,String>
    {

        @Override
        protected String doInBackground(String... strings) {
            try{

                URL url=new URL(homeURL+"/"+strings[0]+".jpg");
                HttpsURLConnection connection=(HttpsURLConnection)url.openConnection();
                connection.connect();
                File imageFile = new File(getApplicationContext().getExternalCacheDir(), strings[0] + ".jpg");
                if((connection.getResponseCode()==HttpsURLConnection.HTTP_OK) && (connection.getContentLength() != -1)) {
                        if(imageFile.exists() && imageFile.length()!=connection.getContentLength())
                            imageFile.delete();
                        if (!imageFile.exists()) {
                            imageFile.createNewFile();
                        InputStream inputStream = new BufferedInputStream(url.openStream());
                        FileOutputStream fout = new FileOutputStream(imageFile);
                        byte data[] = new byte[1024];
                        int readBytes;
                        while ((readBytes = inputStream.read(data)) != -1) {
                            fout.write(data, 0, readBytes);
                        }
                        fout.flush();
                        inputStream.close();
                        fout.close();
                    }
                    publishProgress(BitmapFactory.decodeFile(imageFile.getAbsolutePath()));
                }
                connection.disconnect();

            }catch (Exception e)
            {
                e.printStackTrace();
            }
            return "OK";
        }

        @Override
        protected void onPostExecute(String msg) {
            super.onPostExecute(msg);
            /*downloadBar.setVisibility(View.INVISIBLE);
            if(msg.equals("NO_DATA_MAP"))
                Toast.makeText(getApplicationContext(),"DataMap is deleted.. Contact admin!!",Toast.LENGTH_SHORT).show();
            if(msg.equals("ERROR"))
                Toast.makeText(getApplicationContext(),"Error in downloading word's images",Toast.LENGTH_SHORT).show();*/
        /*}

        @Override
        protected void onProgressUpdate(Bitmap... values) {
            super.onProgressUpdate(values);
            imageDefinition oneImg=new imageDefinition(values[0]);
            imagesList.add(oneImg);
            imageAdapter.notifyDataSetChanged();

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
           // imagesList.clear();
        }
    }*/
}
