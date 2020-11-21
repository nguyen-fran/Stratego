package edu.up.cs301.Stratego;

import edu.up.cs301.game.GameFramework.GameComputerPlayer;
import edu.up.cs301.game.GameFramework.infoMessage.GameInfo;

/**
 * A smarter computer player to play Stratego
 *
 * @author Gabby Marshak
 * @author Francisco Nguyen
 * @author Blake Nygren
 * @author Jack Volonte
 */
public class StrategoSmartComputerPlayer extends GameComputerPlayer {

    public static final int BLUE =  0;
    public static final int RED = 1;
    public boolean shouldDefend = false;
    /**
     * constructor
     *
     * @param name the player's name (e.g., "John")
     */
    public StrategoSmartComputerPlayer(String name) {
        super(name);
    }

    /**
     * method to determine what move the computer should take
     * sends either move or swap actions
     * @param info current state of the game
     */
    @Override
    protected void receiveInfo(GameInfo info) {
        int squareSrc = -1;
        int squareDest = -1;
        BoardSquare source = null;
        BoardSquare destination = null;

        StrategoGameState gameState = new StrategoGameState((StrategoGameState) info);
        if (gameState.getCurrPlayerIndex() != playerNum) {
            return;
        }

        //TODO: add board setup logic

        //case statement for determining what piece to move and where (call helper methods)
        //need to assign source and destination squares (convert from BoardSquare coordinates)
        //TODO: priority order to finish helper methods is flagAttack > defaultMove > everything else in reverse order of precedence

        //call move action
        game.sendAction(new StrategoMoveAction(this, squareSrc, squareDest));

    }

