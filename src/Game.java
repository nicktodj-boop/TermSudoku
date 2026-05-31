import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * 遊戲主控：把盤面、求解器、題庫、排行榜「合成」在一起，
 * 負責主選單與遊玩流程，並處理玩家輸入的字串指令（字串）。
 */
public class Game {

    private final Scanner in = new Scanner(System.in);
    private final Random rng = new Random();
    private final Solver solver = new Solver();        // 合成：擁有一個求解器
    private final PuzzleBank bank = new PuzzleBank();   // 合成：擁有題庫
    private final Generator generator = new Generator();// 合成：擁有隨機出題器
    private final Leaderboard board;                   // 合成：擁有排行榜

    // 遊玩時顯示在盤面上方的「上一步結果」狀態列，不會被下一次重畫沖掉
    private String status = "";

    // 本局已使用的提示次數（登上排行榜時一併記錄）
    private int hintsUsed = 0;

    // 步驟歷史（用於復原）：每筆記錄 {列, 行, 該格先前的值}
    private final Deque<int[]> history = new ArrayDeque<>();

    public Game() {
        bank.load("resources/puzzles.txt");
        board = new Leaderboard("scores.txt");
    }

    /** 主選單迴圈。 */
    public void run() {
        while (true) {
            printMenu();
            String choice = readLine();
            if (choice == null) {
                return;                                 // 輸入結束（EOF）
            }
            switch (choice.trim()) {
                case "1": startByDifficulty(); break;
                case "2": chooseFromList(); break;
                case "3": solveDemo(); break;
                case "4": showLeaderboard(); break;
                case "5": startGenerated(); break;
                case "0":
                    System.out.println("  再見！");
                    return;
                default:
                    System.out.println("  無效選項，請輸入 0-5。");
            }
        }
    }

    // ---------- 選單畫面 ----------

    private void printBanner() {
        System.out.println("================================================");
        System.out.println("   終端數獨 TermSudoku   v1.0");
        System.out.println("   文字介面數獨：可玩 · 可解 · 可評難度");
        System.out.println("================================================");
    }

    private void printMenu() {
        System.out.println();
        printBanner();
        System.out.println();
        System.out.println("  [1] 開始新遊戲（選擇難度）");
        System.out.println("  [2] 從題庫選題遊玩");
        System.out.println("  [3] 自動求解示範（觀看回溯解題）");
        System.out.println("  [4] 難度排行榜");
        System.out.println("  [5] 隨機出新題並遊玩（程式生成唯一解）");
        System.out.println("  [0] 離開");
        System.out.println();
        System.out.print("  請輸入選項：");
    }

    // ---------- 選題 ----------

    /** 選難度，從該難度的題目中隨機挑一題開始遊玩。 */
    private void startByDifficulty() {
        System.out.print("  選擇難度（1 簡單 / 2 普通 / 3 困難）：");
        String s = readLine();
        if (s == null) return;
        Integer level = parseInt(s);
        if (level == null || level < 1 || level > 3) {
            System.out.println("  請輸入 1-3。");
            return;
        }
        List<Puzzle> pool = bank.byLevel(level);
        if (pool.isEmpty()) {
            System.out.println("  這個難度目前沒有題目。");
            return;
        }
        play(pool.get(rng.nextInt(pool.size())));
    }

    /** 選難度，由出題器即時生成一道保證唯一解的新題並開始遊玩。 */
    private void startGenerated() {
        System.out.print("  選擇難度（1 簡單 / 2 普通 / 3 困難）：");
        String s = readLine();
        if (s == null) return;
        Integer level = parseInt(s);
        if (level == null || level < 1 || level > 3) {
            System.out.println("  請輸入 1-3。");
            return;
        }
        System.out.println("  產生唯一解新題中……");
        Puzzle p = generator.generate(bank.size() + 1, level);   // 題號接在題庫之後
        play(p);
    }

    /** 列出整個題庫（已依難度排序），讓玩家用題號挑選。 */
    private void chooseFromList() {
        System.out.println();
        System.out.println("  題庫（依難度排序）：");
        for (Puzzle p : bank.all()) {
            System.out.printf("   #%-2d  [%s]  提示數 %2d  %s%n",
                    p.id(), p.levelName(), p.givens(), p.name());
        }
        System.out.print("  輸入題號：");
        String s = readLine();
        if (s == null) return;
        Integer id = parseInt(s);
        Puzzle p = (id == null) ? null : bank.byId(id);
        if (p == null) {
            System.out.println("  找不到該題號。");
            return;
        }
        play(p);
    }

