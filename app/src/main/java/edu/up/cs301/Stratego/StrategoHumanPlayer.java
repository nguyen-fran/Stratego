package edu.up.cs301.Stratego;

import android.view.View;

import edu.up.cs301.R;
import edu.up.cs301.game.GameFramework.GameHumanPlayer;
import edu.up.cs301.game.GameFramework.GameMainActivity;
import edu.up.cs301.game.GameFramework.infoMessage.GameInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class StrategoHumanPlayer extends GameHumanPlayer implements OnClickListener {

    private GameMainActivity myActivity;
    private Button swap;
    private Button move;

    private ViewGroup gameBoardGrid;

    private int firstClick = -1;
    private int secondClick = -1;

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

    }

    @Override
    public void setAsGui(GameMainActivity activity) {
        myActivity = activity;
        activity.setContentView(R.layout.stratego_layout);

        gameBoardGrid = (ViewGroup) myActivity.findViewById(R.id.gameBoardGrid);
        Button temp;
        for (int i = 0; i < 100; i++) {
            temp = new Button(myActivity);
            temp.setId(i);
            gameBoardGrid.addView(temp);
        }

//        swap = myActivity.findViewById(R.id.swap);
//        move = myActivity.findViewById(R.id.move);
    }

    @Override
    public void onClick(View v) {
        if(firstClick > 0){
            secondClick = v.getId();
            // TODO need better way to determine which action is being attempted
            if(Math.abs((firstClick-secondClick)) == 1){
                game.sendAction(new StrategoMoveAction(this, firstClick, secondClick));
            }
            else{
                game.sendAction(new StrategoSwapAction(this, firstClick, secondClick));
            }
        }
        else{
            firstClick = v.getId();
        }
        /*
          switch(v.getId()){
            case R.id.swap:
                game.sendAction(new StrategoSwapAction(this));
                break;
            case R.id.move:
                game.sendAction(new StrategoMoveAction(this));
                break;
            default:
                break;
        }
         */
    }
}
