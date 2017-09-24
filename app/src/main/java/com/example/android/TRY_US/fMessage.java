package com.example.android.TRY_US;

public class fMessage {
    public String author;
    public String content;

    public fMessage(){} //これがないとcom.google.firebase.database.DatabaseException: Class jp.co.crowdworks.fugafugaworks.Message is missing a constructor with no arguments みたいなエラーで落ちる

    public fMessage(String author, String content) {
        this.author = author;
        this.content = content;
    }
}