    /**
     * checks if human player's flag piece is within one square of a computer player's piece
     * checks if in a straight line from a computer player's scouts
     * TODO: add some sort of logic from graveyard counts/visible pieces to determine where the flag is
     * TODO: set a destination square that can be used by receiveInfo before returning (applies to all helper methods)
     * right now the method is just going to always know where the flag is whether or not it's visible
     * @param gameState current state of the game being analyzed
     * @return source square for movement
     */
    public BoardSquare flagAttack(StrategoGameState gameState){
        BoardSquare flag = null;
        BoardSquare source = null;

        //for loop through human player's pieces to find where the flag is
        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){
                //TODO: potentially rename this gamepiece to something more indicative
                GamePiece flagg = gameState.getBoardSquares()[i][j].getPiece();
                if(flagg.getRank() == 0){
                    flag = gameState.getBoardSquares()[i][j];
                }
            }
        }

        //if you return here, something went wrong, either on the board or in the for loop
        if(flag == null){
            return null;
        }

        //check squares immediately adjacent to flag (if on board) (only 4 squares so doing it manually)
        //destination squares for these if statements should be the flag square
        if((flag.getRow() - 1 >= 0) &&
           (gameState.getBoardSquares()[flag.getRow()-1][flag.getCol()].getPiece().getTeam() == RED)){
            source = gameState.getBoardSquares()[flag.getRow()-1][flag.getCol()];
        }else if((flag.getRow() + 1 < 10) &&
                (gameState.getBoardSquares()[flag.getRow()+1][flag.getCol()].getPiece().getTeam() == RED)){
            source = gameState.getBoardSquares()[flag.getRow()+1][flag.getCol()];
        }else if((flag.getCol() - 1 >= 0) &&
                (gameState.getBoardSquares()[flag.getRow()][flag.getCol() - 1].getPiece().getTeam() == RED)){
            source = gameState.getBoardSquares()[flag.getRow()][flag.getCol() - 1];
        }else if((flag.getCol() + 1 < 0) &&
                (gameState.getBoardSquares()[flag.getRow()][flag.getCol() + 1].getPiece().getTeam() == RED)){
            source = gameState.getBoardSquares()[flag.getRow()][flag.getCol() + 1];
        }

        //check in straight lines from the human flag until it hits another piece or goes off the board
        //make a helper method for this

        //if the piece is a red scout, destination should be the flag square
        if(source.getPiece().getTeam() == RED && source.getPiece().getRank() == 2){

        }
        //if the piece is any other mobile red piece, destination should be moving one square towards the flag
        else if(source.getPiece().getTeam() == RED &&
                !(source.getPiece().getRank() == 0 || source.getPiece().getRank() == 11)){

        }

        return null;
    }

    /**
     * checks if computer player's flag is reachable by human player's pieces, moves to defend the flag if possible
     * similar logic to flagAttack for the checks, just with teams swapped around
     * @param gameState current state of the game
     * @return source square for movement
     * TODO: maybe break this up into helper methods
     */
    public BoardSquare flagDefend(StrategoGameState gameState){
        BoardSquare flag = null;
        //loop through and find our (the computer players) flag
        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){
                GamePiece flagg = gameState.getBoardSquares()[i][j].getPiece();
                if(flagg.getRank() == 0 && flagg.getTeam() == BLUE) {
                    flag = gameState.getBoardSquares()[i][j];
                }
            }
        }

        BoardSquare killThisOne = null;
        //check if the flag can be killed, and get the square of who is attacking the flag
        if((flag.getRow() - 1 >= 0) &&
                (gameState.getBoardSquares()[flag.getRow()-1][flag.getCol()].getPiece().getTeam() == RED)){
            this.shouldDefend = true;
            killThisOne = gameState.getBoardSquares()[flag.getRow()-1][flag.getCol()];
        }else if((flag.getRow() + 1 < 10) &&
                (gameState.getBoardSquares()[flag.getRow()+1][flag.getCol()].getPiece().getTeam() == RED)){
            this.shouldDefend = true;
            killThisOne = gameState.getBoardSquares()[flag.getRow()+1][flag.getCol()];
        }else if((flag.getCol() - 1 >= 0) &&
                (gameState.getBoardSquares()[flag.getRow()][flag.getCol() - 1].getPiece().getTeam() == RED)){
            this.shouldDefend = true;
            killThisOne = gameState.getBoardSquares()[flag.getRow()][flag.getCol() - 1];
        }else if((flag.getCol() + 1 < 0) &&
                (gameState.getBoardSquares()[flag.getRow()][flag.getCol() + 1].getPiece().getTeam() == RED)){
            this.shouldDefend = true;
            killThisOne = gameState.getBoardSquares()[flag.getRow()][flag.getCol() + 1];
        }

        //check if we can defend and kill the attacking piece, even a trade is fine here
        BoardSquare defendWithThis = null;
        if ( this.shouldDefend = true ) {
            if((killThisOne.getRow() - 1 >= 0) &&
                    (gameState.getBoardSquares()[killThisOne.getRow()-1][flag.getCol()].getPiece().getTeam() == BLUE) &&
                gameState.getBoardSquares()[killThisOne.getRow()-1][flag.getCol()].getPiece().getRank() >= killThisOne.getPiece().getRank()) {
                defendWithThis = gameState.getBoardSquares()[killThisOne.getRow()-1][flag.getCol()];
            }else if((flag.getRow() + 1 < 10) &&
                    (gameState.getBoardSquares()[killThisOne.getRow()+1][flag.getCol()].getPiece().getTeam() == BLUE) &&
                            gameState.getBoardSquares()[killThisOne.getRow()+1][flag.getCol()].getPiece().getRank() >= killThisOne.getPiece().getRank()){
               defendWithThis = gameState.getBoardSquares()[killThisOne.getRow()+1][flag.getCol()];
            }else if((flag.getCol() - 1 >= 0) &&
                    (gameState.getBoardSquares()[killThisOne.getRow()][flag.getCol() - 1].getPiece().getTeam() == BLUE) &&
                    gameState.getBoardSquares()[killThisOne.getRow()][flag.getCol() - 1].getPiece().getRank() >= killThisOne.getPiece().getRank()){
                defendWithThis = gameState.getBoardSquares()[killThisOne.getRow()][flag.getCol() - 1];

            }else if((flag.getCol() + 1 < 0) &&
                    (gameState.getBoardSquares()[killThisOne.getRow()][flag.getCol() + 1].getPiece().getTeam() == BLUE) &&
                    gameState.getBoardSquares()[killThisOne.getRow()][flag.getCol() + 1].getPiece().getRank() >= killThisOne.getPiece().getRank()){
                defendWithThis = gameState.getBoardSquares()[killThisOne.getRow()][flag.getCol() + 1];
            }

            //so now we have the square where we need to attack, and the square to attack with
            int defX = defendWithThis.getCol();
            int defY = defendWithThis.getRow();
            int attX = killThisOne.getCol();
            int attY = killThisOne.getRow();
            int firstClick = 0;
            int secondClick = 0;
            if ( defX == 0 && defY == 0 ) {
                firstClick = 0;
            } else if ( defY == 0 ) {
                firstClick = defX;
            } else if ( defX == 0 ) {
                firstClick = defY * 10;
            } else {
                firstClick = defX * defY;
            }

            if ( attX == 0 && attY == 0 ) {
                secondClick = 0;
            } else if ( attY == 0 ) {
                secondClick = attX;
            } else if ( attX == 0 ) {
                secondClick = attY * 10;
            } else {
                secondClick = attX * attY;
            }
            game.sendAction(new StrategoMoveAction(this, firstClick, secondClick));
        }

        return null;
    }

    /**
     * Smart AI makes move to get closer to or attack opp marshall or bomb if either is visible
     * TODO: choose specific bomb or marshall to attack
     *
     * @param gameState
     * @return
     */
    public BoardSquare specialCaseAttack(StrategoGameState gameState) {
        boolean visibleBomb = false, visibleMarshall = false;
        int squareDest;

        //look for visible opp marshall or bomb
        for (int i = 0; i < StrategoGameState.BOARD_SIZE; i++) {
            for (int j = 0; j < StrategoGameState.BOARD_SIZE; j++) {
                BoardSquare currSquareAnalyze = gameState.getBoardSquares()[i][j];
                if (gameState.getBoardSquares()[i][j].getPiece() != null
                        && gameState.getBoardSquares()[i][j].getPiece().getTeam() != playerNum && gameState.getBoardSquares()[i][j].getPiece().getVisible()) {
                    if (gameState.getBoardSquares()[i][j].getPiece().getRank() == GamePiece.BOMB) { //found a bom
                        visibleBomb = true;
                        if (reachableSquare(gameState, i, j)) {
                            squareDest = 1 *10 +j;
                        }
                    } else if (gameState.getBoardSquares()[i][j].getPiece().getRank() == 10) {  //found a marshall
                        visibleMarshall = true;
                        if (reachableSquare(gameState, i, j)) {
                            squareDest = 1 *10 +j;
                        }
                    }
                }
            }
        }

        if (visibleBomb) {
            for (int i = 0; i < StrategoGameState.BOARD_SIZE; i++) {
                for (int j = 0; j < StrategoGameState.BOARD_SIZE; j++) {
                    
                }
            }
        }

        return null;
    }

    /**
     * Checks to see if a square has an unoccupied square next to it
     *
     * @param gameState
     * @param i row of squareDest
     * @param j col of squareDest
     * @return  true if there is a square to top, bottom, left, or right of squareDest
     */
    private boolean reachableSquare(StrategoGameState gameState, int i, int j) {
        if ((gameState.getBoardSquares()[i][j].getRow() < StrategoGameState.BOARD_SIZE && gameState.getBoardSquares()[i + 1][j].getPiece() == null)
            || (gameState.getBoardSquares()[i][j].getRow() > 0 && gameState.getBoardSquares()[i - 1][j].getPiece() == null)
            || (gameState.getBoardSquares()[i][j].getCol() < StrategoGameState.BOARD_SIZE && gameState.getBoardSquares()[i][j + 1].getPiece() == null)
            || (gameState.getBoardSquares()[i][j].getCol() > 0 && gameState.getBoardSquares()[i][j - 1].getPiece() == null)) {
            return true;
        }
        return false;
    }

    public BoardSquare scoutAttack(StrategoGameState gameState){
        return null;
    }

    //i think this method should return the firstCLick and secondClick that the computer wants to move on, in the case of the piece being hidden, these can be called
    //into the hiddenPieceAttack method
    public BoardSquare normalAttack(StrategoGameState gameState){
        return null;
    }

    public boolean hiddenPieceAttack(StrategoGameState gameState, int rowFirst, int colFirst) {
        //getting the graveyard
        int[] redGY = gameState.getRedGY();
        int[] pieceNumbers = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

        //setting up doubles/ints for math later
        double chanceOfWinning = 0;
        double chanceOfLosing = 0;
        int totalDead = 0;
        int weCanWin = 0;
        int weWillLose = 0;

        //getting square we want to attack with
        BoardSquare attackingSquare = gameState.getBoardSquares()[rowFirst][colFirst];
        for ( int i = 0; i < redGY.length; i++ ) {
                if ( pieceNumbers[i] < attackingSquare.getPiece().getRank() ) {
                    weWillLose+=redGY[i];
                } else {
                    weCanWin+=redGY[i];
                }
                totalDead++;
        }

        //doing math for winning/losing
        chanceOfWinning = Math.abs((totalDead) / (weCanWin));
        chanceOfLosing = Math.abs((totalDead) / (weWillLose));
        if ( chanceOfWinning > chanceOfLosing ) {
            return true;
        }
        return false;
    }

    public BoardSquare defaultMove(StrategoGameState gameState) {
        //find the furthest move piece towards the other player, (down the board if computer player is only player 2)
        BoardSquare moveThisOne = null;

        //loop through the board and find the piece for this player that can move the most forward
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (gameState.getBoardSquares()[i][j].getPiece().getRank() != 0 && gameState.getBoardSquares()[i][j].getPiece().getRank() != 11) {
                    if (gameState.getBoardSquares()[i][j].getPiece().getTeam() == BLUE) {
                        if (gameState.getBoardSquares()[i][j + 1].getPiece() != null) {
                            moveThisOne = gameState.getBoardSquares()[i][j];
                        }
                    }
                }

            }
        }
        //if we cant find a piece to move forward, then we gotta move left or right with a piece
        if ( moveThisOne == null ) {
            //update this later to reflect last comment ^^
        }


        //get the int value of the piece we want to move
        int moveX = moveThisOne.getCol();
        int moveY = moveThisOne.getRow();
        int firstClick = 0;
        int secondClick = 0;
        if ( moveX == 0 && moveY == 0 ) {
            firstClick = 0;
        } else if ( moveY == 0 ) {
            firstClick = moveX;
        } else if ( moveX == 0 ) {
            firstClick = moveY * 10;
        } else {
            firstClick = moveX * moveY;
        }
        //make the piece move down
        secondClick = firstClick + 10;
        //send the move action
        game.sendAction(new StrategoMoveAction(this, firstClick, secondClick));
        return null;
    }

}
