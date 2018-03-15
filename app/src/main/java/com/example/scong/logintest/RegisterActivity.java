package com.example.scong.logintest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_re_pass;
    private EditText et_re_confirmpass;
    private Button bt_re_confirm;
    private EditText et_re_user;
    private String responseMeg = "";
    private Handler handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String mess = (String) msg.obj;
            Toast.makeText(RegisterActivity.this,mess,Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_register);
        et_re_user = (EditText) findViewById(R.id.et_re_user);
        et_re_pass = (EditText) findViewById(R.id.et_re_pass);
        et_re_confirmpass = (EditText) findViewById(R.id.et_re_confirmpass);
        bt_re_confirm = (Button) findViewById(R.id.bt_re_confirm);
        bt_re_confirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_re_confirm:
                IntentWorkConnected intentWrokConnected = new IntentWorkConnected();
                boolean isNet = intentWrokConnected.isNetWorkConnect(RegisterActivity.this);
                if (isNet==true){
                    Register();
                }else {
                    Toast.makeText(RegisterActivity.this,"请检查网络是否连接！",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void Register() {
        final String username = et_re_user.getText().toString();
        final String password = et_re_pass.getText().toString();
        final String confirmPassword = et_re_confirmpass.getText().toString();
        if (TextUtils.isEmpty(username)||TextUtils.isEmpty(password)||TextUtils.isEmpty(confirmPassword)){
            Toast.makeText(RegisterActivity.this,"用户名密码和确认密码不能为空！",Toast.LENGTH_SHORT).show();
        }else if (!password.equals(confirmPassword)){
            Toast.makeText(RegisterActivity.this,"两次输入的密码不相同，请重新输入！",Toast.LENGTH_SHORT).show();
            clearEditText();
        }else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection connection = null;
                    BufferedReader reader = null;
                    try {
                        String serverPath = "http://47.95.193.106:8080/ServletTest/register";
                        URL url = new URL(serverPath+"?username="+username+"&password="+password);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setReadTimeout(8000);
                        connection.setConnectTimeout(8000);
                        connection.setRequestProperty("contentType","utf-8");
                        int responseCode = connection.getResponseCode();
                        if (responseCode == 200){
                            InputStream input = connection.getInputStream();
                            reader = new BufferedReader(new InputStreamReader(input,"utf-8"));
                            String line;
                            StringBuilder response =new StringBuilder();
                            while ((line=reader.readLine())!=null){
                                response.append(line);
                            }
                            if (response.toString().equals("equal")){
                                responseMeg="该用户名已经被注册了！";
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        clearEditText();
                                    }
                                });
                            }else if (response.toString().equals("true")){
                                responseMeg="注册成功！";
                                SharedPreferences.Editor editor =getSharedPreferences("pass",MODE_PRIVATE).edit();
                                editor.putString("username",username);
                                editor.putString("password",password);
                                editor.putBoolean("isclick",true);
                                editor.apply();
                                Intent intent =new Intent(RegisterActivity.this,LoginActivity.class);
                                startActivity(intent);
                            }
                        }else {
                            responseMeg="responseCode="+responseCode;
                        }
                        Message msg =new Message();
                        msg.obj=responseMeg;
                        handler.sendMessage(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        if (reader!=null){
                            try {
                                reader.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (connection!=null){
                            connection.disconnect();
                        }
                    }
                }
            }).start();
        }
    }

    private void clearEditText() {
        et_re_user.setText(null);
        et_re_pass.setText(null);
        et_re_confirmpass.setText(null);
    }
}
