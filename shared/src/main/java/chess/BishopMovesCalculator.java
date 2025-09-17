package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Calculates all legal moves for a King piece.
 */
public class BishopMovesCalculator implements PieceMovesCalculator {
    @Override

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        int[] dx = {};
        int[] dy = {};
        ArrayList<ChessMove> moves = new ArrayList<>();
        int currentRow = position.getRow();
        int currentCol = position.getColumn();
        ChessPiece bishopEl = board.getPiece(position);
        for (int i = 0; i < dx.length; i++) {
            int newRow = currentRow + dx[i];
            int newCol = currentCol + dy[i];
            if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                ChessPosition movementOption = new ChessPosition(newRow, newCol);
                ChessPiece targetPiece = board.getPiece(movementOption);
                if (targetPiece == null) {
                    moves.add(new ChessMove(position, movementOption, null));
                    System.out.println(movementOption + " is a valid move");
                } else if(targetPiece.getTeamColor() != bishopEl.getTeamColor()){
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