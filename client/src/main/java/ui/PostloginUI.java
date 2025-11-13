package ui;
import chess.ChessGame;
import serverfacade.ServerFacade;
import serverfacade.ServerException;
import java.util.Scanner;
import model.*;

public class PostloginUI {
    private final ServerFacade facade;
    private final String authToken;
    private final String username;
    private final Scanner scanner;

    public PostloginUI(ServerFacade facade, String authToken, String username) {
        this.facade = facade;
        this.authToken = authToken;
        this.username = username;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        boolean running = true;
        while (running) {
            displayMenu();
            String choice = scanner.nextLine().trim().toLowerCase();

            switch (choice) {
                case "1", "l", "list" -> listGames();
                case "2", "c", "create" -> createGame();
                case "3", "j", "join" -> joinGame();
                case "4", "o", "logout" -> {
                    logout();
                    running = false;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void displayMenu() {
        System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_WHITE + "♕ Chess - Logged in as " + username + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println("1. List Games");
        System.out.println("2. Create Game");
        System.out.println("3. Join Game");
        System.out.println("4. Logout");
        System.out.print("Enter choice: ");
    }

    private void createGame() {
        System.out.print("Enter new game name (empty to cancel): ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Cancelled.");
            return;
        }

        try {
            model.CreateGameResult result = facade.createGame(name, authToken);
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Created Game with ID: " + result.gameID() + EscapeSequences.RESET_TEXT_COLOR);
        } catch (ServerException e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Create game failed: " + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    private ListGamesResult fetchGames() throws ServerException {
        return facade.listGames(authToken);
    }

    private void listGames() {
        try {
            ListGamesResult result = fetchGames();
            if (result.games() == null || result.games().isEmpty()) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "No Games Available." + EscapeSequences.RESET_TEXT_COLOR);
            } else {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "\n=== Games ===" + EscapeSequences.RESET_TEXT_COLOR);
                int index = 1;
                for (model.GameData game: result.games()) {
                    String white = game.whiteUsername() != null ? game.whiteUsername() : "OPEN";
                    String black = game.blackUsername() != null ? game.blackUsername() : "OPEN";
                    System.out.printf("%d. %s |  White: %s | Black: %s%n", index, game.gameName(), white, black);
                    index++;
                }
            }
        } catch (ServerException e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "List Games Failed: " + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    private void joinGame() {
        try {
            listGames();

            System.out.print(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Enter Game Number to Join (0 to cancel): " + EscapeSequences.RESET_TEXT_COLOR);
            String input = scanner.nextLine().trim();
            int gameNum;
            try {
                gameNum = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid Input");
                return;
            }

            if (gameNum == 0) {
                System.out.println("Cancelled");
                return;
            }

            ListGamesResult result = fetchGames();
            java.util.List<model.GameData> gameList = new java.util.ArrayList<>(result.games());
            if(gameNum < 1 || gameNum > gameList.size()) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid Game Number");
                return;
            }
            GameData selectedGame = gameList.get(gameNum-1);

            System.out.println("Join as (W)hite or (B)lack or (V)iewer?");
            String colorInput = scanner.nextLine().trim().toUpperCase();
            String playerColor;
            if (colorInput.equals("W")) {
                playerColor = "WHITE";
            } else if (colorInput.equals("B")) {
                playerColor = "BLACK";
            } else if (colorInput.equals("V")) {
                playerColor = null;
            } else {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid color choice" + EscapeSequences.RESET_TEXT_COLOR);
                return;
            }

            if (playerColor != null) {
                JoinGameRequest joinReq = new model.JoinGameRequest(playerColor, selectedGame.gameID());
                facade.joinGame(joinReq, authToken);
                System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN +
                        "You will be playing as " +
                        playerColor +
                        "!" +
                        EscapeSequences.RESET_TEXT_COLOR);
            }
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Joined game!" + EscapeSequences.RESET_TEXT_COLOR);
            displayGameBoard(selectedGame, playerColor);

        } catch (ServerException e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Join Game Failed: " + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    private void logout() {
        try {
            facade.logout(authToken);
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Logged out successfully!" + EscapeSequences.RESET_TEXT_COLOR);
        } catch (ServerException e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Logout failed: " + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    private void displayGameBoard(model.GameData game, String playerColor) {
        if (game.game() == null) {
            System.out.println("That Game Hasn't Started Yet");
            return;
        }

        System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_YELLOW + "=== Game Board ===" + EscapeSequences.RESET_TEXT_COLOR);
        boolean whiteOnBottom = playerColor == null || playerColor.equals("WHITE");

        chess.ChessBoard board = game.game().getBoard();
        printBoard(board, whiteOnBottom);
    }

    private void printBoard(chess.ChessBoard board, boolean whiteOnBottom) {
        int startRank = whiteOnBottom ? 8 : 1;
        int endRank = whiteOnBottom ? 1 : 8;
        int rankStep = whiteOnBottom ? -1 : 1;

        System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREEN + "    ");
        for (int fileIdx = 1; fileIdx <= 8; fileIdx++) {
            int actualFile = whiteOnBottom ? fileIdx : 9 - fileIdx;
            char fileLetter = (char) ('A' + actualFile - 1);
            System.out.printf(fileLetter+"  ");
            if(fileIdx == 1 || fileIdx == 2 || fileIdx == 4 || fileIdx == 6 || fileIdx == 7) {System.out.print(" ");}
        }
        System.out.println("  "+EscapeSequences.RESET_BG_COLOR);

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

        System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREEN + "    ");
        for (int fileIdx = 1; fileIdx <= 8; fileIdx++) {
            int actualFile = whiteOnBottom ? fileIdx : 9 - fileIdx;
            char fileLetter = (char) ('A' + actualFile - 1);
            System.out.printf(fileLetter+"  ");
            if(fileIdx == 1 || fileIdx == 2 || fileIdx == 4 || fileIdx == 6 || fileIdx == 7) {System.out.print(" ");}
        }
        System.out.println("  "+EscapeSequences.RESET_BG_COLOR);
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
