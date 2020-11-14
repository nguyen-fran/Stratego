package edu.up.cs301.Stratego;

import android.graphics.Color;
import android.util.Log;
import android.view.View;

import edu.up.cs301.R;
import edu.up.cs301.game.GameFramework.GameHumanPlayer;
import edu.up.cs301.game.GameFramework.GameMainActivity;
import edu.up.cs301.game.GameFramework.infoMessage.GameInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Class that interacts with human through gui
 *
 * @author Gabby Marshak
 * @author Francisco Nguyen
 * @author Blake Nygren
 * @author Jack Volonte
 */
public class StrategoHumanPlayer extends GameHumanPlayer implements OnClickListener {

    private GameMainActivity myActivity;
    private Button swap;
    private Button move;
    private TextView turnIndicator;

    private ViewGroup gameBoardGrid;
    private ViewGroup playerGY;
    private ViewGroup oppGY;

    private int firstClick = -1;
    private int secondClick = -1;

    private StrategoGameState gameState = null;

    /**
     * constructor
     *
     * @param name the name of the player
     */
    public StrategoHumanPlayer(String name) {
        super(name);
    }

    @Override
    public View getTopView() {
        return myActivity.findViewById(R.id.top_view);
    }

    @Override
    public void receiveInfo(GameInfo info) {
        Log.i("human player", "recieveInfo called");

        if(!(info instanceof StrategoGameState)){
            flash(Color.RED, 10);
            return;
        }

        gameState = (StrategoGameState) info;

        /**
         External Citation
         Date: 7 Nov 2020
         Problem: needed a way to loop though layouts to get all the elements
         Resource: https://stackoverflow.com/questions/19523860/iterate-through-all-objects-in-gridview
         Solution: used this as an example
         */

        //setting up player graveyard with a loop
        for (int i = 0; i < 11; i++){
            TextView GY = (TextView)playerGY.getChildAt(i + 11);
            //setting text to whatever value is in graveyard array at that coord
            //might need to adjust +- 1 depending to avoid out of bounds errors
            GY.setText("" + ((StrategoGameState) info).getBlueGY()[i] + "/" + StrategoGameState.NUM_OF_PIECES[i + 1]);
        }

        //setting up computer graveyard with a loop
        for (int i = 0; i < 11; i++){
            TextView GY = (TextView)oppGY.getChildAt(i + 11);
            //setting text to whatever value is in graveyard array at that coord
            //might need to adjust +- 1 depending to avoid out of bounds errors
            GY.setText("" + ((StrategoGameState) info).getRedGY()[i] + "/" + StrategoGameState.NUM_OF_PIECES[i + 1]);
        }

        //double for loop to update game board from game state
        for (int i = 0; i < 10; i++){
            for (int j = 0; j < 10; j++){
                //using this to be able to get from the board grid at the correct place
                int gridCoord = (i*10) + j;
                ImageButton square = (ImageButton)gameBoardGrid.getChildAt(gridCoord);
                boardImagePicker(square, (StrategoGameState)info, i, j);
            }
        }

        //updating turn indicator
        if(((StrategoGameState) info).getCurrPlayerIndex() == StrategoGameState.BLUE){
            turnIndicator.setText("Player's turn");
        }else{
            turnIndicator.setText("Opponent's turn");
        }

    }

    /**
     * helper method to pick out image for a given image button based on the game state
     * there is absolutely a better way to do this but i don't know what it is
     * @param button button to update the image of
     * @param gameState current state of the game
     * @param i row of game board array to pull from
     * @param j col of game board array to pull from
     */
    public void boardImagePicker(ImageButton button, StrategoGameState gameState, int i, int j){
        //lakes/empty spaces
        if(gameState.getBoardSquares()[i][j].getOccupied() &&
                gameState.getBoardSquares()[i][j].getPiece() == null){ //lake
            button.setImageResource(R.drawable.lake);
        }else if(!gameState.getBoardSquares()[i][j].getOccupied() &&
                gameState.getBoardSquares()[i][j].getPiece() == null){ //empty square
            button.setImageResource(R.drawable.empty_space);
        }else if (gameState.getBoardSquares()[i][j].getPiece().getTeam() == StrategoGameState.BLUE){ //blue pieces
            imagePickerBlue(button, gameState, i, j);
        }else if(gameState.getBoardSquares()[i][j].getPiece().getTeam() == StrategoGameState.RED){ //red pieces
            if(gameState.getBoardSquares()[i][j].getPiece().getVisible() == false){
                button.setImageResource(R.drawable.red_unknown);
            }else{
                imagePickerRed(button, gameState, i, j);
            }
        }
    }

