import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 題庫：用 List 收藏多道題目（收藏），並提供
 * 載入（字串解析）、依難度排序（遞迴合併排序）、以及搜尋（線性與二分）。
 */
public class PuzzleBank {

    // 以 ArrayList 收藏所有題目
    private final List<Puzzle> puzzles = new ArrayList<>();

    /**
     * 從檔案載入題庫；若找不到檔案或讀取失敗，改用內建題目，確保程式仍可執行。
     * 檔案每行格式： level|81字元題面|名稱 （# 開頭為註解，空行略過）
     */
    public void load(String path) {
        puzzles.clear();
        List<String> lines = null;
        try {
            Path p = Paths.get(path);
            if (Files.exists(p)) {
                lines = Files.readAllLines(p, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            // 讀檔失敗就維持 null，下面改用內建題目
        }
        if (lines == null) {
            lines = Arrays.asList(BUILTIN);
        }

        int id = 1;
        for (String raw : lines) {
            String s = raw.trim();
            if (s.isEmpty() || s.startsWith("#")) {
                continue;                          // 字串：略過空行與註解
            }
            String[] parts = s.split("\\|");       // 字串：以 | 切出三個欄位
            if (parts.length < 3) {
                continue;
            }
            int level;
            try {
                level = Integer.parseInt(parts[0].trim());
            } catch (NumberFormatException e) {
                continue;
            }
            String line = parts[1].trim();
            String name = parts[2].trim();
            if (line.length() == 81) {
                puzzles.add(new Puzzle(id++, level, name, line));
            }
        }
        sortByLevel();                             // 載入後立即依難度排序
    }

    /** 全部題目（已依難度排序）。 */
    public List<Puzzle> all() {
        return puzzles;
    }

    public int size() {
        return puzzles.size();
    }

    /** 依題號線性搜尋。 */
    public Puzzle byId(int id) {
        for (Puzzle p : puzzles) {
            if (p.id() == id) return p;
        }
        return null;
    }

    /** 取得某難度的所有題目。 */
    public List<Puzzle> byLevel(int level) {
        List<Puzzle> out = new ArrayList<>();
        for (Puzzle p : puzzles) {
            if (p.level() == level) out.add(p);
        }
        return out;
    }

    /** 依難度排序：合併排序（merge sort），以遞迴實作。 */
    public void sortByLevel() {
        List<Puzzle> sorted = mergeSort(puzzles);
        puzzles.clear();
        puzzles.addAll(sorted);
    }

    /** 合併排序的遞迴主體：把清單對半切，各自排好再合併。 */
    private List<Puzzle> mergeSort(List<Puzzle> list) {
        if (list.size() <= 1) {
            return new ArrayList<>(list);          // 終止條件：0 或 1 個元素本身即有序
        }
        int mid = list.size() / 2;
        List<Puzzle> left = mergeSort(new ArrayList<>(list.subList(0, mid)));
        List<Puzzle> right = mergeSort(new ArrayList<>(list.subList(mid, list.size())));
        return merge(left, right);
    }

    /** 合併兩個已排序清單，依難度由小到大。 */
    private List<Puzzle> merge(List<Puzzle> a, List<Puzzle> b) {
        List<Puzzle> out = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < a.size() && j < b.size()) {
            if (a.get(i).level() <= b.get(j).level()) {
                out.add(a.get(i++));
            } else {
                out.add(b.get(j++));
            }
        }
        while (i < a.size()) out.add(a.get(i++));
        while (j < b.size()) out.add(b.get(j++));
        return out;
    }

    /**
     * 二分搜尋第一道「指定難度」的題目（前提：清單已依難度排序）。
     * 找不到回傳 null。示範二分搜索。
     */
    public Puzzle firstByLevel(int level) {
        int lo = 0;
        int hi = puzzles.size() - 1;
        int found = -1;
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            int v = puzzles.get(mid).level();
            if (v >= level) {
                if (v == level) found = mid;       // 記下候選，繼續往左找更前面的
                hi = mid - 1;
            } else {
                lo = mid + 1;
            }
        }
        return found >= 0 ? puzzles.get(found) : null;
    }

    // 內建題目（找不到 resources/puzzles.txt 時的後備，確保專案可直接執行）
    private static final String[] BUILTIN = {
        "1|003020600900305001001806400008102900700000008006708200002609500800203009005010300|入門一",
        "1|530070000600195000098000060800060003400803001700020006060000280000419005000080079|入門二",
        "2|200080300060070084030500209000105408000000000402706000301007040720040060004010003|進階一",
        "2|030050040008010500460000012070502080000603000040109030250000098001020600080060020|進階二",
        "3|000000907000420180000705026100904000050000040000507009920108000034059000507000000|挑戰一",
        "3|100920000524010000000000070050008102000000000402700090060000000000030945000071006|挑戰二",
    };
}
