const express = require('express'),
app = express(),
http = require('http'),
server = http.createServer(app),
io = require('socket.io').listen(server);

var Player = require("./player.js");
var Game = require("./game.js");

var mysql = require('mysql');
var con = mysql.createConnection({
    host:'127.0.0.1',
    user:'root',
    password:'asdf1234',
});

app.get('/', (req, res) => {
    res.send('Chat Server is running on port 3000')
});

var waitingPlayer = null;
var game = null;
var gameID = 0;
io.on('connection', (socket) => {  
    console.log('connection')

    socket.on('login', function(message) {
            var userJSON = JSON.parse(message);
            var password = userJSON.nameValuePairs.password
            var username = userJSON.nameValuePairs.username
            con.query('Select * from mydatabase.users where username=?',[username],function(err,result,field){
                con.on('error',function(err){
                    console.log('[MySQL Error]',err);
                    socket.emit('LoginResult',{
                        Success: "false",
                        token: ""
                    });
                })
                if(result && result.length){
                    if(result[0].password==password){
                        console.log(socket.id + " login successful")
                        socket.emit('LoginResult',{
                            Success: "true",
                            token: "token"
                        });
                    }else{
                        console.log(username + " typed wrong password")
                        socket.emit('LoginResult',{
                            Success: "false",
                            token: ""
                        });
                    }
                }else{
                    console.log(username + " not exist")
                    socket.emit('LoginResult',{
                        Success: "false",
                        token: ""
                    });
                }
            });
    });
    
    socket.on('register', function(message) {        
        var userJSON = JSON.parse(message);
        var username = userJSON.nameValuePairs.username
        var password = userJSON.nameValuePairs.password
        
        con.query('Select * from mydatabase.users where username=?',[username],function(err,result,field){
            con.on('error',function(err){
                console.log('[MySQL Error]',err);
            })
            if(result && result.length){
                console.log("User already exists")
                socket.emit('registerResult',{
                    Success: "false",
                    message: "User already exists"
                });
            }else{
                con.query("Insert into mydatabase.users(username,password) Values (?,?)", [username,password],function(err,result,fields){
                    con.on('error',function(err){
                        console.log('[MySQL Error]',err);
                        socket.emit('registerResult',{
                            Success: "false",
                            message: "Unknow SQL Error"
                        });
                    });
                    console.log("Registed " + username)
                    socket.emit('registerResult',{
                        Success: "true"
                    });
        
                })
            }
        });

    });
    
    socket.on('disconnect', function() {
        if(game!=null){
            if(game.getPlayer1Socket().id==socket.id || game.getPlayer2Socket().id==socket.id){
                game._endGame();
                console.log( 'Player disconnected' , game.ended)
                game = null;
                waitingPlayer = null; 
                socket.broadcast.emit('close', "");
            }  
        }else{
            console.log( 'User disconnected')
        }
        socket.broadcast.emit("userdisconnect"," user has left ") 
    });

    socket.on('findingOpponent', function(message){
        var userJSON = JSON.parse(message);
        var name = userJSON.nameValuePairs.username
        console.log(name + " finding Opponent " + game);
        if(!(game==null)){
            console.log("Gaming")             
            socket.emit("gaming", {player1: game.getPlayer1Name(), player2: game.getPlayer2Name(), board: game.getBoard()});
        }else{
            if(waitingPlayer){
                console.log(waitingPlayer.name + " already waiting")
                if(waitingPlayer.name != name){
                    console.log(name + " is new player")  
                    addPlayer(socket, name)  
                }
            }else{
                console.log("No waitingPlayer waiting")
                addPlayer(socket, name)
            } 
        } 
    }) 
  
    socket.on('gameClosed', function(message){ 
        console.log(socket.id, ' Game Closed');
        if(game!=null){
            game._endGame();
            game = null; 
        }
        waitingPlayer = null
        socket.broadcast.emit('close', "");
    })
});

server.listen(3000,()=>{
    console.log('Node app is running on port 3000');
});

function addPlayer(socket, name) {
    if (waitingPlayer) {  
        try {
            console.log(socket.id, name, 'Creating game');
            game = new Game(gameID, waitingPlayer, new Player(socket, name));
            game.start();
            gameID++;
            waitingPlayer = null;
        } catch (error) {
            console.log(socket.id, name, 'Error:', error);
        }
    } else {
        console.log(socket.id, name, '[addPlayer] Waiting');
        waitingPlayer = new Player(socket, name);   
        
        waitingPlayer.listenDisconnection((player) => {
            console.log(player.socket.id, player.name, 'Disconnected');
            if (waitingPlayer && waitingPlayer.socket === socket) {
                waitingPlayer = null;
            }
        });

        waitingPlayer.notifyWaiting();
    }
  }
