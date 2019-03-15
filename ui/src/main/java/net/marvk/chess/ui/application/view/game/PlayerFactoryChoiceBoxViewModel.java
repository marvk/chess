package net.marvk.chess.ui.application.view.game;

import lombok.Data;
import net.marvk.chess.core.board.PlayerFactory;

@Data
public class PlayerFactoryChoiceBoxViewModel {
    private final PlayerFactory playerFactory;
    private final String name;
}
