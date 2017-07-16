package com.example.android.TRY_US;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import com.example.android.TRY_US.FFT;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, TextToSpeech.OnInitListener{

    public static final int SPEECH_RECOG_REQUEST = 42;
  //  Handler handler= new Handler();
    TextView textView;
    EditText editText;
    int SAMPLING_RATE = 44100;
    // FFTのポイント数
    int FFT_SIZE = 4096;
    private TextToSpeech tts;
    double syuhasu;
    double onryou;


    double dB_baseline = Math.pow(2, 15) * FFT_SIZE * Math.sqrt(2);

    // 分解能の計算
    double resol = ((SAMPLING_RATE / (double) FFT_SIZE));
    int max_i;
    int max_db;
    AudioRecord audioRec = null;
    boolean bIsRecording = false;
    int bufSize;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView textView = (TextView)findViewById(R.id.FFTtext);


        textView.setText("fft" + "周波数："+ String.valueOf(syuhasu) + " [Hz] 音量：" + String.valueOf(onryou));
        speechRecogStuff();
        tts = new TextToSpeech(this, this);
        bufSize = AudioRecord.getMinBufferSize(SAMPLING_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        Button ttsButton = (Button)findViewById(R.id.button_tts);
        ttsButton.setOnClickListener(this);

        Button buttonlisten = (Button)findViewById(R.id.button_write);
        buttonlisten.setOnClickListener(this);

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
        // AudioRecordの作成
        audioRec = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufSize * 2);
        audioRec.startRecording();
        bIsRecording = true;




        editText = (EditText) findViewById(R.id.edit_text);

    }



/*
    public void onClickButton(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // マルチスレッドにしたい処理 ここから

                final String result = getMessage(); // 何かの処理
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        msgView.setText(result); // 画面に描画する処理
                    }
                });

                // マルチスレッドにしたい処理 ここまで
            }
        }).start();
    }
*/
    @Override
    public void onInit(int status) {
        // TTS初期化
        if (TextToSpeech.SUCCESS == status) {
          //  Log.d(, "initialized");
        } else {
         //   Log.e(TAG, "faile to initialize");
        }
    }

    @Override
    public void onClick(View v) {
        speechText();
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
                bufferStatus.setText("counter = " + bufferCounter);
            }


            @Override
            public void onError(int error) {
                String errorString = getErrorString(error);
                speechRecogStatus.setText("エラー = " + errorString);
                try{
                    Thread.sleep(2000);
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
                TextView recently1 = (TextView) findViewById(R.id.recently_speech_recog_result1);
                TextView recently2 = (TextView) findViewById(R.id.recently_speech_recog_result2);
                TextView recently3 = (TextView) findViewById(R.id.recently_speech_recog_result3);
                recently3.setText(recently2.getText());
                recently2.setText(recently1.getText());
                recently1.setText(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0));
                try{
                    Thread.sleep(2000);
                }catch (InterruptedException e){}
                startSpeechRecog();
            }

            /*@Override
            public void onPartialResults(Bundle partialResults) {
                speechRecogStatus.setText("リアルタイム結果");
                processResults(partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));
            }*/
            @Override
            public void onPartialResults(Bundle partialResults) {
                processResults(partialResults.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION));
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
    private TextView bufferStatus;

    @Override
    protected void onDestroy(){
        super.onDestroy();
       // shutDown();
    }
        //unregisterReceiver(broadcastReceiver);


    private void speechRecogStuff() {
        speechRecogResult = (TextView) findViewById(R.id.speech_recog_result);
        speechRecogStatus = (TextView) findViewById(R.id.speech_recog_status_text);
        bufferStatus = (TextView) findViewById(R.id.buffer_status);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(myListener);
        startSpeechRecog();
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
        bufferStatus.setText(null);
        Intent recogIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
                .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                .putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES,"en-US")
                .putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES,"ja-JP");
        //startActivityForResult(recogIntent, SPEECH_RECOG_REQUEST);

        speechRecognizer.startListening(recogIntent);
    }


    Handler handler = new Handler();
    @Override
    protected void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // マルチスレッドにしたい処理 ここから
                handler.post(new Runnable() {

                @Override
                        public void run(){
                // final String result = getMessage(); // 何かの処理


                        //       @Override

                        //   public void run() {
                        // 画面に描画する処理
                        byte buf[] = new byte[bufSize * 2];
                while(bIsRecording)

                        {
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


                            Log.d("fft", "周波数：" + resol * max_i + " [Hz] 音量：" + max_db + " [dB]");

                            syuhasu = resol * max_i;
                            onryou = max_db;

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

    };

}