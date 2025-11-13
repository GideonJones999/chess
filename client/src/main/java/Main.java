import serverfacade.ServerFacade;
import ui.*;

public class Main {
    public static void main(String[] args) {
        ServerFacade facade = new ServerFacade(8080);

        PreloginUI ui = new PreloginUI(facade);
        ui.run();
    }
}