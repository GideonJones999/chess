package ui;
import chess.ChessGame;
import model.*;

import java.util.Locale;
import java.util.Scanner;

public class GameplayUI {
    private final GameData game;
    private final String playerColor;
    private final Scanner scanner;

    public GameplayUI(GameData game, String playerColor) {
        this.game = game;
        this.playerColor = playerColor;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        displayGameBoard();

        boolean inGame = true;
        while(inGame) {
            displayGameMenu();
            String choice = scanner.nextLine().trim().toLowerCase();
            switch (choice) {
                case "1", "r", "redraw" -> displayGameBoard();
                case "2", "h", "help" -> displayHelp();
                case "3", "l", "leave" -> {
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Leaving Game..."+EscapeSequences.RESET_TEXT_COLOR);
                    inGame = false;
                }
                default -> System.out.println("Invalid Choice. Type 'help' for options");
            }
        }
    }

    private void displayGameMenu() {
        String role = playerColor != null ? "Playing as " + playerColor : "Observing";
        System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_WHITE + "♕ " + game.gameName() + " - " + role + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println("1. Redraw Board");
        System.out.println("2. Help");
        System.out.println("3. Leave Game");
        System.out.print("Enter Choice: ");
    }

    private void displayHelp() {
        System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_YELLOW + "=== Gameplay Help ===" + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println("Redraw - Refresh the chess board display");
        System.out.println("Help - Show this help message");
        System.out.println("Leave - Exit the game and return to main menu");
        System.out.println("\nNote: Gameplay features will be added in Phase 6");
    }

    private void displayGameBoard() {
        if(game.game() == null) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Game has not started yet" + EscapeSequences.RESET_TEXT_COLOR);
            return;
        }
        System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_YELLOW + "=== " + game.gameName() + " ===" + EscapeSequences.RESET_TEXT_COLOR);
        boolean whiteOnBottom = playerColor == null || playerColor.equals("WHITE");

        chess.ChessBoard board = game.game().getBoard();
        printBoard(board, whiteOnBottom);
    }

    private void printFileLabel(boolean whiteOnBottom) {
        System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREEN + "    ");
        for (int fileIdx = 1; fileIdx <= 8; fileIdx++) {
            int actualFile = whiteOnBottom ? fileIdx : 9 - fileIdx;
            char fileLetter = (char) ('A' + actualFile - 1);
            System.out.printf(fileLetter+"  ");
            if(fileIdx == 1 || fileIdx == 2 || fileIdx == 4 || fileIdx == 6 || fileIdx == 7) {System.out.print(" ");}
        }
        System.out.println("  "+EscapeSequences.RESET_BG_COLOR);
    }

    private void printBoard(chess.ChessBoard board, boolean whiteOnBottom) {
        int startRank = whiteOnBottom ? 8 : 1;
        int endRank = whiteOnBottom ? 1 : 8;
        int rankStep = whiteOnBottom ? -1 : 1;

        printFileLabel(whiteOnBottom);
        // Print board rows
        for (int rank = startRank; rank != endRank + rankStep; rank += rankStep) {
            System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREEN + " " + rank + " " + EscapeSequences.RESET_BG_COLOR);

            for (int fileIdx = 1; fileIdx <= 8; fileIdx++) {
                int actualFile = whiteOnBottom ? fileIdx : 9 - fileIdx;
                chess.ChessPiece piece = board.getPiece(new chess.ChessPosition(rank, actualFile));
                boolean isLightSquare = (rank + actualFile) % 2 == 0;
                String squareColor = isLightSquare ?
                        EscapeSequences.SET_BG_COLOR_LIGHT_GREY :
                        EscapeSequences.SET_BG_COLOR_DARK_GREY;
                if (piece == null) {
                    System.out.print(squareColor + EscapeSequences.EMPTY + EscapeSequences.RESET_BG_COLOR);
                } else {
                    System.out.print(squareColor + getPieceSymbol(piece) + EscapeSequences.RESET_BG_COLOR);
                }
            }
            System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREEN + " " + rank + " " + EscapeSequences.RESET_BG_COLOR);
            System.out.println();
        }

        printFileLabel(whiteOnBottom);
    }

    private String getPieceSymbol(chess.ChessPiece piece) {
        String symbol = "";
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            symbol = switch (piece.getPieceType()) {
                case KING -> EscapeSequences.WHITE_KING;
                case QUEEN -> EscapeSequences.WHITE_QUEEN;
                case ROOK -> EscapeSequences.WHITE_ROOK;
                case BISHOP -> EscapeSequences.WHITE_BISHOP;
                case KNIGHT -> EscapeSequences.WHITE_KNIGHT;
                case PAWN -> EscapeSequences.WHITE_PAWN;
            };
        } else {
            symbol = EscapeSequences.SET_TEXT_COLOR_BLACK;
            symbol += switch (piece.getPieceType()) {
                case KING -> EscapeSequences.BLACK_KING;
                case QUEEN -> EscapeSequences.BLACK_QUEEN;
                case ROOK -> EscapeSequences.BLACK_ROOK;
                case BISHOP -> EscapeSequences.BLACK_BISHOP;
                case KNIGHT -> EscapeSequences.BLACK_KNIGHT;
                case PAWN -> EscapeSequences.BLACK_PAWN;
            };
            symbol += EscapeSequences.RESET_TEXT_COLOR;
        }
        return symbol;
    }
}
