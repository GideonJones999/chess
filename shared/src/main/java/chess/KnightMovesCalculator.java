package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Calculates all legal moves for a King piece.
 */
public class KnightMovesCalculator implements PieceMovesCalculator {
    @Override

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        int[] dx = {2,2,-2,-2,1,1,-1,-1};
        int[] dy = {1,-1,1,-1,2,-2,2,-2};
        ArrayList<ChessMove> moves = new ArrayList<>();
        int currentRow = position.getRow();
        int currentCol = position.getColumn();
        ChessPiece knightEl = board.getPiece(position);
        for (int i=0; i < dx.length; i++) {
                int newRow = currentRow + dx[i];
                int newCol = currentCol + dy[i];
                if (newRow < 1 || newRow > 8 || newCol < 1 || newCol > 8) {continue;}
                ChessPosition movementOption = new ChessPosition(newRow, newCol);
                ChessPiece targetPiece = board.getPiece(movementOption);
                if (targetPiece == null) {
                    moves.add(new ChessMove(position, movementOption, null));
                } else if(targetPiece.getTeamColor() != knightEl.getTeamColor()){
                    moves.add(new ChessMove(position, movementOption, null));
                }
        }

        return moves;
    }
}