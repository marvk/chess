package net.marvk.chess.board;

public enum EndCondition {
    CHECKMATE,
    DRAW_BY_STALEMATE,
    DRAW_BY_THREEFOLD_REPETITION,
    DRAW_BY_FIFTY_MOVE_RULE,
    DRAW_BY_DEAD_POSITION,
    DRAW_BY_MUTUAL_AGREEMENT
}
