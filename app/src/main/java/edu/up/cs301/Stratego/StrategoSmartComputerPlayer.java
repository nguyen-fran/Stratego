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

    }

    /**
     * checks if human player's flag piece is within one square of a computer player's piece
     * checks if in a straight line from a computer player's scouts
     * TODO: add some sort of logic from graveyard counts/visible pieces to determine where the flag is
     * right now the method is just going to always know where the flag is whether or not it's visible
     * @param gameState current state of the game being analyzed
     */
    public void flagAttack(StrategoGameState gameState){
        BoardSquare flag = null;
        BoardSquare source = null;
        BoardSquare dest = null;

        int sourceCoord;
        int destCoord;

        //for loop through human player's pieces to find where the flag is
        for(int i = 0; i < StrategoGameState.BOARD_SIZE; i++){
            for(int j = 0; j < StrategoGameState.BOARD_SIZE; j++){
                //TODO: potentially rename this gamepiece to something more indicative
                GamePiece flagg = gameState.getBoardSquares()[i][j].getPiece();
                if(flagg.getRank() == 0){
                    flag = gameState.getBoardSquares()[i][j];
                }
            }
        }

        //if you return here, something went wrong, either on the board or in the for loop
        if(flag == null){
            return;
        }

        //check in straight lines from the human flag until it hits another piece or goes off the board
        source = straightLineChecker(flag, gameState);

        //if there are no computer pieces in straight lines from the flag, then return and do something else
        if(source == null){
            return;
        }

        //if the piece is a red scout, destination should be the flag square
        if(source.getPiece().getTeam() == StrategoGameState.RED && source.getPiece().getRank() == 2){
            dest = flag;
        }
        //if the piece is any other mobile red piece, destination should be moving one square towards the flag
        else if(source.getPiece().getTeam() == StrategoGameState.RED &&
                !(source.getPiece().getRank() == GamePiece.FLAG || source.getPiece().getRank() == GamePiece.BOMB)){
            //check if row/col is shared between the flag and source
            if(source.getRow() == flag.getRow()){
                //destination depends on if source column is greater or less than flag column
                if(source.getCol() > flag.getCol()){
                    dest = gameState.getBoardSquares()[flag.getRow()][source.getCol() - 1];
                }else{
                    dest = gameState.getBoardSquares()[flag.getRow()][source.getCol() + 1];
                }
            }else if(source.getCol() == flag.getCol()){
                //destination depends on if source column is greater or less than flag columns
                if(source.getRow() > flag.getRow()){
                    dest = gameState.getBoardSquares()[source.getRow() - 1][flag.getCol()];
                }else{
                    dest = gameState.getBoardSquares()[source.getRow() + 1][flag.getCol()];
                }
            }
        }

        //making sure destination is not null before doing anything with it. if it is then something went wrong
        if(dest == null){
            return;
        }
        sourceCoord = coordConverter(source);
        destCoord = coordConverter(dest);

        game.sendAction(new StrategoMoveAction(this, sourceCoord, destCoord));
    }

    /**
     * checks squares in a straight line from a given board square in all four directions
     * checking for computer player's pieces or the end of the board, whichever comes first
     * decides what to return by prioritising the first scout it finds
     * otherwise just the first occupied board square it finds
     * @param square board square we are checking around
     * @param gameState current state of the game
     * @return square with piece that we want to move on it
     */
    public BoardSquare straightLineChecker(BoardSquare square, StrategoGameState gameState){
        BoardSquare north = null;
        BoardSquare south = null;
        BoardSquare east = null;
        BoardSquare west = null;

        //north (moving up on board, row - 1 every time, col is constant)
        for(int i = square.getRow(); i >= 0; i--){
            if(gameState.getBoardSquares()[i][square.getCol()].getOccupied() &&
                    (gameState.getBoardSquares()[i][square.getCol()].getPiece().getTeam() == StrategoGameState.RED)){
                north = gameState.getBoardSquares()[i][square.getCol()];
                if(north.getPiece().getRank() == 2){
                    return north;
                }
                break;
            }
        }

        //south (moving down on board, row + 1 every time, col is constant)
        for(int i = square.getRow(); i < StrategoGameState.BOARD_SIZE; i++){
            if(gameState.getBoardSquares()[i][square.getCol()].getOccupied() &&
                    (gameState.getBoardSquares()[i][square.getCol()].getPiece().getTeam() == StrategoGameState.RED)){
                south = gameState.getBoardSquares()[i][square.getCol()];
                if(south.getPiece().getRank() == 2){
                    return south;
                }
                break;
            }
        }

        //east (moving right on board, col + 1 every time, row is constant)
        for(int i = square.getCol(); i < StrategoGameState.BOARD_SIZE; i++){
            if(gameState.getBoardSquares()[square.getRow()][i].getOccupied() &&
                    (gameState.getBoardSquares()[square.getRow()][i].getPiece().getTeam() == StrategoGameState.RED)){
                east = gameState.getBoardSquares()[square.getRow()][i];
                if(east.getPiece().getRank() == 2){
                    return east;
                }
                break;
            }
        }

        //west (moving left on board, col - 1 every time, row is constant)
        for(int i = square.getCol(); i >= 0; i--){
            if(gameState.getBoardSquares()[square.getRow()][i].getOccupied() &&
                    (gameState.getBoardSquares()[square.getRow()][i].getPiece().getTeam() == StrategoGameState.RED)){
                west = gameState.getBoardSquares()[square.getRow()][i];
                if(west.getPiece().getRank() == 2){
                    return west;
                }
                break;
            }
        }

        //if none of the pieces found are scouts, just return the first one that is occupied
        if(north != null){
            return north;
        }else if(south != null){
            return south;
        }else if(east != null){
            return east;
        }else if(west != null){
            return west;
        }else{
            return null;
        }
    }

    /**
     * checks if computer player's flag is reachable by human player's pieces, moves to defend the flag if possible
     * similar logic to flagAttack for the checks, just with teams swapped around
     * @param gameState current state of the game
     * @return source square for movement
     * TODO: maybe break this up into helper methods
     */
    public void flagDefend(StrategoGameState gameState){
        BoardSquare flag = null;
        //loop through and find our (the computer players) flag
        for(int i = 0; i < StrategoGameState.BOARD_SIZE; i++){
            for(int j = 0; j < StrategoGameState.BOARD_SIZE; j++){
                GamePiece flagg = gameState.getBoardSquares()[i][j].getPiece();
                if(flagg.getRank() == 0 && flagg.getTeam() == StrategoGameState.BLUE) {
                    flag = gameState.getBoardSquares()[i][j];
                }
            }
        }

        BoardSquare killThisOne = null;
        //check if the flag can be killed, and get the square of who is attacking the flag
        if((flag.getRow() - 1 >= 0) &&
                (gameState.getBoardSquares()[flag.getRow()-1][flag.getCol()].getPiece().getTeam() == StrategoGameState.RED)){
            this.shouldDefend = true;
            killThisOne = gameState.getBoardSquares()[flag.getRow()-1][flag.getCol()];
        }else if((flag.getRow() + 1 < StrategoGameState.BOARD_SIZE) &&
                (gameState.getBoardSquares()[flag.getRow()+1][flag.getCol()].getPiece().getTeam() == StrategoGameState.RED)){
            this.shouldDefend = true;
            killThisOne = gameState.getBoardSquares()[flag.getRow()+1][flag.getCol()];
        }else if((flag.getCol() - 1 >= 0) &&
                (gameState.getBoardSquares()[flag.getRow()][flag.getCol() - 1].getPiece().getTeam() == StrategoGameState.RED)){
            this.shouldDefend = true;
            killThisOne = gameState.getBoardSquares()[flag.getRow()][flag.getCol() - 1];
        }else if((flag.getCol() + 1 < StrategoGameState.BOARD_SIZE) &&
                (gameState.getBoardSquares()[flag.getRow()][flag.getCol() + 1].getPiece().getTeam() == StrategoGameState.RED)){
            this.shouldDefend = true;
            killThisOne = gameState.getBoardSquares()[flag.getRow()][flag.getCol() + 1];
        }

        //check if we can defend and kill the attacking piece, even a trade is fine here
        BoardSquare defendWithThis = null;
        if ( this.shouldDefend = true ) {
            if((killThisOne.getRow() - 1 >= 0) &&
                    (gameState.getBoardSquares()[killThisOne.getRow()-1][flag.getCol()].getPiece().getTeam() == StrategoGameState.BLUE) &&
                gameState.getBoardSquares()[killThisOne.getRow()-1][flag.getCol()].getPiece().getRank() >= killThisOne.getPiece().getRank()) {
                defendWithThis = gameState.getBoardSquares()[killThisOne.getRow()-1][flag.getCol()];
            }else if((flag.getRow() + 1 < StrategoGameState.BOARD_SIZE) &&
                    (gameState.getBoardSquares()[killThisOne.getRow()+1][flag.getCol()].getPiece().getTeam() == StrategoGameState.BLUE) &&
                            gameState.getBoardSquares()[killThisOne.getRow()+1][flag.getCol()].getPiece().getRank() >= killThisOne.getPiece().getRank()){
               defendWithThis = gameState.getBoardSquares()[killThisOne.getRow()+1][flag.getCol()];
            }else if((flag.getCol() - 1 >= 0) &&
                    (gameState.getBoardSquares()[killThisOne.getRow()][flag.getCol() - 1].getPiece().getTeam() == StrategoGameState.BLUE) &&
                    gameState.getBoardSquares()[killThisOne.getRow()][flag.getCol() - 1].getPiece().getRank() >= killThisOne.getPiece().getRank()){
                defendWithThis = gameState.getBoardSquares()[killThisOne.getRow()][flag.getCol() - 1];

            }else if((flag.getCol() + 1 < StrategoGameState.BOARD_SIZE) &&
                    (gameState.getBoardSquares()[killThisOne.getRow()][flag.getCol() + 1].getPiece().getTeam() == StrategoGameState.BLUE) &&
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
    }

    /**
     * Smart AI makes move to get closer to or attack opp marshall or bomb if either is visible
     *
     * @param gameState current state of the game
     */
    public void specialCaseAttack(StrategoGameState gameState) {
        boolean reachableBomb = false;
        boolean reachableMarshall = false;
        int squareSrc = -1;
        int squareDest = -1;
        int aBomb = -1;
        int aMarshall = -1;

        //look for visible and reachable opp marshall or bomb
        //TODO: choose specific bomb or marshall to attack
        for (int i = 0; i < StrategoGameState.BOARD_SIZE; i++) {
            for (int j = 0; j < StrategoGameState.BOARD_SIZE; j++) {
                //look for opp's piece that is visible
                if (gameState.getBoardSquares()[i][j].getPiece() != null
                    && gameState.getBoardSquares()[i][j].getPiece().getTeam() != playerNum && gameState.getBoardSquares()[i][j].getPiece().getVisible()) {
                    if (gameState.getBoardSquares()[i][j].getPiece().getRank() == GamePiece.BOMB && lonelySquare(gameState, i, j)) { //found a bomb
                        reachableBomb = true;
                        aBomb = i *10 + j;
                    } else if (gameState.getBoardSquares()[i][j].getPiece().getRank() == 10 && lonelySquare(gameState, i, j)) {  //found a marshall
                        reachableMarshall = true;
                        aMarshall = i *10 + j;
                    }
                }
            }
        }

        if (reachableBomb) {
            foundReachableBomb(squareSrc, squareDest, aBomb);
        }
        //TODO: do almost everything I did for bomb but for marshall
        if (reachableMarshall) {

        }

        game.sendAction(new StrategoMoveAction(this, squareSrc, squareDest));
    }

    /**
     * helper method for specialCaseAttack method. Holds logic for if comp found a bomb
     *
     * @param squareSrc     square with piece to move
     * @param squareDest    square to move piece to
     * @param aBomb         the bomb that was found
     */
    public void foundReachableBomb(int squareSrc, int squareDest, int aBomb) {
        //check if smart comp has any miner to attack bomb
        if (playerNum == 0 && gameState.getBlueGY()[5-1] == StrategoGameState.NUM_OF_PIECES[5]
            || playerNum == 1 && gameState.getRedGY()[5-1] == StrategoGameState.NUM_OF_PIECES[5]) {
            return;
        }

        int closestMiner = -1;
        //looking for a miner that can move around
        for (int i = 0; i < StrategoGameState.BOARD_SIZE; i++) {
            for (int j = 0; j < StrategoGameState.BOARD_SIZE; j++) {
                if (gameState.getBoardSquares()[i][j].getPiece() != null
                    && gameState.getBoardSquares()[i][j].getPiece().getRank() == 5 && lonelySquare(i, j)) {
                    squareSrc = i * 10 + j;
                }
                //check if new squareSrc is closer to bomb than old closestMiner
                if (closestMiner < 0 || closestSquare(squareSrc, closestMiner, aBomb)) {
                    closestMiner = squareSrc;
                }
            }
        }
        squareSrc = closestMiner;
        //at this point squareSrc should have the closest miner to the bomb that can move

        //find which move will get the miner even closer to the bomb
        //TODO: check if miner will attack if it moves in certain dir
        if (gameState.squareOnBoard(squareSrc + 10) && closestSquare(squareSrc + 10, squareSrc, aBomb)) {   //move down
            squareDest = squareSrc + 10;
        } else if (gameState.squareOnBoard(squareSrc - 10) && closestSquare(squareSrc - 10, squareSrc, aBomb)) {    //move up
            squareDest = squareSrc - 10;
        } else if (gameState.squareOnBoard(squareSrc + 1) && closestSquare(squareSrc + 1, squareSrc, aBomb)) {  //move right
            squareDest = squareSrc + 1;
        } else if (gameState.squareOnBoard(squareSrc - 1) && closestSquare(squareSrc - 1, squareSrc, aBomb)) {  //move left
            squareDest = squareSrc - 1;
        }

        //if trying to move miner into a lake square, move it right or left (whichever is closer to bomb)
        //this does no checking for any sort of attack so it will probably make bad moves sometimes
        //TODO: depending on how we implement how to choose which move to make, this could make infinite loop of moving into piece on same team
        if (gameState.isLakeSquare(squareDest)) {
            if (closestSquare(squareSrc + 1, squareSrc - 1, aBomb)
                ||  (gamestate.getBoardSquares[(squareSrc - 1) / 10][(squareDest - 1) % 10].getPiece != null
                && gamestate.getBoardSquares[(squareSrc - 1) / 10][(squareDest - 1) % 10].getPiece.getTeam == playerNum)) {
                squareDest = squareSrc + 1;
            } else {
                squareDest = squareSrc - 1;
            }
        }
    }

    /**
     * Finds which of two src squares is closer to given dest square
     *
     * @param squareSrc1    first src square to compare distance from dest square
     * @param squareSrc2    second src square to compare distance from dest square
     * @param squareDest    dest square
     * @return  true if squareSrc1 is closer to squareDest than squareSrc2 or they're equidistant from squareDest
     */
    private boolean closestSquare(int squareSrc1, int squareSrc2, int squareDest) {
        double src1ToDestDist = Math.sqrt(Math.pow((squareSrc1 / 10) -  (squareDest / 10), 2) + Math.pow((squareSrc1 % 10) -  (squareDest % 10), 2));
        double src2ToDestDist = Math.sqrt(Math.pow((squareSrc2 / 10) -  (squareDest / 10), 2) + Math.pow((squareSrc2 % 10) -  (squareDest % 10), 2));
        return (src1ToDestDist <= src2ToDestDist);
    }

    /**
     * Checks to see if a square has any unoccupied square next to it
     *
     * @param gameState current state of the game board
     * @param i row of a square
     * @param j col of a square
     * @return  true if there is an unoccupied square to top, bottom, left, or right of square at gamestate.getBoardSquares[i][j]
     */
    private boolean lonelySquare(StrategoGameState gameState, int i, int j) {
        if (gameState.squareOnBoard(i * 10 + j)
            && ((i + 1 < StrategoGameState.BOARD_SIZE && !gameState.getBoardSquares()[i + 1][j].getOccupied())
            || (i - 1 > 0 && !gameState.getBoardSquares()[i - 1][j].getOccupied())
            || (j + 1 < StrategoGameState.BOARD_SIZE && !gameState.getBoardSquares()[i][j + 1].getOccupied())
            || (j - 1 > 0 && !gameState.getBoardSquares()[i][j - 1].getOccupied()))) {
            return true;
        }
        return false;
    }

    /**
     * method to attack enemy scouts
     * @param gameState current state of the game
     */
    public void scoutAttack(StrategoGameState gameState){

    }

    //i think this method should return the firstCLick and secondClick that the computer wants to move on, in the case of the piece being hidden, these can be called
    //into the hiddenPieceAttack method
    public void normalAttack(StrategoGameState gameState){
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

    public void defaultMove(StrategoGameState gameState) {
        //find the furthest move piece towards the other player, (down the board if computer player is only player 2)
        BoardSquare moveThisOne = null;

        //loop through the board and find the piece for this player that can move the most forward
        for (int i = 0; i < StrategoGameState.BOARD_SIZE; i++) {
            for (int j = 0; j < StrategoGameState.BOARD_SIZE; j++) {
                if (gameState.getBoardSquares()[i][j].getPiece().getRank() != 0 && gameState.getBoardSquares()[i][j].getPiece().getRank() != 11) {
                    if (gameState.getBoardSquares()[i][j].getPiece().getTeam() == StrategoGameState.BLUE) {
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
    }

    /**
     * method that converts a given board square into the integer coordinate needed to create a move action
     * TODO: test if this works
     * @param square the board square to get the coordinate of
     * @return the coordinate of the board square
     */
    public int coordConverter(BoardSquare square){
        int ret = -1;
        if(square == null){
            return ret;
        }
        int row = square.getRow();
        int col = square.getCol();

        ret = (row * StrategoGameState.BOARD_SIZE) + col;
        return ret;
    }

}
