package net.marvk.chess.lichess4j;

import lombok.Data;
import lombok.NonNull;
import net.marvk.chess.lichess4j.model.Room;

@Data
public class LichessChatResponse {
    private final @NonNull Room room;
    private final @NonNull String message;
}
