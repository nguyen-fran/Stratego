package edu.up.cs301.Stratego;

import edu.up.cs301.game.GameFramework.GamePlayer;
import edu.up.cs301.game.GameFramework.actionMessage.GameAction;

public class StrategoSwapAction extends GameAction {

    /**
     * constructor for GameAction
     *
     * @param player the player who created the action
     */
    public StrategoSwapAction(GamePlayer player) {
        super(player);
    }
}
