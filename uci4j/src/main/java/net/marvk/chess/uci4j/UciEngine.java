package net.marvk.chess.uci4j;

import net.marvk.chess.core.UciMove;

public abstract class UciEngine {
    protected final UIChannel uiChannel;

    public UciEngine(final UIChannel uiChannel) {
        this.uiChannel = uiChannel;
    }

    /**
     * <p>UCI Description:</p>
     * <p>tell engine to use the uci (universal chess interface),
     * this will be sent once as a first command after program boot
     * to tell the engine to switch to uci mode.
     * After receiving the uci command the engine must identify itself with the "id" command
     * and send the "option" commands to tell the GUI which engine settings the engine supports if any.
     * After that the engine should send "uciok" to acknowledge the uci mode.
     * If no uciok is sent within a certain time period, the engine task will be killed by the GUI.</p>
     */
    public abstract void uci();

    /**
     * <p>UCI Description:</p>
     * <p>switch the debug mode of the engine on and off.
     * In debug mode the engine should send additional infos to the GUI, e.g. with the "info string" command,
     * to help debugging, e.g. the commands that the engine has received etc.
     * This mode should be switched off by default and this command can be sent
     * any time, also when the engine is thinking.</p>
     *
     * @param debug debug enabled
     */
    public abstract void setDebug(final boolean debug);

    /**
     * <p>UCI Description:</p>
     * <p>	this is used to synchronize the engine with the GUI. When the GUI has sent a command or
     * multiple commands that can take some time to complete,
     * this command can be used to wait for the engine to be ready again or
     * to ping the engine to find out if it is still alive.
     * E.g. this should be sent after setting the path to the tablebases as this can take some time.
     * This command is also required once before the engine is asked to do any search
     * to wait for the engine to finish initializing.
     * This command must always be answered with "readyok" and can be sent also when the engine is calculating
     * in which case the engine should also immediately answer with "readyok" without stopping the search.</p>
     */
    public abstract void isReady();

    /**
     * <p>UCI Description:</p>
     * <p>this is sent to the engine when the user wants to change the internal parameters
     * of the engine. For the "button" type no value is needed.
     * One string will be sent for each parameter and this will only be sent when the engine is waiting.
     * The name and value of the option in <id> should not be case sensitive and can inlude spaces.
     * The substrings "value" and "name" should be avoided in <id> and <x> to allow unambiguous parsing,
     * for example do not use <name> = "draw value".</p>
     *
     * @param name  the name
     * @param value the value
     */
    public abstract void setOption(final String name, final String value);

    /**
     * <p>UCI Description:</p>
     * <p>this is the command to tell the engine that registration
     * will be done later. See also: {@link UciEngine#register(String, String)}</p>
     */
    public abstract void registerLater();

    /**
     * <p>UCI Description:</p>
     * <p></p>this is the command to try to register an engine. This command should always be sent if the engine has sent "registration error"
     * at program startup.</p>
     *
     * @param name the engine should be registered with the name
     * @param code the engine should be registered with the code
     */
    public abstract void register(final String name, final String code);

    /**
     * <p>UCI Description:</p>
     * <p>this is sent to the engine when the next search (started with "position" and "go") will be from
     * a different game. This can be a new game the engine should play or a new game it should analyse but
     * also the next position from a testsuite with positions only.
     * If the GUI hasn't sent a "ucinewgame" before the first "position" command, the engine shouldn't
     * expect any further ucinewgame commands as the GUI is probably not supporting the ucinewgame command.
     * So the engine should not rely on this command even though all new GUIs should support it.
     * As the engine's reaction to "ucinewgame" can take some time the GUI should always send "isready"
     * after "ucinewgame" to wait for the engine to finish its operation.</p>
     */
    public abstract void uciNewGame();

    /**
     * <p>UCI Description:</p>
     * <p>set up the starting position on the internal board and
     * play the moves on the internal chess board.
     * Note: no "new" command is needed. However, if this position is from a different game than
     * the last position sent to the engine, the GUI should have sent a "ucinewgame" inbetween.</p>
     *
     * @param moves the moves
     */
    public abstract void positionFromDefault(final UciMove[] moves);

    /**
     * <p>UCI Description:</p>
     * <p>set up the position described in fenstring on the internal board and
     * play the moves on the internal chess board.
     * Note: no "new" command is needed. However, if this position is from a different game than
     * the last position sent to the engine, the GUI should have sent a "ucinewgame" inbetween.</p>
     *
     * @param fenString the fen string
     * @param moves     the moves
     */
    public abstract void position(final String fenString, final UciMove[] moves);

    /**
     * <p>UCI Description:</p>
     * <p>     * start calculating on the current position set up with the "position" command.
     * There are a number of commands that can follow this command, all will be sent in the same string.
     * If one command is not sent its value should be interpreted as it would not influence the search.</p>
     *
     * See also {@link Go}
     *
     * @param go the values for the go command
     */
    public abstract void go(final Go go);

    public void go() {
        go(Go.EMPTY);
    }

    /**
     * <p>UCI Description:</p>
     * <p>stop calculating as soon as possible,
     * don't forget the "bestmove" and possibly the "ponder" token when finishing the search</p>
     */
    public abstract void stop();

    /**
     * <p>UCI Description:</p>
     * <p>the user has played the expected move. This will be sent if the engine was told to ponder on the same move
     * the user has played. The engine should continue searching but switch from pondering to normal search.</p>
     */
    public abstract void ponderHit();

    /**
     * <p>UCI Description:</p>
     * <p>quit the program as soon as possible</p>
     */
    public abstract void quit();
}
