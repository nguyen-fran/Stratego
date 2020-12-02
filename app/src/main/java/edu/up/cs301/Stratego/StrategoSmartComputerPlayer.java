package edu.up.cs301.Stratego;

import android.util.Log;

import java.util.Random;

import edu.up.cs301.game.GameFramework.GameComputerPlayer;
import edu.up.cs301.game.GameFramework.infoMessage.GameInfo;
import edu.up.cs301.game.GameFramework.infoMessage.GameState;

/**
 * A smarter computer player to play Stratego
 * TODO: replace all checks for BLUE/RED so they will work with both blue player and red player configurations
 *
 * @author Gabby Marshak
 * @author Francisco Nguyen
 * @author Blake Nygren
 * @author Jack Volonte
 */
public class StrategoSmartComputerPlayer extends GameComputerPlayer {

    public boolean shouldDefend = false;
    private StrategoGameState gameState;
    private boolean moveSuccessful;

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
        if (!(info instanceof StrategoGameState)) {
            return;
        }
        gameState = new StrategoGameState((StrategoGameState) info);

        if (gameState.getCurrPlayerIndex() != playerNum) {
            return;
        }

        /*moveSuccess is used to determine if some kind of move has been made successfully and
        * should be changed to true before any move sending happens, so we can know whether to call
        * another helper method to attempt another type of move, or to return (reset to false every turn)*/
        moveSuccessful = false;

