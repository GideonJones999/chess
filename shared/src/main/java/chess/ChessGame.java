package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor currentTurn;

    public ChessGame() {
        this.board = new ChessBoard();
        board.resetBoard();
        this.currentTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {return null;}
        Collection<ChessMove> allMoves = new ArrayList<>();

        Collection <ChessMove> basicMoves = piece.pieceMoves(board, startPosition);

        for (ChessMove move : basicMoves) {
            if (!moveLeavesKingInCheck(move, piece.getTeamColor())) {
                allMoves.add(move);
            }
        }

//      Todo: Add Castling Moves for Kings
//      Todo: Add en passant moves for Pawns

        return allMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        System.out.println(piece + " moving from " + move.getStartPosition() + " to " + move.getEndPosition());

        if (piece == null) {
            throw new InvalidMoveException("No Piece at Start");
        }
        if (piece.getTeamColor() != getTeamTurn()) {
            throw new InvalidMoveException("Not your turn");
        }

        Collection<ChessMove> legalMoves = validMoves(move.getStartPosition());
        if (legalMoves == null || !legalMoves.contains(move)) {
            throw new  InvalidMoveException("Invalid Move");
        }
//        Todo: handle castling
//        Todo: handle en passant

        System.out.println("Legal Moves contains " + move.getEndPosition() + ": " + legalMoves.contains(move));

        board.addPiece(move.getStartPosition(), null);
        board.addPiece(move.getEndPosition(), piece);

        if (move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
        }
        setTeamTurn((getTeamTurn() == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE);
        System.out.println(board);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPos = findKing(teamColor);
        if (kingPos == null) {
            return false;
//            uhhh... this shouldn't happen :D
        }
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() != teamColor) {
                    Collection<ChessMove> moves = piece.pieceMoves(board, position);
                    for (ChessMove move : moves) {
                        if(move.getEndPosition().equals(kingPos)) {
                            return true;
//                            the king is currently in check :D
                        }
                    }
                }
            }
        }
        return false;      }

//    check if that move would leave the king in check
    private boolean moveLeavesKingInCheck (ChessMove move, TeamColor teamColor) {
        ChessPiece movingPiece = board.getPiece(move.getStartPosition());
        ChessPiece capturedPiece = board.getPiece(move.getEndPosition());

//        temp adding of pieces to see if would be in check
        board.addPiece(move.getEndPosition(), movingPiece);
        board.addPiece(move.getStartPosition(), null);

        boolean inCheck = isInCheck(teamColor);
//        removing temp pieces
        board.addPiece(move.getStartPosition(), movingPiece);
        board.addPiece(move.getEndPosition(), capturedPiece);

        return inCheck;
    }

//    check if the king has no valid moves
    private boolean hasNoValidMoves(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(position);
                    if (moves != null && !moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private ChessPosition findKing(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return position;
                }
            }
        }
        return null;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        return hasNoValidMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        return hasNoValidMoves(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChessGame chessGame)) {
            return false;
        }
        return Objects.equals(board, chessGame.board) && currentTurn == chessGame.currentTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentTurn);
    }
}
