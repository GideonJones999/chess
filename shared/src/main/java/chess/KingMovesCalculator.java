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
                if (isAdjacentToEnemyKing(board, movementOption,kingEl.getTeamColor())) {
                    continue;
                }
                if (targetPiece == null) {
                    moves.add(new ChessMove(position, movementOption, null));
                } else if(targetPiece.getTeamColor() != kingEl.getTeamColor()){
                    moves.add(new ChessMove(position, movementOption, null));
                }
            }
        }

        return moves;
    }
    private boolean isAdjacentToEnemyKing(ChessBoard board, ChessPosition pos, ChessGame.TeamColor myColor) {
        int[] dx = {-1, -1, -1, 0, 1, 1, 1, 0};
        int[] dy = {-1, 0, 1, 1, 1, 0, -1, -1};

        for (int i = 0; i < dx.length; i++) {
            int adjRow = pos.getRow() + dx[i];
            int adjCol = pos.getColumn() + dy[i];
            if (adjRow >= 1 && adjRow <= 8 && adjCol >= 1 && adjCol <= 8) {
                ChessPosition adjPos = new ChessPosition(adjRow, adjCol);
                ChessPiece piece = board.getPiece(adjPos);
                if (piece != null &&
                        piece.getPieceType() == ChessPiece.PieceType.KING &&
                        piece.getTeamColor() != myColor) {
                    return true;
                }
            }
        }
        return false;
    }
}