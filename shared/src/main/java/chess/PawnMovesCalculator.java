package chess;

import java.util.ArrayList;
import java.util.Collection;

import static chess.ChessGame.TeamColor.WHITE;

/**
 * Calculates all legal moves for a King piece.
 */
public class PawnMovesCalculator implements PieceMovesCalculator {
    @Override

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        ChessPiece pawnEl = board.getPiece(position);
        ChessGame.TeamColor pawnElTeamColor = pawnEl.getTeamColor();
        int direction = (pawnElTeamColor == WHITE) ? 1 : -1; // or -1/1 depending on your board
        int startRow = (pawnElTeamColor == WHITE) ? 2 : 7;
        int promotionRow = (pawnElTeamColor == WHITE) ? 8 : 1;
        ArrayList<ChessMove> moves = new ArrayList<>();
        int currentRow = position.getRow();
        int currentCol = position.getColumn();
        ChessPosition normalForwardPosition = new ChessPosition(currentRow+direction, currentCol);
        if (board.getPiece(normalForwardPosition) == null) {
            moves.add(new ChessMove(position, normalForwardPosition, null));
//            System.out.println(normalForwardPosition + " is a valid move");
            if (currentRow == startRow) {
                ChessPosition startAdvancePosition = new ChessPosition(currentRow + 2 * direction, currentCol);
                if (board.getPiece(startAdvancePosition) == null) {
                    moves.add(new ChessMove(position, startAdvancePosition, null));
//                    System.out.println(startAdvancePosition + " is a valid move, on starting row");
                }
            }
        }

        for (int attackRange : new int[]{-1, 1}) {
            int attackRow = currentRow + direction;
            int attackCol = currentCol + attackRange;
            if (attackRow >= 1 && attackRow <= 8 && attackCol >= 1 && attackCol <= 8) {
                ChessPosition attackDiagonal = new ChessPosition(attackRow, attackCol);
                ChessPiece target = board.getPiece(attackDiagonal);
                if (target != null && target.getTeamColor() != pawnElTeamColor) {
                    moves.add(new ChessMove(position, attackDiagonal, null));
//                    System.out.println(attackDiagonal + " is a valid capture move");
                }
            }
        }

        ArrayList<ChessMove> promotionMoves = new ArrayList<>();
        for (ChessMove move : moves) {
            int endRow = move.getEndPosition().getRow();
            if (endRow == promotionRow) {
                promotionMoves.add(new ChessMove(move.getStartPosition(), move.getEndPosition(), ChessPiece.PieceType.QUEEN));
                promotionMoves.add(new ChessMove(move.getStartPosition(), move.getEndPosition(), ChessPiece.PieceType.ROOK));
                promotionMoves.add(new ChessMove(move.getStartPosition(), move.getEndPosition(), ChessPiece.PieceType.BISHOP));
                promotionMoves.add(new ChessMove(move.getStartPosition(), move.getEndPosition(), ChessPiece.PieceType.KNIGHT));
            }
        }

        moves.removeIf(move -> move.getEndPosition().getRow() == promotionRow ||
                move.getEndPosition().getRow() < 1 ||
                move.getEndPosition().getRow() > 8 ||
                move.getEndPosition().getColumn() < 1 ||
                move.getEndPosition().getColumn() > 8);

        moves.addAll(promotionMoves);
        return moves;
    }
}