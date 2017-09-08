package com.example.android.TRY_US;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SubActivity extends Activity {
    InputStream is = null;
    BufferedReader br = null;
    String text = "";
    String msg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView)parent;
                msg = (String)listView.getItemAtPosition(position);
            }
        });

        listView.setAdapter(adapter);
        Button returnButton = (Button) findViewById(R.id.return_button);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_ret = new Intent();
                intent_ret.putExtra("RESULT", msg);
                setResult(RESULT_OK, intent_ret);
                finish();
            }
        });
    }
}