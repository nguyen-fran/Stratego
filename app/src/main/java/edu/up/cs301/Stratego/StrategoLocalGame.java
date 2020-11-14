package edu.up.cs301.Stratego;

import android.util.Log;

import edu.up.cs301.game.GameFramework.GamePlayer;
import edu.up.cs301.game.GameFramework.LocalGame;
import edu.up.cs301.game.GameFramework.actionMessage.GameAction;

/**
 * Runs a Stratego game
 *
 * @author Gabby Marshak
 * @author Francisco Nguyen
 * @author Blake Nygren
 * @author Jack Volonte
 */
public class StrategoLocalGame extends LocalGame {
    private StrategoGameState gameState;
    private StrategoGameState prevGameState;

    public static final int BLUE = 0;
    public static final int RED = 1;

    public StrategoLocalGame(){
        gameState = new StrategoGameState();
    }

    @Override
    protected boolean canMove(int playerIdx) {
        return (gameState.getCurrPlayerIndex() == playerIdx);

    }

    @Override
    protected void sendUpdatedStateTo(GamePlayer p) {
        p.sendInfo(new StrategoGameState(this.gameState));

    }

    @Override
    protected boolean makeMove(GameAction action) {
        if (action instanceof StrategoMoveAction){
            //TODO: make sure action updates gamestate when moving
            prevGameState = new StrategoGameState(this.gameState);

            //if the move was successful, go on to the next player
            if (move((StrategoMoveAction) action)) {
                if (gameState.getCurrPlayerIndex() == 0) {
                    gameState.setCurrPlayerIndex(1);
                } else {
                    gameState.setCurrPlayerIndex(0);
                }
            }
            return true;
        } else if (action instanceof StrategoSwapAction){
            return swap((StrategoSwapAction) action);
        } else {
            return false;
        }
    }

    /**
     * method for moving the piece on a given board square to another square
     * @param action    the action being done by a game player
     * @return  true if move is legal, false if not
     */
    public boolean move(StrategoMoveAction action) {

        if (!gameState.getGamePhase()) {
            return false;
        }

        //TODO convert the int index coming in into a board square coordinate to get
        int topHalf;
        int bottomHalf;

        topHalf = action.getSquareSrc()/10;
        bottomHalf = action.getSquareSrc() - (topHalf*10);
        BoardSquare squareSrc = gameState.getBoardSquares()[topHalf][bottomHalf];

        topHalf = action.getSquareDest()/10;
        bottomHalf = action.getSquareDest() - (topHalf*10);
        BoardSquare squareDest = gameState.getBoardSquares()[topHalf][bottomHalf];

        //return false if not player's turn or if squareSrc is empty
        //or if src square is not curr player's piece or if dest square is curr player's piece.
        //the last two lines of the conditions should account for if the two squares are the same
        if (!canMove(gameState.getCurrPlayerIndex()) || squareSrc.getPiece() == null
                || squareSrc.getPiece().getTeam() != getPlayerIdx(action.getPlayer())
                || (squareDest.getPiece() != null && squareDest.getPiece().getTeam() == getPlayerIdx(action.getPlayer()))) {
            return false;
        }

        //check if coordinates you want to move to are valid for piece (special exception for scout range, and immobile pieces)
        if (squareSrc.getPiece().getRank() == 11 || squareSrc.getPiece().getRank() == 0) { //immobile pieces (cannot move)
            return false;
        } else if (squareSrc.getPiece().getRank() == 2) { //special scout movement
            //check if not valid scout move
            if (!scoutMove(squareSrc, squareDest)) {
                return false;
            }
        } else { //all other pieces have normal movement range (check if square is in range, and is moving at all)
            if (squareDest.getRow() > squareSrc.getRow() + 1 || squareDest.getRow() < squareSrc.getRow() - 1
                    || squareDest.getCol() > squareSrc.getCol() + 1 || squareDest.getCol() < squareSrc.getCol() - 1) {
                return false;
            }
        }

        //if dest occupied by another piece (ie. is not null), then attack
        if (squareDest.getOccupied() && squareDest.getPiece() == null) { //trying to move into a lake square
            return false;
        } else if (squareDest.getOccupied()) {
            //check if not valid attack
            if(!attack(squareSrc.getPiece(), squareDest.getPiece())) {
                return false;
            }
        }

        //check if src square hasn't been captured
        if (!squareSrc.getPiece().getCaptured()) {
            gameState.getBoardSquares()[squareDest.getRow()][squareDest.getCol()].setPiece(squareSrc.getPiece());
            gameState.getBoardSquares()[squareDest.getRow()][squareDest.getCol()].setOccupied(true);
        }
        //update src square appropriately
        //TODO: make sure this updates the game state, not the copy that the player is looking at
        squareSrc.setPiece(null);
        squareSrc.setOccupied(false);

        return true;
    }

