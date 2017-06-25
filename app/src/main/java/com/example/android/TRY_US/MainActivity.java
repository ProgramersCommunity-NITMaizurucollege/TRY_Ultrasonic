package com.example.android.TRY_US;

import android.content.Intent;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final int SPEECH_RECOG_REQUEST = 42;
    private SpeechRecognizer speechRecognizer;
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
                Thread.sleep(1000);
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
            recently1.setText(StringUtils.join(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION), ','));
            try{
                Thread.sleep(1000);
            }catch (InterruptedException e){}
            startSpeechRecog();
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            speechRecogStatus.setText("リアルタイム結果");
            processResults(partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            speechRecogStatus.setText("onEvent");
        }
    };

    private void processResults(ArrayList<String> speechRecogResults) {
        String finalResult = StringUtils.join(speechRecogResults, ',');
        speechRecogResult.setText("" + finalResult);
    }

    private TextView speechRecogResult;
    private TextView speechRecogStatus;
    private TextView bufferStatus;

    /*private View.OnClickListener startSpeechRecogClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startSpeechRecog();
        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        speechRecogStuff();
    }

    private void speechRecogStuff() {
        speechRecogResult = (TextView) findViewById(R.id.speech_recog_result);
        speechRecogStatus = (TextView) findViewById(R.id.speech_recog_status_text);
        bufferStatus = (TextView) findViewById(R.id.buffer_status);
        //findViewById(R.id.button).setOnClickListener(startSpeechRecogClickListener);
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
                                    .putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                                    .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
                                    .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                                    .putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES,"en-US")
                                    .putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES,"ja-JP");
        //startActivityForResult(recogIntent, SPEECH_RECOG_REQUEST);
        speechRecognizer.startListening(recogIntent);
    }
}
