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
 * Current State of the Game as of Final Release (12/4/20)
 * Rule of play and board setup are implemented (human player can swap pieces around at the start of
 * the game, and make moves during the main gameplay phase).
 * Game can be played with human and computer player or smart computer player and the game
 * works regardless of who is player 1 or 2.
 * UI is fully implemented (can call up a rules popup or reset/quit the game), settings button was
 * cut due to settings (like for the player names/ai settings) being handled in the game framework menus instead.
 *
 * Known Issues: updated (12/4/20)
 * - (this is lack of functionality and not a bug but, will not fix) smart AI will only pick a premade board setup if it is player 2
 *      - implementing the functionality when smart ai is player 1 will require a lot
 *        of fiddling with the gamestate's currPlayerIndex in local game's makeMove
 *        that could make it unable to work if smart ai is player 2
 * - (will not fix) the buttons that make up the board don't fit on-screen for all screen sizes
 *      - tried moving around components and putting them in different sub-components
 *        and tried playing with the weights of everything but it seems that the
 *        images are dominating the sizing
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
