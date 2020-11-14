package edu.up.cs301.Stratego;

import edu.up.cs301.game.GameFramework.GameComputerPlayer;
import edu.up.cs301.game.GameFramework.infoMessage.GameInfo;

/**
 * A smarter computer player to play Stratego
 *
 * @author Gabby Marshak
 * @author Francisco Nguyen
 * @author Blake Nygren
 * @author Jack Volonte
 */
public class StrategoSmartComputerPlayer extends GameComputerPlayer {

    public static final int BLUE =  0;
    public static final int RED = 1;

    /**
     * constructor
     *
     * @param name the player's name (e.g., "John")
     */
    public StrategoSmartComputerPlayer(String name) {
        super(name);
    }

    @Override
    protected void receiveInfo(GameInfo info) {
        int squareSrc = -1;
        int squareDst = -1;
        BoardSquare source = null;
        BoardSquare destination = null;

        StrategoGameState gameState = new StrategoGameState((StrategoGameState) info);
        if (gameState.getCurrPlayerIndex() != playerNum) {
            return;
        }

        //board setup stuff if game state is in setup

        //case statement for determining what piece to move and where (call helper methods)
        //need to assign source and destination squares (convert from BoardSquare coordinates)

        //call move action using

    }

    /**
     * checks if human player's flag piece is within one square of a computer player's piece
     * checks if in a straight line from a computer player's scouts
     * TODO: add some sort of logic from graveyard counts/visible pieces to determine where the flag is
     * TODO: set a destination square that can be used by receiveInfo before returning
     * right now the method is just going to always know where the flag is whether or not it's visible
     * @param gameState current state of the game being analyzed
     * @return the board square to be moved
     */
    public BoardSquare flagAttack(StrategoGameState gameState){
        BoardSquare flag = null;

        //for loop through human player's pieces to find where the flag is
        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){
                if(gameState.getBoardSquares()[i][j].getPiece().getRank() == 0){
                    flag = gameState.getBoardSquares()[i][j];
                }
            }
        }

        //if you return here, something went wrong, either on the board or in the for loop
        if(flag == null){
            return null;
        }

        //check squares immediately adjacent to flag (if on board) (only 4 squares so doing it manually)
        //destination squares for these if statements should be the flag square
        if((flag.getRow() - 1 >= 0) &&
           (gameState.getBoardSquares()[flag.getRow()-1][flag.getCol()].getPiece().getTeam() == RED)){
            return gameState.getBoardSquares()[flag.getRow()-1][flag.getCol()];
        }else if((flag.getRow() + 1 < 10) &&
                (gameState.getBoardSquares()[flag.getRow()+1][flag.getCol()].getPiece().getTeam() == RED)){
            return gameState.getBoardSquares()[flag.getRow()+1][flag.getCol()];
        }else if((flag.getCol() - 1 >= 0) &&
                (gameState.getBoardSquares()[flag.getRow()][flag.getCol() - 1].getPiece().getTeam() == RED)){
            return gameState.getBoardSquares()[flag.getRow()][flag.getCol() - 1];
        }else if((flag.getCol() + 1 < 0) &&
                (gameState.getBoardSquares()[flag.getRow()][flag.getCol() + 1].getPiece().getTeam() == RED)){
            return gameState.getBoardSquares()[flag.getRow()][flag.getCol() + 1];
        }

        //check in straight lines from the human flag until it hits another piece or goes off the board
        //if the piece is a red scout, destination should be the flag square
        //if the piece is any other mobile red piece, destination should be moving one square towards the flag

        return null;
    }

    public BoardSquare flagDefend(StrategoGameState gameState){
        return null;
    }

    public BoardSquare specialCaseAttack(StrategoGameState gameState){
        return null;
    }

    public BoardSquare scoutAttack(StrategoGameState gameState){
        return null;
    }

    public BoardSquare normalAttack(StrategoGameState gameState){
        return null;
    }

    public BoardSquare hiddenPieceAttack(StrategoGameState gameState){
        return null;
    }

    public BoardSquare defaultMove(StrategoGameState gameState){
        return null;
    }
}