    // ---------- 遊玩流程 ----------

    /** 單局遊玩：顯示盤面、讀取指令、計時，直到完成或返回。 */
    private void play(Puzzle puzzle) {
        Board b = puzzle.newBoard();
        long start = System.nanoTime();        // 單調時鐘計時，避免系統時鐘回撥造成負秒
        history.clear();
        hintsUsed = 0;                         // 本局提示次數歸零
        status = "輸入「列 行 數」填數（例如 3 5 7），數字填 0 清除；輸入 ? 看完整指令。";
        while (true) {
            printPlayScreen(puzzle, b, start);

            String cmd = readLine();
            if (cmd == null) return;
            cmd = normalize(cmd);                  // 容錯：全形轉半形、逗號當分隔、收斂空白
            if (cmd.isEmpty()) {
                continue;
            }
            String[] t = cmd.split(" ");
            String op = t[0].toLowerCase();

            if (op.equals("q")) {
                return;
            } else if (op.equals("?") || op.equals("help")) {
                showHelp();
            } else if (op.equals("h")) {
                applyHint(b);
            } else if (op.equals("s")) {
                autoSolve(b);
                return;                            // 自動解完即結束本局（不計入排行榜）
            } else if (op.equals("u")) {
                undo(b);
            } else if (op.equals("r")) {
                b = puzzle.newBoard();             // 重來本題：清空已填，保留題目給定
                history.clear();
                start = System.nanoTime();
                hintsUsed = 0;
                status = "已重來本題（清空已填，保留題目給定）。";
            } else if (op.equals("n")) {
                Puzzle np = pickSameLevel(puzzle); // 換一題：同難度隨機挑另一題
                if (np.id() == puzzle.id()) {
                    status = "同難度暫無其他題目，已重來本題。";
                } else {
                    puzzle = np;
                    status = "換到題目 #" + puzzle.id() + "（難度：" + puzzle.levelName() + "）。";
                }
                b = puzzle.newBoard();
                history.clear();
                start = System.nanoTime();
                hintsUsed = 0;
            } else if (t.length == 3) {
                fillOrClear(b, t);
            } else {
                status = "指令看不懂，輸入 ? 看說明。";
            }

            if (b.isSolved()) {
                System.out.println();
                System.out.print(b.render());
                int secs = (int) ((System.nanoTime() - start) / 1_000_000_000L);
                System.out.println("  恭喜完成！用時 " + formatSecs(secs) + "。");
                offerSaveScore(puzzle, secs);
                return;
            }
        }
    }

    /** 印出遊玩畫面：題目資訊、狀態列、盤面、指令列。 */
    private void printPlayScreen(Puzzle puzzle, Board b, long start) {
        System.out.println();
        System.out.println("  題目 #" + puzzle.id() + "（難度：" + puzzle.levelName()
                + "，提示數 " + puzzle.givens() + "）   剩 " + b.emptyCount() + " 格   已用時間 " + formatElapsed(start));
        if (!status.isEmpty()) {
            System.out.println("  ※ " + status);     // 固定狀態列：顯示上一步結果
        }
        System.out.println();
        System.out.print(b.render());
        System.out.println();
        System.out.println("  指令： 列 行 數（例 3 5 7）｜ 清除 列 行 0（例 3 5 0）｜ 提示 h ｜ 自動解 s");
        System.out.println("         復原 u ｜ 重來 r ｜ 換題 n ｜ 說明 ? ｜ 返回 q");
        System.out.print("  > ");
    }

