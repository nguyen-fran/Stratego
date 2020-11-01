package edu.up.cs301.Stratego;

import edu.up.cs301.game.GameFramework.GamePlayer;
import edu.up.cs301.game.GameFramework.actionMessage.GameAction;

public class StrategoMoveAction extends GameAction {
    private BoardSquare squareSrc;
    private BoardSquare squareDest;

    /**
     * constructor for GameAction
     *
     * @param player the player who created the action
     */
    public StrategoMoveAction(GamePlayer player, BoardSquare squareSrc, BoardSquare squareDest) {
        super(player);
        this.squareSrc = squareSrc;
        this.squareDest = squareDest;
    }

    //getters and setters
    public BoardSquare getSquareSrc() {
        return squareSrc;
    }
    public BoardSquare getSquareDest() {
        return squareDest;
    }

    public void setSquareSrc(BoardSquare squareSrc) {
        this.squareSrc = squareSrc;
    }
    public void setSquareDest(BoardSquare squareDest) {
        this.squareDest = squareDest;
    }
}
