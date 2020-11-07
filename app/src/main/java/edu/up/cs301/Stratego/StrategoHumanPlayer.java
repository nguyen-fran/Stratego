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
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

public class StrategoHumanPlayer extends GameHumanPlayer implements OnClickListener {

    private GameMainActivity myActivity;
    private Button swap;
    private Button move;

    private ViewGroup gameBoardGrid;
    private GridLayout playerGY;
    private GridLayout oppGY;

    public static final int BLUE = 0;
    public static final int RED = 1;

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
            GY.setText(((StrategoGameState) info).getBlueGY()[i]);
        }

        //setting up computer graveyard with a loop
        for (int i = 0; i < 11; i++){
            TextView GY = (TextView)oppGY.getChildAt(i + 11);
            //setting text to whatever value is in graveyard array at that coord
            //might need to adjust +- 1 depending to avoid out of bounds errors
            GY.setText(((StrategoGameState) info).getRedGY()[i]);
        }

        //double for loop to update game board from game state
        for (int i = 0; i < 10; i++){
            for (int j = 0; j < 10; j++){
                //using this to be able to get from the board grid at the correct place
                int gridCoord = (i*10) + j;
                ImageButton square = (ImageButton)gameBoardGrid.getChildAt(gridCoord);

                //TODO: find out how to set text on imagebuttons or get images in here
                //might need to redo this based on filenames when switching to images

                //set text color based on team
                if(((StrategoGameState) info).getBoardSquares()[i][j].getPiece().getTeam() == BLUE){
                    //set text to blue
                }else{
                    //set text to red
                }

                //set text based on rank

            }
        }

    }

    @Override
    public void setAsGui(GameMainActivity activity) {
        myActivity = activity;
        activity.setContentView(R.layout.stratego_layout);

        gameBoardGrid = (ViewGroup) myActivity.findViewById(R.id.gameBoardGrid);
        for (int i = 0; i < 100; i++) {
            gameBoardGrid.addView(new Button(myActivity));
        }

        this.playerGY = activity.findViewById(R.id.blueGY);
        this.oppGY = activity.findViewById(R.id.redGY);

//        swap = myActivity.findViewById(R.id.swap);
//        move = myActivity.findViewById(R.id.move);
    }

    @Override
    public void onClick(View v) {
//        switch(v.getId()){
//            case R.id.swap:
//                game.sendAction(new StrategoSwapAction(this));
//                break;
//            case R.id.move:
//                game.sendAction(new StrategoMoveAction(this));
//                break;
//            default:
//                break;
//        }
    }
}