    /**
     * 處理「列 行 數」：數字 1-9 為填入，0 為清除。會檢查給定格與規則衝突。
     * 把填入與清除統一成同一條規則，介面更一致。
     */
    private void fillOrClear(Board b, String[] t) {
        Integer r = parseIdx(t[0]);
        Integer c = parseIdx(t[1]);
        Integer v = parseInt(t[2]);
        if (r == null || c == null || v == null) {
            status = "請輸入三個數字，例如 3 5 7（清除填 0）。";
            return;
        }
        if (r < 0 || r >= Board.SIZE || c < 0 || c >= Board.SIZE || v < 0 || v > 9) {
            status = "列、行需為 1-9，數字需為 0-9（0 表示清除）。";
            return;
        }
        if (b.isGiven(r, c)) {
            status = "第 " + (r + 1) + " 列第 " + (c + 1) + " 行是題目給定，不可更動。";
            return;
        }
        if (v == 0) {
            if (b.isEmpty(r, c)) {
                status = "第 " + (r + 1) + " 列第 " + (c + 1) + " 行本來就是空的。";
                return;
            }
            history.push(new int[]{r, c, b.get(r, c)});   // 記錄以便復原
            b.set(r, c, Board.EMPTY);
            status = "已清除 第 " + (r + 1) + " 列第 " + (c + 1) + " 行。";
            return;
        }
        if (!b.isLegal(r, c, v)) {
            status = "第 " + (r + 1) + " 列第 " + (c + 1) + " 行填 " + v + "，與同列／同行／同宮衝突。";
            return;
        }
        history.push(new int[]{r, c, b.get(r, c)});       // 記錄以便復原
        b.set(r, c, v);
        status = "已填入 第 " + (r + 1) + " 列第 " + (c + 1) + " 行 = " + v + "。";
    }

    /** 提示一格：用求解器算出某空格的正確值並填入。 */
    private void applyHint(Board b) {
        int[] h = solver.hint(b);
        if (h == null) {
            status = "目前盤面已無解（先前某格雖合規，但與正解矛盾），無法提示。可用 u 復原或 r 重來。";
            return;
        }
        history.push(new int[]{h[0], h[1], b.get(h[0], h[1])});   // 提示也可被復原
        b.set(h[0], h[1], h[2]);
        hintsUsed++;                                              // 計入本局提示次數
        status = "提示：第 " + (h[0] + 1) + " 列第 " + (h[1] + 1) + " 行 = " + h[2] + "。";
    }

    /** 復原上一步：把最近一次變動的格子還原。 */
    private void undo(Board b) {
        if (history.isEmpty()) {
            status = "沒有可復原的步驟。";
            return;
        }
        int[] m = history.pop();
        b.set(m[0], m[1], m[2]);
        status = "已復原 第 " + (m[0] + 1) + " 列第 " + (m[1] + 1) + " 行。";
    }

    /** 從同難度的題目中隨機挑一題（盡量不同於目前題目）。 */
    private Puzzle pickSameLevel(Puzzle current) {
        List<Puzzle> others = new ArrayList<>();
        for (Puzzle p : bank.byLevel(current.level())) {
            if (p.id() != current.id()) {
                others.add(p);                         // 先排除目前題目，避免隨機重抽的無窮迴圈風險
            }
        }
        return others.isEmpty() ? current : others.get(rng.nextInt(others.size()));
    }

