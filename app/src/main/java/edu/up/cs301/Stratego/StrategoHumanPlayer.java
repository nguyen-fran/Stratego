package edu.up.cs301.Stratego;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
    private Button undo;
    private Button reset;
    private Button rules;
    private Button quit;
    private Button settings;
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
        return myActivity.findViewById(R.id.rules_page);
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
            if (!gameState.getBoardSquares()[i][j].getPiece().getVisible()) {
                button.setImageResource(R.drawable.blue_unknown);
            }
            imagePickerBlue(button, gameState, i, j);
        }else if(gameState.getBoardSquares()[i][j].getPiece().getTeam() == StrategoGameState.RED){ //red pieces
            if(!gameState.getBoardSquares()[i][j].getPiece().getVisible()){
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
        ImageButton boardSquareButton;
        for (int i = 0; i < 100; i++) {
            boardSquareButton = new ImageButton(myActivity);
            boardSquareButton.setId(i);
            boardSquareButton.setOnClickListener(this);

            gameBoardGrid.addView(boardSquareButton);
        }

        this.playerGY = activity.findViewById(R.id.blueGY);
        this.oppGY = activity.findViewById(R.id.redGY);
        this.turnIndicator = activity.findViewById(R.id.turnIndicator);

        this.undo = myActivity.findViewById(R.id.undoButton);
        this.undo.setOnClickListener(this);

        this.reset = myActivity.findViewById(R.id.resetButton);
        this.reset.setOnClickListener(this);

        this.rules = myActivity.findViewById(R.id.rulesButton);
        this.rules.setOnClickListener(this);

        this.quit = myActivity.findViewById(R.id.quitButton);
        this.quit.setOnClickListener(this);

        this.settings = myActivity.findViewById(R.id.settingsButton);
        this.settings.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == undo.getId()){
            undo();
        }else if(v.getId() == reset.getId()){
            reset();
        }else if(v.getId() == rules.getId()){
            rules();
        }else if(v.getId() == quit.getId()){
            quit();
        }else if(v.getId() == settings.getId()){
            settings();
        }else if(firstClick >= 0){
            secondClick = v.getId();
            Log.i("testing clicks", "recorded first click " + firstClick + " and made it to record the second" + secondClick);
            ImageButton firstClickButton = myActivity.findViewById(firstClick);
            ImageButton secondClickButton = myActivity.findViewById(secondClick);
            // TODO need better way to determine which action is being attempted
            if(gameState.getGamePhase()){
                game.sendAction(new StrategoMoveAction(this, firstClick, secondClick));
                firstClick = -1;
                secondClick = -1;
            }
            else{
                game.sendAction(new StrategoSwapAction(this, firstClick, secondClick));
                firstClick = -1;
                secondClick = -1;
            }
            firstClickButton.setBackgroundColor(Color.WHITE);
            secondClickButton.setBackgroundColor(Color.WHITE);
        }else{
            firstClick = v.getId();
            ImageButton firstClickButton = myActivity.findViewById(firstClick);
            firstClickButton.setBackgroundColor(Color.GREEN);
        }
    }

    //TODO need to define which moves can be undone
    //use Toast to show illegal move?
    public void undo(){
        Log.i("testing undo button", "undo clicked");
        //tempGameState.setPrevGameState(tempGameState.getPrevGameState());
    }

    public void reset(){
        Log.i("testing reset button", "reset clicked");
    }

    //TODO: header comment, citation for https://stackoverflow.com/questions/10936042/how-to-open-layout-on-button-click-android
    public void rules(){
        Log.i("testing rules button", "rules clicked");
        Intent rulesIntent = new Intent(myActivity.getApplicationContext(), RulesActivity.class);
        myActivity.startActivity(rulesIntent);
    }

    public void quit(){
        //TODO need citation here
        //https://www.tutorialspoint.com/how-to-show-a-dialog-to-confirm-that-the-user-wishes-to-exit-an-android-activity

        new AlertDialog.Builder(myActivity)
                .setTitle("Closing Activity").setMessage("Are you sure you want to quit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myActivity.finish();
                    }
                }).setNegativeButton("No", null).show();
    }


    public void settings(){
        Log.i("testing settings button", "settings clicked");
    }
}
