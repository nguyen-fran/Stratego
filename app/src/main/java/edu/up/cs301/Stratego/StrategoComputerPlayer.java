package edu.up.cs301.Stratego;

import java.util.Random;

import edu.up.cs301.game.GameFramework.GameComputerPlayer;
import edu.up.cs301.game.GameFramework.infoMessage.GameInfo;

/**
 * A not smart computer player to play Stratego
 *
 * @author Gabby Marshak
 * @author Francisco Nguyen
 * @author Blake Nygren
 * @author Jack Volonte
 */
public class StrategoComputerPlayer extends GameComputerPlayer {

    /**
     * constructor
     *
     * @param name the player's name (e.g., "John")
     */
    public StrategoComputerPlayer(String name) {
        super(name);
    }

    /**
     * Dumb comp makes a random move action
     * The dumb comp player is dumb, so it will not do swaps. It will just use what it got.
     * Also, the comp only moves pieces 1 space even if the piece is a scout
     *
     * @param info  current gamestate after an action's been made
     */
    @Override
    protected void receiveInfo(GameInfo info) {
        if (!(info instanceof StrategoGameState)) {
            return;
        }

        StrategoGameState gameState = new StrategoGameState((StrategoGameState) info);
        if (gameState.getCurrPlayerIndex() != playerNum) {
            return;
        }
        //dummy swap in case comp player is first player
        if (!gameState.getGamePhase()) {
            game.sendAction(new StrategoSwapAction(this, -1, -1));
            return;
        }

        Random rand = new Random();
        //getting random coordinates to make a move with a random square
        int squareSrc = rand.nextInt(100);
        int squareDest = squareSrc;
        int randDir = rand.nextInt(4); //randomize which direction to move the gamepiece
        switch (randDir) {
            case 0: //move left
                //only move left if not on the left side of the board
                if (squareSrc % 10 != 0) {
                    squareDest -= 1;
                }
                break;
            case 1: //move right
                //only move right if not on the right side of the board
                if (squareSrc % 10 != 9) {
                    squareDest += 1;
                }
                break;
            case 2: //move up
                //only move up if not at the top of the board
                if (squareSrc / 10 != 0) {
                    squareDest -= 10;
                }
                break;
            case 3: //move down
                //only move down if not at the bottom of the board
                if (squareSrc / 10 != 9) {
                    squareDest += 10;
                }
                break;
            default:
                break;
        }

        //don't need to error check the move, if a move fails the local game won't move to the next player
        //which means this comp player would get another shot
        game.sendAction(new StrategoMoveAction(this, squareSrc, squareDest));
    }

}
