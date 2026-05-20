import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 排行榜：以 List 收藏完成紀錄（收藏），依用時排序（排序），並讀寫檔案保存（字串）。
 */
public class Leaderboard {

    /** 單筆完成紀錄：玩家名稱、難度、用時秒數（封裝）。 */
    public static class Record {
        private final String name;
        private final int level;
        private final int seconds;

        public Record(String name, int level, int seconds) {
            this.name = name;
            this.level = level;
            this.seconds = seconds;
        }

        public String name() {
            return name;
        }

        public int level() {
            return level;
        }

        public int seconds() {
            return seconds;
        }
    }

    private final List<Record> records = new ArrayList<>();
    private final String path;

    public Leaderboard(String path) {
        this.path = path;
        load();
    }

    /** 從檔案載入紀錄；檔案不存在或內容損毀時，視為空榜。 */
    private void load() {
        records.clear();
        try {
            Path p = Paths.get(path);
            if (!Files.exists(p)) {
                return;
            }
            for (String s : Files.readAllLines(p, StandardCharsets.UTF_8)) {
                String[] a = s.split("\\|");       // 字串：以 | 切欄位
                if (a.length >= 3) {
                    records.add(new Record(a[0], Integer.parseInt(a[1]), Integer.parseInt(a[2])));
                }
            }
            sortByTime();
        } catch (IOException | NumberFormatException e) {
            records.clear();                       // 資料有問題就重來，不讓程式中斷
        }
    }

    /** 新增一筆紀錄，重新排序後存檔。 */
    public void add(String name, int level, int seconds) {
        records.add(new Record(name, level, seconds));
        sortByTime();
        save();
    }

    /** 依用時由短到長排序。 */
    private void sortByTime() {
        records.sort(Comparator.comparingInt(Record::seconds));
    }

    /** 將目前紀錄寫回檔案。 */
    private void save() {
        try (BufferedWriter w = Files.newBufferedWriter(Paths.get(path), StandardCharsets.UTF_8)) {
            for (Record r : records) {
                w.write(r.name() + "|" + r.level() + "|" + r.seconds());
                w.newLine();
            }
        } catch (IOException e) {
            System.out.println("  （排行榜存檔失敗：" + e.getMessage() + "）");
        }
    }

    /** 取得用時最短的前 n 名。 */
    public List<Record> top(int n) {
        return records.subList(0, Math.min(n, records.size()));
    }

    public boolean isEmpty() {
        return records.isEmpty();
    }
}
