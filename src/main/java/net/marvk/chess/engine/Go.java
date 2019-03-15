package net.marvk.chess.engine;

import lombok.Builder;
import lombok.Data;

/**
 * searchMoves
 * restrict search to this moves only
 * Example: After "position startpos" and "go infinite searchmoves e2e4 d2d4"
 * the engine should only search the two moves e2e4 and d2d4 in the initial position.
 *
 * ponder
 * start searching in pondering mode.
 * Do not exit the search in ponder mode, even if it's mate!
 * This means that the last move sent in in the position string is the ponder move.
 * The engine can do what it wants to do, but after a "ponderhit" command
 * it should execute the suggested move to ponder on. This means that the ponder move sent by
 * the GUI can be interpreted as a recommendation about which move to ponder. However, if the
 * engine decides to ponder on a different move, it should not display any mainlines as they are
 * likely to be misinterpreted by the GUI because the GUI expects the engine to ponder
 * on the suggested move.
 * whiteTime
 * white has {@code whiteTime} msec left on the clock
 *
 * blackTime
 * black has {@code blackTime} msec left on the clock
 *
 * whiteIncrement
 * white increment per move in mseconds if {@code whiteIncrement} > 0
 *
 * blackIncrement
 * black increment per move in mseconds if {@code blackIncrement} > 0
 *
 * movesToGo
 * there are {@code movesToGo} moves to the next time control,
 * this will only be sent if {@code movesToGo} > 0,
 * if you don't get this and get the wtime and btime it's sudden death
 *
 * depth
 * search {@code } plies only.
 * nodes
 * search {@code } nodes only,
 *
 * mate
 * search for a mate in {@code } moves
 *
 * moveTime
 * search exactly {@code } mseconds
 *
 * infinite
 * search until the "stop" command. Do not exit the search without being told so in this mode!
 */
@Data
@Builder
public class Go {
    private final UciMove[] searchMoves;
    private final Boolean ponder;
    private final Integer whiteTime;
    private final Integer blackTime;
    private final Integer whiteIncrement;
    private final Integer blackIncrement;
    private final Integer movesToGo;
    private final Integer depth;
    private final Integer nodes;
    private final Integer mate;
    private final Integer moveTime;
    private final Boolean infinite;
}
