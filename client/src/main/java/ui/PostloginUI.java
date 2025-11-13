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
                case "4", "o", "observe" -> observeGame();
                case "5", "t", "logout" -> {
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
        System.out.println("4. Observe Game");
        System.out.println("5. Logout");
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

    private GameData selectGame() throws ServerException {
        listGames();

        System.out.print(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Enter Game Number to Join (0 to cancel): " + EscapeSequences.RESET_TEXT_COLOR);
        String input = scanner.nextLine().trim();
        int gameNum;
        try {
            gameNum = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid Input");
            return null;
        }

        if (gameNum == 0) {
            System.out.println("Cancelled");
            return null;
        }

        ListGamesResult result = fetchGames();
        java.util.List<model.GameData> gameList = new java.util.ArrayList<>(result.games());
        if(gameNum < 1 || gameNum > gameList.size()) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid Game Number");
            return null;
        }
        return gameList.get(gameNum-1);
    }

    private void joinGame() {
        try {
            GameData selectedGame = selectGame();
            if (selectedGame == null) {return;}
            System.out.println("Join as (W)hite or (B)lack?");
            String colorInput = scanner.nextLine().trim().toUpperCase();
            String playerColor;
            if (colorInput.equals("W")) {
                playerColor = "WHITE";
            } else if (colorInput.equals("B")) {
                playerColor = "BLACK";
            } else {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Invalid color choice" + EscapeSequences.RESET_TEXT_COLOR);
                return;
            }

            JoinGameRequest joinReq = new model.JoinGameRequest(playerColor, selectedGame.gameID());
            facade.joinGame(joinReq, authToken);
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "You will be playing as " +
                    playerColor +
                    "!" +
                    EscapeSequences.RESET_TEXT_COLOR);
            new GameplayUI(selectedGame, playerColor).run();
        } catch (ServerException e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Join Game Failed: " + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    private void observeGame() {
        try {
            GameData selectedGame = selectGame();
            if (selectedGame == null) {return;}
            new GameplayUI(selectedGame, null).run(); // null = viewer (white's perspective)
        } catch (ServerException e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "View Game Failed: " + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
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
}
