package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Calculates all legal moves for a King piece.
 */
public class RookMovesCalculator implements PieceMovesCalculator {
    @Override

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        int[] dx = {-1,0,1,0};
        int[] dy = {0,-1,0,1};
        ArrayList<ChessMove> moves = new ArrayList<>();
        int currentRow = position.getRow();
        int currentCol = position.getColumn();
        ChessPiece rookEl = board.getPiece(position);
        for (int dir = 0; dir < dx.length; dir++) {
            for (int dis = 1; dis <= 7; dis++) {
                int newRow = currentRow + dx[dir]*dis;
                int newCol = currentCol + dy[dir]*dis;
                if (newRow < 1 || newRow > 8 || newCol < 1 || newCol > 8) {break;}
                ChessPosition movementOption = new ChessPosition(newRow, newCol);
                ChessPiece targetPiece = board.getPiece(movementOption);
                if (targetPiece == null) {
                    moves.add(new ChessMove(position, movementOption, null));
                } else if(targetPiece.getTeamColor() != rookEl.getTeamColor()){
                    moves.add(new ChessMove(position, movementOption, null));
                    break;
                } else {
                    break;
                }
            }
        }

        return moves;
    }
}