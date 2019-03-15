package net.marvk.chess.uci4j;

import net.marvk.chess.core.board.UciMove;

import java.util.List;

public interface UIChannel {

    /**
     * <p>UCI Description:</p>
     * <p>this must be sent after receiving the "uci" command to identify the engine.</p>
     * <p>Alternatively, send {@link UIChannel#idAuthor(String)}</p>
     *
     * @param name the name
     */
    default void idName(final String name) {

    }

    /**
     * {@link UIChannel#idName(String)}
     *
     * @param author the name
     */
    default void idAuthor(final String author) {

    }

    /**
     * <p>UCI Description:</p>
     * <p>Must be sent after the id and optional options to tell the GUI that the engine
     * has sent all infos and is ready in uci mode.</p>
     */
    default void uciOk() {

    }

    /**
     * <p>UCI Description:</p>
     * <p>This must be sent when the engine has received an "isready" command and has
     * processed all input and is ready to accept new commands now.
     * It is usually sent after a command that can take some time to be able to wait for the engine,
     * but it can be used anytime, even when the engine is searching,
     * and must always be answered with "isready".</p>
     */
    default void readyOk() {

    }

    /**
     * {@link UIChannel#bestMove(UciMove, UciMove)}
     *
     * @param move best move the engine found
     */
    void bestMove(final UciMove move);

    /**
     * <p>UCI Description:</p>
     * <p>the engine has stopped searching and found the move <move> best in this position.
     * the engine can send the move it likes to ponder on. The engine must not start pondering automatically.
     * this command must always be sent if the engine stops searching, also in pondering mode if there is a
     * "stop" command, so for every "go" command a "bestmove" command is needed!
     * Directly before that the engine should send a final "info" command with the final search information,
     * the the GUI has the complete statistics about the last search.</p>
     *
     * @param move   best move the engine found
     * @param ponder move engine would like to ponder on
     */
    default void bestMove(final UciMove move, final UciMove ponder) {
        bestMove(move);
    }

    /**
     * <p>UCI Description:</p>
     * <p>this is needed for copyprotected engines. After the uciok command the engine can tell the GUI,
     * that it will check the copy protection now. This is done by "copyprotection checking".
     * If the check is ok the engine should send "copyprotection ok", otherwise "copyprotection error".
     * If there is an error the engine should not function properly but should not quit alone.
     * If the engine reports "copyprotection error" the GUI should not use this engine
     * and display an error message instead!
     * The code in the engine can look like this
     * <pre>
     * TellGUI("copyprotection checking\n");
     * // ... check the copy protection here ...
     * if(ok)
     *     TellGUI("copyprotection ok\n");
     * else
     *     TellGUI("copyprotection error\n");
     * </pre></p>
     */
    default void copyProtection() {

    }

    /**
     * <p>UCI Description:</p>
     * <p>	this is needed for engines that need a username and/or a code to function with all features.
     * Analog to the "copyprotection" command the engine can send "registration checking"
     * after the uciok command followed by either "registration ok" or "registration error".
     * Also after every attempt to register the engine it should answer with "registration checking"
     * and then either "registration ok" or "registration error".
     * In contrast to the "copyprotection" command, the GUI can use the engine after the engine has
     * reported an error, but should inform the user that the engine is not properly registered
     * and might not use all its features.
     * In addition the GUI should offer to open a dialog to
     * enable registration of the engine. To try to register an engine the GUI can send
     * the "register" command.
     * The GUI has to always answer with the "register" command	if the engine sends "registration error"
     * at engine startup (this can also be done with "register later")
     * and tell the user somehow that the engine is not registered.
     * This way the engine knows that the GUI can deal with the registration procedure and the user
     * will be informed that the engine is not properly registered.</p>
     */
    default void registration() {

    }

    /**
     * <p>UCI Description:</p>
     * <p>	the engine wants to send information to the GUI. This should be done whenever one of the info has changed.
     * The engine can send only selected infos or multiple infos with one info command,
     * e.g. "info currmove e2e4 currmovenumber 1" or
     * "info depth 12 nodes 123456 nps 100000".
     * Also all infos belonging to the pv should be sent together
     * e.g. "info depth 2 score cp 214 time 1242 nodes 2124 nps 34928 pv e2e4 e7e5 g1f3"
     * I suggest to start sending "currmove", "currmovenumber", "currline" and "refutation" only after one second
     * to avoid too much traffic.</p>
     */
    default void info(final Info info) {

    }

    /**
     * <p>UCI Description:</p>
     * <p>This command tells the GUI which parameters can be changed in the engine.
     * This should be sent once at engine startup after the "uci" and the "id" commands
     * if any parameter can be changed in the engine.
     * The GUI should parse this and build a dialog for the user to change the settings.
     * Note that not every option needs to appear in this dialog as some options like
     * "Ponder", "UCI_AnalyseMode", etc. are better handled elsewhere or are set automatically.
     * If the user wants to change some settings, the GUI will send a "setoption" command to the engine.
     * Note that the GUI need not send the setoption command when starting the engine for every option if
     * it doesn't want to change the default value.
     * For all allowed combinations see the examples below,
     * as some combinations of this tokens don't make sense.
     * One string will be sent for each parameter.</p>
     */
    default void optionCheck(final String name, final boolean enabled) {

    }

    /**
     * {@link UIChannel#optionCheck(String, boolean)}
     */
    default void optionSpin(final String name, final int defaultValue, final int min, final int max) {

    }

    /**
     * {@link UIChannel#optionCheck(String, boolean)}
     */
    default void optionCombo(final String name, final String defaultValue, final List<String> possibleValues) {

    }

    /**
     * {@link UIChannel#optionCheck(String, boolean)}
     */
    default void optionString(final String name, final String defaultValue) {

    }

    /**
     * {@link UIChannel#optionCheck(String, boolean)}
     */
    default void optionButton(final String name) {

    }
}
