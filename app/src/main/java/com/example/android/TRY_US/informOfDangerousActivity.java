package com.example.android.TRY_US;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.DoubleBuffer;
import java.util.*;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.TRY_US.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class informOfDangerousActivity extends Activity {
    boolean start = false;
    boolean dangerousflag = false;
    private ListView lv;

    // サンプリングレート
    int SAMPLING_RATE = 44100;
    // FFTのポイント数
    int FFT_SIZE = 4096;
    private String filename = "data1";
    ArrayList<Double> list = new ArrayList<Double>();
    ArrayList<Double> listrealtime = new ArrayList<Double>();
        // デシベルベースラインの設定
    double dB_baseline = Math.pow(2, 15) * FFT_SIZE * Math.sqrt(2);

    // 分解能の計算
    double resol = ((SAMPLING_RATE / (double) FFT_SIZE));
    AudioRecord audioRec = null;
    boolean bIsRecording = false;
    int bufSize;
    Thread fft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inform_dangerous);
        bufSize = AudioRecord.getMinBufferSize(SAMPLING_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        final ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        adapter.add("data1");
        adapter.add("data2");
        adapter.add("data3");
        adapter.add("data4");
        adapter.add("data5");


        lv = (ListView) findViewById(R.id.listview);
        lv.setAdapter(adapter);

        //リスト項目が選択された時のイベントを追加
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                filename = ("data" + position + ".txt");
                String msg = "data" + (position+1) + "を選択しました。音声登録を行う場合検知音追加ボタンを押してください";
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                FileInput();
            }
        });

        Button returnbtn = (Button) findViewById(R.id.return_button);
        returnbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bIsRecording = false;
                Intent intent = new Intent(getApplication(), StartActivity.class);
                startActivity(intent);
            }
        });

        Button checkbtn = (Button) findViewById(R.id.checkbutton);
        checkbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileInput();
                for(int i = 0; i < list.size(); i++) {
                    Log.d("CHECK",list.get(i).toString());
                }
            }
        });

        Button add_dangerous_sound = (Button) findViewById(R.id.add_dangerous_sound);
        add_dangerous_sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bIsRecording == true) {
                    //offにする
                    String msg = "録音終了します。";
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    start = false;
                    bIsRecording = false;
                    sampleFileOutput();
                }
                else if (bIsRecording == false) {
                    //onにする
                    String msg = "録音開始します。";
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    start = false;
                    StartRecording();
                }
            }
        });

        Button start_btn = (Button) findViewById(R.id.start_btn);
        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(start == false) {
                    //周囲音録音開始
                    start = true;
                    FileInput();
                    StartRecording();
                }
                else if(start == true){
                    bIsRecording = false;
                    start = false;

                }
            }
        });
    }

    protected void StartRecording() {
        // AudioRecordの作成
        audioRec = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufSize * 2);
        audioRec.startRecording();
        bIsRecording = true;

        //フーリエ解析スレッド
        fft = new Thread(new Runnable() {
            @Override
            public void run() {
                byte buf[] = new byte[bufSize * 2];
                while (bIsRecording) {
                    audioRec.read(buf, 0, buf.length);

                    //エンディアン変換
                    ByteBuffer bf = ByteBuffer.wrap(buf);
                    bf.order(ByteOrder.LITTLE_ENDIAN);
                    short[] s = new short[(int) bufSize];
                    for (int i = bf.position(); i < bf.capacity() / 2; i++) {
                        s[i] = bf.getShort();
                    }

                    //FFTクラスの作成と値の引き渡し
                    FFT fft4g = new FFT(FFT_SIZE);
                    double[] FFTdata = new double[FFT_SIZE];
                    for (int i = 0; i < FFT_SIZE / 2; i++) {
                        FFTdata[i] = (double) s[i];
                    }
                    fft4g.rdft(1, FFTdata);

                     //デシベルの計算
                    double[] dbfs = new double[FFT_SIZE / 2];
                    double max_db = -120d;
                    int max_i = 0;
                    for (int i = 0; i < FFT_SIZE; i += 2) {
                        dbfs[i / 2] = (int) (20 * Math.log10(Math.sqrt(Math
                                .pow(FFTdata[i], 2)
                                + Math.pow(FFTdata[i + 1], 2)) / dB_baseline));
                        if (max_db < dbfs[i / 2]) {
                            max_db = dbfs[i / 2];
                            max_i = i / 2;
                        }
                    }

                    //音量が最大の周波数と，その音量を表示
                    Log.d("fft","周波数："+ resol * max_i+" [Hz] 音量：" +  max_db+" [dB]");
                    if(start == false) {
                        list.add(resol * max_i);
                    }
                    else if(start == true){

                        listrealtime.add(resol * max_i);
                        if(list.size() < listrealtime.size()) {
                            CompareArray();
                            if(dangerousflag == false) {
                                listrealtime.remove(0);
                            }
                        }
                    }
                }
                // 録音停止
                audioRec.stop();
                audioRec.release();
            }
        });
        //スレッドのスタート
        fft.start();
}

    private boolean CompareArray(){
        double total = 0;
        for (int i = 0; i < list.size()-1; i++) {
            if(list.get(i)  > 0) {
                total += (((listrealtime.get(i) - list.get(i)) / list.get(i)) * 100);
            }
        }
        dangerousflag = false;
        if(-10 <= total/list.size() && total/list.size() <= 10){
            //スマホを振動させて特定音検知を通知する
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(1000);
            listrealtime.clear();
            bIsRecording = false;

            dangerousflag = true;
        }
        return dangerousflag;
    }
    private void FileInput(){//ファイルからの読出し

        InputStream in;
        String lineBuffer;

        try {
            in = openFileInput(filename);
            in = this.getClass().getClassLoader().getResourceAsStream(filename);
            BufferedReader reader= new BufferedReader(new InputStreamReader(in,"UTF-8"));
            while( (lineBuffer = reader.readLine()) != null ){
                list.add(Double.valueOf(lineBuffer));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sampleFileOutput(String contents){//ファイルへの書き込み

        File temppath = new File(Environment.getExternalStorageDirectory(),"dangeroussound");
        if(temppath .exists() != true){
            temppath.mkdirs();
        }

        File tempfile = new File(temppath,filename);
        FileWriter output = null;

        try{
            output = new FileWriter(tempfile,true);
            output.write(contents);
            output.write("\n");
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }finally{
            if(output != null){
                try{
                    output.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }

        /*
        OutputStream out;
        try {
            out = openFileOutput(filename,MODE_PRIVATE);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out,"UTF-8"));

            for(int i = 0; i < list.size();i++) {
                writer.println((list.get(i).byteValue()));
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }
}