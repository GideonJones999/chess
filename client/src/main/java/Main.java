import chess.*;
import server.Server;
import serverfacade.ServerFacade;
import ui.*;

public class Main {
    public static void main(String[] args) {
//        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
//        System.out.println("♕ 240 Chess Client: " + piece);
        ServerFacade facade = new ServerFacade(8080);

        PreloginUI ui = new PreloginUI(facade);
        ui.run();
    }
}