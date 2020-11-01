package edu.up.cs301.Stratego;

import edu.up.cs301.game.GameFramework.GameMainActivity;
import edu.up.cs301.game.GameFramework.LocalGame;
import edu.up.cs301.game.GameFramework.gameConfiguration.GameConfig;

public class StrategoMainActivity extends GameMainActivity {

    @Override
    public GameConfig createDefaultConfig() {
        return null;
    }


    @Override
    public LocalGame createLocalGame() {
        return new StrategoLocalGame();
    }
}
