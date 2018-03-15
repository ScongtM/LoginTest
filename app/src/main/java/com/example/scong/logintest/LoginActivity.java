package com.example.scong.logintest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button bt_login;
    private Button bt_register;
    private EditText edit_user;
    private EditText edit_pass;
    private CheckBox checkBox;
    private String responseMessage = "";
    public static final int UPDATE_TEXT = 1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TEXT:
                    String line = (String) msg.obj;
                    Toast.makeText(LoginActivity.this, line, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login);

        bt_login = (Button) findViewById(R.id.bt_login);
        bt_register = (Button) findViewById(R.id.bt_register);
        edit_user = (EditText) findViewById(R.id.et_user);
        edit_pass = (EditText) findViewById(R.id.et_pass);
        checkBox = (CheckBox) findViewById(R.id.checkbox);
        bt_login.setOnClickListener(this);
        bt_register.setOnClickListener(this);

        SharedPreferences pref = getSharedPreferences("pass", MODE_PRIVATE);
        String username = pref.getString("username", "");
        String password = pref.getString("password", "");
        boolean isclick = pref.getBoolean("isclick", false);
        checkBox.setChecked(isclick);
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password) && isclick) {
            edit_user.setText(username);
            edit_pass.setText(password);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_login:
                IntentWorkConnected mIntentWorkConnected = new IntentWorkConnected();
                boolean isNet = mIntentWorkConnected.isNetWorkConnect(LoginActivity.this);
                if (isNet == true) {
                    LoginAndSavePass();
                } else {
                    Toast.makeText(LoginActivity.this, "请检查网络是否连接！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bt_register:
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void LoginAndSavePass() {
        final String username = edit_user.getText().toString().trim();
        final String password = edit_pass.getText().toString().trim();
        boolean isclick = checkBox.isChecked();
        if (checkBox.isClickable()) {
            SharedPreferences.Editor editor = getSharedPreferences("pass", MODE_PRIVATE).edit();
            editor.putString("username", username);
            editor.putString("password", password);
            editor.putBoolean("isclick", isclick);
            editor.apply();
        }
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this, "用户名和密码不能为空！", Toast.LENGTH_SHORT).show();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String serverPath = "http://47.95.193.106:8080/ServletTest/login";
                    HttpURLConnection connection = null;
                    BufferedReader reader = null;
                    try {
                        URL url = new URL(serverPath + "?username=" + username + "&password=" + password);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(8000);
                        connection.setReadTimeout(8000);
                        connection.setRequestProperty("contentType", "utf-8");
                        int responseCode = connection.getResponseCode();
                        if (responseCode == 200) {
                            InputStream input = connection.getInputStream();
                            reader = new BufferedReader(new InputStreamReader(input, "utf-8"));
                            String responseMsg = reader.readLine();
                            if (responseMsg.equals("true")) {
                                responseMessage = "登陆成功！";
                            } else {
                                responseMessage = "登陆失败！";
                            }
                        } else {
                            responseMessage = "responseCode=" + responseCode;
                        }
                        Message msg = new Message();
                        msg.what = UPDATE_TEXT;
                        msg.obj = responseMessage;

                        handler.sendMessage(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                }
            }).start();
        }
    }
}
