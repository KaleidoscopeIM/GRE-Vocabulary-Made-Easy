package com.curiousfreaks.greword;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by gasaini on 3/4/2018.
 */

public class FlashCards extends AppCompatActivity {
    wordDefinition cardWord=null;
    int flip = 0,totalWordsCount,learntWords;
    boolean learnFlag=false;
    ArrayList<Integer> randomNumbersList;
    List<wordDefinition> allWords;
    Button gotit,notyet;
    TextView wordOrMeaningText,sentenceText,progressCount,learningType;
    Animation flashCardAnimationNext,flashCardAnimationDisappear;
    AnimatorSet flashCardFlipAnimator90,flashCardFlipAnimator180;
    LinearLayout flashCardMainLayout;
    DatabaseHandler dbHandler;
    ProgressBar flashCardProgressBar;
    float elevation;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor prefEditor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flash_cards);

        dbHandler = new DatabaseHandler(getApplicationContext());
        cardWord = new wordDefinition();
        flashCardAnimationNext= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.flash_card_next);
        flashCardAnimationDisappear= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.flash_card_disappear);
        flashCardFlipAnimator90=(AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(),R.animator.flash_card_flip_90);
        flashCardFlipAnimator180=(AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(),R.animator.flash_card_flip_180);

        gotit = findViewById(R.id.gotit);
        notyet = findViewById(R.id.notyet);
        wordOrMeaningText = findViewById(R.id.wordOrMeaningFC);
        sentenceText=findViewById(R.id.sentenseFC);
        flashCardMainLayout=findViewById(R.id.flashCardMainLayout);
        flashCardProgressBar=findViewById(R.id.flashCardProgressBar);
        progressCount=findViewById(R.id.progressCount);
        learningType = findViewById(R.id.learningType);
        elevation=flashCardMainLayout.getElevation();
        sharedPreferences=getApplicationContext().getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE);
        prefEditor=sharedPreferences.edit();

        initAllWords();
        setProgressBarProgress();

        if(dbHandler.isAnyLearntNo()) {
            refreshCardView();
        }
        else{
           alreadyLearntAllWords();
        }

        wordOrMeaningText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipCard();
            }
        });
        sentenceText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipCard();
            }
        });
        flashCardMainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipCard();
            }
        });
        gotit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SQLiteDatabase db = dbHandler.getWritableDatabase();
                if((dbHandler.updateLearntOrBookmarked(db,cardWord,"YES",null))==1) {
                    Integer currentCardNum=new Integer((int)cardWord.getId());
                    randomNumbersList.remove(currentCardNum);
                    flashCardAnimationDisappear.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            refreshCardView();
                            flashCardProgressBar.incrementProgressBy(1);
                            progressCount.setText("Congrats! You have learnt "+flashCardProgressBar.getProgress()+" words");
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    flashCardMainLayout.startAnimation(flashCardAnimationDisappear);
                    //setAnimationListner(flashCardAnimationDisappear,dbHandler);

                }else
                {
                    Log.v(MainActivity.TAG, "Update db not returned 1 value");
                    Toast.makeText(getApplicationContext(),"Facing issue in saving your progress",Toast.LENGTH_SHORT).show();
                }
                db.close();
            }
        });

        notyet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flashCardAnimationNext.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        refreshCardView();

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                flashCardMainLayout.startAnimation(flashCardAnimationNext);
            }
        });
    }
    public void refreshCardView()
    {
        int randomID;
        wordDefinition aWord=new wordDefinition();
        if(sentenceText.getVisibility()==View.VISIBLE) {
            sentenceText.setVisibility(View.INVISIBLE);
            flip = 0;
        }
        do{
            if(randomNumbersList.isEmpty()) {
                alreadyLearntAllWords();
                return;
            }
            Collections.shuffle(randomNumbersList);

            randomID=randomNumbersList.get(0).intValue();
            if(cardWord!=null || (new Long(cardWord.getId())!=null))
            {
                if(randomID==cardWord.getId() && !(randomNumbersList.size()==1))
                    continue;
            }
            Iterator<wordDefinition> itr=allWords.iterator();
            while(itr.hasNext())
            {
                aWord=itr.next();
                if(randomID==aWord.getId())
                {
                    if(aWord.getLearnt().equals("NO")) {
                        cardWord.copyWord(aWord);
                        wordOrMeaningText.setText(cardWord.getWord());
                        return;
                    }
                    else
                    {
                        randomNumbersList.remove(new Integer(randomID));
                        break;
                    }
                }
            }
        }while(true);
    }
    public void alreadyLearntAllWords()
    {
        learnFlag=true;
        sentenceText.setVisibility(View.INVISIBLE);
        gotit.setVisibility(View.INVISIBLE);
        notyet.setVisibility(View.INVISIBLE);
        if(allWords.isEmpty())
        {
            wordOrMeaningText.setText("You don't have words list!!");

        }else
        {
            wordOrMeaningText.setText("Congrats ! you have learnt all words");
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.flash_card_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
            if(item.getItemId()==R.id.resetCardsFCMenu)
            {
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                builder.setTitle("Alert!!");
                builder.setMessage("Do you want to reset your progress?");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                        learnFlag=false;
                        String Query="UPDATE "+DatabaseHandler.TABLE_NAME+" SET LEARNT=\"NO\"";
                        dbHandler.executeBlindQuery(Query);
                        initAllWords();
                        refreshCardView();
                        setProgressBarProgress();
                        gotit.setVisibility(View.VISIBLE);
                        notyet.setVisibility(View.VISIBLE);
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                        dialogInterface.dismiss();
                    }
                });

                builder.show();
            }
            if(item.getItemId()==R.id.barron333FCMenu)
            {
                prefEditor.putString("ACT_TYPE","BARRON333");
                prefEditor.commit();
                initAllWords();
                setProgressBarProgress();
                boolean flag=true;
                for(wordDefinition wd:allWords)
                {
                    if(wd.getLearnt().equals("NO"))
                    {
                        learnFlag=false;
                        gotit.setVisibility(View.VISIBLE);
                        notyet.setVisibility(View.VISIBLE);
                        refreshCardView();
                        flag=false;
                        break;
                    }
                }
                if(flag)
                {
                    alreadyLearntAllWords();
                }

            }
            if(item.getItemId()==R.id.dictionaryFCMenu)
            {
                prefEditor.putString("ACT_TYPE","ALL_WORDS");
                prefEditor.commit();
                initAllWords();
                setProgressBarProgress();
                if(dbHandler.isAnyLearntNo()) {
                    learnFlag=false;
                    gotit.setVisibility(View.VISIBLE);
                    notyet.setVisibility(View.VISIBLE);
                    refreshCardView();
                }
                else{
                    alreadyLearntAllWords();
                }
            }
        return super.onOptionsItemSelected(item);
    }
    public void flipCard()
    {
        if(learnFlag)
            return;
        int distance=8000;
        float scale = getResources().getDisplayMetrics().density * distance;
        flashCardMainLayout.setCameraDistance(scale);
        flashCardMainLayout.setElevation(0);

        flashCardFlipAnimator90.setTarget(flashCardMainLayout);
        flashCardFlipAnimator90.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                    if (flip == 0) {
                        wordOrMeaningText.setText(cardWord.getMeaning());
                        sentenceText.setVisibility(View.VISIBLE);
                        sentenceText.setText(cardWord.getSentence());
                        flip = 1;
                        animator.removeAllListeners();
                        rotate360();

                    } else {
                        wordOrMeaningText.setText(cardWord.getWord());
                        sentenceText.setVisibility(View.INVISIBLE);
                        flip = 0;
                        animator.removeAllListeners();
                        rotate360();

                    }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        flashCardFlipAnimator90.start();

    }
    public void rotate360()
    {
        flashCardFlipAnimator180.setTarget(flashCardMainLayout);
        flashCardFlipAnimator180.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                    animator.removeAllListeners();
                    flashCardMainLayout.setElevation(elevation);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        flashCardFlipAnimator180.start();
    }
    public void setProgressBarProgress()
    {
        flashCardProgressBar.setMax(totalWordsCount);
        learntWords=0;//(dbHandler.getSelectedWords("SELECT * FROM "+DatabaseHandler.TABLE_NAME+" WHERE LEARNT = \"YES\"")).size();
        for(wordDefinition wd:allWords)
        {
           if(wd.getLearnt().equals("YES"))
               learntWords++;
        }

        /*if(learntWords>totalWordsCount)
            learntWords=totalWordsCount;*/
        flashCardProgressBar.setProgress(learntWords);
        if(learntWords==0)
        {
            progressCount.setText("No progress yet!");
        }else
        {
            //progressCount.setText("Congrats! You have learnt "+learntWords+" words out of "+allWords.size());
            progressCount.setText("Congrats! You have learnt "+learntWords);
        }

    }
    public void initAllWords()
    {
        SQLiteDatabase db =dbHandler.getReadableDatabase();
        if(allWords==null)
            allWords=new ArrayList<>();
        else
            allWords.clear();
        String activityType=sharedPreferences.getString("ACT_TYPE","");
        if(activityType.equals("") || activityType.equals("ALL_WORDS")) {
            allWords = dbHandler.getAllWords(db);
            learningType.setText("800 High frequency words:");
        }
        if(activityType.equals("BARRON333")) {
            allWords = dbHandler.getBarron333(getAssets(),db);
            learningType.setText("333 High frequency words:");
        }

        totalWordsCount=allWords.size();

        if(randomNumbersList==null)
            randomNumbersList=new ArrayList<>();
        else
            randomNumbersList.clear();

        if (randomNumbersList.isEmpty()) {
            for(int i=0;i<totalWordsCount;++i){
                randomNumbersList.add(new Integer((int)allWords.get(i).getId()));
            }
        }
        db.close();
    }
}