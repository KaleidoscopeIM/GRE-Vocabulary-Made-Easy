package com.curiousfreaks.greword;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gasaini on 2/23/2018.
 */

public class DatabaseHandler extends SQLiteOpenHelper{
    Context con;
    String createQuery="CREATE TABLE "+TABLE_NAME+" (ID INTEGER PRIMARY KEY, WORD TEXT, TYPE TEXT, MEANING TEXT, SENTENCE TEXT, SYNONYMS TEXT," +
            " ANTONYMS TEXT, LINK TEXT, ATTR1 TEXT, ATTR2 TEXT, LEARNT TEXT DEFAULT 'NO', BOOKMARKED TEXT DEFAULT 'NO', FREQUENCY INTEGER DEFAULT 1 )";

    public DatabaseHandler(Context context)
    {
        super(context,DB_NAME,null,DB_VERSION);
        con=context;
    }
    public static final int DB_VERSION=1;
    public static final String DB_NAME="greWords.db";
    public static final String TABLE_NAME="WORDLIST";

    public DatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(createQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String qr= "DROP TABLE IF EXISTS "+TABLE_NAME;
        sqLiteDatabase.execSQL(qr);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void initDB()
    {
        //executeBlindQuery("DROP TABLE "+DatabaseHandler.TABLE_NAME);
        executeBlindQuery(createQuery);
    }
    public void insertAWord(SQLiteDatabase db, long id, String word, String type, String meaning, String sentence, String synonyms, String antonyms, String link, String attr1, String attr2)
    {
        ContentValues value=new ContentValues();
        value.put("ID",id);
        value.put("WORD",word);
        value.put("TYPE",type);
        value.put("MEANING",meaning);
        value.put("SENTENCE",sentence);
        value.put("SYNONYMS",synonyms);
        value.put("ANTONYMS",antonyms);
        value.put("LINK",link);
        value.put("ATTR1",attr1);
        value.put("ATTR2",attr2);

        long newRow=db.insert( this.TABLE_NAME,null,value);
        if(newRow == -1)
            Log.v(MainActivity.TAG,"a row with id ="+id+" already exists");
        else
            Log.v(MainActivity.TAG,"Row count "+Long.toString(newRow));

    }
    public void printDB()
    {
        SQLiteDatabase db=this.getReadableDatabase();
        String[] projection={
                "ID",
                "WORD",
                "TYPE",
                "MEANING",
                "SENTENCE",
                "SYNONYMS",
                "ANTONYMS",
                "LINK",
                "ATTR1",
                "ATTR2",
                "LEARNT",
                "BOOKMARKED",

        };
        Cursor cursor = db.query(
                this.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        cursor.moveToFirst();
       do
        {
            Log.v(MainActivity.TAG,"Printing complete db : "+Long.toString(cursor.getLong(cursor.getColumnIndexOrThrow("ID")))
                    +",  "+cursor.getString(cursor.getColumnIndexOrThrow("WORD"))
                    +",  "+cursor.getString(cursor.getColumnIndexOrThrow("TYPE"))
                    +",  "+cursor.getString(cursor.getColumnIndexOrThrow("MEANING"))
                    +",  "+cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE"))
                    +",  "+cursor.getString(cursor.getColumnIndexOrThrow("SYNONYMS"))
                    +",  "+cursor.getString(cursor.getColumnIndexOrThrow("ANTONYMS"))
                    +",  "+cursor.getString(cursor.getColumnIndexOrThrow("LINK"))
                    +",  "+cursor.getString(cursor.getColumnIndexOrThrow("ATTR1"))
                    +",  "+cursor.getString(cursor.getColumnIndexOrThrow("ATTR2"))
                    +",  "+cursor.getString(cursor.getColumnIndexOrThrow("LEARNT"))
                    +",  "+cursor.getString(cursor.getColumnIndexOrThrow("BOOKMARKED"))
            );
        } while (cursor.moveToNext());
        cursor.close();
        db.close();
    }
    public wordDefinition getAWord(long id, SQLiteDatabase db)
    {
        if(db.isOpen())
        {
            Log.d(MainActivity.TAG,"db This is ope");
        }
        Log.v(MainActivity.TAG, "Entering getAWord");
       /* if(isDBEmpty(db))
        {
            Log.v(MainActivity.TAG, "Database is empty.. nothing to read");
            return null;
        }*/
        String query ="SELECT * FROM "+this.TABLE_NAME+" WHERE ID="+id;
        Log.v(MainActivity.TAG,"Query for a row: "+query);
        Cursor cursor = db.rawQuery(query,null);
        if(!cursor.moveToFirst())
            return null;
        wordDefinition wd=new wordDefinition();

        wd.setId(cursor.getLong(cursor.getColumnIndexOrThrow("ID")));
        wd.setWord(cursor.getString(cursor.getColumnIndexOrThrow("WORD")));
        wd.setType(cursor.getString(cursor.getColumnIndexOrThrow("TYPE")));
        wd.setMeaning(cursor.getString(cursor.getColumnIndexOrThrow("MEANING")));
        wd.setSentence(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE")));
        wd.setSynonyms(cursor.getString(cursor.getColumnIndexOrThrow("SYNONYMS")));
        wd.setAntonyms(cursor.getString(cursor.getColumnIndexOrThrow("ANTONYMS")));
        wd.setLink(cursor.getString(cursor.getColumnIndexOrThrow("LINK")));
        wd.setAttr1(cursor.getString(cursor.getColumnIndexOrThrow("ATTR1")));
        wd.setAttr2(cursor.getString(cursor.getColumnIndexOrThrow("ATTR2")));
        wd.setLearnt(cursor.getString(cursor.getColumnIndexOrThrow("LEARNT")));
        wd.setBookmarked(cursor.getString(cursor.getColumnIndexOrThrow("BOOKMARKED")));


        Log.v(MainActivity.TAG,"a word found:\n "+Long.toString(cursor.getLong(cursor.getColumnIndexOrThrow("ID")))
                +",  "+cursor.getString(cursor.getColumnIndexOrThrow("WORD"))
                +",  "+cursor.getString(cursor.getColumnIndexOrThrow("TYPE"))
                +",  "+cursor.getString(cursor.getColumnIndexOrThrow("MEANING"))
                +",  "+cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE"))
                +",  "+cursor.getString(cursor.getColumnIndexOrThrow("SYNONYMS"))
                +",  "+cursor.getString(cursor.getColumnIndexOrThrow("ANTONYMS"))
                +",  "+cursor.getString(cursor.getColumnIndexOrThrow("LINK"))
                +",  "+cursor.getString(cursor.getColumnIndexOrThrow("ATTR1"))
                +",  "+cursor.getString(cursor.getColumnIndexOrThrow("ATTR2"))
                +",  "+cursor.getString(cursor.getColumnIndexOrThrow("LEARNT"))
                +",  "+cursor.getString(cursor.getColumnIndexOrThrow("BOOKMARKED"))
        );
        cursor.close();
        Log.v(MainActivity.TAG, "Entering getAWord");
        return wd;
    }
    public boolean isDBEmpty(SQLiteDatabase db)
    {
        Cursor cursor = db.rawQuery("select * from "+ this.TABLE_NAME,null);
        if(!cursor.moveToFirst())
        {
            Log.v(MainActivity.TAG,"Database is empty");
            return true;
        }
        Log.v(MainActivity.TAG,"Database is not empty");
        cursor.close();
        return false;
    }
    public int getWordsCount()
    {
        int count;
        String query="SELECT * FROM "+this.TABLE_NAME;
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=db.rawQuery(query,null);
        cursor.moveToFirst();
        count=cursor.getCount();
        cursor.close();
        db.close();
        Log.v(MainActivity.TAG,"Words count for the DB: "+count);
        return count;
    }
    public List<wordDefinition> getAllWords(SQLiteDatabase db)
    {
        List<wordDefinition> wordList =new ArrayList<>();
        String query="SELECT * FROM "+this.TABLE_NAME;
        Cursor cursor=db.rawQuery(query,null);
        if(cursor.moveToFirst()) {
            do {
                wordDefinition aWord = new wordDefinition();
                aWord.setId(cursor.getLong(cursor.getColumnIndexOrThrow("ID")));
                aWord.setWord(cursor.getString(cursor.getColumnIndexOrThrow("WORD")));
                aWord.setType(cursor.getString(cursor.getColumnIndexOrThrow("TYPE")));
                aWord.setMeaning(cursor.getString(cursor.getColumnIndexOrThrow("MEANING")));
                aWord.setSentence(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE")));
                aWord.setSynonyms(cursor.getString(cursor.getColumnIndexOrThrow("SYNONYMS")));
                aWord.setAntonyms(cursor.getString(cursor.getColumnIndexOrThrow("ANTONYMS")));
                aWord.setLink(cursor.getString(cursor.getColumnIndexOrThrow("LINK")));
                aWord.setAttr1(cursor.getString(cursor.getColumnIndexOrThrow("ATTR1")));
                aWord.setAttr2(cursor.getString(cursor.getColumnIndexOrThrow("ATTR2")));
                aWord.setLearnt(cursor.getString(cursor.getColumnIndexOrThrow("LEARNT")));
                aWord.setBookmarked(cursor.getString(cursor.getColumnIndexOrThrow("BOOKMARKED")));
                /*Log.v(MainActivity.TAG, "adding word :\n " + Long.toString(cursor.getLong(cursor.getColumnIndexOrThrow("ID")))
                        + ",  " + cursor.getString(cursor.getColumnIndexOrThrow("WORD"))
                        + ",  " + cursor.getString(cursor.getColumnIndexOrThrow("TYPE"))
                        + ",  " + cursor.getString(cursor.getColumnIndexOrThrow("MEANING"))
                        + ",  " + cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE"))
                        + ",  " + cursor.getString(cursor.getColumnIndexOrThrow("SYNONYMS"))
                        + ",  " + cursor.getString(cursor.getColumnIndexOrThrow("ANTONYMS"))
                        + ",  " + cursor.getString(cursor.getColumnIndexOrThrow("LINK"))
                        + ",  " + cursor.getString(cursor.getColumnIndexOrThrow("ATTR1"))
                        + ",  " + cursor.getString(cursor.getColumnIndexOrThrow("ATTR2"))
                        + ",  " + cursor.getString(cursor.getColumnIndexOrThrow("LEARNT"))
                        + ",  " + cursor.getString(cursor.getColumnIndexOrThrow("BOOKMARKED"))
                );*/
                wordList.add(aWord);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return wordList;

    }
    public List<wordDefinition> getBookmarkedWords1()
    {
        List<wordDefinition> wordList =new ArrayList<>();
        SQLiteDatabase db=this.getReadableDatabase();
        String query="SELECT * FROM "+this.TABLE_NAME+" WHERE BOOKMARKED = \"YES\"";
        Cursor cursor=db.rawQuery(query,null);
        if(cursor.moveToFirst())
        {
            do
            {
                wordDefinition aWord=new wordDefinition();
                aWord.setId(cursor.getLong(cursor.getColumnIndexOrThrow("ID")));
                aWord.setWord(cursor.getString(cursor.getColumnIndexOrThrow("WORD")));
                aWord.setType(cursor.getString(cursor.getColumnIndexOrThrow("TYPE")));
                aWord.setMeaning(cursor.getString(cursor.getColumnIndexOrThrow("MEANING")));
                aWord.setSentence(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE")));
                aWord.setSynonyms(cursor.getString(cursor.getColumnIndexOrThrow("SYNONYMS")));
                aWord.setAntonyms(cursor.getString(cursor.getColumnIndexOrThrow("ANTONYMS")));
                aWord.setLink(cursor.getString(cursor.getColumnIndexOrThrow("LINK")));
                aWord.setAttr1(cursor.getString(cursor.getColumnIndexOrThrow("ATTR1")));
                aWord.setAttr2(cursor.getString(cursor.getColumnIndexOrThrow("ATTR2")));
                aWord.setLearnt(cursor.getString(cursor.getColumnIndexOrThrow("LEARNT")));
                aWord.setBookmarked(cursor.getString(cursor.getColumnIndexOrThrow("BOOKMARKED")));
                Log.v(MainActivity.TAG,"adding word :\n "+Long.toString(cursor.getLong(cursor.getColumnIndexOrThrow("ID")))
                        +",  "+cursor.getString(cursor.getColumnIndexOrThrow("WORD"))
                        +",  "+cursor.getString(cursor.getColumnIndexOrThrow("TYPE"))
                        +",  "+cursor.getString(cursor.getColumnIndexOrThrow("MEANING"))
                        +",  "+cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE"))
                        +",  "+cursor.getString(cursor.getColumnIndexOrThrow("SYNONYMS"))
                        +",  "+cursor.getString(cursor.getColumnIndexOrThrow("ANTONYMS"))
                        +",  "+cursor.getString(cursor.getColumnIndexOrThrow("LINK"))
                        +",  "+cursor.getString(cursor.getColumnIndexOrThrow("ATTR1"))
                        +",  "+cursor.getString(cursor.getColumnIndexOrThrow("ATTR2"))
                        +",  "+cursor.getString(cursor.getColumnIndexOrThrow("LEARNT"))
                        +",  "+cursor.getString(cursor.getColumnIndexOrThrow("BOOKMARKED"))
                );
                wordList.add(aWord);
            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return wordList;

    }
    public List<wordDefinition> getSelectedWords(String query,SQLiteDatabase db)
    {
        List<wordDefinition> wordList =new ArrayList<>();
        Cursor cursor=db.rawQuery(query,null);
        if(cursor.moveToFirst())
        {
            do
            {
                wordDefinition aWord=new wordDefinition();
                aWord.setId(cursor.getLong(cursor.getColumnIndexOrThrow("ID")));
                aWord.setWord(cursor.getString(cursor.getColumnIndexOrThrow("WORD")));
                aWord.setType(cursor.getString(cursor.getColumnIndexOrThrow("TYPE")));
                aWord.setMeaning(cursor.getString(cursor.getColumnIndexOrThrow("MEANING")));
                aWord.setSentence(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE")));
                aWord.setSynonyms(cursor.getString(cursor.getColumnIndexOrThrow("SYNONYMS")));
                aWord.setAntonyms(cursor.getString(cursor.getColumnIndexOrThrow("ANTONYMS")));
                aWord.setLink(cursor.getString(cursor.getColumnIndexOrThrow("LINK")));
                aWord.setAttr1(cursor.getString(cursor.getColumnIndexOrThrow("ATTR1")));
                aWord.setAttr2(cursor.getString(cursor.getColumnIndexOrThrow("ATTR2")));
                aWord.setLearnt(cursor.getString(cursor.getColumnIndexOrThrow("LEARNT")));
                aWord.setBookmarked(cursor.getString(cursor.getColumnIndexOrThrow("BOOKMARKED")));
                Log.v(MainActivity.TAG,"adding word :\n "+Long.toString(cursor.getLong(cursor.getColumnIndexOrThrow("ID")))
                        +",  "+cursor.getString(cursor.getColumnIndexOrThrow("WORD"))
                        +",  "+cursor.getString(cursor.getColumnIndexOrThrow("TYPE"))
                        +",  "+cursor.getString(cursor.getColumnIndexOrThrow("MEANING"))
                        +",  "+cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE"))
                        +",  "+cursor.getString(cursor.getColumnIndexOrThrow("SYNONYMS"))
                        +",  "+cursor.getString(cursor.getColumnIndexOrThrow("ANTONYMS"))
                        +",  "+cursor.getString(cursor.getColumnIndexOrThrow("LINK"))
                        +",  "+cursor.getString(cursor.getColumnIndexOrThrow("ATTR1"))
                        +",  "+cursor.getString(cursor.getColumnIndexOrThrow("ATTR2"))
                        +",  "+cursor.getString(cursor.getColumnIndexOrThrow("LEARNT"))
                        +",  "+cursor.getString(cursor.getColumnIndexOrThrow("BOOKMARKED"))
                );
                wordList.add(aWord);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return wordList;

    }
   /* public void refreshDB_updated(SQLiteDatabase db)
    {
        Log.v(MainActivity.TAG,"Entering refreshDB");
        Long id;
        String word,type,meaning,sentense,synonyms,antonyms,link,attr1,attr2;
        String word1,type1,meaning1,sentense1,synonyms1,antonyms1,link1,attr11,attr21;
        wordDefinition aWord;
        try {
            String jsonString = "";//getJsonStingUsingAssetManager(assetManager, "WordsList.json");
            Log.v(MainActivity.TAG,"received string :\n"+jsonString);
            JSONObject jRootObject = new JSONObject(jsonString);
            JSONArray jArray= jRootObject.optJSONArray("words");
            for(int i=0;i<jArray.length();++i)
            {
                int progress = (i/jArray.length())/100;
                //progressBar.setProgress(progress);
                //progressText.setText("Initializing data files.. "+progress+"%");
                JSONObject jObject =jArray.getJSONObject(i);
                id=Long.parseLong(jObject.getString("ID"));
                word=jObject.getString("WORD");
                type=jObject.getString("TYPE");
                meaning=jObject.getString("MEANING");
                sentense=jObject.getString("SENTENCE");
                synonyms=jObject.getString("SYNONYMS");
                antonyms=jObject.getString("ANTONYMS");
                link=jObject.getString("LINK");
                attr1=jObject.getString("ATTR1");
                attr2=jObject.getString("ATTR2");

                aWord=this.getAWord(id,db);

                if(aWord!=null) {
                    if ( (!(word.equals(aWord.getWord()))) || (!(type.equals(aWord.getType()))) || (!(meaning.equals(aWord.getMeaning()))) || (!(sentense.equals(aWord.getSentence()))) || (!(synonyms.equals(aWord.getSynonyms())))
                            || (!(antonyms.equals(aWord.getAntonyms()))) || (!(link.equals(aWord.getLink()))) || (!(attr1.equals(aWord.getAttr1()))) || (!(attr2.equals(aWord.getAttr2()))))
                    {
                        word1=word.equals(aWord.getWord())?null:word;
                        type1=type.equals(aWord.getType())?null:type;
                        meaning1=meaning.equals(aWord.getMeaning())?null:meaning;
                        sentense1=sentense.equals(aWord.getSentence())?null:sentense;
                        synonyms1=synonyms.equals(aWord.getSynonyms())?null:synonyms;
                        antonyms1=antonyms.equals(aWord.getAntonyms())?null:antonyms;
                        link1=link.equals(aWord.getLink())?null:link;
                        attr11=attr1.equals(aWord.getAttr1())?null:attr1;
                        attr21=attr2.equals(aWord.getAttr2())?null:attr2;
                        updateDBColumn(id,word1,type1,meaning1,sentense1,synonyms1,antonyms1,link1,attr11,attr21,null,null);

                    }
                }else {

                    this.insertAWord(Long.parseLong(jObject.getString("ID")),
                            jObject.getString("WORD"),
                            jObject.getString("TYPE"),
                            jObject.getString("MEANING"),
                            jObject.getString("SENTENCE"),
                            jObject.getString("SYNONYMS"),
                            jObject.getString("ANTONYMS"),
                            jObject.getString("LINK"),
                            jObject.getString("ATTR1"),
                            jObject.getString("ATTR2")
                    );
                }
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            return;
        }
        Toast.makeText(con, "Words list updated successfully!!", Toast.LENGTH_LONG).show();
        Log.v(MainActivity.TAG,"refresh database success.. exiting refreshDB");
    }*/
    public void refreshOneWord(SQLiteDatabase db, long id, String word, String type, String meaning, String sentence, String synonyms,
                               String antonyms, String link, String attr1, String attr2)
    {
        if(db.isOpen())
        {
            Log.d(MainActivity.TAG,"db This is ope");
        }
        wordDefinition aWord=this.getAWord(id, db); // to check if need to write one attribute or all attributes in db
        String word1,type1,meaning1,sentence1,synonyms1,antonyms1,link1,attr11,attr21,learnt1,bookmarked1;
        if(aWord!=null) {
            if ( (!(word.equals(aWord.getWord()))) || (!(type.equals(aWord.getType()))) || (!(meaning.equals(aWord.getMeaning()))) || (!(sentence.equals(aWord.getSentence()))) || (!(synonyms.equals(aWord.getSynonyms())))
                    || (!(antonyms.equals(aWord.getAntonyms()))) || (!(link.equals(aWord.getLink()))) || (!(attr1.equals(aWord.getAttr1()))) || (!(attr2.equals(aWord.getAttr2()))))
            {
                word1=word.equals(aWord.getWord())?"SKIP_UPDATE":word;
                type1=type.equals(aWord.getType())?"SKIP_UPDATE":type;
                meaning1=meaning.equals(aWord.getMeaning())?"SKIP_UPDATE":meaning;
                sentence1=sentence.equals(aWord.getSentence())?"SKIP_UPDATE":sentence;
                synonyms1=synonyms.equals(aWord.getSynonyms())?"SKIP_UPDATE":synonyms;
                antonyms1=antonyms.equals(aWord.getAntonyms())?"SKIP_UPDATE":antonyms;
                link1=link.equals(aWord.getLink())?"SKIP_UPDATE":link;
                attr11=attr1.equals(aWord.getAttr1())?"SKIP_UPDATE":attr1;
                attr21=attr2.equals(aWord.getAttr2())?"SKIP_UPDATE":attr2;
                updateDBColumn(db, id,word1,type1,meaning1,sentence1,synonyms1,antonyms1,link1,attr11,attr21,aWord.getLearnt(),aWord.getBookmarked());

            }
        }else {

            this.insertAWord(db, id,word,type,meaning,sentence,synonyms,antonyms,link,attr1,attr2);
        }

    }
    public String readWordListIntoJSONString()
    {
        File jFile=new File(con.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"WordsList.json");
        //Uri uri = Uri.parse("android.resource://com.curiousfreaks.grewords/raw/WordsList.json");
        //File jFile = new File(uri.getPath();
        String jString="";
        if(!jFile.exists())
        {
            Log.v(MainActivity.TAG,"json file not exists ..exiting");
            return "";
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(jFile.getAbsolutePath()));
            String line;
            while ((line = br.readLine()) != null) {
                jString+=line;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return jString;
    }
    public String getJsonStingUsingAssetManager(AssetManager assetManager, String fileName)
    {

        String jString="";
        try {
            InputStream is = assetManager.open(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                jString+=line;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return jString;
    }
    public int updateDBColumn(SQLiteDatabase db, long id, String word, String type, String meaning, String sentence, String synonyms,
                              String antonyms, String link, String attr1, String attr2, String learnt, String bookmarked){
        ContentValues cv=new ContentValues();
        String selection="ID = ?";

        if(Long.valueOf(id)==null)
        {
            Log.v(MainActivity.TAG,"id can not be null to update a row");
            return 0;
        }else
            cv.put("ID",Long.toString(id));
        if(!word.equals("SKIP_UPDATE"))
            cv.put("WORD",word);
        if(!type.equals("SKIP_UPDATE"))
            cv.put("TYPE",type);
        if(!meaning.equals("SKIP_UPDATE"))
            cv.put("MEANING",meaning);
        if(!sentence.equals("SKIP_UPDATE"))
            cv.put("SENTENCE",sentence);
        if(!synonyms.equals("SKIP_UPDATE"))
            cv.put("SYNONYMS",synonyms);
        if(!antonyms.equals("SKIP_UPDATE"))
            cv.put("ANTONYMS",antonyms);
        if(!link.equals("SKIP_UPDATE"))
            cv.put("LINK",link);
        if(!attr1.equals("SKIP_UPDATE"))
            cv.put("ATTR1",attr1);
        if(!attr2.equals("SKIP_UPDATE"))
            cv.put("ATTR2",attr2);
        if(!learnt.equals("SKIP_UPDATE"))
            cv.put("LEARNT",learnt);
        if(!bookmarked.equals("SKIP_UPDATE"))
            cv.put("BOOKMARKED",bookmarked);

        String[] selectionArgs={Long.toString(id)};

        return db.update(DatabaseHandler.TABLE_NAME,cv,selection,selectionArgs);
    }

    public int updateLearntOrBookmarked(SQLiteDatabase db, wordDefinition aWord, String learnt, String bookmarked){
        ContentValues cv=new ContentValues();
        String selection="ID = ?";

        if(Long.valueOf(aWord.getId())==null)
        {
            Log.v(MainActivity.TAG,"id can not be null to update a row");
            return 0;
        }else
            cv.put("ID",Long.toString(aWord.getId()));
            cv.put("WORD",aWord.getWord());
            cv.put("TYPE",aWord.getType());
            cv.put("MEANING",aWord.getMeaning());
            cv.put("SENTENCE",aWord.getSentence());
            cv.put("SYNONYMS",aWord.getSynonyms());
            cv.put("ANTONYMS",aWord.getAntonyms());
            cv.put("LINK",aWord.getLink());
            cv.put("ATTR1",aWord.getAttr1());
            cv.put("ATTR2",aWord.getAttr2());
        if(learnt!=null)
            cv.put("LEARNT",learnt);
        if(bookmarked!=null)
            cv.put("BOOKMARKED",bookmarked);

        String[] selectionArgs={Long.toString(aWord.getId())};

        return (db.update(DatabaseHandler.TABLE_NAME,cv,selection,selectionArgs));
    }

    public boolean isAnyLearntNo()
    {
        SQLiteDatabase db= this.getReadableDatabase();
        String query="SELECT * FROM "+DatabaseHandler.TABLE_NAME+" WHERE LEARNT=\"NO\"";
        Cursor cursor = db.rawQuery(query,null);
        if(!cursor.moveToFirst())
        {
            Log.v(MainActivity.TAG,"no more learnt NO");
            return false;
        }
        Log.v(MainActivity.TAG,"not learnt all words");
        cursor.close();
        db.close();
        return true;
    }
    public void executeBlindQuery(String query)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL(query);
        db.close();
    }
    public List<wordDefinition> getBarron333(AssetManager assetManager,SQLiteDatabase db)
    {
        //File barron333File=new File(con.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"Barron333.json");
        List<wordDefinition> tempList=new ArrayList<>();
        String jString = "" , query="SELECT * FROM "+DatabaseHandler.TABLE_NAME+" WHERE ID IN (";
        Long id;
        /*if(!barron333File.exists()) {
            return tempList;
        }*/
        try {
            /*BufferedReader br = new BufferedReader(new FileReader(barron333File.getAbsolutePath()));
            String line;
            while ((line = br.readLine()) != null) {
                jString+=line;
            }*/
            jString = getJsonStingUsingAssetManager(assetManager, "Barron333.json");
            JSONObject jRootObject = new JSONObject(jString);
            JSONArray jArray= jRootObject.optJSONArray("BARRON333");
            for(int i=0;i<jArray.length();++i) {
                JSONObject jObject = jArray.getJSONObject(i);
                id = Long.parseLong(jObject.getString("BARRONID"));
                query+=id;
                if(i!=(jArray.length()-1))
                {
                    query+=", ";
                }

            }
            query+=")";
            return this.getSelectedWords(query,db);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

}
