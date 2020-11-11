package edu.up.cs301.Stratego;

import edu.up.cs301.game.GameFramework.GamePlayer;
import edu.up.cs301.game.GameFramework.actionMessage.GameAction;

/**
 * For moving pieces in Stratego
 *
 * @author Gabby Marshak
 * @author Francisco Nguyen
 * @author Blake Nygren
 * @author Jack Volonte
 */
public class StrategoMoveAction extends GameAction {
    private int squareSrc;
    private int squareDest;

    /**
     * constructor for GameAction
     *
     * @param player the player who created the action
     */
    public StrategoMoveAction(GamePlayer player, int squareSrc, int squareDest) {
        super(player);
        this.squareSrc = squareSrc;
        this.squareDest = squareDest;
    }

    //getters and setters

    public int getSquareSrc() {
        return squareSrc;
    }

    public void setSquareSrc(int squareSrc) {
        this.squareSrc = squareSrc;
    }

    public int getSquareDest() {
        return squareDest;
    }

    public void setSquareDest(int squareDest) {
        this.squareDest = squareDest;
    }
}
