/**
 * 終端數獨 TermSudoku 的程式進入點。
 * 文字介面數獨：可載入題庫遊玩、即時檢查衝突、提示一格、以回溯法自動求解、並記錄排行榜。
 */
public class TermSudoku {

    /** 程式進入點：建立遊戲並進入主選單迴圈。 */
    public static void main(String[] args) {
        new Game().run();
    }
}