    /** 自動求解：用回溯法把盤面解完並顯示。 */
    private void autoSolve(Board b) {
        Board copy = new Board(b);
        long t0 = System.nanoTime();
        boolean ok = solver.solve(copy);
        double ms = (System.nanoTime() - t0) / 1_000_000.0;
        if (!ok) {
            status = "目前盤面已無解（先前某格雖合規，但與正解矛盾）。可用 u 復原或 r 重來。";
            System.out.println();
            System.out.println("  目前盤面已無解（先前某格雖合規，但與正解矛盾），無法求解。");
            return;
        }
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                b.set(r, c, copy.get(r, c));
            }
        }
        System.out.println();
        System.out.print(b.render());
        System.out.printf("  已用回溯法自動求解完成（耗時 %.2f ms）。%n", ms);
    }

    /** 顯示指令說明，按 Enter 後返回遊玩畫面。 */
    private void showHelp() {
        System.out.println();
        System.out.println("  指令說明（含範例）：");
        System.out.println("    列 行 數   填入數字 1-9          例如： 3 5 7  （第3列第5行填7）");
        System.out.println("    列 行 0    清除該格              例如： 3 5 0");
        System.out.println("    h          提示一格              例如： h");
        System.out.println("    s          自動求解（回溯示範）  例如： s");
        System.out.println("    u          復原上一步            例如： u");
        System.out.println("    r          重來本題（保留給定）  例如： r");
        System.out.println("    n          換一題（同難度隨機）  例如： n");
        System.out.println("    ?          顯示這份說明          例如： ?");
        System.out.println("    q          返回主選單            例如： q");
        System.out.println("  （數字可用全形或半形，分隔可用空白或逗號。）");
        System.out.print("  按 Enter 繼續…");
        readLine();
    }

    /** 完成後詢問是否登上排行榜。 */
    private void offerSaveScore(Puzzle p, int secs) {
        System.out.print("  輸入名字登上排行榜（直接 Enter 略過）：");
        String name = readLine();
        if (name == null) return;
        name = name.trim();
        if (!name.isEmpty()) {
            board.add(name, p.level(), secs, hintsUsed);
            System.out.println("  已記錄到排行榜（提示 " + hintsUsed + " 次）。");
        }
    }

    // ---------- 其他選單功能 ----------

    /** 自動求解示範：挑一題，顯示題面與回溯解出的答案。 */
    private void solveDemo() {
        System.out.print("  輸入題號（直接 Enter 隨機選一題困難）：");
        String s = readLine();
        if (s == null) return;
        s = s.trim();
        Puzzle p;
        if (s.isEmpty()) {
            List<Puzzle> hard = bank.byLevel(3);
            p = hard.isEmpty() ? bank.all().get(0) : hard.get(rng.nextInt(hard.size()));
        } else {
            Integer id = parseInt(s);
            p = (id == null) ? null : bank.byId(id);
            if (p == null) {
                System.out.println("  找不到該題號。");
                return;
            }
        }
        Board b = p.newBoard();
        System.out.println();
        System.out.println("  題目 #" + p.id() + "（" + p.levelName() + "）：");
        System.out.print(b.render());
        Board copy = new Board(b);
        long t0 = System.nanoTime();
        boolean ok = solver.solve(copy);
        double ms = (System.nanoTime() - t0) / 1_000_000.0;
        System.out.println();
        if (!ok) {
            System.out.println("  此題無解。");
            return;
        }
        System.out.println("  回溯法求得的解：");
        System.out.print(copy.render());
        System.out.printf("  求解耗時 %.2f ms。%n", ms);
    }

    /** 顯示排行榜：簡單／普通／困難三張榜分開，各取用時最短前 10 名。 */
    private void showLeaderboard() {
        System.out.println();
        if (board.isEmpty()) {
            System.out.println("  排行榜目前沒有紀錄，完成一局即可上榜。");
            return;
        }
        for (int level = 1; level <= 3; level++) {
            System.out.println();
            System.out.println("  【" + Puzzle.levelName(level) + "】用時最短前 10 名：");
            List<Leaderboard.Record> rs = board.top(level, 10);
            if (rs.isEmpty()) {
                System.out.println("    （此難度尚無紀錄）");
                continue;
            }
            int rank = 1;
            for (Leaderboard.Record r : rs) {
                System.out.printf("   %2d. %-10s %s  提示 %d 次  %s%n",
                        rank++, r.name(), formatSecs(r.seconds()), r.hints(), r.date());
            }
        }
    }

    // ---------- 工具方法 ----------

    /** 讀取一行輸入；輸入結束（EOF）時回傳 null。 */
    private String readLine() {
        return in.hasNextLine() ? in.nextLine() : null;
    }

    /**
     * 將輸入正規化：全形數字轉半形、逗號（半形/全形/頓號）轉空白、收斂多餘空白。
     * 讓「３，５，７」「3,5,7」「3   5   7」都能被接受。
     */
    private String normalize(String s) {
        StringBuilder sb = new StringBuilder();
        for (char ch : s.trim().toCharArray()) {
            if (ch == ',' || ch == '，' || ch == '、') {
                sb.append(' ');                              // 逗號（半／全形）、頓號 → 空白
            } else if (ch == '　') {
                sb.append(' ');                              // 全形空格 → 半形空白
            } else if (ch >= '！' && ch <= '～') {
                sb.append((char) (ch - 0xFEE0));             // 全形 ASCII（數字＋英文字母）→ 半形
            } else {
                sb.append(ch);
            }
        }
        return sb.toString().trim().replaceAll("\\s+", " ");
    }

    /** 將字串轉整數，失敗回傳 null。 */
    private Integer parseInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 將「1-9 的座標」字串轉成 0 起算的索引；失敗回傳 null。 */
    private Integer parseIdx(String s) {
        Integer v = parseInt(s);
        return (v == null) ? null : v - 1;
    }

    private String formatElapsed(long startNanos) {
        return formatSecs((int) ((System.nanoTime() - startNanos) / 1_000_000_000L));
    }

    private String formatSecs(int s) {
        return String.format("%02d:%02d", s / 60, s % 60);
    }
}
