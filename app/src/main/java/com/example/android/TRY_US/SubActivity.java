package com.example.android.TRY_US;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import okhttp3.internal.Util;

public class SubActivity extends AppCompatActivity {
    InputStream is = null;
    BufferedReader br = null;
    String text = "";
    String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("定型文");
        UtilCommon editable = (UtilCommon)getApplication();
        UtilCommon speakermode = (UtilCommon)getApplication();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //ListViewオブジェクトの取得
        final ListView listView=(ListView)findViewById(R.id.list_view);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1);
        //Adapterのセット
        listView.setAdapter(adapter);

        try {
            try {
                // assetsフォルダ内の sample.txt をオープンする
                is = this.getAssets().open("temp_text.txt");
                br = new BufferedReader(new InputStreamReader(is));

                // １行ずつ読み込み、改行を付加する
                String str;
                while ((str = br.readLine()) != null) {
                    // 要素の追加（1）
                    adapter.add(str);
                }
            } finally {
                if (is != null) is.close();
                if (br != null) br.close();
            }
        } catch (Exception e){
            // エラー発生時の処理
        }
        if (!editable.getGlobal()) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ListView listView = (ListView) parent;
                    msg = (String) listView.getItemAtPosition(position);
                    Intent intent_ret = new Intent();
                    intent_ret.putExtra("RESULT", msg);
                    setResult(RESULT_OK, intent_ret);
                    finish();
                }
            });
        }else if(editable.getGlobal()){
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final ListView listView = (ListView) parent;
                    msg = (String) listView.getItemAtPosition(position);
                    setContentView(R.layout.edit_text);
                    final EditText editText=(EditText) findViewById(R.id.edit_text);
                    editText.setText(msg);
                    editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            boolean handled = false;
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                // ... なんかの処理 ...
                                msg=editText.getText().toString();
                                //int idx=listView.SelectedItems[0].index;
                                listView.getItemAtPosition(0);
                                handled = true;
                                finish();
                            }
                            return handled; // このメソッド中でアクションを消化したら true を返す。
                        }
                    });
                }
            });
        }

        listView.setAdapter(adapter);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}