//VoiceText.java

package com.example.android.TRY_US;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import android.os.AsyncTask;

import okhttp3.*;

import java.io.IOException;

public class VoiceText extends AsyncTask<String,Void,Void> {
    OkHttpClient client = new OkHttpClient();
    @Override
    protected Void doInBackground(String... params) {
        String credential = Credentials.basic("bqt2v2g43kjz1p76", "");
        
        RequestBody requestBody = new FormBody.Builder()
                .add("text", String.valueOf(params[0]))
                .add("speaker","hikari") //パラメーターはここでいじる
                .add("speed","100")
                .add("pitch","80")
                .add("volume","100")
                .build();
        Request request = new Request.Builder() //ここにBASE64にエンコードしたAPIキーを書く
                .header("Authorization", credential)
                .url("https://api.voicetext.jp/v1/tts")
                .post(requestBody)
                .build();
        try
        {
            Response response = client.newCall(request).execute();
            byte[] hoge = response.body().bytes();
            // バッファサイズを取得
            int bufSize = AudioTrack.getMinBufferSize(44000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            // AudioTrackインスタンスを生成
            AudioTrack audioTrack = new AudioTrack(AudioManager.MODE_IN_COMMUNICATION, 44000, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufSize, AudioTrack.MODE_STREAM);
            // 再生
            audioTrack.play();
            audioTrack.write(hoge,0, hoge.length);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}