        //determining if smart computer player should make moves or set up the board depending on game phase
        if(gameState.getGamePhase()){
            //going down the list of different types of moves to make until one actually works

            flagDefend();
            if(moveSuccessful){
                Log.i("smart ai movement", "defended computer player's flag");
                return;
            }

            specialCaseAttack();
            if(moveSuccessful){
                Log.i("smart ai movement", "made special case attack");
                return;
            }

            scoutAttack();
            if(moveSuccessful){
                Log.i("smart ai movement", "made scout attack");
                return;
            }

            normalAttack();
            if(moveSuccessful){
                Log.i("smart ai movement", "made normal attack");
                return;
            }

            defaultMove();
            if(moveSuccessful){
                Log.i("smart ai movement", "made default move");
            }else{
                Log.i("smart ai movement", "could not make move. something went wrong");
            }

        }else{
            //TODO: add initial board setup/presets

            //make between 1 and 7 swaps between random pieces on the board
            //may need to add error checking
            Random rand = new Random();
            int swapNum = rand.nextInt(7) + 1;
            int swap1;
            int swap2;

            for(int i = 0; i < swapNum; i++){
                swap1 = rand.nextInt(100);
                swap2 = rand.nextInt(100);
                game.sendAction(new StrategoSwapAction(this, swap1, swap2));
                Log.i("smart ai setup", "swapped " + swap1 + " and " + swap2);
            }
        }
    }

    /**
     * checks if human player's flag piece is within one square of a computer player's piece
     * checks if in a straight line from a computer player's scouts
     * right now the method is just going to always know where the flag is whether or not it's visible
     * may need to formally remove depending on if it's needed or not
     */
    public void flagAttack(){
        BoardSquare flag = null;
        BoardSquare source = null;
        BoardSquare dest = null;

        int sourceCoord;
        int destCoord;

        //for loop through human player's pieces to find where the flag is
        BoardSquare current = null;
        for(int i = 0; i < StrategoGameState.BOARD_SIZE; i++){
            for(int j = 0; j < StrategoGameState.BOARD_SIZE; j++){
                current = gameState.getBoardSquares()[i][j];

                if(current.getOccupied() && current.getPiece() != null){
                    if(current.getPiece().getRank() == 0
                            && current.getPiece().getTeam() == StrategoGameState.BLUE) {
                        flag = gameState.getBoardSquares()[i][j];
                        break;
                    }
                }

            }
        }

        //if you return here, something went wrong, either on the board or in the for loop
        if(flag == null){
            return;
        }

        //check in straight lines from the human flag until it hits another piece or goes off the board
        source = straightLineChecker(flag);

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

        moveSuccessful = true;
        game.sendAction(new StrategoMoveAction(this, sourceCoord, destCoord));
    }

    /**
     * checks squares in a straight line from a given board square in all four directions
     * checking for computer player's pieces or the end of the board, whichever comes first
     * decides what to return by prioritising the first scout it finds
     * otherwise just the first occupied board square it finds
     * @param square board square we are checking around
     * @return square with piece that we want to move on it
     */
    public BoardSquare straightLineChecker(BoardSquare square){
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
     * TODO: maybe break this up into helper methods
     */
    public void flagDefend(){
        BoardSquare flag = null;
        //loop through and find computer player's flag
        BoardSquare current = null;
        for(int i = 0; i < StrategoGameState.BOARD_SIZE; i++){
            for(int j = 0; j < StrategoGameState.BOARD_SIZE; j++){
                current = gameState.getBoardSquares()[i][j];
                if (isPlayerPiece(current) && current.getPiece().getRank() == GamePiece.FLAG) {
                        flag = current;
                }
            }
        }

        //if we did not find the flag, then something went wrong here
        if(flag == null){
            Log.i("flagDefend", "could not find flag");
            return;
        }

        //finding the piece that is right next to the flag
        BoardSquare killThisOne = null;
        if ( flag.getRow() - 1 >= 0 ) {
            if (isOppPiece(gameState.getBoardSquares()[flag.getRow() - 1][flag.getCol()])) {
                killThisOne = gameState.getBoardSquares()[flag.getRow() - 1][flag.getCol()];
            }
        }
        if ( flag.getRow() + 1 < StrategoGameState.BOARD_SIZE) {
            if (isOppPiece(gameState.getBoardSquares()[flag.getRow() + 1][flag.getCol()])) {
                killThisOne = gameState.getBoardSquares()[flag.getRow() + 1][flag.getCol()];
            }
        }
        if ( flag.getCol() -1 >= 0 ) {
            if (isOppPiece(gameState.getBoardSquares()[flag.getRow()][flag.getCol() - 1])) {
                killThisOne = gameState.getBoardSquares()[flag.getRow()][flag.getCol() - 1];
            }
        }
        if ( flag.getCol() + 1 < StrategoGameState.BOARD_SIZE ) {
            if (isOppPiece(gameState.getBoardSquares()[flag.getRow()][flag.getCol() + 1])) {
                killThisOne = gameState.getBoardSquares()[flag.getRow()][flag.getCol() + 1];
            }
        }

        //attacking the piece that's about to attack the flag
        BoardSquare defendWithThis = null;
        if ( killThisOne != null ) {
            if (killThisOne.getRow() + 1 < StrategoGameState.BOARD_SIZE) {
                if (isPlayerPiece(gameState.getBoardSquares()[killThisOne.getRow() + 1][killThisOne.getCol()])) {
                    if ((killThisOne.getPiece().getVisible()
                            && killThisOne.getPiece().getRank() <= gameState.getBoardSquares()[killThisOne.getRow() + 1][killThisOne.getCol()].getPiece().getRank())
                            || (!killThisOne.getPiece().getVisible() && hiddenPieceAttack(gameState.getBoardSquares()[killThisOne.getRow() + 1][killThisOne.getCol()]))) {
                        defendWithThis = gameState.getBoardSquares()[killThisOne.getRow() + 1][killThisOne.getCol()];

                        moveSuccessful = true;
                        game.sendAction(new StrategoMoveAction(this, coordConverter(defendWithThis), coordConverter(killThisOne)));
                    }
                }
            }
            if (killThisOne.getRow() - 1 >= 0) {
                if (isPlayerPiece(gameState.getBoardSquares()[killThisOne.getRow() - 1][killThisOne.getCol()])) {
                    if ((killThisOne.getPiece().getVisible()
                            && killThisOne.getPiece().getRank() <= gameState.getBoardSquares()[killThisOne.getRow() - 1][killThisOne.getCol()].getPiece().getRank())
                            || (!killThisOne.getPiece().getVisible() && hiddenPieceAttack(gameState.getBoardSquares()[killThisOne.getRow() - 1][killThisOne.getCol()]))) {
                        defendWithThis = gameState.getBoardSquares()[killThisOne.getRow() - 1][killThisOne.getCol()];

                        moveSuccessful = true;
                        game.sendAction(new StrategoMoveAction(this, coordConverter(defendWithThis), coordConverter(killThisOne)));
                    }
                }
            }
            if (killThisOne.getCol() + 1 < StrategoGameState.BOARD_SIZE) {
                if (isPlayerPiece(gameState.getBoardSquares()[killThisOne.getRow()][killThisOne.getCol() + 1])) {
                    if ((killThisOne.getPiece().getVisible()
                            && killThisOne.getPiece().getRank() <= gameState.getBoardSquares()[killThisOne.getRow()][killThisOne.getCol() + 1].getPiece().getRank())
                            || (!killThisOne.getPiece().getVisible() && hiddenPieceAttack(gameState.getBoardSquares()[killThisOne.getRow()][killThisOne.getCol() + 1]))) {
                        defendWithThis = gameState.getBoardSquares()[killThisOne.getRow()][killThisOne.getCol() + 1];

                        moveSuccessful = true;
                        game.sendAction(new StrategoMoveAction(this, coordConverter(defendWithThis), coordConverter(killThisOne)));
                    }
                }
            }
            if (killThisOne.getCol() - 1 >= 0) {
                if (isPlayerPiece(gameState.getBoardSquares()[killThisOne.getRow()][killThisOne.getCol() - 1])) {
                    if ((killThisOne.getPiece().getVisible()
                            && killThisOne.getPiece().getRank() <= gameState.getBoardSquares()[killThisOne.getRow()][killThisOne.getCol() - 1].getPiece().getRank())
                            || (!killThisOne.getPiece().getVisible() && hiddenPieceAttack(gameState.getBoardSquares()[killThisOne.getRow()][killThisOne.getCol() - 1]))) {
                        defendWithThis = gameState.getBoardSquares()[killThisOne.getRow()][killThisOne.getCol() - 1];

                        moveSuccessful = true;
                        game.sendAction(new StrategoMoveAction(this, coordConverter(defendWithThis), coordConverter(killThisOne)));
                    }
                }
            }
        }
    }

    /**
     * Smart AI makes move to get closer to or attack opp marshall or bomb if either is visible
     */
    public void specialCaseAttack() {
        boolean reachableBomb = false;
        boolean reachableMarshall = false;
        BoardSquare aBomb = null;
        BoardSquare theMarshall = null;

        //look for visible and reachable opp marshall or bomb
        //TODO: choose specific bomb or marshall to attack
        for (int i = 0; i < StrategoGameState.BOARD_SIZE; i++) {
            for (int j = 0; j < StrategoGameState.BOARD_SIZE; j++) {
                //look for opp's piece that is visible
                if ((gameState.getBoardSquares()[i][j].getPiece() != null) &&
                        (gameState.getBoardSquares()[i][j].getPiece().getTeam() != playerNum) &&
                        (gameState.getBoardSquares()[i][j].getPiece().getVisible())) {
                    if (gameState.getBoardSquares()[i][j].getPiece().getRank() == GamePiece.BOMB && lonelySquare(i, j)) { //found a movable bomb
                        reachableBomb = true;
                        aBomb = gameState.getBoardSquares()[i][j];
                    } else if (gameState.getBoardSquares()[i][j].getPiece().getRank() == 10 && lonelySquare(i, j)) {  //found a movable marshall
                        reachableMarshall = true;
                        theMarshall = gameState.getBoardSquares()[i][j];
                    }
                }
            }
        }

        if (reachableBomb) {
            foundReachableBomb(aBomb);
        } else if (reachableMarshall) {
            foundReachableMarshall(theMarshall);
        } else {
            Log.i("special case attack", "did not find bomb or marshall to move");
            return;
        }
    }

    /**
     * helper method for specialCaseAttack method. Holds logic for if comp found a bomb
     *
     * @param aBomb the bomb that was found
     */
    public void foundReachableBomb(BoardSquare aBomb) {
        BoardSquare squareSrc = null;
        BoardSquare squareDest = null;

        //check if smart comp has any miner to attack bomb
        if (playerNum == StrategoGameState.BLUE && gameState.getBlueGY()[5-1] == StrategoGameState.NUM_OF_PIECES[5]
            || playerNum == StrategoGameState.RED && gameState.getRedGY()[5-1] == StrategoGameState.NUM_OF_PIECES[5]) {
            return;
        }

        BoardSquare closestMiner = null;
        //looking for a miner that can move around
        for (int i = 0; i < StrategoGameState.BOARD_SIZE; i++) {
            for (int j = 0; j < StrategoGameState.BOARD_SIZE; j++) {
                //found a miner that's movable in at least one direction (could be surrounded on three sides)
                if (isPlayerPiece(gameState.getBoardSquares()[i][j]) && gameState.getBoardSquares()[i][j].getPiece().getRank() == 5
                    && lonelySquare(i, j)) {
                    squareSrc = gameState.getBoardSquares()[i][j];
                }
                //check if new squareSrc is closer to bomb than old closestMiner
                if (closestMiner == null || closestSquare(squareSrc, closestMiner, aBomb)) {
                    closestMiner = squareSrc;
                }
            }
        }
        //if conditional is true, the for loops couldn't find a miner that's movable
        if (squareSrc  == null) {
            return;
        }

        squareSrc = closestMiner;
        //at this point squareSrc should have the closest miner to the bomb that can move

        squareDest = getDirToMove(squareSrc, aBomb);

        //check if failed to move
        if (squareDest == null) {
            return;
        }

        moveSuccessful = true;
        game.sendAction(new StrategoMoveAction(this, coordConverter(squareSrc), coordConverter(squareDest)));
    }

    /**
     * helper method for specialCaseAttack method. Holds logic for if comp found the marshall
     *
     * @param theMarshall   the marshall that was found
     */
    public void foundReachableMarshall(BoardSquare theMarshall) {
        BoardSquare squareSrc = null;
        BoardSquare squareDest = null;

        //check if smart comp has any miner to attack bomb
        if (playerNum == 0 && gameState.getBlueGY()[1-1] == StrategoGameState.NUM_OF_PIECES[1]
                || playerNum == 1 && gameState.getRedGY()[1-1] == StrategoGameState.NUM_OF_PIECES[1]) {
            return;
        }

        //looking for the spy piece
        for (int i = 0; i < StrategoGameState.BOARD_SIZE; i++) {
            for (int j = 0; j < StrategoGameState.BOARD_SIZE; j++) {
                if (isPlayerPiece(gameState.getBoardSquares()[i][j]) && gameState.getBoardSquares()[i][j].getPiece().getRank() == 1
                        && lonelySquare(i, j)) {
                    squareSrc = gameState.getBoardSquares()[i][j];
                }
            }
        }
        //if conditional is true, the spy isn't movable
        if (squareSrc == null) {
            return;
        }

        //setting dest to src. if dest == src after trying to move then failed to get closer
        squareDest = getDirToMove(squareSrc, theMarshall);

        //check if failed to move
        if (squareDest == null) {
            return;
        }

        moveSuccessful = true;
        game.sendAction(new StrategoMoveAction(this, coordConverter(squareSrc), coordConverter(squareDest)));
    }

    /**
     * Finds which of two src squares is closer to given dest square
     *
     * @param squareSrc1    coordinates of first src square to compare distance from dest square
     * @param squareSrc2    coordinates of second src square to compare distance from dest square
     * @param squareDest    coordinates of dest square
     * @return  true if squareSrc1 is closer to squareDest than squareSrc2 or they're equidistant from squareDest
     */
    private boolean closestSquare(BoardSquare squareSrc1, BoardSquare squareSrc2, BoardSquare squareDest) {
        double src1ToDestDist = Math.sqrt(Math.pow(squareSrc1.getRow() - squareDest.getRow(), 2) + Math.pow(squareSrc1.getCol() - squareDest.getCol(), 2));
        double src2ToDestDist = Math.sqrt(Math.pow(squareSrc2.getRow() - squareDest.getRow(), 2) + Math.pow(squareSrc2.getCol() - squareDest.getCol(), 2));
        return (src1ToDestDist <= src2ToDestDist);
    }

    /**
     * Checks to see if a square has any unoccupied square next to it
     *
     * @param i row of a square
     * @param j col of a square
     * @return  true if there is an unoccupied square to top, bottom, left, or right of square at gamestate.getBoardSquares[i][j]
     */
    private boolean lonelySquare(int i, int j) {
        if (gameState.squareOnBoard(coordToSquareConverter(i * 10 + j))
            && ((i + 1 < StrategoGameState.BOARD_SIZE && !gameState.getBoardSquares()[i + 1][j].getOccupied())
            || (i - 1 > 0 && !gameState.getBoardSquares()[i - 1][j].getOccupied())
            || (j + 1 < StrategoGameState.BOARD_SIZE && !gameState.getBoardSquares()[i][j + 1].getOccupied())
            || (j - 1 > 0 && !gameState.getBoardSquares()[i][j - 1].getOccupied()))) {
            return true;
        }
        return false;
    }

    /**
     * Find direction to move that will get the src square closer to its goal.
     * Avoids moves that will make the src square run into another piece or a lake square.
     *
     * @param squareSrc square to move
     * @param goal      square that squareSrc wants to reach in least amount of moves
     * @return  a squareDest that squareSrc will move to on its next move
     */
    private BoardSquare getDirToMove(BoardSquare squareSrc, BoardSquare goal) {
        BoardSquare squareDest;
        //find a squareDest that'll closer to goal than squareSrc and isn't occupied
        if (squareSrc.getRow() + 1 < StrategoGameState.BOARD_SIZE && closestSquare(coordToSquareConverter(coordConverter(squareSrc) + 10), squareSrc, goal)
                && !coordToSquareConverter(coordConverter(squareSrc) + 10).getOccupied()) {   //move down
            squareDest = coordToSquareConverter(coordConverter(squareSrc) + 10);
        } else if (squareSrc.getRow() - 1 >= 0 && closestSquare(coordToSquareConverter(coordConverter(squareSrc) - 10), squareSrc, goal)
                && !coordToSquareConverter(coordConverter(squareSrc) - 10).getOccupied()) {    //move up
            squareDest = coordToSquareConverter(coordConverter(squareSrc) - 10);
        } else if (squareSrc.getCol() + 1 < StrategoGameState.BOARD_SIZE && closestSquare(coordToSquareConverter(coordConverter(squareSrc) + 1), squareSrc, goal)
                && !coordToSquareConverter(coordConverter(squareSrc) + 1).getOccupied()) {  //move right
            squareDest = coordToSquareConverter(coordConverter(squareSrc) + 1);
        } else if (squareSrc.getCol() - 1 >= 0 && closestSquare(coordToSquareConverter(coordConverter(squareSrc) - 1), squareSrc, goal)
                && !coordToSquareConverter(coordConverter(squareSrc) - 1).getOccupied()) {  //move left
            squareDest = coordToSquareConverter(coordConverter(squareSrc) - 1);
        } else {
            return null;
        }

        //if trying to move squareSrc piece into a lake square, move it right or left (whichever is closer to goal)
        if (squareDest != null && gameState.isLakeSquare(squareDest)) {
            //if there are pieces to the left and right of squareSrc, don't move because the only move left is backwards
            if (!coordToSquareConverter(coordConverter(squareSrc) + 1).getOccupied() && !coordToSquareConverter(coordConverter(squareSrc) - 1).getOccupied()) {
                return null;
            }
            //move right if there is a piece to the left of squareSrc or if it'll get piece on squareSrc closer to the bomb
            if (coordToSquareConverter(coordConverter(squareSrc) - 1).getOccupied()
                    || closestSquare(coordToSquareConverter(coordConverter(squareSrc) + 1), coordToSquareConverter(coordConverter(squareSrc) - 1), goal)) {
                squareDest = coordToSquareConverter(coordConverter(squareSrc) + 1);
            } else {
                squareDest = coordToSquareConverter(coordConverter(squareSrc) - 1);
            }
        }
        return squareDest;
    }

    /**
     * method to attack enemy scouts
     */
    public void scoutAttack(){
        BoardSquare source = null;
        BoardSquare dest = null;
        BoardSquare current = null;
        int sourceCoord;
        int destCoord;

        //double for loop through board for human player's scouts (rank 2)
        for (int i = 0; i < StrategoGameState.BOARD_SIZE; i++) {
            for (int j = 0; j < StrategoGameState.BOARD_SIZE; j++) {
                current = gameState.getBoardSquares()[i][j];

                //checks first the the current piece is a visible scout owned by the human player
                if((current.getOccupied()) && (current.getPiece() != null) &&
                        (current.getPiece().getTeam() == StrategoGameState.BLUE) &&
                        (current.getPiece().getRank() == 2) && (current.getPiece().getVisible())){
                    //check for adjacent occupied squares with red pieces, then check visibility of current piece

                    //north
                    if((i+1 < StrategoGameState.BOARD_SIZE) &&
                            (gameState.getBoardSquares()[i+1][j].getPiece() != null) &&
                            (gameState.getBoardSquares()[i+1][j].getPiece().getTeam() == StrategoGameState.RED)){
                            source = gameState.getBoardSquares()[i+1][j];
                            dest = current;
                    }

                    //south
                    if((i-1 >= 0) &&
                            (gameState.getBoardSquares()[i-1][j].getPiece() != null) &&
                            (gameState.getBoardSquares()[i-1][j].getPiece().getTeam() == StrategoGameState.RED)){
                            source = gameState.getBoardSquares()[i-1][j];
                            dest = current;
                    }

                    //east
                    if((j+1 < StrategoGameState.BOARD_SIZE) &&
                            (gameState.getBoardSquares()[i][j+1].getPiece() != null) &&
                            (gameState.getBoardSquares()[i][j+1].getPiece().getTeam() == StrategoGameState.RED)){
                            source = gameState.getBoardSquares()[i][j+1];
                            dest = current;
                    }

                    //west
                    if((j-1 >= 0) &&
                            (gameState.getBoardSquares()[i][j-1].getPiece() != null) &&
                            (gameState.getBoardSquares()[i][j-1].getPiece().getTeam() == StrategoGameState.RED)){
                            source = gameState.getBoardSquares()[i][j-1];
                            dest = current;
                    }
                }
            }
        }

        if(source == null || dest == null){
            Log.i("scoutAttack", "source or destination square was null");
            return;
        }

        //send info
        sourceCoord = coordConverter(source);
        destCoord = coordConverter(dest);

        moveSuccessful = true;
        game.sendAction(new StrategoMoveAction(this, sourceCoord, destCoord));
    }

    /**
     * method to attack visible pieces that the computer can capture
     * specifically only looks at being directly adjacent for now but may add scout usage later
     */
    public void normalAttack() {
        BoardSquare current = null;
        BoardSquare source = null;
        BoardSquare dest = null;

        //loop through the board to check human player's pieces
        for (int i = 0; i < StrategoGameState.BOARD_SIZE; i++) {
            for (int j = 0; j < StrategoGameState.BOARD_SIZE; j++) {
                current = gameState.getBoardSquares()[i][j];

                if(isOppPiece(current)){
                    //check for adjacent occupied squares with comp pieces, then check rank and visibility of current piece
                    //if the current piece is invisible, then use the hidden piece attack

                    //north
                    if((i+1 < StrategoGameState.BOARD_SIZE) && isPlayerPiece(gameState.getBoardSquares()[i+1][j])){
                        //checking visibility/rank
                        if(!isBombOrFlag(gameState.getBoardSquares()[i+1][j])
                            && ((current.getPiece().getVisible() && gameState.getBoardSquares()[i+1][j].getPiece().getRank() > current.getPiece().getRank())
                            || (!current.getPiece().getVisible() && hiddenPieceAttack(gameState.getBoardSquares()[i+1][j])))){
                            source = gameState.getBoardSquares()[i+1][j];
                            dest = current;
                        }
                    }

                    //south
                    if((i-1 >= 0) && isPlayerPiece(gameState.getBoardSquares()[i-1][j])){
                        //checking visibility/rank
                        if(!isBombOrFlag(gameState.getBoardSquares()[i-1][j])
                            && ((current.getPiece().getVisible() && gameState.getBoardSquares()[i-1][j].getPiece().getRank() > current.getPiece().getRank())
                            || (!current.getPiece().getVisible() && hiddenPieceAttack(gameState.getBoardSquares()[i-1][j])))){
                            source = gameState.getBoardSquares()[i-1][j];
                            dest = current;
                        }
                    }

                    //east
                    if((j+1 < StrategoGameState.BOARD_SIZE) && isPlayerPiece(gameState.getBoardSquares()[i][j+1])){
                        //checking visibility/rank
                        if(!isBombOrFlag(gameState.getBoardSquares()[i][j+1])
                            && ((current.getPiece().getVisible() && gameState.getBoardSquares()[i][j+1].getPiece().getRank() > current.getPiece().getRank())
                            || (!current.getPiece().getVisible() && hiddenPieceAttack(gameState.getBoardSquares()[i][j+1])))){
                            source = gameState.getBoardSquares()[i][j+1];
                            dest = current;
                        }
                    }

                    //west
                    if((j-1 >= 0) && isPlayerPiece(gameState.getBoardSquares()[i][j-1])){
                        //checking visibility/rank
                        if(!isBombOrFlag(gameState.getBoardSquares()[i][j-1])
                            && ((current.getPiece().getVisible() && gameState.getBoardSquares()[i][j-1].getPiece().getRank() > current.getPiece().getRank())
                            || (!current.getPiece().getVisible() && hiddenPieceAttack(gameState.getBoardSquares()[i][j-1])))){
                            source = gameState.getBoardSquares()[i][j-1];
                            dest = current;
                        }
                    }
                }
            }
        }

        if(source == null || dest == null){
            Log.i("normal attack", "could not get either a source or destination to attack with");
            return;
        }

        int sourceCoord = coordConverter(source);
        int destCoord = coordConverter(dest);

        moveSuccessful = true;
        game.sendAction(new StrategoMoveAction(this, sourceCoord, destCoord));
    }

    /**
     *calculates odds of successfully attacking a piece that has not been revealed to the computer player
     *
     * @param attackSquare   a comp square that is adjacent to a square with an invisible opp piece
     * @return true if the computer will attack the piece, false if it will not
     */
    public boolean hiddenPieceAttack(BoardSquare attackSquare) {
        int attackPieceRank = attackSquare.getPiece().getRank();
        //getting the graveyard
        int[] oppGY;
        if (playerNum == StrategoGameState.BLUE) {
            oppGY = gameState.getRedGY();
        } else {
            oppGY = gameState.getBlueGY();
        }
        /*will increment for every opp piece alive whose rank is less than attackPieceRank
        and decrement for every opp piece alive whose rank is greater than or equal to attackPieceRank
        if by the end oddOfWinning is > 0, comp can probably win*/
        int oddsOfWinning = 0;

        //adjusting oddsOfWinning based on all of opp's alive pieces
        for (int i = 0; i < oppGY.length - 1; i++) {
            //REMINDER: index in GY array = piece's rank - 1 (ex. rank 5 piece deaths are counted in oppGY[4])
            //'i' right now is index in GY array, so piece rank to compare is (i + 1)
            if (i + 1 < attackPieceRank) {
                //add num of pieces of rank (i + 1) to oddsOfWinning because comp would win if it attacked them
                oddsOfWinning += StrategoGameState.NUM_OF_PIECES[i + 1] - oppGY[i];
            } else {
                //subtract num of pieces of rank (i + 1) from oddsOfWinning because comp would lose if it attacked them
                oddsOfWinning -= StrategoGameState.NUM_OF_PIECES[i + 1] - oppGY[i];
            }
        }

        //account for all the opp pieces that are visible
        for (int i = 0; i < StrategoGameState.BOARD_SIZE; i++) {
            for (int j = 0; j < StrategoGameState.BOARD_SIZE; j++) {
                if (isOppPiece(gameState.getBoardSquares()[i][j]) && gameState.getBoardSquares()[i][j].getPiece().getVisible()) {
                    //if there is an opp piece of greater rank that is visible, comp knows that it isn't attacking it
                    if (gameState.getBoardSquares()[i][j].getPiece().getRank() >= attackPieceRank) {
                        oddsOfWinning++;
                    } else {
                        oddsOfWinning--;
                    }
                }
            }
        }

        Log.i("hiddenPieceAttack", "calculated hidden piece odds:" + oddsOfWinning);
        return (oddsOfWinning > 0);
    }

    /**
     * default movement action (used if no other type of movment can be done)
     * TODO: can occasionally cause the ai to hang if it can't find a move to make
     */
    public void defaultMove() {
        //find the furthest move piece towards the other player, (down the board if computer player is only player 2)
        BoardSquare moveThisOne = null;
        BoardSquare moveThisIfRandomIsBad = null;
        boolean moveForward = false;
        int step;
        if (playerNum == StrategoGameState.BLUE) {
            step = -1;
        } else {
            step = 1;
        }

        //loop through the board and find the piece for this player that can move the most forward
        for (int i = 0; i < StrategoGameState.BOARD_SIZE; i++) {
            for (int j = 0; j < StrategoGameState.BOARD_SIZE; j++) {
                if ((isPlayerPiece(gameState.getBoardSquares()[i][j])) &&
                    (!isBombOrFlag(gameState.getBoardSquares()[i][j])) &&
                    (i + step < StrategoGameState.BOARD_SIZE && i + step >= 0) &&
                    (!gameState.getBoardSquares()[i+step][j].getOccupied())){
                        if ( gameState.getBoardSquares()[i+step][j] != null ) {
                            moveSuccessful = true;
                            moveThisOne = gameState.getBoardSquares()[i][j];
                            game.sendAction(new StrategoMoveAction(this, coordConverter(moveThisOne), coordConverter(gameState.getBoardSquares()[moveThisOne.getRow() + step][moveThisOne.getCol()])));
                            return;
                        }
                }
            }
        }

        //if we cant find a piece to move forward, then we gotta move left or right with a piece
        //if can move left or right, set that boardSquare to a variable
        //if you cant move forward, then find one that can move left or right
        for (int i = 0; i < StrategoGameState.BOARD_SIZE; i++) {
            for (int j = 0; j < StrategoGameState.BOARD_SIZE; j++) {
                if ((isPlayerPiece(gameState.getBoardSquares()[i][j])) &&
                    (!isBombOrFlag(gameState.getBoardSquares()[i][j])) &&
                    (j + 1 < StrategoGameState.BOARD_SIZE) &&
                    (!gameState.getBoardSquares()[i][j+1].getOccupied())){
                    if ( gameState.getBoardSquares()[i][j+1] != null ) {
                        moveSuccessful = true;
                        game.sendAction(new StrategoMoveAction(this, coordConverter(gameState.getBoardSquares()[i][j]), coordConverter(gameState.getBoardSquares()[i][j+1])));
                        return;
                    }
                } else if ((isPlayerPiece(gameState.getBoardSquares()[i][j])) &&
                        (!isBombOrFlag(gameState.getBoardSquares()[i][j])) &&
                        (j - 1 >= 0) &&
                        (!gameState.getBoardSquares()[i][j-1].getOccupied())){
                    if ( gameState.getBoardSquares()[i][j-1] != null ) {
                        moveSuccessful = true;
                        game.sendAction(new StrategoMoveAction(this, coordConverter(gameState.getBoardSquares()[i][j]), coordConverter(gameState.getBoardSquares()[i][j-1])));
                        return;
                    }
                }
            }
        }

        //if cant move forwards or side to side, then backwards? idk if this will ever be reached but yea
        for (int i = 0; i < StrategoGameState.BOARD_SIZE; i++) {
            for (int j = 0; j < StrategoGameState.BOARD_SIZE; j++) {
                if ((isPlayerPiece(gameState.getBoardSquares()[i][j])) &&
                        (!isBombOrFlag(gameState.getBoardSquares()[i][j])) &&
                        (i - step < StrategoGameState.BOARD_SIZE && i - step >= 0) &&
                        (!gameState.getBoardSquares()[i-step][j].getOccupied())){
                    if ( gameState.getBoardSquares()[i-step][j] != null ) {
                        moveSuccessful = true;
                        moveThisOne = gameState.getBoardSquares()[i][j];
                        game.sendAction(new StrategoMoveAction(this, coordConverter(moveThisOne), coordConverter(gameState.getBoardSquares()[moveThisOne.getRow() - step][moveThisOne.getCol()])));
                        return;
                    }
                }
            }
        }
    }

    /**
     * checks if a square has a bomb or flag on it
     *
     * @param square    the boardsquare to check
     * @return  true if square has either a bomb or a flag occupying it
     */
    private boolean isBombOrFlag(BoardSquare square) {
        return (square.getPiece() != null && (square.getPiece().getRank() == GamePiece.BOMB || square.getPiece().getRank() == GamePiece.FLAG));
    }

    /**
     * method that converts a given board square into the integer coordinate needed to create a move action
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

    /**
     * converts given coordinates for a BoardSquare in gameStates.boardSquares
     *
     * @param square    coordinates for a square
     * @return  BoardSquare at given coordinates
     */
    public BoardSquare coordToSquareConverter(int square) {
        if (square < 0 || square >= 100) {
            return null;
        }
        return gameState.getBoardSquares()[square / 10][square % 10];
    }

    /**
     * check if a square is occupied by an opponent piece
     *
     * @param square    square to check
     * @return  true if the square's gamepiece is occupied by an opp piece
     */
    private boolean isOppPiece(BoardSquare square) {
        return (square.getPiece() != null && square.getPiece().getTeam() != playerNum);
    }

    /**
     * check if a square is occupied by a comp piece
     *
     * @param square    square to check
     * @return  true if the square's gamepiece is occupied by a comp's piece
     */
    private boolean isPlayerPiece(BoardSquare square) {
        return (square.getPiece() != null && square.getPiece().getTeam() == playerNum);
    }
}
