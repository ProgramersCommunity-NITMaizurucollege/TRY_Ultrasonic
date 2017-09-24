package com.example.android.TRY_US;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import okhttp3.internal.Util;

/**
 * Created by fujitayuuya on 2017/09/10.
 */

public class StartActivity extends Activity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startscreen);
        final UtilCommon editable = (UtilCommon)getApplication();
        final UtilCommon talksomeone = (UtilCommon)getApplication();
        ImageButton aloneButton = (ImageButton) findViewById(R.id.gotoTalkAlone);
        aloneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                talksomeone.setGlobal(false);
                Intent intent = new Intent(getApplication(), MainActivity.class);
                startActivity(intent);
            }
        });

        ImageButton someButton = (ImageButton) findViewById(R.id.gotoTalkSomeone);
        someButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                talksomeone.setGlobal(true);
                Intent intent = new Intent(getApplication(), MainActivity.class);
                startActivity(intent);
            }
        });
        Button inform = (Button)findViewById(R.id.informOfDangerous);
        inform.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplication(),informOfDangerousActivity.class);
                startActivity(intent);
            }
        });

        final FabSpeedDial fabSpeedDial = (FabSpeedDial) findViewById(R.id.floatingActionButton);
        fabSpeedDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                //TODO: Start some activity
                switch (menuItem.getItemId()){
                    case R.id.action_library:
                        editable.setGlobal(true);
                        Intent intent = new Intent(getApplication(), SubActivity.class);
                        startActivityForResult(intent, 1000);
                        break;
                    case R.id.action_help:
                        break;
                    default:
                        return super.onMenuItemSelected(menuItem);
                }
                return false;
            }
        });
    }
}