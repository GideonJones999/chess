package ui;
import serverfacade.ServerFacade;
import serverfacade.ServerException;
import model.*;

import java.util.Locale;
import java.util.Scanner;

public class PreloginUI {
    private ServerFacade facade;
    private Scanner scanner;

    public PreloginUI(ServerFacade facade) {
        this.facade = facade;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        boolean running = true;
        while(running) {
            try {
                displayMenu();
                String choice = scanner.nextLine().trim().toLowerCase();

                switch (choice) {
                    case "1", "r", "register" -> registerUser();
                    case "2", "l", "login" -> loginUser();
                    case "3", "h", "help" -> displayHelp();
                    case "4", "q", "quit" -> {
                        running = false;
                        System.out.println("Goodbye!");
                    }
                    default -> System.out.println("Invalid Choice, try again");
                }
            } catch (Exception e) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "An error occurred: " + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
                System.out.println("Returning to Menu...");
            }
        }
    }

    private void displayMenu() {
        System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_WHITE + "♕ Chess Client" + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Help");
        System.out.println("4. Quit");
        System.out.print("Enter choice: ");
    }

    private void registerUser() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        try {
            RegisterResult result = facade.register(new RegisterRequest(username, password, email));
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Registration successful!" + EscapeSequences.RESET_TEXT_COLOR);
            System.out.println("Logging in...");
            loginWithCredentials(result.username(), password);
        } catch (ServerException e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Registration failed: " + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
            return;
        }
        loginUser();
    }

    private void loginUser() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        loginWithCredentials(username, password);
    }

    private void loginWithCredentials(String username, String password) {
        try {
            LoginResult result = facade.login(new LoginRequest(username, password));
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Login successful!" + EscapeSequences.RESET_TEXT_COLOR);
            new PostloginUI(facade, result.authToken(), result.username()).run();
            return;
        } catch (ServerException e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Login failed: " + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    private void displayHelp() {
        System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_YELLOW + "Help" + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println("Register: Create a new account");
        System.out.println("Login: Sign in to your account");
        System.out.println("Quit: Exit the application");
    }

}
