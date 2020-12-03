package edu.up.cs301.Stratego;

import android.util.Log;

import java.util.Random;

import edu.up.cs301.game.GameFramework.GameComputerPlayer;
import edu.up.cs301.game.GameFramework.infoMessage.GameInfo;

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
            //TODO: find more efficient way to call these/check/structure this


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

            //scoutAttack();
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
        if ( flag.getRow() - 1 >= 0 ) {
            killThisOne = gameState.getBoardSquares()[flag.getRow() - 1][flag.getCol()];
        }
        if ( flag.getRow() + 1 < 10 ) {
            killThisOne = gameState.getBoardSquares()[flag.getRow()+1][flag.getCol()];
        }
        if ( flag.getCol() -1 >= 0 ) {
            killThisOne = gameState.getBoardSquares()[flag.getRow()][flag.getCol() - 1];
        }
        if ( flag.getCol() + 1 < 10 ) {
            killThisOne = gameState.getBoardSquares()[flag.getRow()][flag.getCol() + 1];
        }

        if(killThisOne == null || isHumanPiece(killThisOne)){
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
     * specifically only looks at being directly adjacent for now but may add scout usage later
     * TODO: reformat this
     */
    public void normalAttack() {
        BoardSquare attackWith = null;
        BoardSquare defendWith = null;
        for ( int i = 0; i < 10; i++ ) {
            for (int j = 0; j < 10; j++) {
                attackWith = gameState.getBoardSquares()[i][j];
                if (attackWith.getPiece() != null) {
                    if (attackWith.getPiece().getRank() == 0 || attackWith.getPiece().getRank() == 11) {

                    } else {
                        if (attackWith.getPiece() != null && attackWith.getPiece().getTeam() == playerNum) {
                            int rank = attackWith.getPiece().getRank();
                            if (attackWith.getCol() + 1 < 10) {
                                defendWith = gameState.getBoardSquares()[attackWith.getRow()][attackWith.getCol() + 1];
                                if (defendWith.getPiece() != null) {
                                    if (defendWith.getPiece().getVisible() && defendWith.getPiece().getTeam() != playerNum ) {
                                        if (defendWith.getPiece().getRank() <= rank) {
                                            moveSuccessful = true;
                                            Log.d("Tried attacking with:", "" + attackWith.getRow() + ", " + attackWith.getCol() + " / " + defendWith.getRow() + ", " + defendWith.getCol());
                                            game.sendAction(new StrategoMoveAction(this, coordConverter(attackWith), coordConverter(defendWith)));
                                            return;
                                        }
                                    }
                                }

                            }
                            if (attackWith.getCol() - 1 >= 0) {
                                defendWith = gameState.getBoardSquares()[attackWith.getRow()][attackWith.getCol() - 1];
                                if (defendWith.getPiece() != null) {
                                    if (defendWith.getPiece().getVisible() && defendWith.getPiece().getTeam() != playerNum ) {
                                        if (defendWith.getPiece().getRank() <= rank) {
                                            moveSuccessful = true;
                                            Log.d("Tried attacking with:", "" + attackWith.getRow() + ", " + attackWith.getCol() + " / " + defendWith.getRow() + ", " + defendWith.getCol());
                                            game.sendAction(new StrategoMoveAction(this, coordConverter(attackWith), coordConverter(defendWith)));
                                            return;
                                        }
                                    }
                                }
                            }
                            if (attackWith.getRow() - 1 >= 0) {
                                defendWith = gameState.getBoardSquares()[attackWith.getRow() - 1][attackWith.getCol()];
                                if (defendWith.getPiece() != null) {
                                    if (defendWith.getPiece().getVisible() && defendWith.getPiece().getTeam() != playerNum  ) {
                                        if (defendWith.getPiece().getRank() <= rank) {
                                            moveSuccessful = true;
                                            Log.d("Tried attacking with:", "" + attackWith.getRow() + ", " + attackWith.getCol() + " / " + defendWith.getRow() + ", " + defendWith.getCol());
                                            game.sendAction(new StrategoMoveAction(this, coordConverter(attackWith), coordConverter(defendWith)));
                                            return;
                                        }
                                    }
                                }
                            }
                            if (attackWith.getRow() + 1 < 10) {
                                defendWith = gameState.getBoardSquares()[attackWith.getRow() + 1][attackWith.getCol()];
                                if (defendWith.getPiece() != null) {
                                    if (defendWith.getPiece().getVisible() && defendWith.getPiece().getTeam() != playerNum ) {
                                        if (defendWith.getPiece().getRank() <= rank) {
                                            moveSuccessful = true;
                                            Log.d("Tried attacking with:", "" + attackWith.getRow() + ", " + attackWith.getCol() + " / " + defendWith.getRow() + ", " + defendWith.getCol());
                                            game.sendAction(new StrategoMoveAction(this, coordConverter(attackWith), coordConverter(defendWith)));
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     *calculates odds of successfully attacking a piece that has not been revealed to the computer player
     *
     * @return true if the computer will attack the piece, false if it will not
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
        Log.d("total pieces", "" + totalPieces);

        //doing math for winning/losing
        if ( attackingSquare != null && defendingSquare != null ) {
            if (attackingSquare.getPiece().getRank() != 11 && attackingSquare.getPiece().getRank() != 0) {
                if (weCanWin == 0) {

                } else {
                    Log.d("", " +  " + weCanWin);
                    double weWin = (double) weCanWin;
                    double total = (double) totalPieces;
                    double chanceOfWinning = (weWin / total) * 100;
                    Log.d("chance of winning: ", "" + chanceOfWinning);
                    if (chanceOfWinning >= 60) {
                        moveSuccessful = true;
                        Log.d("trying to move: ", "" + attackingSquare.getRow() + ", " +
                                attackingSquare.getCol() + " to: " + defendingSquare.getRow() + ", " + defendingSquare.getCol());
                        game.sendAction(new StrategoMoveAction(this, coordConverter(attackingSquare), coordConverter(defendingSquare)));
                    }
                }
            }
        }
    }

    /**
     * default movement action (used if no other type of movment can be done)
     */
    public void defaultMove() {
        //find the furthest move piece towards the other player, (down the board if computer player is only player 2)
        BoardSquare moveThisOne = null;
        BoardSquare moveThisIfRandomIsBad = null;
        boolean moveForward = false;

        //loop through the board and find the piece for this player that can move the most forward
        for (int i = 0; i < StrategoGameState.BOARD_SIZE; i++) {
            for (int j = 0; j < StrategoGameState.BOARD_SIZE; j++) {
                if ((i + 1 < StrategoGameState.BOARD_SIZE) &&
                        (!gameState.getBoardSquares()[i+1][j].getOccupied()) &&
                        (gameState.getBoardSquares()[i][j].getPiece() != null) &&
                        (gameState.getBoardSquares()[i][j].getPiece().getRank() != 0) &&
                        (gameState.getBoardSquares()[i][j].getPiece().getRank() != 11) &&
                        (gameState.getBoardSquares()[i][j].getPiece().getTeam() == playerNum)){
                            if ( gameState.getBoardSquares()[i+1][j] != null ) {
                                moveSuccessful = true;
                                moveThisOne = gameState.getBoardSquares()[i][j];
                                game.sendAction(new StrategoMoveAction(this, coordConverter(moveThisOne), coordConverter(gameState.getBoardSquares()[moveThisOne.getRow() + 1][moveThisOne.getCol()])));
                            }
                }
            }
        }
        //if we cant find a piece to move forward, then we gotta move left or right with a piece
        boolean moveLeft = false;
        boolean moveRight = false;
        //if can move left or right, set that boardSquare to a variable
        //if you cant move forward, then find one that can move left or right
        for (int i = 0; i < StrategoGameState.BOARD_SIZE; i++) {
            for (int j = 0; j < StrategoGameState.BOARD_SIZE; j++) {
                if ((j + 1 < StrategoGameState.BOARD_SIZE) &&
                        (!gameState.getBoardSquares()[i][j+1].getOccupied()) &&
                        (gameState.getBoardSquares()[i][j].getPiece() != null) &&
                        (gameState.getBoardSquares()[i][j].getPiece().getRank() != 0) &&
                        (gameState.getBoardSquares()[i][j].getPiece().getRank() != 11) &&
                        (gameState.getBoardSquares()[i][j].getPiece().getTeam() == playerNum)){
                    if ( gameState.getBoardSquares()[i][j+1] != null ) {
                        moveSuccessful = true;
                        game.sendAction(new StrategoMoveAction(this, coordConverter(gameState.getBoardSquares()[i][j]), coordConverter(gameState.getBoardSquares()[i][j+1])));
                    }
                } else if ((j - 1 >= 0) &&
                        (!gameState.getBoardSquares()[i][j-1].getOccupied()) &&
                        (gameState.getBoardSquares()[i][j].getPiece() != null) &&
                        (gameState.getBoardSquares()[i][j].getPiece().getRank() != 0) &&
                        (gameState.getBoardSquares()[i][j].getPiece().getRank() != 11) &&
                        (gameState.getBoardSquares()[i][j].getPiece().getTeam() == playerNum)){
                    if ( gameState.getBoardSquares()[i][j-1] != null ) {
                        moveSuccessful = true;
                        game.sendAction(new StrategoMoveAction(this, coordConverter(gameState.getBoardSquares()[i][j]), coordConverter(gameState.getBoardSquares()[i][j-1])));
                    }
                }
            }
        }

        //if cant move forwards or side to side, then backwards? idk if this will ever be reached but yea
        for (int i = 0; i < StrategoGameState.BOARD_SIZE; i++) {
            for (int j = 0; j < StrategoGameState.BOARD_SIZE; j++) {
                if ((i - 1 >= 0) &&
                        (!gameState.getBoardSquares()[i-1][j].getOccupied()) &&
                        (gameState.getBoardSquares()[i][j].getPiece() != null) &&
                        (gameState.getBoardSquares()[i][j].getPiece().getRank() != 0) &&
                        (gameState.getBoardSquares()[i][j].getPiece().getRank() != 11) &&
                        (gameState.getBoardSquares()[i][j].getPiece().getTeam() == playerNum)){
                    if ( gameState.getBoardSquares()[i-1][j] != null ) {
                        moveSuccessful = true;
                        moveThisOne = gameState.getBoardSquares()[i][j];
                        game.sendAction(new StrategoMoveAction(this, coordConverter(moveThisOne), coordConverter(gameState.getBoardSquares()[moveThisOne.getRow() - 1][moveThisOne.getCol()])));
                    }
                }
            }
        }

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
