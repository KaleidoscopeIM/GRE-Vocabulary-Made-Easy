package com.curiousfreaks.greword;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * Created by gasaini on 3/21/2018.
 */

public class NewWordEntry extends AppCompatActivity {
    EditText newWord,newType,newMeaning,newSentence,newSynonyms,newAntonyms;
    Button save;
    ImageView newWordBookmarked;
    String bookmark="NO";
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_word_entry);

        newWord=findViewById(R.id.newWord);
        newType=findViewById(R.id.newWordType);
        newMeaning=findViewById(R.id.newWordMeaning);
        newSentence=findViewById(R.id.newWordSentence);
        newSynonyms=findViewById(R.id.newWordSynonyms);
        newAntonyms=findViewById(R.id.newWordAntonyms);
        newWordBookmarked=findViewById(R.id.newWordBookmark);
        save=findViewById(R.id.newWordSave);

        newWordBookmarked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bookmark.equals("NO"))
                {
                    bookmark="YES";
                    newWordBookmarked.setImageResource(R.mipmap.yellow_star);
                }
                else
                {
                    bookmark="NO";
                    newWordBookmarked.setImageResource(R.mipmap.bw_star);
                }
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!newWord.getText().toString().equals("") && !newMeaning.getText().toString().equals(""))
                {
                    DatabaseHandler dbHandler=new DatabaseHandler(getApplicationContext());
                    SQLiteDatabase db = dbHandler.getWritableDatabase();
                    int id = getWordId();
                    wordDefinition aWord = new wordDefinition();
                    aWord.setId(id);
                    aWord.setWord(newWord.getText().toString());
                    aWord.setType(newType.getText().toString());
                    aWord.setMeaning(newMeaning.getText().toString());
                    aWord.setSentence(newSentence.getText().toString());
                    aWord.setSynonyms(newSynonyms.getText().toString());
                    aWord.setAntonyms(newAntonyms.getText().toString());
                    dbHandler.insertAWord(db, id,newWord.getText().toString(),newType.getText().toString(),newMeaning.getText().toString(),
                            newSentence.getText().toString(),newSynonyms.getText().toString(),newAntonyms.getText().toString(),"","","");
                    if(bookmark.equals("YES"))
                        dbHandler.updateLearntOrBookmarked(db,aWord,null,"YES");
                    finish();
                    db.close();

                }
                else
                {
                    if(newMeaning.getText().toString().equals("")) {
                        newMeaning.setError("Word's meaning required.");
                    }else
                    {
                        newMeaning.setError(null);
                    }
                    if(newWord.getText().toString().equals("")) {
                        newWord.setError("Word name required.");
                    }
                    else{
                        newWord.setError(null);
                    }
                }
            }
        });


    }
    int getWordId()
    {
        int id;
        sharedPreferences=getApplicationContext().getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        id=sharedPreferences.getInt("ID",-1);
        if(id==-1){
            id=2000;
            editor.putInt("ID",id);
            editor.commit();
        }else{
            id++;
           editor.putInt("ID",id);
           editor.commit();
        }
        return id;
    }
}
