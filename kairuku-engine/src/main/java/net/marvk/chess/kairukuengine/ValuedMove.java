package net.marvk.chess.kairukuengine;

import lombok.Data;
import net.marvk.chess.core.bitboards.Bitboard;

@Data
public class ValuedMove {
    private final int value;
    private final Bitboard.BBMove move;
    private final ValuedMove pvChild;
}
