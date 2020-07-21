package com.curiousfreaks.greword;

import java.util.Comparator;

/**
 * Created by gasaini on 2/24/2018.
 */

public class wordDefinition {

    private long id;
    private String word;
    private String type;
    private String meaning;
    private String sentence;
    private String synonyms;
    private String antonyms;
    private String link;
    private String attr1;
    private String attr2;
    private String learnt;
    private String bookmarked;

    public wordDefinition()
    {

    }
    public wordDefinition(long id, String word, String type, String meaning, String sentence, String synonyms, String antonyms, String link, String attr1, String attr2, String learnt, String bookmarked)
    {
        this.id=id;
        this.word=word;
        this.type=type;
        this.meaning=meaning;
        this.sentence=sentence;
        this.synonyms=synonyms;
        this.antonyms=antonyms;
        this.link=link;
        this.attr1=attr1;
        this.attr2=attr2;
        this.learnt=learnt;
        this.bookmarked=bookmarked;
    }
    public void copyWord(wordDefinition wd)
    {
        this.id=wd.id;
        this.word=wd.word;
        this.type=wd.type;
        this.meaning=wd.meaning;
        this.sentence=wd.sentence;
        this.synonyms=wd.synonyms;
        this.antonyms=wd.antonyms;
        this.link=wd.link;
        this.attr1=wd.attr1;
        this.attr2=wd.attr2;
        this.learnt=wd.learnt;
        this.bookmarked=wd.bookmarked;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaninng) {
        this.meaning = meaninng;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public String getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(String synonyms) {
        this.synonyms = synonyms;
    }

    public String getAntonyms() {
        return antonyms;
    }

    public void setAntonyms(String antonyms) {
        this.antonyms = antonyms;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getAttr2() {
        return attr2;
    }

    public void setAttr2(String attr2) {
        this.attr2 = attr2;
    }
    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getAttr1() {
        return attr1;
    }

    public void setAttr1(String attr1) {
        this.attr1 = attr1;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLearnt() { return learnt; }

    public void setLearnt(String learnt) { this.learnt = learnt; }

    public String getBookmarked() {  return bookmarked;  }

    public void setBookmarked(String bookmarked) { this.bookmarked = bookmarked; }

    public static Comparator<wordDefinition> alphabeticallyAtoZ=new Comparator<wordDefinition>() {
        @Override
        public int compare(wordDefinition wordDefinition, wordDefinition t1) {
            String word1=wordDefinition.getWord();
            String word2=t1.getWord();
            return word1.compareTo(word2);
        }
    };
    public static Comparator<wordDefinition> alphabeticallyZtoA=new Comparator<wordDefinition>() {
        @Override
        public int compare(wordDefinition wordDefinition, wordDefinition t1) {
            String word1=wordDefinition.getWord();
            String word2=t1.getWord();
            return word2.compareTo(word1);
        }
    };

}
