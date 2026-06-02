import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Method;
import java.util.List;

/**
 * TermSudoku 自我測試（不依賴外部測試框架）。
 *
 * 涵蓋：出題器唯一解與提示數、排行榜分難度與向後相容、二分搜尋取題、
 * 唯一解過濾、全形輸入正規化、排行榜壞行不清空。
 *
 * 編譯與執行（在專案根目錄）：
 *   javac -encoding UTF-8 -d out src/*.java test/SelfTest.java
 *   java -cp out SelfTest
 * 全部通過時 exit code 0，有任何失敗時印出 FAIL 並回傳非 0。
 */
public class SelfTest {

    private static int pass = 0;
    private static int fail = 0;

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "  PASS " : "  FAIL ") + name);
        if (ok) {
            pass++;
        } else {
            fail++;
        }
    }

    public static void main(String[] args) throws Exception {
        Solver solver = new Solver();
        Generator gen = new Generator();

        // ---- 出題器：每難度生成唯一解、可解、提示數達標 ----
        int[] target = {0, 36, 30, 26};   // 以 level 為索引
        for (int lvl = 1; lvl <= 3; lvl++) {
            Puzzle p = gen.generate(100 + lvl, lvl);
            check("出題器 難度" + lvl + " 題面長度 81", p.line().length() == 81);
            check("出題器 難度" + lvl + " level 正確", p.level() == lvl);
            Board b = p.newBoard();
            check("出題器 難度" + lvl + " 唯一解", solver.countSolutions(new Board(b), 2) == 1);
            check("出題器 難度" + lvl + " 可解出", solver.solve(new Board(b)));
            check("出題器 難度" + lvl + " 提示數 " + target[lvl], p.givens() == target[lvl]);
        }
        check("出題器 同難度兩次生成不同題（隨機性）",
                !gen.generate(1, 2).line().equals(gen.generate(2, 2).line()));

        // ---- 題庫：二分搜尋取題、多解題被擋下 ----
        PuzzleBank bank = new PuzzleBank();
        bank.load("resources/puzzles.txt");
        check("題庫 載入非空（皆唯一解）", bank.size() > 0);
        for (int lvl = 1; lvl <= 3; lvl++) {
            final int L = lvl;
            Puzzle first = bank.firstByLevel(lvl);
            List<Puzzle> pool = bank.byLevel(lvl);
            check("題庫 byLevel(" + lvl + ") 全為該難度且非空",
                    !pool.isEmpty() && pool.stream().allMatch(q -> q.level() == L));
            check("題庫 firstByLevel 即該難度第一題",
                    first != null && pool.get(0).id() == first.id());
        }
        Path multi = Files.createTempFile("ts_multi", ".txt");
        String empty = "0".repeat(81);
        String unique = "003020600900305001001806400008102900700000008006708200002609500800203009005010300";
        Files.write(multi, ("1|" + empty + "|多解\n1|" + unique + "|唯一\n").getBytes(StandardCharsets.UTF_8));
        PuzzleBank b2 = new PuzzleBank();
        b2.load(multi.toString());
        check("題庫 多解題被 countSolutions 擋下（只收唯一解）", b2.size() == 1);

        // ---- 輸入正規化：全形數字／空格／字母（反射呼叫私有方法） ----
        Game game = new Game();
        Method nm = Game.class.getDeclaredMethod("normalize", String.class);
        nm.setAccessible(true);
        check("正規化 全形數字＋全形空格 ３　５　７", nm.invoke(game, "３　５　７").equals("3 5 7"));
        check("正規化 全形逗號 ３，５，７", nm.invoke(game, "３，５，７").equals("3 5 7"));
        check("正規化 全形字母 Ｈ→H", nm.invoke(game, "Ｈ").equals("H"));
        check("正規化 全形字母 Ｓ→S", nm.invoke(game, "Ｓ").equals("S"));
        check("正規化 半形維持 3 5 7", nm.invoke(game, "3 5 7").equals("3 5 7"));

        // ---- 排行榜：壞行不清空、向後相容、分難度、日期與提示 ----
        Path bad = Files.createTempFile("ts_bad", ".txt");
        Files.write(bad, "Alice|1|30\nBADLINE|x|y\nBob|2|45\n".getBytes(StandardCharsets.UTF_8));
        Leaderboard lbBad = new Leaderboard(bad.toString());
        check("排行榜 壞行略過、保留兩筆（非整榜清空）", lbBad.top(10).size() == 2);

        Path oldfmt = Files.createTempFile("ts_old", ".txt");
        Files.write(oldfmt, "張三|1|40\n".getBytes(StandardCharsets.UTF_8));
        Leaderboard lbOld = new Leaderboard(oldfmt.toString());
        check("排行榜 舊 3 欄相容：日期→「—」", lbOld.top(10).get(0).date().equals("—"));
        check("排行榜 舊 3 欄相容：提示→0", lbOld.top(10).get(0).hints() == 0);

        Path neu = Files.createTempFile("ts_new", ".txt");
        Files.deleteIfExists(neu);
        Leaderboard lb = new Leaderboard(neu.toString());
        lb.add("王五", 1, 33, 2);
        lb.add("趙六", 1, 20, 0);
        lb.add("錢七", 3, 99, 5);
        Leaderboard reload = new Leaderboard(neu.toString());
        check("排行榜 新 5 欄 round-trip：保留 hints",
                reload.top(10).stream().anyMatch(r -> r.name().equals("王五") && r.hints() == 2));
        check("排行榜 新 5 欄 round-trip：日期已戳記",
                reload.top(10).get(0).date().length() >= 8);
        List<Leaderboard.Record> easy = reload.top(1, 10);
        check("排行榜 分難度：簡單榜只含簡單兩筆",
                easy.size() == 2 && easy.stream().allMatch(r -> r.level() == 1));
        check("排行榜 分難度：簡單榜依時間排序（趙六 20 在前）",
                easy.get(0).name().equals("趙六"));
        check("排行榜 分難度：普通榜無紀錄回空", reload.top(2, 10).isEmpty());

        // ---- 清理暫存 ----
        for (Path tmp : new Path[]{multi, bad, oldfmt, neu}) {
            try { Files.deleteIfExists(tmp); } catch (IOException ignored) { }
        }

        System.out.println();
        System.out.println("結果：PASS " + pass + " / FAIL " + fail);
        if (fail > 0) {
            System.exit(1);
        }
    }
}
