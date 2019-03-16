package net.marvk.chess.core.bitboards;

import net.marvk.chess.core.board.Square;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class MagicBitboard {
    public static final MagicBitboard ROOK;
    public static final MagicBitboard BISHOP;

    private static final Square[] SQUARES = Square.values();

    static {
        final long[] rookMagics = {
                0x8001a040001080L, 0x2040001008200045L, 0x100102001290240L, 0xa00060010082040L,
                0x8004000a804800L, 0x5100410028020c00L, 0x280008001005200L, 0x80022100024080L,
                0x85800122400380L, 0xc003400120100240L, 0x8802000841002L, 0x404600408a002010L,
                0x8002002200880410L, 0x8002002200880410L, 0x402000438020013L, 0x3000080d10002L,
                0x40008000802042L, 0x20108020400080L, 0x8002020040308020L, 0xc008010100100020L,
                0x4008014808800L, 0x8602008100040080L, 0x740040002101108L, 0x4420000a40041L,
                0x4040002480104080L, 0x8d00500840002004L, 0xc010802200104200L, 0x1208000880801000L,
                0x2848008080086400L, 0x2202801300440L, 0x3040420400481083L, 0x24c48200004401L,
                0x40008000802042L, 0x10601000e0400040L, 0x404600408a002010L, 0x1208000880801000L,
                0x400820801800400L, 0x8001002209001c00L, 0x402100854000102L, 0x1004484422000091L,
                0x714008808000L, 0x608420100240c000L, 0x802014a80220010L, 0x1001001000250018L,
                0x219000800850050L, 0x8002000400028080L, 0x108805040002L, 0x3000080d10002L,
                0x200400021800080L, 0xc003400120100240L, 0xc010802200104200L, 0x4040840800100280L,
                0x1480014008080L, 0x800080ac00160080L, 0x2000081a41100400L, 0x1000642208100L,
                0x81008000a01841L, 0x2201002010844001L, 0xd004010182001L, 0x8040200500089001L,
                0x2102005020080c42L, 0x5021000884000a01L, 0x100a004102980402L, 0x440c0608408c106L
        };

        ROOK = new MagicBitboard(
                square -> Configuration.rookConfiguration(square, rookMagics[square.getBitboardIndex()]),
                rookMagics
        );

        final long[] bishopMagics = {
                0x2204a0210c11200L, 0x2204a0210c11200L, 0x4014240400444100L, 0x184040a98002130L,
                0x4051040010c0080L, 0x1100290080800L, 0x2204a0210c11200L, 0x2208404205200L,
                0x4000407084008480L, 0x1a4404040042L, 0x4040414034490L, 0xa042040400804100L,
                0x1408031040220800L, 0xa200091008240001L, 0x8041386882026L, 0x40010c400880915L,
                0x21040042085a0408L, 0x404002038420050L, 0x184004241020204L, 0x4042200802084010L,
                0x4124808404a00401L, 0x8002000408823820L, 0x402b004610822024L, 0x9824210104210400L,
                0xb0088806081009L, 0x4200022080100L, 0x800480004080210L, 0x1406008088028002L,
                0x88c1011041004004L, 0x6012020000880100L, 0x6012020000880100L, 0x4051040010c0080L,
                0x10028800a00800L, 0x1100290080800L, 0xa122209000080820L, 0x2a00a20084080080L,
                0xb0060080025004L, 0x10a0200408800L, 0x4014240400444100L, 0x1028890040050402L,
                0xb01011840082001L, 0x400411010008805L, 0x120101808010400L, 0x2160301414000800L,
                0x2055540d0c020600L, 0xa0489010800040L, 0x2204a0210c11200L, 0x2204040045400204L,
                0x2204a0210c11200L, 0x2091010130220002L, 0x1a4404040042L, 0x20280088c040800L,
                0x21000041104b0020L, 0x400400801810000L, 0xc120081000809360L, 0x2204a0210c11200L,
                0x2208404205200L, 0x40010c400880915L, 0x50000a061080811L, 0x40002250104980aL,
                0x100800210020208L, 0x1120201020110441L, 0x4000407084008480L, 0x2204a0210c11200L
        };

        BISHOP = new MagicBitboard(
                square -> Configuration.bishopConfiguration(square, bishopMagics[square.getBitboardIndex()]),
                bishopMagics
        );
    }

    private final long[] magics;

    private final long[] masks;
    private final long[] hashShifts;
    private final int[] hashMasks;

    private final long[][] attacks;

    private MagicBitboard(final Function<Square, Configuration> configurationGenerator) {
        this(configurationGenerator, null);
    }

    private MagicBitboard(final Function<Square, Configuration> configurationGenerator, final long[] magics) {
        final boolean predefinedMagic = magics != null;

        this.magics = predefinedMagic ? magics : new long[64];

        this.masks = new long[64];
        this.hashShifts = new long[64];
        this.hashMasks = new int[64];

        this.attacks = new long[64][];

        for (final Square square : SQUARES) {
            final Configuration configuration = configurationGenerator.apply(square);

            final int index = square.getBitboardIndex();

            if (!predefinedMagic) {
                this.magics[index] = configuration.getMagic();
            }
            this.masks[index] = configuration.getMask();
            this.hashShifts[index] = configuration.getHashShift();
            this.hashMasks[index] = configuration.getHashMask();

            this.attacks[index] = configuration.generateAllAttacks();
        }
    }

    public long attacks(final long occupancy, final Square square) {
        return attacks[square.getBitboardIndex()][hash(occupancy, square.getBitboardIndex())];
    }

    public long attacks(final long occupancy, final int squareIndex) {
        return attacks[squareIndex][hash(occupancy, squareIndex)];
    }

    private int hash(final long l, final int squareIndex) {
        return ((int) ((((l & masks[squareIndex]) * magics[squareIndex]) >> hashShifts[squareIndex]) & hashMasks[squareIndex]));
    }

    private String generateMagicLongArrayRepresentation() {
        return "final long[] magics = {" +
                Arrays.stream(magics)
                      .mapToObj(Long::toHexString)
                      .map(l -> "0x" + l + "L")
                      .collect(Collectors.joining(", ")) + "};";
    }
}
