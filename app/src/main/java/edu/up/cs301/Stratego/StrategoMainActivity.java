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
 * Known Issues:
 * -hiding pieces only works for human being the blue team and computer being the red team
 *  we would need to add another check for which team the computer player is on when drawing the board to add to the conditionals
 *
 * -smart ai is fairly buggy and runs into situations where it can't decide on a move to make, freezing the game
 *  the default move in the smart computer player class should mitigate this, but can fail (particularly if
 *  pieces are enclosed on three sides). One way to help keep this from happening would be to enable the ai to move
 *  pieces backwards, and tuning up the other helper methods for types of movements would likely help catch
 *  situations like this before they become a problem. Another spot that this tends to happen is when the
 *  human player is moving to attack the computer player's flag to win the game. If the computer player
 *  cannot defend the flag like it should, it will keep running through receiveInfo and trying to call
 *  the method to defend the flag, without changing turns and letting the human player move. This scenario
 *  should be handled by checking if the square to attack/attack with has been found or not, but there
 *  seems to be an issue with how that is determined, resulting in the computer trying to make an
 *  illegal move repeatedly.
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
