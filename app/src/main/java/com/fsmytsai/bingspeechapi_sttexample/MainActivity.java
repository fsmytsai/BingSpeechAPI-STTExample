package com.fsmytsai.bingspeechapi_sttexample;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.microsoft.bing.speech.SpeechClientStatus;
import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionResult;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionMode;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;
import static android.Manifest.permission.RECORD_AUDIO;

public class MainActivity extends AppCompatActivity implements ISpeechRecognitionServerEvents {

    private MicrophoneRecognitionClient micClient = null;
    private EditText tv_Result;
    private Button bt_Start;
    private String oldText = "";

    //Return APIKey
    public String getPrimaryKey() {
        return this.getString(R.string.primaryKey);
    }

    //Return Locale
    private String getDefaultLocale() {
        return "zh-tw";
    }

    //Return AuthenticationUri
    private String getAuthenticationUri() {
        return this.getString(R.string.authenticationUri);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //文字顯示的 EditText
        this.tv_Result = (EditText) findViewById(R.id.tv_Result);

        //開始說話的按鈕
        this.bt_Start = (Button) findViewById(R.id.bt_Start);

        //判斷有無錄音權限，沒有的話請求權限
        int permission = ActivityCompat.checkSelfPermission(this, RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{RECORD_AUDIO},
                    1);
        } else {
            //有錄音權限則設置開始按鈕點擊事件
            this.bt_Start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    StartButton_Click();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.bt_Start.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        StartButton_Click();
                    }
                });
            } else {
                //請求權限被拒絕
                Toast.makeText(this, "您拒絕錄音", Toast.LENGTH_SHORT).show();
                this.bt_Start.setEnabled(false);
            }
        }
    }

    private void StartButton_Click() {
        //暫存上一次說完話的文字結果，用於串接
        oldText = tv_Result.getText().toString();

        this.bt_Start.setEnabled(false);

        //設置Context、辨識模式、語言、處理各個事件的類別及PrimaryKey
        this.micClient = SpeechRecognitionServiceFactory.createMicrophoneClient(
                this,
                SpeechRecognitionMode.ShortPhrase,
                this.getDefaultLocale(),
                this,
                this.getPrimaryKey());

        //設置取得Token的Uri
        this.micClient.setAuthenticationUri(this.getAuthenticationUri());

        //開始錄音及辨識
        this.micClient.startMicAndRecognition();
    }

    public void onPartialResponseReceived(final String response) {
        //即時的辨識回覆，將上次結果串接即時結果後顯示
        this.tv_Result.setText(oldText + response);
    }

    public void onFinalResponseReceived(final RecognitionResult response) {
        if (null != this.micClient) {
            this.micClient.endMicAndRecognition();
        }
        //設置最終辨識出的最佳結果
        String FianlText = oldText + response.Results[0].DisplayText;
        this.tv_Result.setText(FianlText);
        this.tv_Result.setSelection(FianlText.length());
    }

    @Override
    public void onIntentReceived(String s) {

    }

    public void onError(final int errorCode, final String response) {
        this.bt_Start.setEnabled(true);
        new AlertDialog.Builder(this)
                .setTitle("錯誤訊息")
                .setMessage("Error code: " + SpeechClientStatus.fromInt(errorCode) + " " + errorCode + "\nError text: " + response)
                .setPositiveButton("知道了", null)
                .show();
    }

    public void onAudioEvent(boolean recording) {
        if (!recording) {
            //結束錄音及辨識
            this.micClient.endMicAndRecognition();
            this.bt_Start.setEnabled(true);
        }
    }
}
