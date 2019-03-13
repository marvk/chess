package net.marvk.chess.engine;

import lombok.Builder;
import lombok.Data;

/**
 * * info
 * the engine wants to send information to the GUI. This should be done whenever one of the info has changed.
 * The engine can send only selected infos or multiple infos with one info command,
 * e.g. "info currmove e2e4 currmovenumber 1" or
 * "info depth 12 nodes 123456 nps 100000".
 * Also all infos belonging to the pv should be sent together
 * e.g. "info depth 2 score cp 214 time 1242 nodes 2124 nps 34928 pv e2e4 e7e5 g1f3"
 * I suggest to start sending "currmove", "currmovenumber", "currline" and "refutation" only after one second
 * to avoid too much traffic.
 * Additional info:
 * * depth <x>
 * search depth in plies
 * * seldepth <x>
 * selective search depth in plies,
 * if the engine sends seldepth there must also be a "depth" present in the same string.
 * * time <x>
 * the time searched in ms, this should be sent together with the pv.
 * * nodes <x>
 * x nodes searched, the engine should send this info regularly
 * * pv <move1> ... <movei>
 * the best line found
 * * multipv <num>
 * this for the multi pv mode.
 * for the best move/pv add "multipv 1" in the string when you send the pv.
 * in k-best mode always send all k variants in k strings together.
 * * score
 * * cp <x>
 * the score from the engine's point of view in centipawns.
 * * mate <y>
 * mate in y moves, not plies.
 * If the engine is getting mated use negative values for y.
 * * lowerbound
 * the score is just a lower bound.
 * * upperbound
 * the score is just an upper bound.
 * * currmove <move>
 * currently searching this move
 * * currmovenumber <x>
 * currently searching move number x, for the first move x should be 1 not 0.
 * * hashfull <x>
 * the hash is x permill full, the engine should send this info regularly
 * * nps <x>
 * x nodes per second searched, the engine should send this info regularly
 * * tbhits <x>
 * x positions where found in the endgame table bases
 * * sbhits <x>
 * x positions where found in the shredder endgame databases
 * * cpuload <x>
 * the cpu usage of the engine is x permill.
 * * string <str>
 * any string str which will be displayed be the engine,
 * if there is a string command the rest of the line will be interpreted as <str>.
 * * refutation <move1> <move2> ... <movei>
 * move <move1> is refuted by the line <move2> ... <movei>, i can be any number >= 1.
 * Example: after move d1h5 is searched, the engine can send
 * "info refutation d1h5 g6h5"
 * if g6h5 is the best answer after d1h5 or if g6h5 refutes the move d1h5.
 * if there is no refutation for d1h5 found, the engine should just send
 * "info refutation d1h5"
 * The engine should only send this if the option "UCI_ShowRefutations" is set to true.
 * * currline <cpunr> <move1> ... <movei>
 * this is the current line the engine is calculating. <cpunr> is the number of the cpu if
 * the engine is running on more than one cpu. <cpunr> = 1,2,3....
 * if the engine is just using one cpu, <cpunr> can be omitted.
 * If <cpunr> is greater than 1, always send all k lines in k strings together.
 * The engine should only send this if the option "UCI_ShowCurrLine" is set to true.
 */
@Data
@Builder(buildMethodName = "generate")
public class Info {
    private final Integer depth;
    private final Integer selectiveDepth;
    private final Integer time;
    private final Long nodes;
    private final UciMove[] principalVariation;
    private final Integer multiPrincipalVariation;
    private final Score score;
    private final UciMove currentMove;
    private final Integer currentMoveNumber;
    private final Integer hashFull;
    private final Long nps;
    private final Integer tableHits;
    private final Integer shredderTableHits;
    private final Integer cpuLoad;
    private final String string;
    private final UciMove[] refutation;
    private final UciMove[] currentLine;

    public String toCommand() {
        return "info"
                + CommandUtil.toCommand("depth", depth)
                + CommandUtil.toCommand("seldepth", selectiveDepth)
                + CommandUtil.toCommand("time", time)
                + CommandUtil.toCommand("nodes", nodes)
                + CommandUtil.toCommand("pv", principalVariation)
                + CommandUtil.toCommand("multipv", multiPrincipalVariation)
                + CommandUtil.toCommand("score", score, Score::toCommand)
                + CommandUtil.toCommand("currmove", currentMove)
                + CommandUtil.toCommand("currmovenumber", currentMoveNumber)
                + CommandUtil.toCommand("hashfull", hashFull)
                + CommandUtil.toCommand("nps", nps)
                + CommandUtil.toCommand("tbhits", tableHits)
                + CommandUtil.toCommand("sbhits", shredderTableHits)
                + CommandUtil.toCommand("cpuload", cpuLoad)
                + CommandUtil.toCommand("string", string)
                + CommandUtil.toCommand("refutation", refutation)
                + CommandUtil.toCommand("currline", currentLine);
    }
}
