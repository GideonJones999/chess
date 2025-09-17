package chess;

import java.util.Collection;

/**
 * Interface for calculating available moves for a chess piece.
 */
public interface PieceMovesCalculator {
    /**
     * Calculates all legal moves for a piece at the given position on the board.
     * @param board the chess board
     * @param position the position of the piece
     * @return a collection of legal moves
     */
    Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position);
}