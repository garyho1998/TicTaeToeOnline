const WINNING_PATTERNS = [
    [[1, 1, 1], [0, 0, 0], [0, 0, 0]],
    [[0, 0, 0], [1, 1, 1], [0, 0, 0]],
    [[0, 0, 0], [0, 0, 0], [1, 1, 1]],
    [[1, 0, 0], [1, 0, 0], [1, 0, 0]],
    [[0, 1, 0], [0, 1, 0], [0, 1, 0]],
    [[0, 0, 1], [0, 0, 1], [0, 0, 1]],
    [[1, 0, 0], [0, 1, 0], [0, 0, 1]],
    [[0, 0, 1], [0, 1, 0], [1, 0, 0]],
  ];

module.exports = class Game {
    constructor(gameID, playerA, playerB) {
        this.gameID = gameID;
        this.players = [playerA, playerB];
        this.turn = 0;
        this.board = [
          [null, null, null],
          [null, null, null],
          [null, null, null],
        ];
        this.movesCount = 0;
        this.winner = null;
        this.ended = false;
        
        return this;
    }

    start() {
        this._startPlayer(0, 1);
        this._startPlayer(1, 0);
    }

    getBoard(){
      return this.board;
    }
    getPlayer1Name(){
      return this.players[0].name;
    }
    getPlayer2Name(){
      return this.players[1].name;
    }
    getPlayer1Socket(){
      return this.players[0].socket;
    }
    getPlayer2Socket(){
      return this.players[1].socket;
    }

    _startPlayer(selfId, opponentId) {
      const self = this.players[selfId];
      const opponent = this.players[opponentId];

      self.start(selfId,opponent.name,this.board,this.turn);

      self.listen((movement) => {
          console.log('Movement', self.name, movement);
          
          if (!this.ended) {
              try {
                  this._move(selfId, opponentId, movement);
                  if (this._haveWon(selfId)) {

                    console.log(self.name, 'Winner, board:', this.board);
                    self.notifyEnd(this.board, selfId);
                    this.ended = true;
                    
                  } else if (this.movesCount === 9) {

                    console.log(self.name, 'End, board:', this.board);
                    self.notifyEnd(this.board, null);
                    this.ended = true;
                    
                  } else {

                    console.log(self.name, 'Moved, Board:', this.board);
                    self.notifySpectators(this.board, this.turn);
                    this._update();

                  }
              } catch (error) {

                  console.log(self.name, 'Error:',error);
                  self.notifyError(error);
                  this._update();

              }
          } else {
              console.log('gameID: '+ this.gameID , self.socket.id, self.name, 'Made a movement when there was a winner');
          }
      });
    
      self.listenDisconnection(() => {
          if (!this.ended) {
            self.notifyClose(selfId);
          }
      });
    }

    _endGame(){
      this.ended = true;
    }
    _update() {
        this.players[0].update(this.board, this.turn);
        this.players[1].update(this.board, this.turn);
      }
    
      _move(selfId, opponentId, movement) {
        var row = Math.floor((movement-1)/3)
        var col = movement - row*3 - 1
        //console.log("movement: " + movement + " " + movement/3 + " " + movement%3);

        if (this.turn !== selfId) {
          console.log("Not Your\'s turn: " + this.turn + " " + selfId + " " + this.ended);
          throw new Error('gameID: '+ this.gameID + ' Not Your\'s turn, movement ignored');
        }
    
        if (movement<1 || movement>9) {
          throw new Error(`Cell ${row},${col} doesn't exist, movement ignored`);
        }
        
        if (this.board[row][col] !== null) {
          throw new Error(`Cell ${row},${col} is marked, movement ignored`);
        }
    
        this.board[row][col] = selfId;
        this.movesCount += 1;
        this.turn = opponentId;
      }
    
      _haveWon(selfId) {
        return WINNING_PATTERNS.some(pattern => (
          pattern.every((line, lineIndex) => (
            line.every((cell, columnIndex) => (
              !cell || this.board[lineIndex][columnIndex] === selfId
            ))
          ))
        ));
      }    
}

