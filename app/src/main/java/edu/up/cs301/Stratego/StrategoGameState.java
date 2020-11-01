package edu.up.cs301.Stratego;

import java.util.Random;

import edu.up.cs301.game.GameFramework.infoMessage.GameState;

public class StrategoGameState extends GameState {
    //Stratego only has two phases: setup and main gameplay
    private boolean gamePhase;  //false if on setup, true if on main gameplay
    private int currPlayerIndex; //true if human's turn, false if com's turn
    //these arrays holds the number of deaths of each type of piece (no flag) in order of: 1, 2, ..., 9, 10, bomb
    private int[] blueGY = new int[11];
    private int[] redGY = new int[11];

    private StrategoGameState prevGameState;
    private BoardSquare[][] boardSquares = new BoardSquare[10][10];

    private static final int BOARD_SIZE = 10;
    public static final int BLUE = 0;   //team blue will always go first
    public static final int RED = 1;    //team red will always go second
    public static final boolean HUMAN_TURN = true;
    public static final boolean COMP_TURN = false;

    //the amount of each type of piece each player has in order of: flag, 1, 2, ..., 9, 10, bomb
    private static final int[] NUM_OF_PIECES = {1, 1, 8, 5, 4, 4, 4, 3, 2, 1, 1, 6};
    //coordinates for the Lake Squares which can't be occupied
    private static final int[][] LAKE_SQUARES = {{4, 2}, {4, 3}, {5, 2}, {5, 3}, {4, 6}, {4, 7}, {5, 6}, {5, 7}};

    /**
     * constructor
     */
    public StrategoGameState() {
        gamePhase = false;
        //TODO: figure out how to set up who goes first
        currPlayerIndex = 0;
        //there are zero deaths at the start of a game
        for (int i = 0; i < blueGY.length; i++) {
            blueGY[i] = 0;
            redGY[i] = 0;
        }


        //making the BoardSquares that start empty
        for (int j = 4; j < 6; j++) {
            for (int k = 0; k < BOARD_SIZE; k++) {
                boardSquares[j][k] = new BoardSquare(false, j, k, null);
            }
        }
        //updating the Lake Squares which can't be occupied by any pieces
        for (int[] lakeSquare : LAKE_SQUARES) {
            boardSquares[lakeSquare[0]][lakeSquare[1]].setOccupied(true);
        }

        //making the squares for RED and BLUE team
        //TODO: set up board appropriately depending on who goes first
        if (currPlayerIndex == 0) {
            //human goes first
            this.makeTeam(0, RED);
            this.makeTeam(6, BLUE);
        } else {
            //computer goes first
            this.makeTeam(0, BLUE);
            this.makeTeam(6, RED);
        }

        //the game starts with a randomized board
        randomize(0, 4, 0, 10);
        randomize(6, 10, 0, 10);

        //this will be used for the undo action later in StrategoLocalGame, but for this checkpoint it is just null to avoid infinite recursion
        prevGameState = null;
    }

