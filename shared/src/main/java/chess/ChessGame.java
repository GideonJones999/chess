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

//    Castling Tracking
    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteKingsideRookMoved = false;
    private boolean whiteQueensideRookMoved = false;
    private boolean blackKingsideRookMoved = false;
    private boolean blackQueensideRookMoved = false;

//    En Passant Tracking
    private ChessMove lastMove = null;

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
        if (piece.getPieceType() ==  ChessPiece.PieceType.KING) {
            int expectedRow = (piece.getTeamColor() == TeamColor.WHITE) ? 1 : 8;
            if (startPosition.getRow() == expectedRow) {
                allMoves.addAll(getCastlingMoves(startPosition, piece.getTeamColor()));
            }
        }
//      Todo: Add en passant moves for Pawns
        if (piece.getPieceType() ==  ChessPiece.PieceType.PAWN) {
            allMoves.addAll(getEnPassantMoves(startPosition, piece.getTeamColor()));
        }

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

        updateCastlingFlags(move, piece);

//        Todo: handle castling
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            // Check if this is a castling move (king moves 2 squares)
            int colDiff = Math.abs(move.getEndPosition().getColumn() - move.getStartPosition().getColumn());
            if (colDiff == 2) {
                executeCastle(move);
                lastMove = move;
                setTeamTurn((getTeamTurn() == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE);
//                System.out.println(board);
                return;
            }
        }
