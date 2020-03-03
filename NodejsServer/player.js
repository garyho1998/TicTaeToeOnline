module.exports = class Player{
    constructor(socket, name) {
        this.socket = socket;
        this.name = name;
        this.disconnected = false;
      }

      start(id, opponent, board, turn) {
        console.log('emit start ' + id);
        this.socket.emit('start', {
          id, opponent, board, turn,
        });
      }

      update(board, turn) {
        this.socket.emit('state', { board, turn });
      }

      notifySpectators(board, turn) {
        console.log('notifySpectators' + board);
        this.socket.broadcast.emit('spectator',{ board, turn});
      }
      
      notifyWaiting() {
        console.log(this.name + " Waiting for opponent");
        this.socket.emit('waiting', {"State":"Waiting for opponent"});
      }

      notifyEnd(board, winner = null) {
        this.socket.emit('end', { board, winner });
        this.socket.broadcast.emit('end', { board, winner });
      }

      notifyError(error) { 
        console.log("serverCatchedError: " + error.toString());
        this.socket.emit('serverCatchedError', { error: error.toString() });
      }

      notifyClose(id) {
        try {
          this.socket.emit('close', { player: id });
          this.socket.broadcast.emit('close', { player: id });
        } catch (error) {
          console.log('notifyClose Error:',error);
        }
      }

      listen(callback) {
        this.socket.on('movement', callback);
      }

      listenDisconnection(callback) {
        const handle = (message) => {
          console.log(this.name, message);
          this.disconnected = true;
          callback(this);
        };
    
        this.socket.on('disconnect', handle);
        this.socket.on('error', handle);
      }
}