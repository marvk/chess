package net.marvk.chess.application.view.game;

import lombok.Data;
import net.marvk.chess.board.PlayerFactory;

@Data
public class PlayerFactoryChoiceBoxViewModel {
    private final PlayerFactory playerFactory;
    private final String name;
}
