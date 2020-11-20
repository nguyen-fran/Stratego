package edu.up.cs301.Stratego;

/**
 * tells state of individual board square, tells whether it is occupied, what piece is on it, coordinates, etc
 * NOTE: the Lake Squares and are represented as occupied being true but piece is null
 *
 * @author Gabby Marshak
 * @author Francisco Nguyen
 * @author Blake Nygren
 * @author Jack Volonte
 */
public class BoardSquare {
    private boolean occupied;
    private int row;
    private int col;

    private GamePiece piece;

    /**
     * constructor for BoardSquare, sets instance variables to given values
     * @param occupied whether the square should be occupied or not
     * @param piece game piece on the square (null if none exists)
     * @param row x coordinate of the square on the board
     * @param col y coordinate of the square on the board
     */
    public BoardSquare(boolean occupied, int row, int col, GamePiece piece){
        this.occupied = occupied;
        this.row = row;
        this.col = col;

        this.piece = piece;
    }

    /**
     * copy constructor for BoardSquare
     * @param orig original square being copied
     */
    public BoardSquare(BoardSquare orig) {
        this.occupied = orig.occupied;
        this.row = orig.row;
        this.col = orig.col;

        if (orig.piece == null) {
            this.piece = null;
        } else {
            this.piece = new GamePiece(orig.piece);
        }
    }

    //getters and setters
    public boolean getOccupied(){
        return occupied;
    }
    public int getRow(){
        return row;
    }
    public int getCol(){
        return col;
    }
    public GamePiece getPiece(){
        return piece;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }
    public void setRow(int row) {
        this.row = row;
    }
    public void setCol(int col) {
        this.col = col;
    }
    public void setPiece(GamePiece piece) {
        this.piece = piece;
    }
}

