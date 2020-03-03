package com.example.aymen.androidchat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class RegisterActivity extends AppCompatActivity {
    TextView loginBtn;
    Button registerBtn;
    EditText usernameET;
    EditText passwordET;

    Socket socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        loginBtn = (TextView) findViewById(R.id.goLoginTxt);
        registerBtn = (Button) findViewById(R.id.register_Btn);
        usernameET = (EditText) findViewById(R.id.usernameET_R);
        passwordET = (EditText) findViewById(R.id.passwordET_R);

        try {
            SocketApplication ss = new SocketApplication();
            socket = IO.socket("http://" + ss.ip + ":3000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        loginBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });
        registerBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(socket.connected()) {
                    registerUser(usernameET.getText().toString(),passwordET.getText().toString());
                }else{
                    Toast.makeText(RegisterActivity.this,"Connection fail",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void registerUser(String username, String password) {
        JSONObject user = new JSONObject();
        try {
            user.put("username", username);
            user.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Gson gson = new Gson();
        socket.emit("register", gson.toJson(user));

        socket.on("registerResult", new Emitter.Listener() {
            @Override
            public void call(final Object... data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jo =  (JSONObject) data[0];
                        try {
                            String success = jo.get("Success").toString();
                            Log.i("Register", "success");
                            if(success.equals("true")){
                                Toast.makeText(RegisterActivity.this,"Register Successful!",Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(RegisterActivity.this,"Register Failed",Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.off("LoginResult");
    }
}
