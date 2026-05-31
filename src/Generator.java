import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 隨機出題器：生成保證「唯一解」的新數獨題目。
 *
 * 演算法（複用 Solver 的遞迴回溯，見 docs/出題器流程圖.svg）：
 *   1. 用 Solver.fillRandom 填出一個隨機完整解盤。
 *   2. 把 81 格隨機打亂順序，逐格嘗試挖空。
 *   3. 每挖一格就用 Solver.countSolutions(board, 2) 檢查解的數量：
 *        仍為唯一解（==1）→ 接受挖空；變多解（>1）→ 還原該格。
 *   4. 挖到剩餘提示數達目標、或所有格都試過為止。
 * 隨機洗牌（完整解候選順序＋挖洞順序）是同難度也能無限出新題的關鍵；
 * 每格挖洞後的唯一解檢查，保證產出的題目恰有一個正解（與遊戲提示 h 相容）。
 */
public class Generator {

    private final Solver solver = new Solver();   // 合成：複用回溯求解與解數計數
    private final Random rng = new Random();

    /** 各難度的目標提示數（givens）：越少越難。 */
    private static int targetGivens(int level) {
        switch (level) {
            case 1: return 36;   // 簡單
            case 2: return 30;   // 普通
            case 3: return 26;   // 困難
            default: return 32;
        }
    }

    /**
     * 生成一道指定難度、保證唯一解的新題。
     * @param id    指派給新題的題號
     * @param level 難度 1 簡單 / 2 普通 / 3 困難
     */
    public Puzzle generate(int id, int level) {
        Board board = new Board("0".repeat(Board.SIZE * Board.SIZE));
        solver.fillRandom(board, rng);                 // 步驟 1：隨機完整解盤

        List<int[]> cells = new ArrayList<>();
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                cells.add(new int[]{r, c});
            }
        }
        Collections.shuffle(cells, rng);               // 步驟 2：隨機挖洞順序

        int target = targetGivens(level);
        int givens = Board.SIZE * Board.SIZE;          // 一開始 81 格全為給定
        for (int[] cell : cells) {
            if (givens <= target) {
                break;                                 // 已達目標提示數
            }
            int r = cell[0];
            int c = cell[1];
            int saved = board.get(r, c);
            board.set(r, c, Board.EMPTY);              // 試挖
            if (solver.countSolutions(board, 2) == 1) {
                givens--;                              // 仍唯一解 → 接受
            } else {
                board.set(r, c, saved);                // 變多解 → 還原
            }
        }
        return new Puzzle(id, level, "隨機題", board.toLine());
    }
}
