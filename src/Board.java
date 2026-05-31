/**
 * 數獨盤面模型：保存 9x9 的格子狀態，並負責文字介面的盤面渲染。
 * 示範「物件封裝」：內部陣列為 private，外界只能透過方法存取與修改。
 */
public class Board {

    // 盤面邊長（標準數獨為 9）
    public static final int SIZE = 9;
    // 子方格（宮）邊長（3x3）
    public static final int BOX = 3;
    // 代表空格的內部值
    public static final int EMPTY = 0;

    // 9x9 格子內容，0 表示空格；封裝為 private，外界不可直接更動
    private final int[][] grid;
    // 標記哪些格子是題目給定值（不可被玩家修改）
    private final boolean[][] given;

    /**
     * 以一段 81 字元的字串建立盤面。
     * @param puzzle 長度 81 的字串，字元 1-9 為給定值，'.' 或 '0' 視為空格
     */
    public Board(String puzzle) {
        this.grid = new int[SIZE][SIZE];
        this.given = new boolean[SIZE][SIZE];
        loadFromString(puzzle);
    }

    /**
     * 複製建構子：以另一個盤面為樣本建立獨立副本。
     * 求解器在副本上運算，不會影響玩家正在解的盤面。
     */
    public Board(Board other) {
        this.grid = new int[SIZE][SIZE];
        this.given = new boolean[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            System.arraycopy(other.grid[r], 0, this.grid[r], 0, SIZE);
            System.arraycopy(other.given[r], 0, this.given[r], 0, SIZE);
        }
    }

    /** 解析 81 字元字串並填入盤面，示範「字串處理」。 */
    private void loadFromString(String puzzle) {
        for (int i = 0; i < SIZE * SIZE; i++) {
            char c = puzzle.charAt(i);
            int r = i / SIZE;             // 由一維索引算出列
            int col = i % SIZE;           // 由一維索引算出行
            int v = (c >= '1' && c <= '9') ? c - '0' : EMPTY;
            grid[r][col] = v;
            given[r][col] = (v != EMPTY);
        }
    }

    /** 取得 (r,c) 的值。 */
    public int get(int r, int c) {
        return grid[r][c];
    }

    /** 設定 (r,c) 的值（傳入 EMPTY 即為清除）。不檢查合法性，由呼叫端決定。 */
    public void set(int r, int c, int v) {
        grid[r][c] = v;
    }

    /** (r,c) 是否為題目給定值。 */
    public boolean isGiven(int r, int c) {
        return given[r][c];
    }

    /** (r,c) 是否為空格。 */
    public boolean isEmpty(int r, int c) {
        return grid[r][c] == EMPTY;
    }

    /**
     * 檢查在 (r,c) 放入 v 是否符合數獨規則：同列、同行、同宮不得重複。
     * 比對時會略過 (r,c) 本身，因此也可用來驗證已填入的格子是否合法。
     */
    public boolean isLegal(int r, int c, int v) {
        for (int i = 0; i < SIZE; i++) {
            if (i != c && grid[r][i] == v) return false;   // 同列
            if (i != r && grid[i][c] == v) return false;   // 同行
        }
        int br = (r / BOX) * BOX;
        int bc = (c / BOX) * BOX;
        for (int i = br; i < br + BOX; i++) {
            for (int j = bc; j < bc + BOX; j++) {
                if ((i != r || j != c) && grid[i][j] == v) return false;   // 同宮
            }
        }
        return true;
    }

    /** 找出第一個空格，回傳 {r,c}；沒有空格回傳 null。供回溯求解使用。 */
    public int[] firstEmpty() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c] == EMPTY) return new int[]{r, c};
            }
        }
        return null;
    }

    /** 剩餘空格數量。 */
    public int emptyCount() {
        int n = 0;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c] == EMPTY) n++;
            }
        }
        return n;
    }

    /** 將目前盤面序列化為 81 字元字串（空格輸出 '0'），供出題器產生題面字串。 */
    public String toLine() {
        StringBuilder sb = new StringBuilder(SIZE * SIZE);
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                sb.append((char) ('0' + grid[r][c]));
            }
        }
        return sb.toString();
    }

    /** 盤面是否完成：沒有空格且每一格都合法。 */
    public boolean isSolved() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                int v = grid[r][c];
                if (v == EMPTY || !isLegal(r, c, v)) return false;
            }
        }
        return true;
    }

    /** 將盤面渲染成帶座標與 3x3 框線的多行字串，供終端機顯示。 */
    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("       1 2 3   4 5 6   7 8 9\n");
        sb.append("     ┌───────┬───────┬───────┐\n");
        for (int r = 0; r < SIZE; r++) {
            sb.append("  ").append(r + 1).append("  │ ");
            for (int c = 0; c < SIZE; c++) {
                int v = grid[r][c];
                sb.append(v == EMPTY ? "." : Integer.toString(v));
                sb.append(' ');
                if (c % BOX == BOX - 1) {
                    sb.append("│ ");
                }
            }
            sb.append('\n');
            if (r % BOX == BOX - 1 && r != SIZE - 1) {
                sb.append("     ├───────┼───────┼───────┤\n");
            }
        }
        sb.append("     └───────┴───────┴───────┘\n");
        return sb.toString();
    }
}
