package edu.up.cs301.Stratego;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
    private Button begin;
    private Button reset;
    private Button rules;
    private Button quit;
    private TextView turnIndicator;

    private ViewGroup gameBoardGrid;
    private ViewGroup blueGY;
    private ViewGroup redGY;

    private int firstClick = -1;    //will hold id of first button clicked for move or swap action
    private int secondClick = -1;   //will hold id of second button clicked for move or swap action

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

    /**
     * updates gui to represent gamestate given by param
     *
     * @param info  current gamestate after an action's been made
     */
    @Override
    public void receiveInfo(GameInfo info) {
        Log.i("human player", "receiveInfo called");

        if(!(info instanceof StrategoGameState)){
            flash(Color.RED, 20);
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
        for (int i = 0; i < gameState.getBlueGY().length - 1; i++){
            TextView GY = (TextView) blueGY.getChildAt(i + 11);
            //setting text to whatever value is in graveyard array at that coord
            //might need to adjust +- 1 depending to avoid out of bounds errors
            GY.setText("" + ((StrategoGameState) info).getBlueGY()[i] + "/" + StrategoGameState.NUM_OF_PIECES[i + 1]);
        }

        //setting up computer graveyard with a loop
        for (int i = 0; i < 11; i++){
            TextView GY = (TextView) redGY.getChildAt(i + 11);
            //setting text to whatever value is in graveyard array at that coord
            //might need to adjust +- 1 depending to avoid out of bounds errors
            GY.setText("" + ((StrategoGameState) info).getRedGY()[i] + "/" + StrategoGameState.NUM_OF_PIECES[i + 1]);
        }

        //double for loop to update game board from game state
        for (int i = 0; i < StrategoGameState.BOARD_SIZE; i++){
            for (int j = 0; j < StrategoGameState.BOARD_SIZE; j++){
                //using this to be able to get from the board grid at the correct place
                int gridCoord = (i*10) + j;
                ImageButton square = (ImageButton)gameBoardGrid.getChildAt(gridCoord);
                boardImagePicker(square, (StrategoGameState)info, i, j);
            }
        }

        //updating turn indicator
        if(((StrategoGameState) info).getCurrPlayerIndex() == playerNum){
            turnIndicator.setText("Player's turn");
        }else{
            turnIndicator.setText("Opponent's turn");
        }

    }

    /**
     * helper method to pick out image for a given image button based on the game state
     *
     * @param button button to update the image of
     * @param gameState current state of the game
     * @param i row of game board array to pull from
     * @param j col of game board array to pull from
     */
    public void boardImagePicker(ImageButton button, StrategoGameState gameState, int i, int j){
        //setting image for lake squares
        if(gameState.getBoardSquares()[i][j].getOccupied() &&
                gameState.getBoardSquares()[i][j].getPiece() == null){
            button.setImageResource(R.drawable.lake);
        }
        //setting image for empty spaces
        else if(!gameState.getBoardSquares()[i][j].getOccupied() &&
                gameState.getBoardSquares()[i][j].getPiece() == null){
            button.setImageResource(R.drawable.empty_space);
        }
        //setting image for blue pieces (depending on visibility)
        else if (gameState.getBoardSquares()[i][j].getPiece().getTeam() == StrategoGameState.BLUE) {
            //don't draw blue pieces invisible if human player is blue
            if (playerNum == StrategoGameState.BLUE) {
                imagePickerBlue(button, gameState, i, j);
            } else {
                if (!gameState.getBoardSquares()[i][j].getPiece().getVisible()){
                    button.setImageResource(R.drawable.blue_unknown);
                } else {
                    imagePickerBlue(button, gameState, i, j);
                }
            }
        }
        //setting image for red pieces (depending on visibility)
        else if(gameState.getBoardSquares()[i][j].getPiece().getTeam() == StrategoGameState.RED){
            //don't draw red pieces invisible if human player is red
            //if (playerNum == StrategoGameState.RED) {
                imagePickerRed(button, gameState, i, j);
//            } else {
//                if (!gameState.getBoardSquares()[i][j].getPiece().getVisible()){
//                    button.setImageResource(R.drawable.red_unknown);
//                } else {
//                    imagePickerRed(button, gameState, i, j);
//                }
            //}
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

    /**
     * finds all views in gme layout and makes all the buttons for the gameboard
     *
     * @param activity  main game activity
     */
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

        this.blueGY = activity.findViewById(R.id.blueGY);
        this.redGY = activity.findViewById(R.id.redGY);
        this.turnIndicator = activity.findViewById(R.id.turnIndicator);

        this.begin = myActivity.findViewById(R.id.beginButton);
        this.begin.setOnClickListener(this);

        this.reset = myActivity.findViewById(R.id.resetButton);
        this.reset.setOnClickListener(this);

        this.rules = myActivity.findViewById(R.id.rulesButton);
        this.rules.setOnClickListener(this);

        this.quit = myActivity.findViewById(R.id.quitButton);
        this.quit.setOnClickListener(this);

    }

    /**
     * determines how to act based on what button was pressed
     *
     * @param v button pressed
     */
    @Override
    public void onClick(View v) {
        if(v.getId() == begin.getId()){
            begin();
        }else if(v.getId() == reset.getId()){
            reset();
        }else if(v.getId() == rules.getId()){
            rules();
        }else if(v.getId() == quit.getId()){
            quit();
        }else if(firstClick >= 0){
            secondClick = v.getId();
            ImageButton firstClickButton = myActivity.findViewById(firstClick);
            ImageButton secondClickButton = myActivity.findViewById(secondClick);

            if(gameState.getGamePhase()){
                game.sendAction(new StrategoMoveAction(this, firstClick, secondClick));
            }else{
                game.sendAction(new StrategoSwapAction(this, firstClick, secondClick));
            }
            firstClick = -1;
            secondClick = -1;
            firstClickButton.setBackgroundColor(Color.WHITE);
            secondClickButton.setBackgroundColor(Color.WHITE);
        }else{
            firstClick = v.getId();
            ImageButton firstClickButton = myActivity.findViewById(firstClick);
            firstClickButton.setBackgroundColor(Color.GREEN);
        }
    }


    /**
     * switches the game phase from setup to main gameplay loop
     */
    public void begin(){
        Log.i("testing game phase here", "" + gameState.getGamePhase());
        if(!gameState.getGamePhase()) {
            game.sendAction(new StrategoStartAction(this));
            begin.setAlpha(.5f);
            //begin.setClickable(false);
            Log.i("sent start action", "sent");
        }
        else{
            Log.i("trying to switch game phase", "did it work? " + gameState.getGamePhase());
        }
        //do nothing if not in setup phase
    }

    /**
     * resets the game via alert dialog
     *
     * External Citation
     * Date:    19 November 2020
     * Problem: Needed to be able to reset the game
     *
     * Resource: https://developer.android.com/reference/android/app/Activity.html#recreate%28%29
     * Solution: used this method that activity has
     */
    public void reset(){
        Log.i("testing reset button", "reset clicked");
        new AlertDialog.Builder(myActivity)
                .setTitle("Reset").setMessage("Are you sure you want to reset the game?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myActivity.recreate();
                    }
                }).setNegativeButton("No", null).show();
    }

    /**
     * displays the rules of stratego in an alert dialog
     */
    public void rules(){
        Log.i("testing rules button", "rules clicked");

        String rulesText = "Stratego is a board game, where the goal is to capture the opponent’s " +
                "flag to win. You begin the game by placing your pieces on your side of the board " +
                "(piece values are not visible to the opponent initially)" +
                "\n\nMoveable pieces: 1 Marshal, 1 General, 2 Colonels, 3 Majors, 4 Captains, " +
                "4 Lieutenants, 5 Sergeants, 5 Miners, 6 Scouts, 1 Spy" +
                "\nImmobile pieces: 6 Bombs, 1 Flag" +
                "\n\nOn each player’s turn, they can move to or attack an adjacent square with 1 piece " +
                "(Scouts can move any number of squares, like a rook in chess). When attacking/being " +
                "attacked, the piece with the lower rank/numerical value is captured (if both are " +
                "the same rank, then both are taken off the board). When any piece except for a " +
                "Miner attacks a Bomb, that piece gets captured. Only Miners are able to defuse Bombs " +
                "and capture them. The Spy is the only piece that can capture the Marshal, " +
                "but any piece can capture the Spy.";
        new AlertDialog.Builder(myActivity)
                .setTitle("Rules").setMessage(rulesText)
                .setNegativeButton("Back", null).show();
    }

    /**
     * lets you quit the game via alert dialog
     *
     * External Citation
     * Date:    17 November 2020
     * Problem: Needed to create confirmation box to confirm exit from app
     *
     * Resource: https://www.tutorialspoint.com/how-to-show-a-dialog-to-confirm-that-the-user-wishes-to-exit-an-android-activity
     * Solution: I used the example code snippet to create our dialog for the confirmation box
     */
    public void quit(){
        new AlertDialog.Builder(myActivity)
                .setTitle("Closing Activity").setMessage("Are you sure you want to quit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myActivity.finish();
                    }
                }).setNegativeButton("No", null).show();
    }

}
