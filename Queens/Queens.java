

// From Cracking the Coding Interview (Question 9.9)
// Write an algorithm to print all ways of arranging eight queens on an 8x8
// chess board so that none of them share the same row, column or diagonal. In
// this case, "diagonal" means all diagonals, not just the two that bisect the
// board.

import java.util.*;
import java.io.*;

enum DrawMode { TEXT, HTML };
class BoardMask implements Cloneable {
    private final static String CELL_SIZE = "1cm";
    private final static String BOARD_MARGIN = "1cm";
    private final static String QUEEN_COLOR = "black";
    private final static String NON_QUEEN_COLOR = "white";
    long mask = 0;
    BoardMask() {}
    BoardMask(long mask) {
        this.mask = mask;
    }
    public BoardMask clone() {
        try {
            return (BoardMask)super.clone();
        } catch(Exception e) {
            throw new RuntimeException("Internal error!");
        }
    }
    Long getLong() {
        return mask;
    }
    boolean isAllOnes() {
        return mask == ~0L;
    }
    boolean get(int row, int col) {
        return (mask & getMask(row, col)) != 0L;
    }
    BoardMask set(int row, int col) {
        mask |= getMask(row, col);
        return this;
    }
    BoardMask clr(int row, int col) {
        mask &= ~getMask(row, col);
        return this;
    }
    BoardMask setRow(int row) {
        // Block entire row.
        mask |= 0x00ffL << (row << 3);
        return this;
    }
    BoardMask setCol(int col) {
        long colMask = 1L << col;
        // Block entire col.
        for (int row = 0; row < 8; row++, colMask <<= 8)
            mask |= colMask;
        return this;
    }
    BoardMask setDiagonals(int row, int col) {
        // Block diagonals.
        int[][] ds = {{1,1},{1,-1},{-1,-1},{-1,1}};
        for (int i = 0; i < ds.length; i++) {
            int dr = ds[i][0];
            int dc = ds[i][1];
            for (int r = row + dr, c = col + dc; r >= 0 && r < 8 && c >= 0 && c < 8; r += dr, c += dc)
                mask |= getMask(r, c);
        }
        return this;
    }
    void draw(StringBuilder sb, DrawMode mode) {
        switch (mode) {
            case TEXT:
                drawText(sb);
                break;
            case HTML:
                drawHtml(sb);
                break;
        }
    }
    void drawText(StringBuilder sb) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                sb.append(get(row, col) ? '*' : '-');
            }
            sb.append("\n");
        }
        sb.append("\n");
    }
    void drawHtml(StringBuilder sb) {
        sb.append(String.format("<table style=\"margin:%s;\">\n", BOARD_MARGIN));
        for (int row = 0; row < 8; row++) {
            sb.append("<tr>\n");
            for (int col = 0; col < 8; col++) {
                sb.append(String.format("<td style=\"width:%s; height:%s; border:solid; background-color:%s;\" />\n",
                        CELL_SIZE, CELL_SIZE, get(row, col) ? QUEEN_COLOR : NON_QUEEN_COLOR));
            }
            sb.append("</tr>\n");
        }
        sb.append("</table>\n");
    }
    private long getMask(int row, int col) {
        return (1L << col) << (8 * row);
    }
}
class Board {
    private static int cnt = 0;
    BoardMask queens = new BoardMask();
    BoardMask blocked = new BoardMask();
    Board() {}
    Board(Board ob) {
        this.queens = ob.queens.clone();
        this.blocked = ob.blocked.clone();
    }
    Board(Board ob, int row, int col) {
        this(ob);
        queens.set(row, col);
        blockSquares(row, col);
    }
    BoardMask getQueenMask() {
        return queens;
    }
    Long getQueenMaskAsLong() {
        return queens.getLong();
    }
    boolean isBlocked() {
        return blocked.isAllOnes();
    }
    boolean isBlocked(int row, int col) {
        return blocked.get(row, col);
    }
    private void blockSquares(int row, int col) {
        blocked.setRow(row);
        blocked.setCol(col);
        blocked.setDiagonals(row, col);
    }
    void drawText(StringBuilder sb) {
        queens.drawText(sb);
    }
    void drawHtml(StringBuilder sb) {
        queens.drawHtml(sb);
    }
}

public class Queens {
    // Support form of memoization.
    Set<Long> triedBoards = new HashSet<Long>();
    // Create list that will be filled with all possible boards.
    List<Board> boards = new ArrayList<Board>();
    void recurse(int numQueens, Board board) {
        if (numQueens == 8) {
            // Leaf
            boards.add(board);
        } else if (board.isBlocked()) {
            // This board is completely blocked.
            return;
        }
        // If here, possible to add another queen, but don't recurse if we've
        // tried this combination before...
        if (triedBoards.contains(board.getQueenMaskAsLong())) return;
        //int numQueens = 0; // If this is kept, won't need input arg...
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (!board.isBlocked(row, col)) {
                    recurse(numQueens + 1, new Board(board, row, col));
                }
            }
        }
        triedBoards.add(board.getQueenMaskAsLong());
    }
    void drawBoardsText(StringBuilder sb) {
        int i = 0;
        for (Board board : boards) {
            sb.append(String.format("Board #%d\n", ++i));
            board.drawText(sb);
            sb.append("\n");
        }
    }
    void drawBoardsHtml(StringBuilder sb) {
        int i = 0;
        sb.append("<html>\n<body>\n");
        for (Board board : boards) {
            sb.append(String.format("<h3>Board #%d</h3>\n", ++i));
            board.drawHtml(sb);
        }
        sb.append("</body>\n</html>\n");
    }
    // Usage:
    // java Queens [<HTML|TEXT>]
    // Note: Format defaults to HTML.
    // Output: Visual representation of all possible boards, in either HTML or TEXT format.
    public static void main(String[] args) {
        DrawMode drawMode = args.length == 0 || args[0].equals("HTML") ? DrawMode.HTML : DrawMode.TEXT;
        Queens ob = new Queens();
        // Recurse to find all combinations.
        ob.recurse(0, new Board());

        // Display output.
        StringBuilder sb = new StringBuilder();
        if (drawMode == DrawMode.TEXT)
            ob.drawBoardsText(sb);
        else
            ob.drawBoardsHtml(sb);
        System.out.println(sb);
    }

}
// vim:ts=4:sw=4:et:tw=78
