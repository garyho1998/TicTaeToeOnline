package com.example.aymen.androidchat;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class SocketApplication extends Application {
    public String ip = "10.0.2.2";
    private final String TAG = "SocketIO";
    public Socket socket;
    public Boolean connected = false;

    public void connect() {
        try {
            socket = IO.socket("http://" + ip + ":3000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Connecting");
        socket.connect();
        if(socket.connected()){
            Log.d(TAG, "Connected");
            connected = true;
        }else{
            Log.d(TAG, "Failed to Connect");
            connected = false;
        }
    }

    public void disconnect() {
        if (connected) {
            Log.d(TAG, "Disconnecting");
            connected = false;
            socket.disconnect();
        }
    }
    public Boolean connected(){
        return connected;
    }

    public void sendMovement(int cell){
        socket.emit("movement", cell);
    }
    public void sendIntro(String username){
        JSONObject user = new JSONObject();
        try {
            user.put("username", username);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Gson gson = new Gson();
        socket.emit("findingOpponent", gson.toJson(user));
    }
    public void sendGameClosed(){
        socket.emit("gameClosed");
    }

}
