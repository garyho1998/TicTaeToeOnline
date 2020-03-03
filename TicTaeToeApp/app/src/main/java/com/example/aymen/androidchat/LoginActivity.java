package com.example.aymen.androidchat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;


public class LoginActivity extends AppCompatActivity {


    Button loginBtn;
    TextView registerBtn;
    EditText usernameET;
    EditText passwordET;
    Socket socket;
    SocketApplication sa;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //debug mode
//        Intent i = new Intent(LoginActivity.this, TicTacToeActivity.class);
//        i.putExtra("username", "User(Debug)");
//        startActivity(i);
        //debug mode

        loginBtn = (Button) findViewById(R.id.login_Btn);
        registerBtn = (TextView) findViewById(R.id.goRegisterTxt);
        usernameET = (EditText) findViewById(R.id.usernameET);
        passwordET = (EditText) findViewById(R.id.passwordET);

        sa = (SocketApplication) getApplicationContext();
        sa.connect();
        socket = sa.socket;

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socket.connect();
                if(socket.connected()) {
                    loginUser(usernameET.getText().toString(), passwordET.getText().toString());
                }else{
                    Toast.makeText(LoginActivity.this,"Connection fail",Toast.LENGTH_SHORT).show();
                }
            }
        });
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });
    }

    public void loginUser(final String username, String password) {
            JSONObject user = new JSONObject();
            try {
                user.put("username", username);
                user.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Gson gson = new Gson();
            socket.emit("login", gson.toJson(user));

            socket.on("LoginResult", new Emitter.Listener() {
                @Override
                public void call(final Object... data) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject jo =  (JSONObject) data[0];
                            try {
                                String success = jo.get("Success").toString();
                                Log.i("Login", "success");
                                if(success.equals("true")){
                                    Intent i = new Intent(LoginActivity.this, TicTacToeActivity.class);
                                    i.putExtra("username", username);
                                    startActivity(i);
                                }else{
                                    Toast.makeText(LoginActivity.this,"Login Failed",Toast.LENGTH_SHORT).show();
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