package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Calculates all legal moves for a King piece.
 */
public class QueenMovesCalculator implements PieceMovesCalculator {
    @Override

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        int[] dx = {-1,-1,1,1,1,-1,0,0};
        int[] dy = {-1,1,-1,1,0,0,1,-1};
        ArrayList<ChessMove> moves = new ArrayList<>();
        int currentRow = position.getRow();
        int currentCol = position.getColumn();
        ChessPiece queenEl = board.getPiece(position);
        for (int dir = 0; dir < dx.length; dir++) {
            for (int dis = 1; dis <= 7; dis++) {
                int newRow = currentRow + dx[dir]*dis;
                int newCol = currentCol + dy[dir]*dis;
                if (newRow < 1 || newRow > 8 || newCol < 1 || newCol > 8) {break;}
                ChessPosition movementOption = new ChessPosition(newRow, newCol);
                ChessPiece targetPiece = board.getPiece(movementOption);
                if (targetPiece == null) {
                    moves.add(new ChessMove(position, movementOption, null));
                    System.out.println(movementOption + " is a valid move");
                } else if(targetPiece.getTeamColor() != queenEl.getTeamColor()){
                    // TODO: Add Removing Functionality
                    moves.add(new ChessMove(position, movementOption, null));
                    System.out.println(movementOption + " is a valid capture move");
                    break;
                } else {
                    System.out.println(movementOption + " is an invalid move");
                    break;
                }
            }
        }

        return moves;
    }
}