//        Todo: handle en passant
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && isEnPassant(move)) {
            executeEnPassant(move);
            lastMove = move;
            setTeamTurn((getTeamTurn() == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE);
            return;
        }

        board.addPiece(move.getStartPosition(), null);
        board.addPiece(move.getEndPosition(), piece);

        if (move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(),
                    new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
        }
        lastMove = move;
        setTeamTurn((getTeamTurn() == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE);
//        System.out.println(board);
    }

    private void updateCastlingFlags(ChessMove move, ChessPiece piece) {
//        Track King Moves
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            if (piece.getTeamColor() == TeamColor.WHITE) {
                whiteKingMoved = true;
            } else {
                blackKingMoved = true;
            }
        }
//        Track Rook Moves
        if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            if (piece.getTeamColor() == TeamColor.WHITE) {
                if (move.getStartPosition().getRow() == 1 && move.getStartPosition().getColumn() == 8) {
                    whiteKingsideRookMoved = true;
                } else if (move.getStartPosition().getRow() == 1 && move.getStartPosition().getColumn() == 1) {
                    whiteQueensideRookMoved = true;
                }
            } else {
                if (move.getStartPosition().getRow() == 8 && move.getStartPosition().getColumn() == 8) {
                    blackKingsideRookMoved = true;
                } else if (move.getStartPosition().getRow() == 8 && move.getStartPosition().getColumn() == 1) {
                    blackQueensideRookMoved = true;
                }
            }
        }

        // Track if a rook gets captured at its starting position
        ChessPiece capturedPiece = board.getPiece(move.getEndPosition());
        if (capturedPiece != null && capturedPiece.getPieceType() == ChessPiece.PieceType.ROOK) {
            ChessPosition endPos = move.getEndPosition();
            if (capturedPiece.getTeamColor() == TeamColor.WHITE) {
                if (endPos.getRow() == 1 && endPos.getColumn() == 8) {
                    whiteKingsideRookMoved = true;
                } else if (endPos.getRow() == 1 && endPos.getColumn() == 1) {
                    whiteQueensideRookMoved = true;
                }
            } else {
                if (endPos.getRow() == 8 && endPos.getColumn() == 8) {
                    blackKingsideRookMoved = true;
                } else if (endPos.getRow() == 8 && endPos.getColumn() == 1) {
                    blackQueensideRookMoved = true;
                }
            }
        }
    }

    public ChessMove getLastMove() {
        return lastMove;
    }

    public boolean canCastleKingside(TeamColor color){
        if (color == TeamColor.WHITE) {
            return !whiteKingMoved && !whiteKingsideRookMoved;
        }
        return !blackKingMoved && !blackKingsideRookMoved;
    }

    public boolean canCastleQueenside(TeamColor color){
        if (color == TeamColor.WHITE) {
            return !whiteKingMoved && !whiteQueensideRookMoved;
        }
        return !blackKingMoved && !blackQueensideRookMoved;
    }

    public boolean isPathClear (ChessPosition start, ChessPosition end) {
        int startCol = start.getColumn();
        int endCol =  end.getColumn();
        int row = start.getRow();

        int minCol = Math.min(startCol, endCol);
        int maxCol = Math.max(startCol, endCol);

        for (int col = minCol +1; col < maxCol; col++) {
            if (board.getPiece(new ChessPosition(row, col)) != null) {
                return false;
            }
        }
        return true;
    }

    private Collection<ChessMove> getCastlingMoves(ChessPosition kingPos, TeamColor color) {
        Collection<ChessMove> castlingMoves = new ArrayList<>();
        int row = (color == TeamColor.WHITE) ? 1 : 8;

        if(canCastleKingside(color)) {
            ChessPosition rookPos = new ChessPosition(row, 8);
            ChessPosition kingEndPos = new ChessPosition(row, 7);

            if(isPathClear(kingPos, rookPos)) {
                if (!isInCheck(color) && !moveLeavesKingInCheck(new ChessMove(kingPos, kingEndPos, null), color) && !moveLeavesKingInCheck(new ChessMove(kingPos, new ChessPosition(row,6), null), color)) {
                    castlingMoves.add(new ChessMove(kingPos, kingEndPos, null));
                }
            }
        }
        if(canCastleQueenside(color)) {
            ChessPosition rookPos = new ChessPosition(row, 1);
            ChessPosition kingEndPos = new ChessPosition(row, 3);

            if(isPathClear(kingPos, rookPos)) {
                if (!isInCheck(color) && !moveLeavesKingInCheck(new ChessMove(kingPos, kingEndPos, null), color) && !moveLeavesKingInCheck(new ChessMove(kingPos, new ChessPosition(row,4), null), color)) {
                    castlingMoves.add(new ChessMove(kingPos, kingEndPos, null));
                }
            }
        }
        return castlingMoves;
    }

    private void executeCastle(ChessMove move) throws InvalidMoveException {
        ChessPiece king = board.getPiece(move.getStartPosition());
        int row = move.getStartPosition().getRow();
        int kingStartCol = move.getStartPosition().getColumn();
        int kingEndCol = move.getEndPosition().getColumn();

        board.addPiece(move.getStartPosition(), null);
        board.addPiece(move.getEndPosition(), king);

        // Determine which side and move the rook
        ChessPosition rookStart;
        ChessPosition rookEnd;
        if (kingEndCol > kingStartCol) {
            // Kingside castle: rook moves from h-file to f-file
            rookStart = new ChessPosition(row, 8);
            rookEnd = new ChessPosition(row, 6);
            ChessPiece rook = board.getPiece(rookStart);
            board.addPiece(rookEnd, rook);
            board.addPiece(rookStart, null);
        } else {
            // Queenside castle: rook moves from a-file to d-file
            rookStart = new ChessPosition(row, 1);
            rookEnd = new ChessPosition(row, 4);
            ChessPiece rook = board.getPiece(rookStart);
            board.addPiece(rookEnd, rook);
            board.addPiece(rookStart, null);
        }
    }

    private Collection<ChessMove> getEnPassantMoves(ChessPosition pawnPos, TeamColor color) {
        Collection<ChessMove> enPassantMoves = new ArrayList<>();

        if (lastMove == null) {
            return enPassantMoves;
        }

        ChessPiece lastMovedPiece = board.getPiece(lastMove.getEndPosition());

        // Check if last move was a pawn moving 2 squares
        if (lastMovedPiece == null || lastMovedPiece.getPieceType() != ChessPiece.PieceType.PAWN) {
            return enPassantMoves;
        }

        int startRow = lastMove.getStartPosition().getRow();
        int endRow = lastMove.getEndPosition().getRow();

        // Check if it moved 2 squares
        if (Math.abs(endRow - startRow) != 2) {
            return enPassantMoves;
        }

        // Check if the enemy pawn is now beside our pawn
        int pawnRow = pawnPos.getRow();
        int pawnCol = pawnPos.getColumn();
        int enemyPawnCol = lastMove.getEndPosition().getColumn();

        // Must be on same row and adjacent columns
        if (pawnRow == endRow && Math.abs(pawnCol - enemyPawnCol) == 1) {
            // En passant is possible!
            int captureRow = (color == TeamColor.WHITE) ? pawnRow + 1 : pawnRow - 1;
            ChessPosition capturePos = new ChessPosition(captureRow, enemyPawnCol);
            ChessMove enPassantMove = new ChessMove(pawnPos, capturePos, null);

            // Make sure this doesn't leave king in check
            if (!moveLeavesKingInCheckEnPassant(enPassantMove, color)) {
                enPassantMoves.add(enPassantMove);
            }
        }

        return enPassantMoves;
    }

    private boolean moveLeavesKingInCheckEnPassant(ChessMove move, TeamColor color) {
        // For en passant, we need to remove the captured pawn from beside us, not at destination
        ChessPiece movingPawn = board.getPiece(move.getStartPosition());
        ChessPiece capturedPiece = board.getPiece(move.getEndPosition());

        // The captured pawn is beside us, not at the destination
        int capturedPawnRow = move.getStartPosition().getRow();
        int capturedPawnCol = move.getEndPosition().getColumn();
        ChessPosition capturedPawnPos = new ChessPosition(capturedPawnRow, capturedPawnCol);
        ChessPiece capturedPawn = board.getPiece(capturedPawnPos);

        // Simulate the en passant move
        board.addPiece(move.getEndPosition(), movingPawn);
        board.addPiece(move.getStartPosition(), null);
        board.addPiece(capturedPawnPos, null); // Remove the captured pawn

        boolean inCheck = isInCheck(color);

        // Undo the move
        board.addPiece(move.getStartPosition(), movingPawn);
        board.addPiece(move.getEndPosition(), capturedPiece);
        board.addPiece(capturedPawnPos, capturedPawn);

        return inCheck;
    }

    private void executeEnPassant(ChessMove move) {
        ChessPiece pawn = board.getPiece(move.getStartPosition());

        // Move the pawn to the capture square
        board.addPiece(move.getEndPosition(), pawn);
        board.addPiece(move.getStartPosition(), null);

        // Remove the captured pawn (which is beside the starting position, not at destination)
        int capturedPawnRow = move.getStartPosition().getRow();
        int capturedPawnCol = move.getEndPosition().getColumn();
        board.addPiece(new ChessPosition(capturedPawnRow, capturedPawnCol), null);
    }

    private boolean isEnPassant(ChessMove move) {
        ChessPiece piece = board.getPiece(move.getStartPosition());

        // Must be a pawn
        if (piece.getPieceType() != ChessPiece.PieceType.PAWN) {
            return false;
        }

        // Must be moving diagonally
        if (move.getStartPosition().getColumn() == move.getEndPosition().getColumn()) {
            return false;
        }

        // Destination square must be empty (that's what makes it en passant)
        return board.getPiece(move.getEndPosition()) == null;
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
            return false; // uhhh... this shouldn't happen :D
        }
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() != teamColor) {
                    Collection<ChessMove> moves = piece.pieceMoves(board, position);
                    for (ChessMove move : moves) {
                        if(move.getEndPosition().equals(kingPos)) {
                            return true; // the king is currently in check :D
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
