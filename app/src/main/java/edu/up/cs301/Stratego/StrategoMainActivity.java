package edu.up.cs301.Stratego;

import java.util.ArrayList;

import edu.up.cs301.game.GameFramework.GameMainActivity;
import edu.up.cs301.game.GameFramework.GamePlayer;
import edu.up.cs301.game.GameFramework.LocalGame;
import edu.up.cs301.game.GameFramework.gameConfiguration.GameConfig;
import edu.up.cs301.game.GameFramework.gameConfiguration.GamePlayerType;

/**
 * Main activity class
 *
 * Current State of the Game as of Beta Release (11/25/20)
 * rule of play and board setup are implemented (human player can swap pieces around at the start of
 * the game, and make moves during the main gameplay phase)
 * game can be played with human and computer player where either the computer->red and human->blue or the other way around
 * UI is fully implemented (can call up a rules popup or reset/quit the game), settings button was
 * cut due to settings (like for the player names/ai settings) being handled in the game framework menus instead
 *
 * Known Issues: updated (12/4/20)
 * - smart AI will only pick a premade board setup if it is player 2
 * - sizing on different resolution is messed up
 * - the gui will not work for certain issues if the human player is not player 1
 *
 * @author Gabby Marshak
 * @author Francisco Nguyen
 * @author Blake Nygren
 * @author Jack Volonte
 */
public class StrategoMainActivity extends GameMainActivity {

    private static final int PORT_NUMBER = 2278;

    @Override
    public GameConfig createDefaultConfig() {
        /**
         * External Citation
         * Date:    17 November 2020
         * Problem: Need screen to be kept in portrait mode
         *
         * Resource:    https://stackoverflow.com/questions/3723823/i-want-my-android-application-to-be-only-run-in-portrait-mode
         * Solution:    I put the solution in the Manifest file. The comment is here so that there's
         *              not a big comment block in the Manifest file.
         */

        /**
         * External Citation
         * Date:    1 November 2020
         * Problem: Didn't know how to set up the configuration
         *
         * Resource:    https://github.com/cs301up/PigGameStarter
         * Solution:    I used the code from the project's main activity class, changing some values to fit Stratego
         */
        ArrayList<GamePlayerType> playerTypes = new ArrayList<GamePlayerType>();

        playerTypes.add(new GamePlayerType("Local Human Player") {
            @Override
            public GamePlayer createPlayer(String name) {
                return new StrategoHumanPlayer(name);
            }});
        playerTypes.add(new GamePlayerType("Computer Player") {
            @Override
            public GamePlayer createPlayer(String name) {
                return new StrategoComputerPlayer(name);
            }});
        playerTypes.add(new GamePlayerType("Smart Computer Player") {
            @Override
            public GamePlayer createPlayer(String name) {
                return new StrategoSmartComputerPlayer(name);
            }
        });

        GameConfig defaultConfig = new GameConfig(playerTypes, 2, 2, "Stratego", PORT_NUMBER);
        defaultConfig.addPlayer("Blue", 0);
        defaultConfig.addPlayer("Red", 1);
        defaultConfig.setRemoteData("Remote Human Player", "", 0);
        return defaultConfig;
    }


    @Override
    public LocalGame createLocalGame() {
        return new StrategoLocalGame();
    }
}