    /**
     * helper method to contain the switch statement for setting blue piece images
     * @param button button being updated
     * @param gameState current state of the game
     * @param i row of game board array to pull from
     * @param j col of game board array to pull from
     */
    public void imagePickerBlue(ImageButton button, StrategoGameState gameState, int i, int j){
        switch(gameState.getBoardSquares()[i][j].getPiece().getRank()){
            case 0: //blue flag
                button.setImageResource(R.drawable.bluef);
                break;
            case 1:
                button.setImageResource(R.drawable.blue1);
                break;
            case 2:
                button.setImageResource(R.drawable.blue2);
                break;
            case 3:
                button.setImageResource(R.drawable.blue3);
                break;
            case 4:
                button.setImageResource(R.drawable.blue4);
                break;
            case 5:
                button.setImageResource(R.drawable.blue5);
                break;
            case 6:
                button.setImageResource(R.drawable.blue6);
                break;
            case 7:
                button.setImageResource(R.drawable.blue7);
                break;
            case 8:
                button.setImageResource(R.drawable.blue8);
                break;
            case 9:
                button.setImageResource(R.drawable.blue9);
                break;
            case 10:
                button.setImageResource(R.drawable.blue10);
                break;
            case 11:
                button.setImageResource(R.drawable.blueb); //blue bomb
                break;
        }
    }

    /**
     * helper method to contain the switch statement for setting red piece images
     * @param button button being updated
     * @param gameState current state of the game
     * @param i row of game board array to pull from
     * @param j col of game board array to pull from
     */
    public void imagePickerRed(ImageButton button, StrategoGameState gameState, int i, int j){
        switch(gameState.getBoardSquares()[i][j].getPiece().getRank()){
            case 0: //red flag
                button.setImageResource(R.drawable.redf);
                break;
            case 1:
                button.setImageResource(R.drawable.red1);
                break;
            case 2:
                button.setImageResource(R.drawable.red2);
                break;
            case 3:
                button.setImageResource(R.drawable.red3);
                break;
            case 4:
                button.setImageResource(R.drawable.red4);
                break;
            case 5:
                button.setImageResource(R.drawable.red5);
                break;
            case 6:
                button.setImageResource(R.drawable.red6);
                break;
            case 7:
                button.setImageResource(R.drawable.red7);
                break;
            case 8:
                button.setImageResource(R.drawable.red8);
                break;
            case 9:
                button.setImageResource(R.drawable.red9);
                break;
            case 10:
                button.setImageResource(R.drawable.red10);
                break;
            case 11:
                button.setImageResource(R.drawable.redb); //red bomb
                break;
        }
    }

    @Override
    public void setAsGui(GameMainActivity activity) {
        myActivity = activity;
        activity.setContentView(R.layout.stratego_layout);

        gameBoardGrid = (ViewGroup) myActivity.findViewById(R.id.gameBoardGrid);
        ImageButton temp;
        for (int i = 0; i < 100; i++) {
            temp = new ImageButton(myActivity);
            temp.setId(i);
            temp.setOnClickListener(this);
            gameBoardGrid.addView(temp);
        }

        this.playerGY = activity.findViewById(R.id.blueGY);
        this.oppGY = activity.findViewById(R.id.redGY);
        this.turnIndicator = activity.findViewById(R.id.turnIndicator);
    }

    @Override
    public void onClick(View v) {
        if(firstClick > 0){

            secondClick = v.getId();
            Log.i("testing clicks", "recorded first click " + firstClick + " and made it to record the second" + secondClick);
            // TODO need better way to determine which action is being attempted
            if(gameState.getGamePhase()){
                game.sendAction(new StrategoMoveAction(this, firstClick, secondClick));
                ImageButton tempp = myActivity.findViewById(firstClick);
                ImageButton temppp = myActivity.findViewById(secondClick);
                tempp.setBackgroundColor(Color.WHITE);
                temppp.setBackgroundColor(Color.WHITE);
                firstClick = -1;
                secondClick = -1;
            }
            else{
                game.sendAction(new StrategoSwapAction(this, firstClick, secondClick));
                ImageButton tempp = myActivity.findViewById(firstClick);
                ImageButton temppp = myActivity.findViewById(secondClick);
                tempp.setBackgroundColor(Color.WHITE);
                temppp.setBackgroundColor(Color.WHITE);
                firstClick = -1;
                secondClick = -1;
            }
        }else{
            firstClick = v.getId();
            ImageButton temp = myActivity.findViewById(firstClick);
            temp.setBackgroundColor(Color.GREEN);
        }
    }
}
