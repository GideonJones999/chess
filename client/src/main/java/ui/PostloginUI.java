package ui;
import serverfacade.ServerFacade;
import serverfacade.ServerException;
import java.util.Scanner;

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

    private void listGames() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "List Games (TODO)" + EscapeSequences.RESET_TEXT_COLOR);
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

    private void joinGame() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Join Game (TODO)" + EscapeSequences.RESET_TEXT_COLOR);
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
