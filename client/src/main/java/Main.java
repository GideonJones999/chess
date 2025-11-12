import chess.*;
import server.Server;
import serverfacade.ServerFacade;
import ui.*;

public class Main {
    public static void main(String[] args) {
//        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
//        System.out.println("♕ 240 Chess Client: " + piece);
        Server server = new Server();
        int port = server.run(8080);
        System.out.println("Server Started on port "+ port);
        ServerFacade facade = new ServerFacade(port);

        PreloginUI ui = new PreloginUI(facade);
        ui.run();

        server.stop();


    }
}