    /**
     * Helper method for constructor.
     * Initializes one team's side of the board with the right number of each type of GamePiece
     *
     * @param startRow should be 0 if initializing computer team, 6 if initializing human team
     * @param team     either BLUE or RED.
     */
    private void makeTeam(int startRow, int team) {
        int numOfPiecesIndex = 0;   //this will also signify the rank of the GamePiece being made

        //check if making computer's side of the board, computer's pieces' rank should be invisible
        boolean visible = !(startRow == 0);

        //outer 2 for loops used to provide coordinates of the board square being initialized
        for (int i = startRow; i < startRow + 4; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                //innermost for loop used to initialize correct number of pieces of certain rank
                for (int k = 0; k < NUM_OF_PIECES[numOfPiecesIndex]; k++) {
                    //check if at end of row, if so move to next row and start on col 0
                    if (j >= BOARD_SIZE) {
                        i++;
                        j = 0;
                    }
                    boardSquares[i][j] = new BoardSquare(true, i, j, new GamePiece(numOfPiecesIndex, team, visible, false));

                    //only increment j if there is another piece to make
                    //this avoids j being incremented twice: once on the last piece and again when re-entering the middle for loop
                    if (k < NUM_OF_PIECES[numOfPiecesIndex] - 1) {
                        j++;
                    }
                }
                numOfPiecesIndex++;
            }
        }
    }

    /**
     * Helper method for constructor.
     * Randomizes the pieces in the region of boardSquares within ranges given by params
     * Intended use is the randomize one team's side of the board before the start of a game
     *
     * @param startRow starting row boundary for randomization
     * @param endRow   ending row boundary
     * @param startCol starting column boundary
     * @param endCol   ending column boundary
     */
    private void randomize(int startRow, int endRow, int startCol, int endCol) {
        if (startRow < 0 || endRow > BOARD_SIZE || startCol < 0 || endCol > BOARD_SIZE) {
            return;
        }

        Random rand = new Random();
        int randXPos, randYPos;
        GamePiece temp;
        for (int i = startRow; i < endRow; i++) {
            for (int j = startCol; j < endCol; j++) {
                randXPos = rand.nextInt(endRow - startRow) + startRow;
                randYPos = rand.nextInt(endCol - startCol) + startCol;

                temp = boardSquares[randXPos][randYPos].getPiece();
                boardSquares[randXPos][randYPos].setPiece(boardSquares[i][j].getPiece());
                boardSquares[i][j].setPiece(temp);
            }
        }
    }

    /**
     * deep copy constructor
     *
     * @param orig  original GameState to copy
     */
    public StrategoGameState(StrategoGameState orig) {
        this.gamePhase = orig.gamePhase;
        this.currPlayerIndex = orig.currPlayerIndex;
        for (int i = 0; i < blueGY.length; i++) {
            this.blueGY[i] = orig.blueGY[i];
            this.redGY[i] = orig.redGY[i];
        }

        for (int j = 0; j < BOARD_SIZE; j++) {
            for (int k = 0; k < BOARD_SIZE; k++) {
                this.boardSquares[j][k] = new BoardSquare(orig.getBoardSquares()[j][k]);
            }
        }
    }

    /**
     *method for moving the piece on a given board square to another square
     * @param squareSrc     first square selected by player, has piece with their team on it
     * @param squareDest    second square selected by player, must be empty square (not lake square) or have opponent's piece on it
     * @param playerIndex   who is trying to make the move
     * @return  true if move is legal, false if not
     */
    public boolean move(BoardSquare squareSrc, BoardSquare squareDest, int playerIndex) {
        prevGameState = new StrategoGameState(this);

        //return false if not player's turn or if squareSrc is empty
        //or if src square is not curr player's piece or if dest square is curr player's piece.
        //the last two conditions should account for if the two squares are the same
        if (!canMove(playerIndex) || squareSrc.getPiece() == null ||
                squareSrc.getPiece().getTeam() != playerIndex || squareDest.getPiece().getTeam() == playerIndex) {
            return false;
        }

        //check if coordinates you want to move to are valid for piece (special exception for scout range, and immobile pieces)
        if (squareSrc.getPiece().getRank() == 11 || squareSrc.getPiece().getRank() == 0) { //immobile pieces (cannot move)
            return false;
        } else if (squareSrc.getPiece().getRank() == 2) { //special scout movement
            //check if not valid scout move
            if (!scoutMove(squareSrc, squareDest)) {
                return false;
            }
        } else { //all other pieces have normal movement range (check if square is in range, and is moving at all)
            if (squareDest.getRow() > squareSrc.getRow() + 1 || squareDest.getRow() < squareSrc.getRow() - 1 ||
                    squareDest.getCol() > squareSrc.getCol() + 1 || squareDest.getCol() < squareSrc.getCol() - 1) {
                return false;
            }
        }

        //if dest occupied by another piece (ie. is not null), then attack
        if (squareDest.getOccupied() && squareDest.getPiece() == null) { //trying to move into a lake square
            return false;
        } else if (squareDest.getOccupied()) {
            //check if not valid attack
            if(!attack(squareSrc.getPiece(), squareDest.getPiece())) {
                return false;
            }
        }

        //check if src square hasn't been captured
        if (!squareSrc.getPiece().getCaptured()) {
            boardSquares[squareDest.getRow()][squareDest.getCol()].setPiece(squareSrc.getPiece());
        }
        //update src square appropriately
        squareSrc.setPiece(null);
        squareSrc.setOccupied(false);

        return false;
    }

    /**
     * method for logic of attacks between pieces
     * determines which piece(s) get captured and sent to the graveyard
     * assuming attack is valid move, and that there are both an attacking and defending piece
     *
     * @param attackPiece   piece initiating the attack
     * @param defendPiece   piece being attacked
     * @return true if attack was successful, false if something in the comparison went wrong
     */
    public boolean attack(GamePiece attackPiece, GamePiece defendPiece) {
        //first check for special cases (spy, miner)
        if (attackPiece.getRank() == 1 && defendPiece.getRank() == 10) { //spy attacking marshal
            defendPiece.setCaptured(true);
        } else if (attackPiece.getRank() == 5 && defendPiece.getRank() == 11) { //miner attacking bomb
            defendPiece.setCaptured(true);
        } else if(attackPiece.getRank() < defendPiece.getRank()) { //attacker gets captured
            attackPiece.setCaptured(true);
        } else if (attackPiece.getRank() > defendPiece.getRank()) { //defender gets captured
            defendPiece.setCaptured(true);
        }else if(attackPiece.getRank() == defendPiece.getRank()){ //both get captured
            attackPiece.setCaptured(true);
            defendPiece.setCaptured(true);
        } else { //some other combination, indicates something went wrong
            return false;
        }

        //updating graveyard(s)
        if (attackPiece.getCaptured()) {
            //check which team the attack piece was
            if (attackPiece.getTeam() == BLUE) {
                blueGY[attackPiece.getRank() - 1] += 1;
            } else {
                redGY[attackPiece.getRank() - 1] += 1;
            }
        }
        if (defendPiece.getCaptured()) {
            //check which team the defend piece was
            if (defendPiece.getTeam() == BLUE) {
                blueGY[defendPiece.getRank() - 1] += 1;
            } else {
                redGY[defendPiece.getRank() - 1] += 1;
            }
        }

        //on attacking or being attacked, pieces will become visible to opponent
        attackPiece.setVisible(true);
        defendPiece.setVisible(true);
        return true;
    }

    /**
     * Helper method to determine if selected square is a valid move for a scout piece.
     * Assumes the two squares in param are valid for a move action (different squares, different teams)
     *
     * @param squareSrc     initial board square that the scout piece is on
     * @param squareDest    new square scout might move to
     * @return true if movement is valid, false if not
     */
    public boolean scoutMove(BoardSquare squareSrc, BoardSquare squareDest) {
        //check if dest is not in straight line from src
        if (squareSrc.getRow() != squareDest.getRow() && squareSrc.getCol() != squareDest.getCol()) {
            return false;
        }

        //if the squares are in the same row
        if (squareSrc.getRow() != squareDest.getRow()) {
            //determine if dest is to the left or right of src
            //use this to determine if step should be +1 or -1 horizontally
            int diff = squareSrc.getCol() - squareDest.getCol();
            int step;
            if (diff > 0) { //means dest is right of src (src is moving right)
                step = -1;
            } else {    //means dest is left of src (src is moving left)
                step = 1;
            }

            //use diff for for loop, for each square in the range (NOT inclusive of dest), check if occupied
            for (int i = squareSrc.getCol(); i < squareDest.getCol(); i += step) {
                //if a square is occupied, then return false
                if (boardSquares[squareSrc.getRow()][i].getOccupied()) {
                    return false;
                }
            }
        } else {    //if the squares are in the same col
            int diff = squareSrc.getRow() - squareDest.getRow();
            int step;
            if (diff > 0) { //means dest is south of src (src is moving south)
                step = -1;
            } else {    //means dest is north of src (src is moving north)
                step = 1;
            }

            //use diff for for loop, for each square in the range (NOT inclusive of dest), check if occupied
            for (int i = squareSrc.getCol(); i < squareDest.getCol(); i += step) {
                //if a square is occupied, then return false
                if (boardSquares[squareSrc.getRow()][i].getOccupied()) {
                    return false;
                }
            }
        }

        //should only hit here if new square is a valid movement
        return true;
    }

    /**
     * Swaps pieces that are on the same team, this is for the setup phase of the game
     *
     * @param squareSrc first square being selected for swap
     * @param squareDest second square being selected for swap
     * @param playerIndex    to know if human player or comp player
     * @return true if swap was successful, false otherwise
     */
    public boolean setup(BoardSquare squareSrc, BoardSquare squareDest, int playerIndex) {
        //check if there are pieces on the squares to swap
        if (squareSrc.getPiece() == null || squareDest.getPiece() == null) {
            return false;
        }
        //check if the pieces belong to the player doing the swap
        if (squareSrc.getPiece().getTeam() != playerIndex || squareDest.getPiece().getTeam() != playerIndex) {
            return false;
        }

        GamePiece temp = squareSrc.getPiece();
        squareSrc.setPiece(squareDest.getPiece());
        squareDest.setPiece(temp);
        return true;
    }

    /**
     * helper method to check if a given player can make a move or not
     *
     * @param playerIndex   index of the current player
     * @return  true if they can move, false if they can't
     */
    public boolean canMove(int playerIndex){
        return (playerIndex == currPlayerIndex);
    }

    /*
    getters and setters
    */

    //getters
    public boolean getGamePhase(){
        return gamePhase;
    }
    public int getCurrPlayerIndex(){
        return currPlayerIndex;
    }
    public int[] getBlueGY(){
        return blueGY;
    }
    public int[] getRedGY() {
        return redGY;
    }
    public BoardSquare[][] getBoardSquares() {
        return boardSquares;
    }
    public StrategoGameState getPrevGameState() {
        return prevGameState;
    }

    //setters
    public void setGamePhase(boolean gamePhase){
        this.gamePhase = gamePhase;
    }
    public void setCurrPlayerIndex(int currPlayerIndex) {
        this.currPlayerIndex = currPlayerIndex;
    }
    public void setBlueGY(int[] blueGY) {
        this.blueGY = blueGY;
    }
    public void setPlayerGYIdx(int index, int val){
        if(index >= 0 && index < blueGY.length ){
            this.blueGY[index] = val;
        }
    }
    public void setRedGY(int[] redGY) {
        this.redGY = redGY;
    }
    public void setOppGYIdx(int index, int val){
        if(index >= 0 && index < redGY.length ){
            this.redGY[index] = val;
        }
    }
    public void setBoardSquares(BoardSquare[][] boardSquares) {
        this.boardSquares = boardSquares;
    }
    public void setPrevGameState(StrategoGameState prevGameState) {
        this.prevGameState = new StrategoGameState(prevGameState);
    }
}

