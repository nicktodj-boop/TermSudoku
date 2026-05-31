import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 排行榜：以 List 收藏完成紀錄（收藏），依用時排序（排序），並讀寫檔案保存（字串）。
 */
public class Leaderboard {

    /** 單筆完成紀錄：玩家名稱、難度、用時秒數、完成日期、提示次數（封裝）。 */
    public static class Record {
        private final String name;
        private final int level;
        private final int seconds;
        private final String date;     // 完成日期 yyyy-MM-dd；舊檔無此欄時為「—」
        private final int hints;       // 該局用掉的提示次數；舊檔無此欄時為 0

        public Record(String name, int level, int seconds, String date, int hints) {
            this.name = name;
            this.level = level;
            this.seconds = seconds;
            this.date = date;
            this.hints = hints;
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

        public String date() {
            return date;
        }

        public int hints() {
            return hints;
        }
    }

    private final List<Record> records = new ArrayList<>();
    private final String path;

    public Leaderboard(String path) {
        this.path = path;
        load();
    }

    /** 從檔案載入紀錄；檔案不存在或讀檔失敗時視為空榜，單行損毀只略過該行。 */
    private void load() {
        records.clear();
        List<String> lines;
        try {
            Path p = Paths.get(path);
            if (!Files.exists(p)) {
                return;
            }
            lines = Files.readAllLines(p, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return;                                // 讀檔失敗：視為空榜，不動既有檔
        }
        for (String s : lines) {
            String[] a = s.split("\\|");           // 字串：以 | 切欄位
            if (a.length >= 3) {
                try {
                    int level = Integer.parseInt(a[1]);
                    int seconds = Integer.parseInt(a[2]);
                    String date = (a.length >= 4) ? a[3] : "—";     // 向後相容舊 3 欄格式
                    int hints = (a.length >= 5) ? Integer.parseInt(a[4]) : 0;
                    records.add(new Record(a[0], level, seconds, date, hints));
                } catch (NumberFormatException e) {
                    // 單行數字損毀只略過該行，不清空其餘正常紀錄（避免整榜遺失）
                }
            }
        }
        sortByTime();
    }

    /** 新增一筆紀錄（自動以今日日期戳記），重新排序後存檔。 */
    public void add(String name, int level, int seconds, int hints) {
        records.add(new Record(name, level, seconds, LocalDate.now().toString(), hints));
        sortByTime();
        save();
    }

    /** 依用時由短到長排序。 */
    private void sortByTime() {
        records.sort(Comparator.comparingInt(Record::seconds));
    }

    /** 將目前紀錄寫回檔案（5 欄：名稱|難度|秒數|日期|提示次數）。 */
    private void save() {
        try (BufferedWriter w = Files.newBufferedWriter(Paths.get(path), StandardCharsets.UTF_8)) {
            for (Record r : records) {
                w.write(r.name() + "|" + r.level() + "|" + r.seconds() + "|" + r.date() + "|" + r.hints());
                w.newLine();
            }
        } catch (IOException e) {
            System.out.println("  （排行榜存檔失敗：" + e.getMessage() + "）");
        }
    }

    /** 取得用時最短的前 n 名（全難度混排）。 */
    public List<Record> top(int n) {
        return records.subList(0, Math.min(n, records.size()));
    }

    /** 取得「指定難度」用時最短的前 n 名（分難度榜）。 */
    public List<Record> top(int level, int n) {
        List<Record> out = new ArrayList<>();
        for (Record r : records) {                 // records 已依用時排序，依序挑出該難度即有序
            if (r.level() == level) {
                out.add(r);
                if (out.size() >= n) break;
            }
        }
        return out;
    }

    public boolean isEmpty() {
        return records.isEmpty();
    }
}
