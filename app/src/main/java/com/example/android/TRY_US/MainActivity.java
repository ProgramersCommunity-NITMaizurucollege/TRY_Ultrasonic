package com.example.android.TRY_US;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.bassaer.chatmessageview.models.Message;
import com.github.bassaer.chatmessageview.models.User;
import com.github.bassaer.chatmessageview.views.ChatView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import okhttp3.internal.Util;

import static android.R.id.content;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, TextToSpeech.OnInitListener, OnCheckedChangeListener, GoogleApiClient.OnConnectionFailedListener {
    InputStream is = null;
    BufferedReader br = null;
    String text = "";
    ListView listView;
    String res_sub;
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
    boolean fftBool = false;
    AudioManager mAudioManager;
    private int audioLevel = 0;
    private ChatView mChatView;

    private static final String TAG = "MainActivity";
    public static final String MESSAGES_CHILD = "messages";
    private static final int REQUEST_INVITE = 1;
    private static final int REQUEST_IMAGE = 2;
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 10;
    public static final String ANONYMOUS = "me";
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    private static final String MESSAGE_URL = "https://try-us-28793.firebaseio.com/message/";
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";

    private String mUsername;
    private String mYourname;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;
    boolean mSomeoneTalk;

    private Button mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private FirebaseAnalytics mFirebaseAnalytics;
    private EditText mMessageEditText;
    private ImageView mAddMessageImageView;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mFirebaseAuth;

    private FirebaseAuth.AuthStateListener mAuthListener;
    double dB_baseline = Math.pow(2, 15) * FFT_SIZE * Math.sqrt(2);
    private static final String MESSAGE_STORE = "message";
    // 分解能の計算
    double resol = ((SAMPLING_RATE / (double) FFT_SIZE));
    AudioRecord audioRec = null;
    boolean bIsRecording = false;
    boolean sendText=true;
    int bufSize;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final UtilCommon talksomeone = (UtilCommon)getApplication();
        setContentView(R.layout.activity_main);
        //ArrayAdapterオブジェクト生成
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        adapter_temp_sentence = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        //listView = (ListView)findViewById(R.id.list_temp_sentence);
        //Buttonオブジェクト取得
        // ここで1秒間スリープし、スプラッシュを表示させたままにする。
        //mFirebaseAuth.signOut();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        setContentView(R.layout.activity_main);
        mUsername = ANONYMOUS;
        //Googleサインイン
        if (talksomeone.getGlobal()) {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            // Initialize Firebase Auth
            mFirebaseAuth = FirebaseAuth.getInstance();
            mFirebaseUser = mFirebaseAuth.getCurrentUser();

            if (mFirebaseUser == null) {
                // Not signed in, launch the Sign In activity
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                return;
            } else {
                mUsername = mFirebaseUser.getDisplayName();
                if (mFirebaseUser.getPhotoUrl() != null) {
                    mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
                }
            }

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API)
                    .build();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        toolbar.setBackgroundColor(ContextCompat.getColor(this,R.color.gray200));
        toolbar.setTitleTextColor(Color.BLACK);
        toolbar.setTitle("Chat");
        setSupportActionBar(toolbar);
        //final TextView textView = (TextView)findViewById(R.id.FFTtext);
        final UtilCommon editable = (UtilCommon)getApplication();
        //textView.setText("fft" + "周波数："+ String.valueOf(freq) + " [Hz] 音量：" + String.valueOf(vol));
        if (!talksomeone.getGlobal()) {
            speechRecogStuff();
        }else {
            displayDialog();
            Firebase.setAndroidContext(this);
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myref = database.getReference("message");
            myref.addChildEventListener(new com.google.firebase.database.ChildEventListener() {
                @Override
                public void onChildAdded(com.google.firebase.database.DataSnapshot dataSnapshot, String s) {
                    String data = dataSnapshot.child("content").getValue().toString();
                    //Log.e("message", data);
                    if (data!=null) {
                        Random rnd = new Random();
                        int yourId = rnd.nextInt(10)+1;
                        Bitmap yourIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_you);
                        mYourname = dataSnapshot.child("author").getValue().toString();
                        if (!mUsername.equals(mYourname)) {
                            adapter.add(data);
                            final User you = new User(yourId, mYourname, yourIcon);
                            final Message receivedMessage = new Message.Builder()
                                    .setUser(you)
                                    .setRightMessage(false)
                                    .setMessageText(data)
                                    .build();
                            mChatView.receive(receivedMessage);
                        }else if (mUsername.equals(mYourname)){
                            //User id
                            int myId = 0;
                            //User icon
                            Bitmap myIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_mine);
                            final User me = new User(myId, mUsername, myIcon);
                            Message message = new Message.Builder()
                                    .setUser(me)
                                    .setRightMessage(true)
                                    .setMessageText(data)
                                    .hideIcon(false)
                                    .build();
                            //Set to chat view
                            mChatView.send(message);
                            sendText=false;
                        }
                    }
                }

                @Override
                public void onChildChanged(com.google.firebase.database.DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(com.google.firebase.database.DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(com.google.firebase.database.DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        //User id
        int myId = 0;
        //User icon
        Bitmap myIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_mine);
        //User name

        final User me = new User(myId, mUsername, myIcon);
        int yourId = 1;
        Bitmap yourIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_you);
        final User you = new User(yourId, mYourname, yourIcon);
        mChatView = (ChatView) findViewById(R.id.chat_view);

        //Set UI parameters if you need
        mChatView.setRightBubbleColor(ContextCompat.getColor(this, R.color.green500));
        mChatView.setLeftBubbleColor(ContextCompat.getColor(this,R.color.gray200));
        mChatView.setBackgroundResource(R.drawable.sky);
        mChatView.setSendButtonColor(ContextCompat.getColor(this, R.color.teal500));
        mChatView.setSendIcon(R.drawable.ic_action_send);
        mChatView.setRightMessageTextColor(Color.WHITE);
        mChatView.setLeftMessageTextColor(Color.BLACK);
        mChatView.setUsernameTextColor(Color.BLACK);
        mChatView.setSendTimeTextColor(Color.BLACK);
        mChatView.setDateSeparatorColor(Color.BLACK);
        mChatView.setInputTextHint("新規メッセージ");
        mChatView.setMessageMarginTop(10);
        mChatView.setMessageMarginBottom(5);

        mChatView.setOnClickSendButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new message
                if (talksomeone.getGlobal()) {
                    Message message = new Message.Builder()
                            .setUser(me)
                            .setRightMessage(true)
                            .setMessageText(mChatView.getInputText())
                            .hideIcon(false)
                            .build();
                    //Set to chat view
                    sendMessage(mChatView.getInputText());
                }else{
                    Message message = new Message.Builder()
                            .setUser(me)
                            .setRightMessage(true)
                            .setMessageText(mChatView.getInputText())
                            .hideIcon(false)
                            .build();
                    writeContents(mChatView.getInputText());
                    mChatView.send(message);
                }
                /*FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                getMessageRef().push().setValue(new fMessage(user.getUid(), mChatView.getInputText())).continueWith(new Continuation<Void, Object>() {
                    @Override
                    public Object then(@NonNull Task<Void> task) throws Exception {
                        if (!task.isSuccessful()) {
                            Log.e("FugaFugaWorks","error", task.getException());
                            return null;
                        }
                        return null;
                    }
                });*/
                //speechText();
                if (!talksomeone.getGlobal()) {
                    new VoiceText().execute(mChatView.getInputText());
                    AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    if (audioManager.isMusicActive()) {
                        // 再生中!!
                        speechRecognizer.destroy();
                    }
                }
                //Reset edit text
                mChatView.setInputText("");
            }

        });
        mChatView.setOnClickOptionButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editable.setGlobal(false);
                Intent intent = new Intent(getApplication(), SubActivity.class);
                startActivityForResult(intent, 1000);
            }
        });
    }

    private void displayDialog() {
        // AlertDialog.Builder でダイアログを作成
        AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        // ダイアログのタイトルをセット
        dlg.setTitle("Info");
        // ダイアログのメッセージをセット
        dlg.setMessage("文字入力のみで会話しますか?");

        // AlertDialog.Builder#setPositiveButton
        // OK とか YES とか。左に配置される
        dlg.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //YES
            }
        });

        // AlertDialog.Builder#setNegativeButton
        // No とか。右に配置される
        dlg.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //NO
                speechRecogStuff();
            }
        });

        // これを忘れるとダイアログは表示されないよ
        dlg.show();
    }

    private DatabaseReference getMessageRef() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        return database.getReference(MESSAGE_STORE);
    }
    private void sendMessage(String content) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        getMessageRef().push().setValue(new fMessage(user.getDisplayName(), content)).continueWith(new Continuation<Void, Object>() {
            @Override
            public Object then(@NonNull Task<Void> task) throws Exception {
                if (!task.isSuccessful()) {
                    Log.e("FugaFugaWorks","error", task.getException());
                    return null;
                }

                return null;
            }
        });
    }
    private void writeContents(String contents) {
        File temppath = new File(Environment.getExternalStorageDirectory(), "temp");
        if (!temppath.exists()) {
            temppath.mkdir();
        }
        File tempfile = new File(temppath, "test.txt");
        FileWriter output = null;
        try {
            output = new FileWriter(tempfile, true);
            output.write(contents);
            output.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
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


    private void shutDown() {
        if (null != tts) {
            // to release the resource of TextToSpeech
            tts.shutdown();
        }
    }


    // 読み上げのスピード
    private void setSpeechRate(float rate) {
        if (null != tts) {
            tts.setSpeechRate(rate);
        }
    }


    // 読み上げのピッチ
    private void setSpeechPitch(float pitch) {
        if (null != tts) {
            tts.setPitch(pitch);
        }
    }


    public android.speech.SpeechRecognizer speechRecognizer;
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
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
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
            //mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, audioLevel, 0);
            speechRecogStatus.setText("結果");
            ArrayList<String> values = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String finalResult = StringUtils.join(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION), ',');
            speechRecogResult.setText("" + finalResult);
            final UtilCommon talksomeone = (UtilCommon)getApplication();

            if (talksomeone.getGlobal()) {
                //User id
                int myId = 0;
                //User icon
                Bitmap myIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_mine);
                //User name
                final User me = new User(myId, mUsername, myIcon);
                final Message receivedMessage = new Message.Builder()
                        .setUser(me)
                        .setRightMessage(true)
                        .hideIcon(false)
                        .setMessageText(values.get(0))
                        .build();
                //mChatView.send(receivedMessage);
                sendMessage(values.get(0));
            }else {
                //Receive message
                int yourId = 1;
                Bitmap yourIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_you);
                mYourname = "You";
                final User you = new User(yourId, mYourname, yourIcon);
                final Message receivedMessage = new Message.Builder()
                        .setUser(you)
                        .setRightMessage(false)
                        .setMessageText(values.get(0))
                        .build();
                mChatView.receive(receivedMessage);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
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
    protected void onDestroy() {
        super.onDestroy();
        shutDown();
    }


    private void speechRecogStuff() {
        speechRecogResult = (TextView) findViewById(R.id.speech_recog_result);
        speechRecogStatus = (TextView) findViewById(R.id.speech_recog_status_text);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(myListener);
        if (fftBool == false) {
            startSpeechRecog();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SPEECH_RECOG_REQUEST:
                if (resultCode == RESULT_OK) {
                    processResults(data.getExtras().getStringArrayList(RecognizerIntent.EXTRA_RESULTS));
                }
                break;

            case 1000:
                if(resultCode == RESULT_OK) {
                    res_sub = data.getStringExtra("RESULT");
                    mChatView.setInputText(res_sub);
                }
                break;
        }
    }



    private void startSpeechRecog() {
        final UtilCommon talksomeone = (UtilCommon)getApplication();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //audioLevel = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        //mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
        if (talksomeone.getGlobal()) {
            mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            mAudioManager.startBluetoothSco();
        }else{
            mAudioManager.setMode(AudioManager.MODE_NORMAL);
            mAudioManager.stopBluetoothSco();
        }
        speechRecogResult.setText(null);
        Intent recogIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
                .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                .putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES,"en-US")
                .putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES,"ja-JP");
        //startActivityForResult(recogIntent, SPEECH_RECOG_REQUEST);
        speechRecognizer.startListening(recogIntent);
    }

    public void onReceive(Context context, Intent intent) {
        if (AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED.equals(intent.getAction())) {
            int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_ERROR);
            if (state == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
                Intent recogIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                        .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
                        .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                        .putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES,"en-US")
                        .putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES,"ja-JP");
                //startActivityForResult(recogIntent, SPEECH_RECOG_REQUEST);
                speechRecognizer.startListening(recogIntent);
            }
        }
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

                            Log.d("fft", "周波数：" + resol * max_i + " [Hz] 音量：" + max_db + " [dB]");
                            freq = resol * max_i;
                            vol = max_db;
                        }
                        // 録音停止
                        audioRec.stop();
                        audioRec.release();
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

        if(speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecogStuff();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        final UtilCommon speakermode = (UtilCommon)getApplication();
        if (id == R.id.action_checkbox) {
            // チェックボックスの状態変更を行う
            item.setChecked(!item.isChecked());
            // 反映後の状態を取得する
            boolean check = item.isChecked();
            if (check) {
                //チェックされている場合
                speakermode.setGlobal(true);
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.setMode(AudioManager.MODE_IN_COMMUNICATION);
                am.setSpeakerphoneOn(true);
                return true;
            } else {
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.setMode(AudioManager.MODE_IN_COMMUNICATION);
                am.setSpeakerphoneOn(false);
                //チェックされていない場合
            }
        }
        if(id==R.id.sign_out_menu){
            final UtilCommon talksomeone = (UtilCommon)getApplication();
            if (talksomeone.getGlobal()) {
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mFirebaseUser = null;
                mUsername = ANONYMOUS;
                mPhotoUrl = null;
                startActivity(new Intent(this, SignInActivity.class));
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}