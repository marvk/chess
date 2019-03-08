package net.marvk.chess.util;

import net.marvk.chess.board.Game;
import net.marvk.chess.board.SimpleCpu;

import java.util.Scanner;

public class Headless {
    public static void main(String[] args) {
        final Game game = new Game(SimpleCpu::new, SimpleCpu::new);

        new Scanner(System.in).nextLine();

        while (!game.isGameOver()) {
            game.nextMove();
        }
    }
}
