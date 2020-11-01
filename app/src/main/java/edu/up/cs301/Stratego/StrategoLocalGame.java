package edu.up.cs301.Stratego;

import edu.up.cs301.game.GameFramework.GamePlayer;
import edu.up.cs301.game.GameFramework.LocalGame;
import edu.up.cs301.game.GameFramework.actionMessage.GameAction;

public class StrategoLocalGame extends LocalGame {

    StrategoGameState strategoGameState;

    public StrategoLocalGame(){
        strategoGameState = new StrategoGameState();
    }



    //Done
    @Override
    protected boolean canMove(int playerIdx) {
        if(strategoGameState.getCurrPlayerIndex() != playerIdx)
            return false;
        return true;

    }

    //Done
    @Override
    protected void sendUpdatedStateTo(GamePlayer p) {
        p.sendInfo(new StrategoGameState(this.strategoGameState));

    }

    //TODO add handling for each action. Already set up to figure out which action it is
    @Override
    protected boolean makeMove(GameAction action) {
        if(action instanceof StrategoSwapAction){


        }else if(action instanceof StrategoMoveAction){

            //strategoGameState.move()
        }

        return false;
    }


    //checks if either flag has the 'captured' status (captured = true)
    //TODO need to write specific message for who won
    @Override
    protected String checkIfGameOver() {
        for (int i = 0; i < 10; i++) {
            for(int j = 0; j < 10; j++){
                if(strategoGameState.getBoardSquares()[i][j].getPiece().getRank() == 0){
                    if(strategoGameState.getBoardSquares()[i][j].getPiece().getCaptured()){
                        return "The Game is Over";
                    }
                }
            }

        }
      return null;
    }
}
