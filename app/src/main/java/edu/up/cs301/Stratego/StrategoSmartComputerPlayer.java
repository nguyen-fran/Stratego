package edu.up.cs301.Stratego;

import android.util.Log;

import java.util.Random;

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
        
        try{
            Thread.sleep(2000);
        }catch(Exception e){
            e.printStackTrace();
        }

        //determining if smart computer player should make moves or set up the board depending on game phase
        if(gameState.getGamePhase()){
            //going down the list of different types of moves to make until one actually works
            
            flagDefend();
            if(moveSuccessful){
                Log.i("smart ai movement", "defended computer player's flag");
                return;
            } else {
                Log.i("smart ai movement", "failed to defend computer player's flag");
            }

            specialCaseAttack();
            if(moveSuccessful){
                Log.i("smart ai movement", "made special case attack");
                return;
            } else {
                Log.i("smart ai movement", "failed to make special case attack");
            }

            scoutAttack();
            if(moveSuccessful){
                Log.i("smart ai movement", "made scout attack");
                return;
            } else {
                Log.i("smart ai movement", "failed to make scout attack");
            }

            normalAttack();
            if(moveSuccessful){
                Log.i("smart ai movement", "made normal attack");
                return;
            } else {
                Log.i("smart ai movement", "failed to make normal attack");
            }

            hiddenPieceAttack();
            if ( moveSuccessful) {
                Log.i("smart ai movement", "made hidden piece attack");
                return;
            } else {
                Log.i("smart ai movement", "failed to make hidden piece attack");
            }

            defaultMove();
            if(moveSuccessful){
                Log.i("smart ai movement", "made default move");
                return;
            }

            lastResortMove();
            if (moveSuccessful) {
                Log.i("smart ai movement", "made last resort move");
            }
            else {
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
                swap1 = rand.nextInt((StrategoGameState.BOARD_SIZE)*(StrategoGameState.BOARD_SIZE));
                swap2 = rand.nextInt((StrategoGameState.BOARD_SIZE)*(StrategoGameState.BOARD_SIZE));
                game.sendAction(new StrategoSwapAction(this, swap1, swap2));
                Log.i("smart ai setup", "swapped " + swap1 + " and " + swap2);
            }
        }
    }

    /**
     * checks if computer player's flag is reachable by human player's pieces, moves to defend the flag if possible
     * similar logic to flagAttack for the checks, just with teams swapped around
     */
    public void flagDefend(){
        BoardSquare flag = null;

        //loop through and find computer player's flag
        BoardSquare current = null;
        for(int i = 0; i < StrategoGameState.BOARD_SIZE; i++){
            for(int j = 0; j < StrategoGameState.BOARD_SIZE; j++){
                current = gameState.getBoardSquares()[i][j];
                if (current.getPiece() != null && current.getPiece().getRank() == 0 &&
                        isCompPiece(current)) {
                    flag = current;
                }
            }
        }

        //if we did not find the flag, then something went wrong here
        if(flag == null){
            Log.i("flagDefend", "could not find human player's flag");
            return;
        }

        //finding the piece thats attacking the flag
        BoardSquare killThisOne = null;
        if ( flag.getRow() - 1 >= 0 &&
                isHumanPiece(gameState.getBoardSquares()[flag.getRow() - 1][flag.getCol()])) {
            killThisOne = gameState.getBoardSquares()[flag.getRow() - 1][flag.getCol()];
        } else if ( flag.getRow() + 1 < StrategoGameState.BOARD_SIZE &&
                isHumanPiece(gameState.getBoardSquares()[flag.getRow() + 1][flag.getCol()])) {
            killThisOne = gameState.getBoardSquares()[flag.getRow() + 1][flag.getCol()];
        } else if ( flag.getCol() -1 >= 0 &&
                isHumanPiece(gameState.getBoardSquares()[flag.getRow()][flag.getCol() - 1])) {
            killThisOne = gameState.getBoardSquares()[flag.getRow()][flag.getCol() - 1];
        } else if ( flag.getCol() + 1 < StrategoGameState.BOARD_SIZE &&
                isHumanPiece(gameState.getBoardSquares()[flag.getRow()][flag.getCol() + 1])) {
            killThisOne = gameState.getBoardSquares()[flag.getRow()][flag.getCol() + 1];
        }

        if(killThisOne == null || isCompPiece(killThisOne)){
            Log.i("flagDefend", "could not find piece to kill");
            return;
        }

        Log.i("flagDefend", "killThisOne rank is: " + killThisOne.getPiece().getRank() +
                " at row " + killThisOne.getRow() + " and col " + killThisOne.getCol());

        //attacking the piece thats about to fuck up the flag
        BoardSquare defendWithThis = null;
        if (killThisOne.getRow() + 1 < StrategoGameState.BOARD_SIZE) {
            defendWithThis = gameState.getBoardSquares()[killThisOne.getRow() + 1][killThisOne.getCol()];
        }else if (killThisOne.getRow() - 1 >= 0) {
            defendWithThis = gameState.getBoardSquares()[killThisOne.getRow() - 1][killThisOne.getCol()];
        }else if (killThisOne.getCol() + 1 < StrategoGameState.BOARD_SIZE) {
            defendWithThis = gameState.getBoardSquares()[killThisOne.getRow()][killThisOne.getCol() + 1];
        }else if (killThisOne.getCol() - 1 >= 0) {
            defendWithThis = gameState.getBoardSquares()[killThisOne.getRow()][killThisOne.getCol() - 1];
        }

        if ((defendWithThis.getPiece() != null) &&
                (defendWithThis.getPiece().getRank() != 0 && defendWithThis.getPiece().getRank() != 11) &&
                (defendWithThis.getPiece().getTeam() == playerNum) && (defendWithThis.getPiece().getRank() >= killThisOne.getPiece().getRank())){

            moveSuccessful = true;
            game.sendAction(new StrategoMoveAction(this, coordConverter(defendWithThis), coordConverter(killThisOne)));
        }else{
            Log.i("flagDefend", "defendWithThis does not meet proper criteria");
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
                if (isCompPiece(gameState.getBoardSquares()[i][j]) && gameState.getBoardSquares()[i][j].getPiece().getRank() == 5
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
                if (isCompPiece(gameState.getBoardSquares()[i][j]) && gameState.getBoardSquares()[i][j].getPiece().getRank() == 1
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
                if((current.getOccupied()) &&
                        (isHumanPiece(current)) &&
                        (current.getPiece().getRank() == 2) && (current.getPiece().getVisible())){

                    //check for adjacent occupied squares with computer player pieces, then check visibility of current piece
                    //north
                    if((i+1 < StrategoGameState.BOARD_SIZE) &&
                            isCompPiece(gameState.getBoardSquares()[i+1][j])){
                            source = gameState.getBoardSquares()[i+1][j];
                            dest = current;
                    }

                    //south
                    if((i-1 >= 0) &&
                            isCompPiece(gameState.getBoardSquares()[i-1][j])){
                            source = gameState.getBoardSquares()[i-1][j];
                            dest = current;
                    }

                    //east
                    if((j+1 < StrategoGameState.BOARD_SIZE) &&
                            isCompPiece(gameState.getBoardSquares()[i][j+1])){
                            source = gameState.getBoardSquares()[i][j+1];
                            dest = current;
                    }

                    //west
                    if((j-1 >= 0) &&
                            isCompPiece(gameState.getBoardSquares()[i][j-1])){
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
     * specifically only looks at being directly adjacent
     *
     * will attack a visible enemy piece if its in range, and the rank is <= the piece that's attacking
     */
    public void normalAttack() {
        BoardSquare attackWith = null;
        BoardSquare defendWith = null;
        for ( int i = 0; i < 10; i++ ) {
            for (int j = 0; j < 10; j++) {
                attackWith = gameState.getBoardSquares()[i][j];
                if (attackWith.getPiece() != null) {
                    if (attackWith.getPiece().getRank() == 0 || attackWith.getPiece().getRank() == 11) {
                        Log.i("normalAttack", "trying to move immobile piece");
                        return;
                    } else {
                        if (attackWith.getPiece() != null && attackWith.getPiece().getTeam() == playerNum) {
                            normalAttackHelper(attackWith, defendWith, 0, 1);
                            normalAttackHelper(attackWith, defendWith, 0, -1);
                            normalAttackHelper(attackWith, defendWith, 1, 0);
                            normalAttackHelper(attackWith, defendWith, -1, 0);
                        }
                    }
                }
            }
        }
    }

    /**
     * helper method for normal attack, performs validity checking on a given set of board squares and direction to move in
     * @param attackWith piece that the computer player is attacking with
     * @param defendWith piece that the human player is defending with
     * @param row rows to move in (should only ever be 1 or -1)
     * @param col columns to move in (should only ever be 1 or -1)
     */
    public void normalAttackHelper(BoardSquare attackWith, BoardSquare defendWith, int row, int col) {
        if (attackWith.getCol() + col < 10 && attackWith.getCol() + col >= 0 && attackWith.getRow() + row >= 0 && attackWith.getRow() + row < 10) {
            defendWith = gameState.getBoardSquares()[attackWith.getRow() + row][attackWith.getCol() + col];
            if (defendWith.getPiece() != null) {
                if (defendWith.getPiece().getVisible() && defendWith.getPiece().getTeam() != playerNum) {
                    if ( defendWith.getPiece().getRank() <= attackWith.getPiece().getRank()  ||
                            defendWith.getPiece().getRank() == 10 && attackWith.getPiece().getRank() == 1   ||
                            defendWith.getPiece().getRank() == 11 && attackWith.getPiece().getRank() == 5  ) {
                        moveSuccessful = true;
                        Log.i("normalAttack", "Tried attacking with: [" + attackWith.getRow() + ", " + attackWith.getCol() +
                                "], rank " + attackWith.getPiece().getRank() + " to [" + defendWith.getRow() + ", " +
                                defendWith.getCol() + "], rank " + defendWith.getPiece().getRank());
                    }
                }
            }
        }
    }

    /**
     *calculates odds of successfully attacking a piece that has not been revealed to the computer player
     */
    //NEEDS TO BE IMPLEMENTED SOMEWHERE
    public void hiddenPieceAttack() {
        //getting the graveyard
        int[] blueGY = gameState.getBlueGY();
        int[] pieceNumbers = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

        //setting up doubles/ints for math later
        int totalDead = 0;
        int weCanWin = 0;
        int weWillLose = 0;
        int totalPieces = 40;

        //getting square we want to attack with
        BoardSquare defendingSquare = null;
        BoardSquare attackingSquare = null;
        for ( int j = 9; j >= 0; j-- ) {
            for ( int k = 9; k >= 0; k-- ) {
                if ( j - 1 >= 0 ) {
                    if (gameState.getBoardSquares()[j - 1][k].getPiece() != null &&  gameState.getBoardSquares()[j][k].getPiece() != null) {

                        if (gameState.getBoardSquares()[j - 1][k].getPiece().getTeam() != playerNum &&
                                !gameState.getBoardSquares()[j - 1][k].getPiece().getVisible() &&
                                gameState.getBoardSquares()[j][k].getPiece().getTeam() == playerNum ) {
                            defendingSquare = gameState.getBoardSquares()[j - 1][k];
                            attackingSquare = gameState.getBoardSquares()[j][k];
                        }

                    }
                }
                if ( j + 1 < 10 ) {
                    if (gameState.getBoardSquares()[j + 1][k].getPiece() != null &&  gameState.getBoardSquares()[j][k].getPiece() != null ) {

                        if (gameState.getBoardSquares()[j + 1][k].getPiece().getTeam() != playerNum &&
                                !gameState.getBoardSquares()[j + 1][k].getPiece().getVisible() &&
                                gameState.getBoardSquares()[j][k].getPiece().getTeam() == playerNum) {
                            defendingSquare = gameState.getBoardSquares()[j + 1][k];
                            attackingSquare = gameState.getBoardSquares()[j][k];
                        }

                    }
                }
                if ( k + 1 < 10 ) {
                    if (gameState.getBoardSquares()[j][k + 1].getPiece() != null &&  gameState.getBoardSquares()[j][k].getPiece() != null ) {

                        if (gameState.getBoardSquares()[j][k + 1].getPiece().getTeam() != playerNum &&
                                !gameState.getBoardSquares()[j][k + 1].getPiece().getVisible() &&
                                gameState.getBoardSquares()[j][k].getPiece().getTeam() == playerNum) {
                            defendingSquare = gameState.getBoardSquares()[j][k + 1];
                            attackingSquare = gameState.getBoardSquares()[j][k];
                        }

                    }
                }
                if ( k - 1 >= 0 ) {
                    if (gameState.getBoardSquares()[j][k - 1].getPiece() != null &&  gameState.getBoardSquares()[j][k].getPiece() != null) {

                        if (gameState.getBoardSquares()[j][k - 1].getPiece().getTeam() != playerNum &&
                                !gameState.getBoardSquares()[j][k - 1].getPiece().getVisible() &&
                                gameState.getBoardSquares()[j][k].getPiece().getTeam() == playerNum) {
                            defendingSquare = gameState.getBoardSquares()[j][k - 1];
                            attackingSquare = gameState.getBoardSquares()[j][k];
                        }

                    }
                }
            }
        }

        for ( int i = 0; i < blueGY.length - 1; i++ ) {
            //if rank of attacking piece < piece rank in array, then add 1 to win
            if (attackingSquare != null) {
                if (pieceNumbers[i] > attackingSquare.getPiece().getRank()) {
                    //if rank of attacking piece is < piece rank in the # array, then add 1 to lose
                    weWillLose += blueGY[i];
                } else {
                    weCanWin += blueGY[i];
                }
                totalDead += blueGY[i];
            }
        }

        totalPieces = totalPieces - totalDead;
        Log.i("total pieces", "" + totalPieces);

        //doing math for winning/losing
        if ( attackingSquare != null && defendingSquare != null &&
                !(isBombOrFlag(attackingSquare)) && (weCanWin != 0)) {
            Log.i("", " +  " + weCanWin);
            double weWin = (double) weCanWin;
            double total = (double) totalPieces;
            double chanceOfWinning = (weWin / total) * 100;
            Log.i("hiddenPieceAttack", "chance of winning: " + chanceOfWinning);
            if (chanceOfWinning >= 6) {
                moveSuccessful = true;
                Log.i("hiddenPieceAttack", "trying to move: " + attackingSquare.getRow() + ", " +
                        attackingSquare.getCol() + " to: " + defendingSquare.getRow() + ", " + defendingSquare.getCol());
                game.sendAction(new StrategoMoveAction(this, coordConverter(attackingSquare), coordConverter(defendingSquare)));
            }else{
                Log.i("hiddenPieceAttack", "chance of winning less than 60%");
            }
        }
    }

    /**
     * default movement action (used if no other type of movment can be done)
     */
    public void defaultMove() {
        //find the furthest move piece towards the other player, (down the board if computer player is only player 2)
        BoardSquare moveThisOne = null;
        int step;
        //for comp as player 1 functionality
        if (playerNum == StrategoGameState.BLUE) {
            //default move up a row
            step = -1;
        } else {
            //default move down a row
            step = 1;
        }

        //loop through the board and find the piece for this player that can move the most forward
        for (int i = 0; i < StrategoGameState.BOARD_SIZE; i++) {
            for (int j = 0; j < StrategoGameState.BOARD_SIZE; j++) {
                if ((isCompPiece(gameState.getBoardSquares()[i][j])) &&
                        (!isBombOrFlag(gameState.getBoardSquares()[i][j])) &&
                        (i + step < StrategoGameState.BOARD_SIZE && i + step >= 0) &&
                        (!gameState.getBoardSquares()[i+step][j].getOccupied())){
                    moveSuccessful = true;
                    moveThisOne = gameState.getBoardSquares()[i][j];
                    game.sendAction(new StrategoMoveAction(this, coordConverter(moveThisOne),
                            coordConverter(gameState.getBoardSquares()[moveThisOne.getRow() + step][moveThisOne.getCol()])));
                    return;
                }
            }
        }

        //if we cant find a piece to move forward, then we gotta move left or right
        //if can move left or right, set that boardSquare to a variable
        for (int i = 0; i < StrategoGameState.BOARD_SIZE; i++) {
            for (int j = 0; j < StrategoGameState.BOARD_SIZE; j++) {
                if (isCompPiece(gameState.getBoardSquares()[i][j]) && !isBombOrFlag(gameState.getBoardSquares()[i][j])) {
                    if ((j + 1 < StrategoGameState.BOARD_SIZE) && (!gameState.getBoardSquares()[i][j + 1].getOccupied())) {
                        moveSuccessful = true;
                        game.sendAction(new StrategoMoveAction(this, coordConverter(gameState.getBoardSquares()[i][j]),
                                coordConverter(gameState.getBoardSquares()[i][j + 1])));
                        return;
                    } else if ((j - 1 >= 0) && (!gameState.getBoardSquares()[i][j - 1].getOccupied())) {
                        moveSuccessful = true;
                        game.sendAction(new StrategoMoveAction(this, coordConverter(gameState.getBoardSquares()[i][j]),
                                coordConverter(gameState.getBoardSquares()[i][j - 1])));
                        return;
                    }
                }
            }
        }

        //if cant move forward or sideways, then backwards
        for (int i = 0; i < StrategoGameState.BOARD_SIZE; i++) {
            for (int j = 0; j < StrategoGameState.BOARD_SIZE; j++) {
                if ((isCompPiece(gameState.getBoardSquares()[i][j])) &&
                        (!isBombOrFlag(gameState.getBoardSquares()[i][j])) &&
                        (i + step < StrategoGameState.BOARD_SIZE && i - step >= 0) &&
                        (!gameState.getBoardSquares()[i + step][j].getOccupied())){
                    moveSuccessful = true;
                    moveThisOne = gameState.getBoardSquares()[i][j];
                    game.sendAction(new StrategoMoveAction(this, coordConverter(moveThisOne),
                            coordConverter(gameState.getBoardSquares()[moveThisOne.getRow() - step][moveThisOne.getCol()])));
                    return;
                }
            }
        }
    }

    /**
     * if there is absolutely nothing else comp can do, then make a random move
     */
    public void lastResortMove() {
        BoardSquare squareSrc = null;
        BoardSquare squareDest = null;
        for (int i = 0; i < StrategoGameState.BOARD_SIZE; i++) {
            for (int j = 0; j < StrategoGameState.BOARD_SIZE; j++) {
                if (isCompPiece(gameState.getBoardSquares()[i][j]) && !isBombOrFlag(gameState.getBoardSquares()[i][j])) {
                    squareSrc = gameState.getBoardSquares()[i][j];
                }
            }
        }

        if(squareSrc == null || squareSrc.getPiece() == null){
            Log.i("lastResortMove", "squareSrc or the piece on it did not exist");
            return;
        }

        Random rand = new Random();
        int randDir = rand.nextInt(4);
        switch (randDir) {
            case 0: //move down
                if (squareSrc.getRow() + 1 < StrategoGameState.BOARD_SIZE) {
                    squareDest = gameState.getBoardSquares()[squareSrc.getRow() + 1][squareSrc.getCol()];
                }
                break;
            case 1: //move up
                if (squareSrc.getRow() - 1 >= 0) {
                    squareDest = gameState.getBoardSquares()[squareSrc.getRow() - 1][squareSrc.getCol()];
                }
                break;
            case 2: //move right
                if (squareSrc.getCol() + 1 < StrategoGameState.BOARD_SIZE) {
                    squareDest = gameState.getBoardSquares()[squareSrc.getRow()][squareSrc.getCol() + 1];
                }
                break;
            case 3: //move left
                if (squareSrc.getCol() - 1 >= 0) {
                    squareDest = gameState.getBoardSquares()[squareSrc.getRow()][squareSrc.getCol() - 1];
                }
                break;
            default:
                break;
        }

        moveSuccessful = true;
        game.sendAction(new StrategoMoveAction(this, coordConverter(squareSrc), coordConverter(squareDest)));
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
     * @return  true if the square's gamepiece is occupied by a human player's piece
     */
    private boolean isHumanPiece(BoardSquare square) {
        return (square.getPiece() != null && square.getPiece().getTeam() != playerNum);
    }

    /**
     * check if a square is occupied by a comp piece
     *
     * @param square    square to check
     * @return  true if the square's gamepiece is occupied by a computer player's piece
     */
    private boolean isCompPiece(BoardSquare square) {
        return (square.getPiece() != null && square.getPiece().getTeam() == playerNum);
    }
}
