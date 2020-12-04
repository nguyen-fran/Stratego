package edu.up.cs301.Stratego;

import android.se.omapi.SEService;
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

    /**
     * External Citation
     * Date: 3 December 2020
     * Problem: Needed professional setups for default smart ai setups
     *
     * Resource: https://www.ultraboardgames.com/stratego/setups.php
     * Solution: Used setups on the site
     */
    //Vincent Deboer first setup on the site
    public static final int[][] SETUP_1 = {{6,2,2,5,2,6,3,10,2,6},
                                            {5,4,11,1,9,2,7,7,8,2},
                                            {4,11,4,7,8,5,11,5,6,4},
                                            {2,3,11,2,3,11,0,11,3,3}};
    //Vincent Deboer third setup on the site
    public static final int[][] SETUP_2 = {{6,2,4,9,6,2,2,10,2,6},
                                            {5,2,7,5,11,2,7,7,8,3},
                                            {4,8,1,3,11,2,6,5,5,11},
                                            {3,11,4,11,4,2,3,3,11,0}};
    //Vincent Deboer fourth setup on the site
    public static final int[][] SETUP_3 = {{2,8,5,2,6,2,9,3,2,6},
                                            {10,2,7,8,2,6,11,5,11,5},
                                            {6,4,7,1,7,5,11,4,11,4},
                                            {3,2,3,3,4,11,0,11,3,2}};
    //Philip Atzemoglou setup
    public static final int[][] SETUP_4 = {{10,7,3,4,11,11,4,3,7,9},
                                            {7,2,8,2,6,5,2,8,2,1},
                                            {11,6,2,5,4,11,6,2,3,2},
                                            {0,11,5,3,11,4,3,6,2,5}};
    //Mike Rowles Bomb Barrier
    public static final int[][] SETUP_5 = {{11,9,10,2,4,5,4,5,5,11},
                                            {8,1,11,6,4,2,2,11,5,6},
                                            {11,7,3,3,8,7,2,7,4,2},
                                            {0,11,3,3,3,6,2,2,6,2}};


    private boolean madeSetup = false;
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
     *
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
            try{
                Thread.sleep(1000);
            }catch(Exception e){
                e.printStackTrace();
            }
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

        } else if (!madeSetup) {
            //making swaps to copy one of the default setups
            setupCompBoard();

            //make between 1 and 7 swaps between random pieces on the board
            Random rand = new Random();
            int swapNum = rand.nextInt(7) + 1;
            int swap1;
            int swap2;
            //setting range
            int upperBounds = 100;
            int lowerBounds = 60;
            if (playerNum == StrategoGameState.RED) {
                upperBounds = 40;
                lowerBounds = 0;
            }

            for (int i = 0; i < swapNum; i++) {
                //don't swap the flag
                do {
                    swap1 = rand.nextInt(upperBounds - lowerBounds) + lowerBounds;
                    swap2 = rand.nextInt(upperBounds - lowerBounds) + lowerBounds;
                } while (coordToSquareConverter(swap1).getPiece().getRank() == GamePiece.FLAG
                        || coordToSquareConverter(swap2).getPiece().getRank() == GamePiece.FLAG);
                game.sendAction(new StrategoSwapAction(this, swap1, swap2));
                Log.i("smart ai setup", "swapped " + swap1 + " and " + swap2);
            }
            madeSetup = true;
            //a dummy swap after comp finishes its setup, this is so the human can make swaps if it wants
            game.sendAction(new StrategoSwapAction(this, -1, -1));
        } else {
            game.sendAction(new StrategoSwapAction(this, -1, -1));
            Log.i("smart ai", "still in setup phase but smart ai already made setup");
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
            if (chanceOfWinning >= 60) {
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
                        (i - step < StrategoGameState.BOARD_SIZE && i - step >= 0) &&
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
        //find any comp piece that's not a bomb or flag
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

        //move it in a random direction
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
     * chooses one of the class's static default setups and does a series of swaps so that
     * comp's side on the board is a copy of that setup
     */
    public void setupCompBoard() {
        //randomly choose one of the five default setups to match
        int[][] chosenSetup = new int[SETUP_1.length][SETUP_1[0].length];
        chooseRandSetup(chosenSetup);
        //set the starting and ending rows do swaps in, based on if comp is player 1 or 2
        int rowStart;
        int rowEnd;
        if (playerNum == StrategoGameState.BLUE) {
            rowStart = 6;
            rowEnd = StrategoGameState.BOARD_SIZE;
        } else {
            //reverse the array because the arrangement is in player 1's perspective on this app
            reverse2DArray(chosenSetup);
            rowStart = 0;
            rowEnd = 4;
        }

        //this faux sorting algorithm works similar to a selection but rather than
        //iterating to find the least or greatest value, it looks for an exact value
        boolean madeSwap;
        GamePiece tempPiece;
        for (int i = rowStart; i < rowEnd; i++) {
            for (int j = 0; j < StrategoGameState.BOARD_SIZE; j++) {
                //leave if at the last indices of the setup arrays
                if (i >= rowEnd - 1 && j >= StrategoGameState.BOARD_SIZE - 1) {
                    break;
                }
                //if the current piece at (i, j) is not the right rank, then find the right one and do a swap
                if (gameState.getBoardSquares()[i][j].getPiece().getRank() != chosenSetup[i - rowStart][j]) {
                    //k and l set where to start the search for the right piece
                    int k = i;
                    int l = j + 1;
                    //if at the end of a row, move on to the next row before starting the search
                    if (j >= StrategoGameState.BOARD_SIZE - 1) {
                        k = i + 1;
                        l = 0;
                    }
                    madeSwap = false;
                    for (int m = k; m < rowEnd; m++) {
                        //if the search has moved on to the next row, the old 'l' value is no longer the column to start the search at
                        if (m > k) l = 0;
                        for (int n = l; n < StrategoGameState.BOARD_SIZE; n++) {
                            if (gameState.getBoardSquares()[m][n].getPiece().getRank() == chosenSetup[i - rowStart][j]) {
                                //update local copy of gamestate
                                tempPiece = gameState.getBoardSquares()[i][j].getPiece();
                                gameState.getBoardSquares()[i][j].setPiece(gameState.getBoardSquares()[m][n].getPiece());
                                gameState.getBoardSquares()[m][n].setPiece(tempPiece);
                                //update shared copy of gamestate
                                game.sendAction(new StrategoSwapAction(this, coordConverter(gameState.getBoardSquares()[i][j]), coordConverter(gameState.getBoardSquares()[m][n])));
                                madeSwap = true;
                                break;
                            }
                        }
                        if (madeSwap) break;
                    }
                }
            }
        }
    }

    private void chooseRandSetup(int[][] setup) {
        Random rand = new Random();
        int randInt = rand.nextInt(5);
        int[][] randDefault;
        switch (randInt) {
            case 0:
                randDefault = SETUP_1;
                break;
            case 1:
                randDefault = SETUP_2;
                break;
            case 2:
                randDefault = SETUP_3;
                break;
            case 3:
                randDefault = SETUP_4;
                break;
            case 4:
                randDefault = SETUP_5;
                break;
            default:
                return;
        }

        for (int i = 0; i < setup.length; i++) {
            for (int j = 0; j < setup[0].length; j++) {
                setup[i][j] = randDefault[i][j];
            }
        }
    }

    /**
     * reverses a 2d array
     *
     * @param arr the 2d array to reverse
     */
    private void reverse2DArray(int[][] arr) {
        int temp;
        //flip arr along vertical axis
        for (int i = 0; i < arr.length; i++ ) {
            for (int j = 0; j < arr[0].length / 2; j++) {
                temp = arr[i][j];
                arr[i][j] = arr[i][(arr[0].length - 1) - j];
                arr[i][(arr[0].length - 1) - j] = temp;
            }
        }
        int[] tempArr;
        //flip arr along horizontal axis
        for (int i = 0; i < arr.length / 2; i++) {
            tempArr = arr[i];
            arr[i] = arr[(arr.length - 1) - i];
            arr[(arr.length - 1) - i] = tempArr;
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

    public boolean getMadeSetup() {
        return this.madeSetup;
    }
}
