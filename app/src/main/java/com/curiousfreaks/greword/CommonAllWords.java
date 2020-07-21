package com.curiousfreaks.greword;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


/**
 * Created by gasaini on 2/23/2018.
 */

public class CommonAllWords extends AppCompatActivity implements wordListRecyclerAdapter.myRecyclerItemClickListner{

    public List<wordDefinition> allWords;
    private RecyclerView recyclerView;
    private wordListRecyclerAdapter mAdapter=null;
    DatabaseHandler dbHandler;
    SQLiteDatabase database;
    String ACTIVITY_TYPE="";  // ALL_WORDS,  MY_FAV_WORDS,  BARRON_333
    TextToSpeech textToSpeech;
    SharedPreferences sharedPreferences;
    public SharedPreferences.Editor prefEditor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.list_recycler);
        dbHandler = new DatabaseHandler(getApplicationContext());
        sharedPreferences=getApplicationContext().getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE);
        prefEditor = sharedPreferences.edit();

        allWords = new ArrayList<>();
        /*if(this.getIntent().getExtras()!=null && this.getIntent().getExtras().containsKey("ACTIVITY_TYPE")){
            ACTIVITY_TYPE=getIntent().getExtras().getString("ACTIVITY_TYPE");
        }*/

        textToSpeech=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i!= TextToSpeech.ERROR)
                {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                }

            }
        });

        initActivity();
    }
    public void initActivity()
    {
        ACTIVITY_TYPE = sharedPreferences.getString("ACTIVITY_TYPE","");
        database = dbHandler.getReadableDatabase();
        if(ACTIVITY_TYPE.equals("ALL_WORDS")) {
            prefEditor.putString("WORD_DETAIL_RETURN","");
            prefEditor.apply();
            prefEditor.commit();
            allWords = dbHandler.getAllWords(database);
            if(allWords.isEmpty()){
                Toast.makeText(getApplicationContext(),"Initialization in progress.. Please wait!!.",Toast.LENGTH_SHORT).show();
            }
            //else {
            initWords();
            //}
        }
        if(ACTIVITY_TYPE.equals("MY_FAV_WORDS")) {
            allWords = dbHandler.getSelectedWords("SELECT * FROM "+DatabaseHandler.TABLE_NAME+" WHERE BOOKMARKED = \"YES\"",database);
            if(allWords.isEmpty()) {
                if (Build.VERSION.SDK_INT >= 28) {
                    Toast.makeText(getApplicationContext(), "You don't have bookmarked words!!\n " +
                            "Click on star to bookmark a word.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "You don't have bookmarked words!!", Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), "Click on star to bookmark a word.", Toast.LENGTH_SHORT).show();
                }
            }
            initWords();

        }
        if(ACTIVITY_TYPE.equals("BARRON_333")) {
            AssetManager assetManager = getAssets();
            allWords = dbHandler.getBarron333(assetManager,database);
            if(allWords.isEmpty())
            {
                Toast.makeText(getApplicationContext(),"Initialization in progress.. Please wait!!.",Toast.LENGTH_SHORT).show();
            }
            //else{
            initWords();
            //}
        }
        database.close();
    }
    public void initWords()
    {
        if(mAdapter ==null)
        {
            recyclerView = findViewById(R.id.wordLIstRecycler);
            mAdapter = new wordListRecyclerAdapter();
            RecyclerView.LayoutManager lm = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(lm);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            recyclerView.setAdapter(mAdapter);
            mAdapter.setOnItemCLickListner(this);
        }
        mAdapter.setWordsList(allWords);
        mAdapter.notifyDataSetChanged();
    }

    /*@Override
    public void onIDClicked(View view, int position, long uniqueId) {

    }*/

    @Override
    public void onWordClicked(View view, int position, long uniqueId) {
        Intent i=new Intent(getApplicationContext(),WordDetails.class);
        i.putExtra("uniqueId",uniqueId);
        //i.putExtra("wordsList", (ArrayList<wordDefinition>) allWords);
        startActivity(i);
    }

    @Override
    public void onMeaningClicked(View view, int position, long uniqueId) {
        Intent i=new Intent(getApplicationContext(),WordDetails.class);
        i.putExtra("uniqueId",uniqueId);
        startActivity(i);
    }

    @Override
    public void onMainLayoutClicked(View view, int position, long uniqueId) {
        Intent i=new Intent(getApplicationContext(),WordDetails.class);
        i.putExtra("uniqueId",uniqueId);
        startActivity(i);
    }

    @Override
    public void onStarClicked(View view, int position, long uniqueId) {
        Log.v(MainActivity.TAG,"Entering On click star Image"+uniqueId);
        ImageView starImage=(ImageView)view;
        Iterator<wordDefinition> itr=allWords.iterator();
        wordDefinition aWord;
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        int success=0;

        while(itr.hasNext())
        {
            aWord=itr.next();
            if(aWord.getId()==uniqueId)
            {

                if(ACTIVITY_TYPE.equals("ALL_WORDS") || ACTIVITY_TYPE.equals("BARRON_333")) {


                    if (aWord.getBookmarked().equals("YES")) {
                        aWord.setBookmarked("NO");
                        success = dbHandler.updateLearntOrBookmarked(db,aWord,null,"NO");
                        starImage.setImageResource(R.mipmap.outline_star_green);
                        break;
                    }
                    if (aWord.getBookmarked().equals("NO")) {
                        aWord.setBookmarked("YES");
                        success = dbHandler.updateLearntOrBookmarked(db,aWord,null,"YES");
                        starImage.setImageResource(R.mipmap.yellow_star);
                        break;
                    }
                }
                if(ACTIVITY_TYPE.equals("MY_FAV_WORDS"))
                {

                    success=dbHandler.updateLearntOrBookmarked(db,aWord,null,"NO");
                    allWords.remove(aWord);
                    mAdapter.notifyDataSetChanged();
                    break;
                }

            }
        }
        db.close();
        if(success==1)
        {
            Log.v(MainActivity.TAG,"Exiting On click star Image"+uniqueId);
        }
    }

    @Override
    public void onSpeakClicked(View view, int position, long uniqueId) {
        ImageView speak=(ImageView)view;
        Iterator<wordDefinition> itr=allWords.iterator();
        wordDefinition aWord;
        String speakWord;
        while(itr.hasNext())
        {
            aWord=itr.next();
            if(aWord.getId()==uniqueId)
            {
                speakWord=aWord.getWord();
                textToSpeech.setSpeechRate(sharedPreferences.getFloat("VOICE_MODULATION",0.7f));
                textToSpeech.speak(speakWord,TextToSpeech.QUEUE_FLUSH,null,null);
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(!allWords.isEmpty()) {
            MenuInflater mi = getMenuInflater();
            mi.inflate(R.menu.search_menu, menu);

            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) menu.findItem(R.id.searchOptionMenuId).getActionView();

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {

                    filterWords(s);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    filterWords(s);
                    return false;
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }
    public void filterWords(String query)
    {
        List<wordDefinition> fitleredList=new ArrayList<>();
        for(wordDefinition aWord:allWords)
        {
            if(aWord.getWord().toLowerCase().startsWith(query.toLowerCase()))
            {
                fitleredList.add(aWord);
            }
        }
        mAdapter.setFilter(fitleredList);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       if(item.getItemId()==R.id.sortAtoZ)
       {
           Collections.sort(allWords,wordDefinition.alphabeticallyAtoZ);
           mAdapter.notifyDataSetChanged();
       }
       if(item.getItemId()==R.id.sortZtoA)
       {
           Collections.sort(allWords,wordDefinition.alphabeticallyZtoA);
           mAdapter.notifyDataSetChanged();
       }
       if(item.getItemId()==R.id.random)
       {
           Collections.shuffle(allWords);
           mAdapter.notifyDataSetChanged();

       }
       return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        if (textToSpeech.isSpeaking())
        {
            textToSpeech.stop();
        }
        /*if(textToSpeech!=null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }*/
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean refresh_require = sharedPreferences.getBoolean("REFRESH_LIST",false);
        if(refresh_require) {
            initActivity();
            prefEditor.putBoolean("REFRESH_LIST",false);
            prefEditor.commit();
        }

    }
}