package edu.up.cs301.Stratego;

import java.util.Random;

import edu.up.cs301.game.GameFramework.GameComputerPlayer;
import edu.up.cs301.game.GameFramework.infoMessage.GameInfo;

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
     * @param info
     */
    @Override
    protected void receiveInfo(GameInfo info) {
        StrategoGameState gameState = new StrategoGameState((StrategoGameState) info);
        if (gameState.getCurrPlayerIndex() != playerNum) {
            return;
        }

        Random rand = new Random();
        //getting random coordinates to make a move with a random square
        int randRow = rand.nextInt(StrategoGameState.BOARD_SIZE);
        int randCol = rand.nextInt(StrategoGameState.BOARD_SIZE);
        int randDir = rand.nextInt(4); //randomize which direction to move the gamepiece
        int[] dir = {0, 0}; //the dir to move a gamepiece
        switch (randDir) {
            case 0: //move left
                dir[0] = -1;
                break;
            case 1: //move right
                dir[0] = 1;
                break;
            case 2: //move up
                dir[1] = -1;
                break;
            case 3: //move down
                dir[1] = 1;
                break;
            default:
                break;
        }

        //don't need to error check the move, if a move fails the local game won't move to the player
        //which means this comp player would get another shot
        //TODO: convert board square coordinates to int, or refactor how the move action handles picking coords
        game.sendAction(new StrategoMoveAction(this, gameState.getBoardSquares()[randRow][randCol], gameState.getBoardSquares()[randRow + dir[0]][randCol + dir[1]]));
    }
    
}
