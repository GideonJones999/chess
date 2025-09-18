package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Calculates all legal moves for a King piece.
 */
public class KingMovesCalculator implements PieceMovesCalculator {
    @Override

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        int[] dx = {-1, -1, -1, 0, 1, 1, 1, 0};
        int[] dy = {-1, 0, 1, 1, 1, 0, -1, -1};
        ArrayList<ChessMove> moves = new ArrayList<>();
        int currentRow = position.getRow();
        int currentCol = position.getColumn();
        ChessPiece kingEl = board.getPiece(position);
        for (int i = 0; i < dx.length; i++) {
            int newRow = currentRow + dx[i];
            int newCol = currentCol + dy[i];
            if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                ChessPosition movementOption = new ChessPosition(newRow, newCol);
                ChessPiece targetPiece = board.getPiece(movementOption);
                if (targetPiece == null) {
                    moves.add(new ChessMove(position, movementOption, null));
                    System.out.println(movementOption + " is a valid move");
                } else if(targetPiece.getTeamColor() != kingEl.getTeamColor()){
                    // TODO: Add Removing Functionality
                    moves.add(new ChessMove(position, movementOption, null));
                    System.out.println(movementOption + " is a valid capture move");
                } else {
                    System.out.println(movementOption + " is an invalid move");
                }
            }
        }

        return moves;
    }
}