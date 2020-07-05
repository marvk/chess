# Chess

This is a multi part repository, consisting of the Kairuku Chess Engine, a [Universal Chess Interface (UCI)](https://en.wikipedia.org/wiki/Universal_Chess_Interface) implementation, a [Lichess](https://lichess.org/) API and [QueensGamBOT](https://lichess.org/@/QueensGamBOT), a [Lichess](https://lichess.org/) bot utilizing the other modules in this repository.

## Kairuku Chess Engine

![kairuku logo](https://i.imgur.com/Cn7dzhf.png)

Kairuku is a Chess Engine build around a fast psuedo legal move generator utilizes Magic Bitboards. It makes use of negamax search with piece square tables, transposition tables, _Most Valuable Victim - Least Valuable Aggressor_ (MVV-LVA), quiescence search, zobrist hashing and more techniques from chess programming.

###### Core classes

* [net.marvk.chess.core.bitboards.Bitboard](https://github.com/marvk/chess/blob/master/core/src/main/java/net/marvk/chess/core/bitboards/Bitboard.java)
* [net.marvk.chess.core.bitboards.MagicBitboard](https://github.com/marvk/chess/blob/master/core/src/main/java/net/marvk/chess/core/bitboards/MagicBitboard.java)
* [net.marvk.chess.kairukuengine.KairukuEngine](https://github.com/marvk/chess/blob/master/kairuku-engine/src/main/java/net/marvk/chess/kairukuengine/KairukuEngine.java)

## UCI4J


###### Core classes

* [net.marvk.chess.uci4j.UiChannel](https://github.com/marvk/chess/blob/master/uci4j/src/main/java/net/marvk/chess/uci4j/UiChannel.java)  
* [net.marvk.chess.uci4j.ConsoleUiChannel](https://github.com/marvk/chess/blob/master/uci4j/src/main/java/net/marvk/chess/uci4j/ConsoleUiChannel.java)
* [net.marvk.chess.uci4j.UciEngine](https://github.com/marvk/chess/blob/master/uci4j/src/main/java/net/marvk/chess/uci4j/UciEngine.java)
* [net.marvk.chess.uci4j.ConsoleEngineChannel](https://github.com/marvk/chess/blob/master/uci4j/src/main/java/net/marvk/chess/uci4j/ConsoleEngineChannel.java)



## Lichess4J

###### Core classes

* [net.marvk.chess.lichess4j.LichessClient](https://github.com/marvk/chess/blob/master/lichess4j/src/main/java/net/marvk/chess/lichess4j/LichessClient.java)
* [net.marvk.chess.lichess4j.LichessClientBuilder](https://github.com/marvk/chess/blob/master/lichess4j/src/main/java/net/marvk/chess/lichess4j/LichessClientBuilder.java)

## QueensGamBOT

[QueensGamBOT](https://lichess.org/@/QueensGamBOT) is a bot with more than a thousand games played on lichess, with more than 70 followers. Currently, it plays Bullet exclusively.

The sole class is [net.marvk.chess.queensgambot.QueensGamBotApp](https://github.com/marvk/chess/blob/master/queensgambot/src/main/java/net/marvk/chess/queensgambot/QueensGamBotApp.java), which is a good example of the previous modules in use together

