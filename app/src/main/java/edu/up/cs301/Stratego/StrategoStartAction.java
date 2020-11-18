package edu.up.cs301.Stratego;

import edu.up.cs301.game.GameFramework.GamePlayer;
import edu.up.cs301.game.GameFramework.actionMessage.GameAction;

public class StrategoStartAction extends GameAction {

    /**
     * constructor for GameAction
     * starts the game by changing the flag to signify
     * that the game has started
     *
     * @param player the player who created the action
     */
    public StrategoStartAction(GamePlayer player) {

        super(player);
    }
}
