package net.marvk.chess.engine;

public interface Engine {
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
    void uci();

    /**
     * <p>UCI Description:</p>
     * <p>switch the debug mode of the engine on and off.
     * In debug mode the engine should send additional infos to the GUI, e.g. with the "info string" command,
     * to help debugging, e.g. the commands that the engine has received etc.
     * This mode should be switched off by default and this command can be sent
     * any time, also when the engine is thinking.</p>
     *
     * @param debug
     */
    void setDebug(final boolean debug);

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
    void setOption(final String name, final String value);

    /**
     * <p>UCI Description:</p>
     * {@link Engine#setOption(String, String)}
     *
     * @param name
     */
    void setOption(final String name);

    /**
     * <p>UCI Description:</p>
     * <p>this is the command to tell the engine that registration
     * will be done later. See also: {@link Engine#register(String, String)}</p>
     */
    void registerLater();

    /**
     * <p>UCI Description:</p>
     * <p></p>this is the command to try to register an engine. This command should always be sent if the engine has sent "registration error"
     * at program startup.</p>
     *
     * @param name the engine should be registered with the name
     * @param code the engine should be registered with the code
     */
    void register(final String name, final String code);

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
    void uciNewGame();

    /**
     * <p>UCI Description:</p>
     * <p>set up the starting position on the internal board and
     * play the moves on the internal chess board.
     * Note: no "new" command is needed. However, if this position is from a different game than
     * the last position sent to the engine, the GUI should have sent a "ucinewgame" inbetween.</p>
     *
     * @param moves the moves
     */
    void positionFromDefault(final UciMove[] moves);

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
    void position(final String fenString, final UciMove[] moves);

    /**
     * <p>UCI Description:</p>
     * <p>     * start calculating on the current position set up with the "position" command.
     * There are a number of commands that can follow this command, all will be sent in the same string.
     * If one command is not sent its value should be interpreted as it would not influence the search.</p>
     *
     * @param searchMoves    restrict search to this moves only
     *                       Example: After "position startpos" and "go infinite searchmoves e2e4 d2d4"
     *                       the engine should only search the two moves e2e4 and d2d4 in the initial position.
     * @param ponder         start searching in pondering mode.
     *                       Do not exit the search in ponder mode, even if it's mate!
     *                       This means that the last move sent in in the position string is the ponder move.
     *                       The engine can do what it wants to do, but after a "ponderhit" command
     *                       it should execute the suggested move to ponder on. This means that the ponder move sent by
     *                       the GUI can be interpreted as a recommendation about which move to ponder. However, if the
     *                       engine decides to ponder on a different move, it should not display any mainlines as they are
     *                       likely to be misinterpreted by the GUI because the GUI expects the engine to ponder
     *                       on the suggested move.
     * @param whiteTime      white has {@code whiteTime} msec left on the clock
     * @param blackTime      black has {@code blackTime} msec left on the clock
     * @param whiteIncrement white increment per move in mseconds if {@code whiteIncrement} > 0
     * @param blackIncrement black increment per move in mseconds if {@code blackIncrement} > 0
     * @param movesToGo      there are {@code movesToGo} moves to the next time control,
     *                       this will only be sent if {@code movesToGo} > 0,
     *                       if you don't get this and get the wtime and btime it's sudden death
     * @param depth          search {@code } plies only.
     * @param nodes          search {@code } nodes only,
     * @param mate           search for a mate in {@code } moves
     * @param moveTime       search exactly {@code } mseconds
     * @param infinite       search until the "stop" command. Do not exit the search without being told so in this mode!
     */
    void go(
            final UciMove[] searchMoves,
            final boolean ponder,
            final long whiteTime,
            final long blackTime,
            final long whiteIncrement,
            final long blackIncrement,
            final int movesToGo,
            final int depth,
            final int nodes,
            final int mate,
            final long moveTime,
            final boolean infinite
    );

    /**
     * <p>UCI Description:</p>
     * <p>stop calculating as soon as possible,
     * don't forget the "bestmove" and possibly the "ponder" token when finishing the search</p>
     */
    void stop();

    /**
     * <p>UCI Description:</p>
     * <p>the user has played the expected move. This will be sent if the engine was told to ponder on the same move
     * the user has played. The engine should continue searching but switch from pondering to normal search.</p>
     */
    void ponderHit();

    /**
     * <p>UCI Description:</p>
     * <p>quit the program as soon as possible</p>
     */
    void quit();
}
