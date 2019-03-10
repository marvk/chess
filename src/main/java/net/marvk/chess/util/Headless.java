package net.marvk.chess.util;

import net.marvk.chess.board.AlphaBetaPlayer;
import net.marvk.chess.board.Game;
import net.marvk.chess.board.PlayerFactory;
import net.marvk.chess.board.SimpleHeuristic;

import java.util.Scanner;

public class Headless {
    public static void main(String[] args) {
        final PlayerFactory factory = color -> new AlphaBetaPlayer(color, new SimpleHeuristic(), 5);

        final Game game = new Game(factory, factory);

        new Scanner(System.in).nextLine();

        while (!game.isGameOver()) {
            game.nextMove();
        }
    }
}
