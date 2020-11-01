package edu.up.cs301.Stratego;

import java.util.Random;

import edu.up.cs301.game.GameFramework.infoMessage.GameState;

public class StrategoGameState extends GameState {
    //Stratego only has two phases: setup and main gameplay
    private boolean gamePhase;  //false if on setup, true if on main gameplay
    private int currPlayerIndex; //true if human's turn, false if com's turn
    //these arrays holds the number of deaths of each type of piece in order of: 1, 2, ..., 9, 10, bomb, flag (ie.index = piece's rank - 1, except for flag)
    private int[] blueGY = new int[12];
    private int[] redGY = new int[12];

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

