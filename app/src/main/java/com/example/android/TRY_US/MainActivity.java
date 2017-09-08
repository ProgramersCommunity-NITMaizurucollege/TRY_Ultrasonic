package com.example.android.TRY_US;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;

import static android.R.attr.id;
import static android.R.id.list;
import static com.example.android.TRY_US.R.id.text;


public class MainActivity extends ListActivity
        implements View.OnClickListener, TextToSpeech.OnInitListener, OnCheckedChangeListener{
    InputStream is = null;
    BufferedReader br = null;
    String text = "";
    ListView listView;
    ArrayAdapter<String> adapter;
    ArrayAdapter<String> adapter_temp_sentence;
    public static final int SPEECH_RECOG_REQUEST = 42;
   // TextView fftText;
    EditText editText;
    int SAMPLING_RATE = 44100;
    // FFTのポイント数
    int FFT_SIZE = 4096;
    private TextToSpeech tts;
    double freq;
    double vol;
    boolean fftBool=false;
    AudioManager mAudioManager;
    double dB_baseline = Math.pow(2, 15) * FFT_SIZE * Math.sqrt(2);

    // 分解能の計算
    double resol = ((SAMPLING_RATE / (double) FFT_SIZE));
    AudioRecord audioRec = null;
    boolean bIsRecording = false;
    int bufSize;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ArrayAdapterオブジェクト生成
        adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        adapter_temp_sentence = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        listView = (ListView)findViewById(R.id.list_temp_sentence);
        //Buttonオブジェクト取得

        //ListAdapterセット
        setListAdapter(adapter);
        // ここで1秒間スリープし、スプラッシュを表示させたままにする。
            try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        setContentView(R.layout.activity_main);

        //final TextView textView = (TextView)findViewById(R.id.FFTtext);

        //textView.setText("fft" + "周波数："+ String.valueOf(freq) + " [Hz] 音量：" + String.valueOf(vol));
        speechRecogStuff();
        tts = new TextToSpeech(this, this);
        bufSize = AudioRecord.getMinBufferSize(SAMPLING_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        findViewById(R.id.button_tts).setOnClickListener(this);
        findViewById(R.id.button).setOnClickListener(this);

        //チェックボックス設定
        final CheckBox checkBox = (CheckBox)findViewById(R.id.internal_speaker_checkbox);
        //デフォルト:未チェック
        checkBox.setChecked(false);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check = checkBox.isChecked();
                if (check){
                    //チェックされている場合
                    AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    am.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    am.setSpeakerphoneOn(true);
                }else {
                    //チェックされていない場合
                }
            }
        });


        editText = (EditText) findViewById(R.id.edit_text);
        editText.setBackgroundColor(Color.WHITE);
        //fftText = (TextView) findViewById(R.id.FFTtext);
        // AudioRecordの作成
        audioRec = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufSize * 2);
    }


    @Override
    public void onClick(View v) {
        if (v != null) {
            switch (v.getId()) {
                case R.id.button:
                    tempSentence();
                    // クリック処理
                    break;

                case R.id.button_tts:
                    speechText();
                    // クリック処理
                    break;

                default:
                    break;
            }
        }
    }

    private void tempSentence(){
        try {
            try {
                // assetsフォルダ内の sample.txt をオープンする
                is = this.getAssets().open("temp_text.txt");
                br = new BufferedReader(new InputStreamReader(is));

                // １行ずつ読み込み、改行を付加する
                String str;
                while ((str = br.readLine()) != null) {
                    adapter_temp_sentence.add(str);
                    listView.setAdapter(adapter_temp_sentence);
                }
            } finally {
                if (is != null) is.close();
                if (br != null) br.close();
            }
        } catch (Exception e){
            // エラー発生時の処理
        }

// 読み込んだ文字列を EditText に設定し、画面に表示する

    }

    private void writeContents(String contents) {
        File temppath = new File(Environment.getExternalStorageDirectory(), "temp");
        if (temppath.exists() != true){
            temppath.mkdir();
        }
        File tempfile = new File(temppath,"test.txt");
        FileWriter output = null;
        try{
            output = new FileWriter(tempfile,true);
            output.write(contents);
            output.write("\n");
        }
        catch (IOException e){
            e.printStackTrace();
        }
        finally {
            if(output != null){
                try{
                    output.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onInit(int status) {
        // TTS初期化
        if (TextToSpeech.SUCCESS == status) {
            //  Log.d(, "initialized");
        } else {
            //   Log.e(TAG, "faile to initialize");
        }
    }


    private void shutDown(){
        if (null != tts) {
            // to release the resource of TextToSpeech
            tts.shutdown();
        }
    }


    private void speechText() {
        EditText editor = (EditText)findViewById(R.id.edit_text);
        editor.selectAll();
        // EditTextからテキストを取得
        String string = editor.getText().toString();
        writeContents(editor.getText().toString());
        adapter.add(editor.getText().toString());
        if (0 < string.length()) {
            if (tts.isSpeaking()) {
                tts.stop();
                return;
            }
            setSpeechRate(1.0f);
            setSpeechPitch(1.0f);

            // tts.speak(text, TextToSpeech.QUEUE_FLUSH, null) に
            // KEY_PARAM_UTTERANCE_ID を HasMap で設定
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"messageID");

            tts.speak(string, TextToSpeech.QUEUE_FLUSH, map);
            setTtsListener();

        }
    }



    // 読み上げのスピード
    private void setSpeechRate(float rate){
        if (null != tts) {
            tts.setSpeechRate(rate);
        }
    }


    // 読み上げのピッチ
    private void setSpeechPitch(float pitch){
        if (null != tts) {
            tts.setPitch(pitch);
        }
    }


    // 読み上げの始まりと終わりを取得
    private void setTtsListener(){
        // android version more than 15th
        // 市場でのシェアが15未満は数パーセントなので除外
        if (Build.VERSION.SDK_INT >= 15)
        {
            int listenerResult = tts.setOnUtteranceProgressListener(new UtteranceProgressListener()
            {
                @Override
                public void onDone(String utteranceId)
                {
                    //     Log.d(TAG,"progress on Done " + utteranceId);
                }

                @Override
                public void onError(String utteranceId)
                {
                    //   Log.d(TAG,"progress on Error " + utteranceId);
                }

                @Override
                public void onStart(String utteranceId)
                {
                    // Log.d(TAG,"progress on Start " + utteranceId);
                }

            });
            if (listenerResult != TextToSpeech.SUCCESS)
            {
                //   Log.e(TAG, "failed to add utterance progress listener");
            }
        }
        else {
            // Log.e(TAG, "Build VERSION is less than API 15");
        }
    }


    private android.speech.SpeechRecognizer speechRecognizer;
    private RecognitionListener myListener = new RecognitionListener() {
        public int bufferCounter = 0;


        @Override
        public void onReadyForSpeech(Bundle params) {
            speechRecogStatus.setText("録音開始");
        }

        @Override
        public void onBeginningOfSpeech() {
            speechRecogStatus.setText("認識中");
            bufferCounter = 0;
        }

        @Override
        public void onRmsChanged(float rmsdB) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            bufferCounter++;
//            speechRecogStatus.setText("onBufferReceived() - " + bufferCounter);
        }

        @Override
        public void onEndOfSpeech() {
            speechRecogStatus.setText("認識終了");
        }

        @Override
        public void onError(int error) {
            String errorString = getErrorString(error);
            speechRecogStatus.setText("エラー = " + errorString);
            try{
                Thread.sleep(100);
            }catch (InterruptedException e){}
            startSpeechRecog();
        }

        private String getErrorString(int error) {
            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    return "オーディオエラー";
                case SpeechRecognizer.ERROR_CLIENT:
                    return "クライアントエラー";
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    return "権限が必要";
                case SpeechRecognizer.ERROR_NETWORK:
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                case SpeechRecognizer.ERROR_SERVER:
                    return "サーバー/ネットワークエラー";
                case SpeechRecognizer.ERROR_NO_MATCH:
                    return "マッチなし";
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    return "apparently, recognizer busy";
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    return "タイムアウト";
            }
            return "";
        }

        @Override
        public void onResults(Bundle results) {
            speechRecogStatus.setText("結果");
            String finalResult = StringUtils.join(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION), ',');
            speechRecogResult.setText("" + finalResult);
            ListView listView=(ListView) findViewById(list);
            adapter.add(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0));
            writeContents(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0));
            try{
                Thread.sleep(100);
            }catch (InterruptedException e){}
            startSpeechRecog();
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            processResults(partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            speechRecogStatus.setText("onEvent");
        }
    };


    private void processResults(ArrayList<String> speechRecogResults) {
        String finalResult = StringUtils.join(speechRecogResults, ',');
        speechRecogResult.setText(speechRecogResults.get(0));
    }


    private TextView speechRecogResult;
    private TextView speechRecogStatus;


    @Override
    protected void onDestroy(){
        super.onDestroy();
        shutDown();
    }



    private void speechRecogStuff() {
        speechRecogResult = (TextView) findViewById(R.id.speech_recog_result);
        speechRecogStatus = (TextView) findViewById(R.id.speech_recog_status_text);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(myListener);
        if (fftBool==false) {
            startSpeechRecog();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SPEECH_RECOG_REQUEST:
                if(resultCode == RESULT_OK) {
                    processResults(data.getExtras().getStringArrayList(RecognizerIntent.EXTRA_RESULTS));
                }
                break;
        }
    }


    private void startSpeechRecog() {
        speechRecogResult.setText(null);
        Intent recogIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
                .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                .putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES,"en-US")
                .putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES,"ja-JP");
        //startActivityForResult(recogIntent, SPEECH_RECOG_REQUEST);
        speechRecognizer.startListening(recogIntent);
    }


    public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked){
        if(isChecked==true){
            fftBool=true;
            speechRecognizer.cancel();
            bIsRecording = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(isChecked==true) {

                        // マルチスレッドにしたい処理 ここから

                        audioRec.startRecording();
                        // 画面に描画する処理
                        byte buf[] = new byte[bufSize * 2];
                        while (bIsRecording) {
                            //1秒ディレイ
                            try{
                                Thread.sleep(1000);
                            }catch (InterruptedException e){}

                            audioRec.read(buf, 0, buf.length);

                            //エンディアン変換
                            ByteBuffer bf = ByteBuffer.wrap(buf);
                            bf.order(ByteOrder.LITTLE_ENDIAN);
                            short[] s = new short[(int) bufSize * 2];
                            for (int i = bf.position(); i < bf.capacity() / 2; i++) {
                                s[i] = bf.getShort();
                            }
                            //FFTクラスの作成と値の引き渡し
                            FFT fft = new FFT(FFT_SIZE);
                            double[] FFTdata = new double[FFT_SIZE];
                            for (int i = 0; i < FFT_SIZE; i++) {
                                FFTdata[i] = (double) s[i];
                            }
                            fft.rdft(1, FFTdata);

                            // デシベルの計算
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

                            //Log.d("fft", "周波数：" + resol * max_i + " [Hz] 音量：" + max_db + " [dB]");
                          /*  runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    fftText.setText("周波数：" + resol * freq + " [Hz] 音量：" + vol + " [dB]");
                                }
                            });*/
                            freq = resol * max_i;
                            vol = max_db;
                        }
                        // 録音停止
                        audioRec.stop();
                        audioRec.release();
                        //   }
                        //        });
                        // マルチスレッドにしたい処理 ここまで
                    }
                }
            }).start();
        } else{
            // 録音停止
            audioRec.stop();
            audioRec.release();
            fftBool=false;
            Log.d("fftChecked","isFALSE");

            speechRecogStuff();
        }
    }


    Handler handler = new Handler();
    @Override
    protected void onResume() {
        super.onResume();
        speechRecognizer.destroy();
        speechRecogStuff();
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}