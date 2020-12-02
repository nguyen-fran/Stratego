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

    /**
     * holds logic for what action players make and updates gamestate
     *
     * @param action    The move that the player has sent to the game
     * @return  if successful/legal action
     */
    @Override
    protected boolean makeMove(GameAction action) {
        if (action instanceof StrategoMoveAction){
            //if the move was successful, go on to the next player
            if (move((StrategoMoveAction) action)) {
                if (gameState.getCurrPlayerIndex() == 0) {
                    gameState.setCurrPlayerIndex(1);
                } else {
                    gameState.setCurrPlayerIndex(0);
                }
                return true;
            }
            return false;
        } else if (action instanceof StrategoSwapAction){
            //change curr player if comp tries to swap, this avoid app freezing if comp is first player
            if (action.getPlayer() instanceof StrategoComputerPlayer) {
                if (gameState.getCurrPlayerIndex() == 0) {
                    gameState.setCurrPlayerIndex(1);
                } else {
                    gameState.setCurrPlayerIndex(0);
                }
            }
            return swap((StrategoSwapAction) action);
        } else if(action instanceof StrategoStartAction){
            return (begin());
        }
        return false;
    }

    /**
     * method for moving the piece on a given board square to another square
     * @param action    the action being done by a game player
     * @return  true if move is legal, false if not
     */
    public boolean move(StrategoMoveAction action) {
        //correct phase and turn checking
        if (!gameState.getGamePhase() || !canMove(getPlayerIdx(action.getPlayer()))) {
            return false;
        }
        //bounds checking
        if (action.getSquareSrc() < 0 || action.getSquareSrc() >= 100 || action.getSquareDest() < 0 || action.getSquareDest() >= 100) {
            return false;
        }

        BoardSquare squareSrc = gameState.getBoardSquares()[action.getSquareSrc() / 10][action.getSquareSrc() % 10];
        BoardSquare squareDest = gameState.getBoardSquares()[action.getSquareDest() / 10][action.getSquareDest() % 10];

        //return false if squareSrc is empty or if src square is not curr player's piece or if dest square is curr player's piece.
        //this should account for if the two squares are the same as well
        if (squareSrc.getPiece() == null || squareSrc.getPiece().getTeam() != getPlayerIdx(action.getPlayer())
                || (squareDest.getPiece() != null && squareDest.getPiece().getTeam() == getPlayerIdx(action.getPlayer()))) {
            return false;
        }

        //check if coordinates you want to move to are valid for piece (special exception for scout range, and immobile pieces)
        if (squareSrc.getPiece().getRank() == GamePiece.BOMB || squareSrc.getPiece().getRank() == GamePiece.FLAG) { //immobile pieces (cannot move)
            return false;
        } else if (squareSrc.getPiece().getRank() == 2) { //special scout movement
            //return false if not valid scout move
            if (!scoutMove(squareSrc, squareDest)) {
                return false;
            }
        } else { //all other pieces have normal movement range (check if square is in range, and is moving at all)
            if (squareDest.getRow() > squareSrc.getRow() + 1 || squareDest.getRow() < squareSrc.getRow() - 1
                    || squareDest.getCol() > squareSrc.getCol() + 1 || squareDest.getCol() < squareSrc.getCol() - 1
                    || squareSrc.getCol() != squareDest.getCol() && squareSrc.getRow() != squareDest.getRow()) {
                return false;
            }
        }

        //if dest occupied by another piece (ie. is not null), then attack
        if (squareDest.getOccupied() && squareDest.getPiece() == null) { //trying to move into a lake square
            return false;
        } else if (squareDest.getOccupied()) {
            //return false if not valid attack
            if(!attack(squareSrc.getPiece(), squareDest.getPiece())) {
                return false;
            }
        }

        //this conditional is meant as reassurance in the case that two pieces are the same rank
        if (squareDest.getPiece() != null && squareDest.getPiece().getCaptured()) {
            squareDest.setPiece(null);
            squareDest.setOccupied(false);
        }
        //move src square if src square hasn't been captured
        if (!squareSrc.getPiece().getCaptured()) {
            squareDest.setPiece(squareSrc.getPiece());
            squareDest.setOccupied(true);
        }
        //update src square appropriately
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
        } else if (attackPiece.getRank() == 5 && defendPiece.getRank() == GamePiece.BOMB) { //miner attacking bomb
            defendPiece.setCaptured(true);
        } else if (attackPiece.getRank() < defendPiece.getRank()) { //attacker gets captured
            attackPiece.setCaptured(true);
        } else if (attackPiece.getRank() > defendPiece.getRank()) { //defender gets captured
            defendPiece.setCaptured(true);
        } else if (attackPiece.getRank() == defendPiece.getRank()) { //both get captured
            attackPiece.setCaptured(true);
            defendPiece.setCaptured(true);
        } else { //some other combination, indicates something went wrong
            return false;
        }

        //updating graveyard(s)
        if (attackPiece.getCaptured()) {
            //check which team the attack piece was
            if (attackPiece.getTeam() == StrategoGameState.BLUE) {
                gameState.setBlueGYIdx(attackPiece.getRank() - 1, gameState.getBlueGY()[attackPiece.getRank() - 1] + 1);
            } else {
                gameState.setRedGYIdx(attackPiece.getRank() - 1, gameState.getRedGY()[attackPiece.getRank() - 1] + 1);
            }
        }
        if (defendPiece.getCaptured()) {
            if (defendPiece.getRank() == GamePiece.FLAG) { //captured a flag game piece
                if (defendPiece.getTeam() == StrategoGameState.BLUE) {
                    gameState.setBlueGYIdx(11, gameState.getBlueGY()[11] + 1);
                } else {
                    gameState.setRedGYIdx(11, gameState.getRedGY()[11] + 1);
                }
            } else { //captured regular game piece
                if (defendPiece.getTeam() == StrategoGameState.BLUE) {
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
     * Assumes the two squares in param are valid for a move action (different squares, different teams, same row/col)
     *
     * @param squareSrc     initial board square that the scout piece is on
     * @param squareDest    new square scout might move to
     * @return true if movement is valid, false if not
     */
    public boolean scoutMove(BoardSquare squareSrc, BoardSquare squareDest) {
        //if the squares are in the same row
        if (squareSrc.getRow() == squareDest.getRow()) {
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
            for (int i = squareSrc.getCol() + step; i != squareDest.getCol(); i += step) {
                //if a square is occupied, then return false
                if (gameState.getBoardSquares()[squareSrc.getRow()][i].getOccupied()) {
                    return false;
                }
            }
        } else if (squareSrc.getCol() == squareDest.getCol()) {    //if the squares are in the same col
            int diff = squareSrc.getRow() - squareDest.getRow();
            int step;
            if (diff > 0) { //means dest is south of src (src is moving south)
                step = -1;
            } else {    //means dest is north of src (src is moving north)
                step = 1;
            }

            //use diff for for loop, for each square in the range (NOT inclusive of dest), check if occupied
            for (int i = squareSrc.getRow() + step; i != squareDest.getRow(); i += step) {
                //if a square is occupied, then return false
                if (gameState.getBoardSquares()[i][squareSrc.getCol()].getOccupied()) {
                    return false;
                }
            }
        } else {
            return false;
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
        //correct phase and turn checking
        if (gameState.getGamePhase() || !canMove(getPlayerIdx(action.getPlayer()))) {
            return false;
        }
        //bounds checking
        if (action.getSquareSrc() < 0 || action.getSquareSrc() >= 100 || action.getSquareDest() < 0 || action.getSquareDest() >= 100) {
            return false;
        }

        BoardSquare squareSrc = gameState.getBoardSquares()[action.getSquareSrc() / 10][action.getSquareSrc() % 10];
        BoardSquare squareDest = gameState.getBoardSquares()[action.getSquareDest() / 10][action.getSquareDest() % 10];

        //check if there are pieces on the squares to swap
        if (squareSrc.getPiece() == null || squareDest.getPiece() == null) {
            return false;
        }
        //check if the pieces belong to the player doing the swap
        if (squareSrc.getPiece().getTeam() != getPlayerIdx(action.getPlayer())
                || squareDest.getPiece().getTeam() != getPlayerIdx(action.getPlayer())) {
            return false;
        }

        //do the swap
        GamePiece temp = squareSrc.getPiece();
        squareSrc.setPiece(squareDest.getPiece());
        squareDest.setPiece(temp);
        return true;
    }

    /**
     * Begin the game once the setup phase is over
     *
     * @return true if swap was successful, false otherwise
     */
    public boolean begin() {
        if (gameState.getGamePhase()) {
            return true;
        }

        //set game phase signifying that the game has started
        gameState.setGamePhase(true);
        gameState.setCurrPlayerIndex(0);
        return true;
    }

    //checks if the hidden death count for flag in GYs is greater than zero

    /**
     * check if a game is over by checking if a flag has been captured or
     * if a player has run out of movable pieces
     *
     * @return  unique string if there is a winner
     */
    @Override
    protected String checkIfGameOver() {
        boolean redCanMove = false;
        for ( int i = 0; i < 10; i++ ) {
            for ( int j = 0; j < 10; j++ ) {
                if ( gameState.getBoardSquares()[i][j].getPiece() != null ) {
                    if (gameState.getBoardSquares()[i][j].getPiece().getTeam() == StrategoGameState.RED &&
                            gameState.getBoardSquares()[i][j].getPiece().getRank() != 0 &&
                            gameState.getBoardSquares()[i][j].getPiece().getRank() != 11) {
                        redCanMove = true;
                    }
                }
            }
        }

        boolean blueCanMove = false;
        for ( int i = 0; i < 10; i++ ) {
            for ( int j = 0; j < 10; j++ ) {
                if ( gameState.getBoardSquares()[i][j].getPiece() != null ) {
                    if (gameState.getBoardSquares()[i][j].getPiece().getTeam() == StrategoGameState.BLUE &&
                            gameState.getBoardSquares()[i][j].getPiece().getRank() != 0 &&
                            gameState.getBoardSquares()[i][j].getPiece().getRank() != 11) {
                        blueCanMove = true;
                    }
                }
            }
        }

        if (gameState.getBlueGY()[11] > 0) {
            return "" + playerNames[1] + " has won. ";
        } else if (gameState.getRedGY()[11] > 0) {
            return "" + playerNames[0] + " has won. ";
        } else if (!redCanMove) {
            return "Red team has won. ";
        } else if (!blueCanMove) {
            return "Red team has won. ";
        } else {
            return null;
        }
    }
}
