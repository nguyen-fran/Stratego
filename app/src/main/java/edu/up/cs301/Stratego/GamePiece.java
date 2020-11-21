package edu.up.cs301.Stratego;

/**
 * data holding class that describes an individual game piece/unit (rank, team, rank visibility, is captured)
 *
 * @author Gabby Marshak
 * @author Francisco Nguyen
 * @author Blake Nygren
 * @author Jack Volonte
 */
public class GamePiece {
    private int rank;   //standard nums for mobile pieces, bomb is 11, flag is 0
    private int team;   //0 is blue, 1 is red
    private boolean visible;
    private boolean captured;

    public static final int FLAG = 0;
    public static final int BOMB = 11;

    /**
     * constructor, creates new game piece and sets values to the ones given
     *
     * @param rank piece's numerical rank (0-11)
     * @param team piece's team (RED or BLUE)
     * @param visible whether the piece is visible or not
     * @param captured whether the piece has been captured or not
     */
    public GamePiece(int rank, int team, boolean visible, boolean captured){
        this.rank = rank;
        this.team = team;
        this.visible = visible;
        this.captured = captured;
    }

    /**
     * deep copy constructor
     *
     * @param orig  original GamePiece to copy
     */
    public GamePiece(GamePiece orig) {
        this.rank = orig.getRank();
        this.team = orig.getTeam();
        this.visible = orig.getVisible();
        this.captured = orig.getCaptured();
    }

    /* getters and setters */
    public int getRank(){
        return rank;
    }
    public int getTeam(){
        return team;
    }
    public boolean getVisible(){
        return visible;
    }
    public boolean getCaptured(){
        return captured;
    }

    public void setVisible(boolean visible){
        this.visible = visible;
    }
    public void setCaptured(boolean captured){
        this.captured = captured;
    }
}

