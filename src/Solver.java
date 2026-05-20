/**
 * 數獨求解器：以回溯（backtracking）遞迴求解。
 * 這是本專案「遞迴」與「搜索」的核心——在解答空間中嘗試、失敗就退回。
 */
public class Solver {

    /**
     * 就地解出盤面（會填滿傳入的 board）。
     * @return 是否找到解
     */
    public boolean solve(Board board) {
        int[] cell = board.firstEmpty();
        if (cell == null) {
            return true;                       // 沒有空格 → 已完成（遞迴終止條件）
        }
        int r = cell[0];
        int c = cell[1];
        for (int v = 1; v <= Board.SIZE; v++) {     // 依序嘗試 1..9（搜索）
            if (board.isLegal(r, c, v)) {
                board.set(r, c, v);            // 假設這一格填 v
                if (solve(board)) {
                    return true;               // 遞迴往下解，成功就一路回傳
                }
                board.set(r, c, Board.EMPTY);  // 回溯：撤銷剛才的假設
            }
        }
        return false;                          // 1..9 都不行 → 回到上一層重試
    }

    /**
     * 計算盤面的解數量，最多數到 limit 就提前停止。
     * 可用 countSolutions(board, 2) 判斷題目是否「唯一解」。
     */
    public int countSolutions(Board board, int limit) {
        int[] cell = board.firstEmpty();
        if (cell == null) {
            return 1;
        }
        int r = cell[0];
        int c = cell[1];
        int total = 0;
        for (int v = 1; v <= Board.SIZE; v++) {
            if (board.isLegal(r, c, v)) {
                board.set(r, c, v);
                total += countSolutions(board, limit);
                board.set(r, c, Board.EMPTY);
                if (total >= limit) {
                    return total;              // 已達上限，不必再數
                }
            }
        }
        return total;
    }

    /**
     * 對尚未完成的盤面求一個提示。
     * @return {r, c, v}：在第 r 列第 c 行應填入 v；若無解或已填滿回傳 null
     */
    public int[] hint(Board board) {
        Board copy = new Board(board);         // 在副本上求解，不動到玩家盤面
        if (!solve(copy)) {
            return null;
        }
        int[] cell = board.firstEmpty();
        if (cell == null) {
            return null;
        }
        int r = cell[0];
        int c = cell[1];
        return new int[]{r, c, copy.get(r, c)};
    }
}
