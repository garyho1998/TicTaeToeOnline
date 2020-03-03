package com.example.aymen.androidchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

public class TicTacToeActivity extends AppCompatActivity {
    SocketApplication sa;
    EmitterListeners emitterListeners;
    Socket socket;

    Button findBtn;
    Button boardButtons[];
    PopupWindow popupWindow;

    String username;
    String PlayerID;
    String OpponentID;
    TextView stateTV;
    TextView logoutTV;
    TextView gameStateTV;
    TextView text_view_p1;
    TextView text_view_p2;

    Boolean playing = false;
    Boolean spectator = false;
    Boolean gaming = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tictactoe);
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        setUpOnClickListener();
        sa = (SocketApplication) getApplicationContext();
        sa.connect();
        emitterListeners = new EmitterListeners();
        socket = sa.socket;
    }

    public void findingOpponent(){
        socket.connect();
        if(sa.connected()){
            Log.i("findingOpponent","connected");
            sa.sendIntro(username);
            socket.on("serverCatchedError", emitterListeners.serverCatchedErrorEL);
            socket.once("waiting", emitterListeners.waitingEL);
            socket.once("close", emitterListeners.closeEL);
            socket.once("end", emitterListeners.endEL);
            socket.once("start", emitterListeners.startEL);
            socket.once("gaming", emitterListeners.gamingEL);
        }else{
            popupWindow.dismiss();
            sa.disconnect();
            Toast.makeText(TicTacToeActivity.this,"Connection fail",Toast.LENGTH_SHORT).show();
        }
    }

    //Emitter.Listener
    public class EmitterListeners{
        Emitter.Listener waitingEL = new Emitter.Listener() {
            @Override
            public void call(final Object... data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jo = (JSONObject) data[0];
                        try {
                            String state = jo.get("State").toString();
                            Log.i("Waiting", "success");
                            if (state.equals("Waiting for opponent")) {
                                stateTV = (TextView) popupWindow.getContentView().findViewById(R.id.state);
                                stateTV.setText(state);
                                Log.i("Waiting", "waiting for start");
                            } else {
                                Log.i("Waiting", "Unkown Error");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        Emitter.Listener startEL = new Emitter.Listener() {
            @Override
            public void call(final Object... data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jo = (JSONObject) data[0];
                        try {
                            String id = jo.get("id").toString();
                            String opponent = jo.get("opponent").toString();
                            String turn = jo.get("turn").toString();

                            Log.i("Start", "success");
                            PlayerID = id;
                            if(PlayerID.equals("0")){
                                OpponentID = "1";
                                text_view_p1.setText("Player 1: " + username);
                                text_view_p2.setText("Player 2: " +opponent);
                            }else{
                                OpponentID = "0";
                                text_view_p1.setText("Player 1: " + opponent);
                                text_view_p2.setText("Player 2: " + username);
                            }
                            if(PlayerID.equals(turn)){
                                gameStateTV.setText("Game State: Your Turn");
                            }else{
                                gameStateTV.setText("Game State: Opponent's Turn");
                            }

                            playing = true;
                            spectator = false;
                            updateBroad(jo.get("board").toString());
                            socket.on("state", emitterListeners.stateEL);

                            popupWindow.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        Emitter.Listener stateEL = new Emitter.Listener() {
            @Override
            public void call(final Object... data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jo = (JSONObject) data[0];
                        try {
                            updateBroad(jo.get("board").toString());
                            String turn = jo.get("turn").toString();

                            if(PlayerID.equals(turn)){
                                gameStateTV.setText("Game State: Your Turn");
                            }else{
                                gameStateTV.setText("Game State: Opponent's Turn");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        Emitter.Listener gamingEL = new Emitter.Listener() {
            @Override
            public void call(final Object... data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jo = (JSONObject) data[0];
                        try {
                            Toast.makeText(TicTacToeActivity.this,"There is a game already, You will be spectator",Toast.LENGTH_SHORT).show();
                            text_view_p1.setText("Player 1: " + jo.get("player1").toString());
                            text_view_p2.setText("Player 2: " + jo.get("player2").toString());

                            updateBroad(jo.get("board").toString());
                            if(popupWindow!=null){
                                popupWindow.dismiss();
                            }
                            gameStateTV.setText("Game State: Spectator");
                            socket.on("spectator", spectatorEL);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        Emitter.Listener spectatorEL = new Emitter.Listener() {
            @Override
            public void call(final Object... data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jo = (JSONObject) data[0];
                        try {
                            playing = false;
                            spectator = true;
                            updateBroad(jo.get("board").toString());
                            System.out.println("UpdateBoard (spectator): " + jo.get("board").toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        Emitter.Listener endEL = new Emitter.Listener() {
            @Override
            public void call(final Object... data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jo = (JSONObject) data[0];
                        try {
                            System.out.println("endEL: " + data);
                            updateBroad(jo.get("board").toString());
                            String winner = jo.get("winner").toString();
                            System.out.println("endEL jo: " + jo);
                            sa.sendGameClosed();
                            if(winner == "null"){
                                gameStateTV.setText("Game State: Tie");
                            }else if(winner.equals("0")){
                                gameStateTV.setText("Game State: Player 1 Win");
                            }else{
                                gameStateTV.setText("Game State: Player 2 Win");
                            }
                            playing = false;
                            spectator = false;

                            socket.off("state", stateEL);
                            socket.off("spectator", spectatorEL);
                            socket.off("serverCatchedError", emitterListeners.serverCatchedErrorEL);
                            socket.off("waiting", emitterListeners.waitingEL);
                            socket.off("close", emitterListeners.closeEL);
                            socket.off("end", emitterListeners.endEL);
                            socket.off("start", emitterListeners.startEL);
                            socket.off("gaming", emitterListeners.gamingEL);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        Emitter.Listener closeEL = new Emitter.Listener() {
            @Override
            public void call(final Object... data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        playing = false;
                        spectator = false;
                        socket.off("state", stateEL);
                        socket.off("spectator", spectatorEL);
                        socket.off("serverCatchedError", emitterListeners.serverCatchedErrorEL);
                        socket.off("waiting", emitterListeners.waitingEL);
                        socket.off("close", emitterListeners.closeEL);
                        socket.off("end", emitterListeners.endEL);
                        socket.off("start", emitterListeners.startEL);
                        socket.off("gaming", emitterListeners.gamingEL);
                        Toast.makeText(TicTacToeActivity.this,"Player Disconnected, Please start a new game",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        Emitter.Listener serverCatchedErrorEL = new Emitter.Listener() {
            @Override
            public void call(final Object... data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jo = (JSONObject) data[0];
                        String error = "Cant parse error in client";
                        try {
                            error = jo.get("error").toString();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(TicTacToeActivity.this," Error Alert from Server: " + error ,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
    }

    public void updateBroad(String s){
        s = s.replace('[',' ');
        s = s.replace(']',' ');
        s = s.replaceAll("\\s+","");
        Log.i("updateBroad",s);
        String tempArray[] = s.split(",");
        int i = 1;
        for(String temp:tempArray){
            if(temp.equals("0")){
                boardButtons[i].setText("O");
            }else if(temp.equals("1")){
                boardButtons[i].setText("X");
            }else{
                boardButtons[i].setText("");
            }

            i++;
        }
    }

    public void showPopUp(View view) {
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.activity_waiting, null);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                sa.disconnect();
                return true;
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        socket.emit("disconnected", "user disconnected");
        super.onDestroy();
    }

    public void setUpOnClickListener(){
        text_view_p1 = (TextView) findViewById(R.id.text_view_p1);
        text_view_p2 = (TextView) findViewById(R.id.text_view_p2);
        gameStateTV = (TextView) findViewById(R.id.gameStateTV);
        logoutTV = (TextView) findViewById(R.id.logoutTV);
        logoutTV.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(socket!=null){
                        if(playing){
                            socket.emit("gameClosed","");
                            socket.emit("disconnect","");
                        }
                        sa.disconnect();
                    }
                    startActivity(new Intent(TicTacToeActivity.this, LoginActivity.class));
                }
            }
        );
        findBtn =(Button) findViewById(R.id.button_find);
        findBtn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("findBtn.setOnClickListener: playing:" + playing);
                    if(playing){
                        Toast.makeText(TicTacToeActivity.this,"You already playing",Toast.LENGTH_SHORT).show();
                    }else{
                        showPopUp(v);
                        findingOpponent();
                    }
                }
            }
        );

        boardButtons = new Button[10];
        boardButtons[1] = (Button) findViewById(R.id.button_1);
        boardButtons[2] = (Button) findViewById(R.id.button_2);
        boardButtons[3] = (Button) findViewById(R.id.button_3);
        boardButtons[4] = (Button) findViewById(R.id.button_4);
        boardButtons[5] = (Button) findViewById(R.id.button_5);
        boardButtons[6] = (Button) findViewById(R.id.button_6);
        boardButtons[7] = (Button) findViewById(R.id.button_7);
        boardButtons[8] = (Button) findViewById(R.id.button_8);
        boardButtons[9] = (Button) findViewById(R.id.button_9);

        boardButtons[1].setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sa.sendMovement(1);
                    }
                }
        );
        boardButtons[2].setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sa.sendMovement(2);
                    }
                }
        );
        boardButtons[3].setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sa.sendMovement(3);
                    }
                }
        );
        boardButtons[4].setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sa.sendMovement(4);
                    }
                }
        );
        boardButtons[5].setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sa.sendMovement(5);
                    }
                }
        );
        boardButtons[6].setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sa.sendMovement(6);
                    }
                }
        );
        boardButtons[7].setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sa.sendMovement(7);
                    }
                }
        );
        boardButtons[8].setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sa.sendMovement(8);
                    }
                }
        );
        boardButtons[9].setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sa.sendMovement(9);
                    }
                }
        );
    }


}