    /**
     * method for logic of attacks between pieces
     * determines which piece(s) get captured and sent to the graveyard
     * assuming attack is valid move, and that there are both an attacking and defending piece
     *
     * @param attackPiece   piece initiating the attack
     * @param defendPiece   piece being attacked
     * @return true if attack was successful, false if something in the comparison went wrong
     */
    public boolean attack(GamePiece attackPiece, GamePiece defendPiece) {
        //first check for special cases (spy, miner)
        if (attackPiece.getRank() == 1 && defendPiece.getRank() == 10) { //spy attacking marshal
            defendPiece.setCaptured(true);
        } else if (attackPiece.getRank() == 5 && defendPiece.getRank() == 11) { //miner attacking bomb
            defendPiece.setCaptured(true);
        } else if(attackPiece.getRank() < defendPiece.getRank()) { //attacker gets captured
            attackPiece.setCaptured(true);
        } else if (attackPiece.getRank() > defendPiece.getRank()) { //defender gets captured
            defendPiece.setCaptured(true);
        }else if(attackPiece.getRank() == defendPiece.getRank()){ //both get captured
            attackPiece.setCaptured(true);
            defendPiece.setCaptured(true);
        } else { //some other combination, indicates something went wrong
            return false;
        }

        //updating graveyard(s)
        if (attackPiece.getCaptured()) {
            //check which team the attack piece was
            if (attackPiece.getTeam() == BLUE) {
                gameState.setBlueGYIdx(attackPiece.getRank() - 1, gameState.getBlueGY()[attackPiece.getRank() - 1] + 1);
            } else {
                gameState.setRedGYIdx(attackPiece.getRank() - 1, gameState.getRedGY()[attackPiece.getRank() - 1] + 1);
            }
        }
        if (defendPiece.getCaptured()) {
            if (defendPiece.getRank() == 0) { //captured a flag game piece
                if (defendPiece.getTeam() == BLUE) {
                    gameState.setBlueGYIdx(11, gameState.getBlueGY()[11] + 1);
                } else {
                    gameState.setRedGYIdx(11, gameState.getRedGY()[11] + 1);
                }
            } else { //captured regular game piece
                if (defendPiece.getTeam() == BLUE) {
                    gameState.setBlueGYIdx(defendPiece.getRank() - 1, gameState.getBlueGY()[defendPiece.getRank() - 1] + 1);
                } else {
                    gameState.setRedGYIdx(defendPiece.getRank() - 1, gameState.getRedGY()[defendPiece.getRank() - 1] + 1);
                }
            }
        }

        //on attacking or being attacked, pieces will become visible to opponent
        attackPiece.setVisible(true);
        defendPiece.setVisible(true);
        return true;
    }

    /**
     * Helper method to determine if selected square is a valid move for a scout piece.
     * Assumes the two squares in param are valid for a move action (different squares, different teams)
     *
     * @param squareSrc     initial board square that the scout piece is on
     * @param squareDest    new square scout might move to
     * @return true if movement is valid, false if not
     */
    public boolean scoutMove(BoardSquare squareSrc, BoardSquare squareDest) {
        //check if dest is not in straight line from src
        if (squareSrc.getRow() != squareDest.getRow() && squareSrc.getCol() != squareDest.getCol()) {
            return false;
        }

        //if the squares are in the same row
        if (squareSrc.getRow() != squareDest.getRow()) {
            //determine if dest is to the left or right of src
            //use this to determine if step should be +1 or -1 horizontally
            int diff = squareSrc.getCol() - squareDest.getCol();
            int step;
            if (diff > 0) { //means dest is right of src (src is moving right)
                step = -1;
            } else {    //means dest is left of src (src is moving left)
                step = 1;
            }

            //use diff for for loop, for each square in the range (NOT inclusive of dest), check if occupied
            for (int i = squareSrc.getCol(); i < squareDest.getCol(); i += step) {
                //if a square is occupied, then return false
                if (gameState.getBoardSquares()[squareSrc.getRow()][i].getOccupied()) {
                    return false;
                }
            }
        } else {    //if the squares are in the same col
            int diff = squareSrc.getRow() - squareDest.getRow();
            int step;
            if (diff > 0) { //means dest is south of src (src is moving south)
                step = -1;
            } else {    //means dest is north of src (src is moving north)
                step = 1;
            }

            //use diff for for loop, for each square in the range (NOT inclusive of dest), check if occupied
            for (int i = squareSrc.getCol(); i < squareDest.getCol(); i += step) {
                //if a square is occupied, then return false
                if (gameState.getBoardSquares()[squareSrc.getRow()][i].getOccupied()) {
                    return false;
                }
            }
        }

        //should only hit here if new square is a valid movement
        return true;
    }

    /**
     * Swaps pieces that are on the same team, this is for the setup phase of the game
     *
     * @param action    the action being done by a game player
     * @return true if swap was successful, false otherwise
     */
    public boolean swap(StrategoSwapAction action) {

        if (gameState.getGamePhase()) {
            return false;
        }

        int topHalf;
        int bottomHalf;

        topHalf = action.getSquareSrc()/10;
        bottomHalf = (action.getSquareSrc() - (topHalf*10));
        BoardSquare squareSrc = gameState.getBoardSquares()[topHalf][bottomHalf];

        topHalf = action.getSquareDest()/10;
        bottomHalf = (action.getSquareDest() - (topHalf*10));
        BoardSquare squareDest = gameState.getBoardSquares()[topHalf][bottomHalf];

        //check if there are pieces on the squares to swap
        if (squareSrc.getPiece() == null || squareDest.getPiece() == null) {
            return false;
        }
        //check if the pieces belong to the player doing the swap
        if (squareSrc.getPiece().getTeam() != getPlayerIdx(action.getPlayer())
                || squareDest.getPiece().getTeam() != getPlayerIdx(action.getPlayer())) {
            return false;
        }

        GamePiece temp = squareSrc.getPiece();
        squareSrc.setPiece(squareDest.getPiece());
        squareDest.setPiece(temp);
        return true;
    }

    //checks if either flag has the 'captured' status (captured = true)
    //TODO need to write specific message for who won
    @Override
    protected String checkIfGameOver() {
        if (gameState.getBlueGY()[11] > 0) {
            return "Red team has won. ";
        } else if (gameState.getRedGY()[11] > 0) {
            return "Blue team has won. ";
        } else {
            return null;
        }
    }
}
