/**
 * 一道數獨題目：封裝題號、難度、名稱與 81 字元題面。
 * 與 Board 的關係是「合成」——由題面字串產生一個盤面物件。
 */
public class Puzzle {

    private final int id;          // 題號
    private final int level;       // 難度：1 簡單 / 2 普通 / 3 困難
    private final String name;     // 題目名稱
    private final String line;     // 81 字元題面

    public Puzzle(int id, int level, String name, String line) {
        this.id = id;
        this.level = level;
        this.name = name;
        this.line = line;
    }

    public int id() {
        return id;
    }

    public int level() {
        return level;
    }

    public String name() {
        return name;
    }

    public String line() {
        return line;
    }

    /** 由題面字串產生一個全新的盤面物件（合成）。 */
    public Board newBoard() {
        return new Board(line);
    }

    /** 題目給定的數字個數（提示數），可作為難度的粗略指標。 */
    public int givens() {
        int n = 0;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch >= '1' && ch <= '9') n++;
        }
        return n;
    }

    /** 此題難度的中文名稱。 */
    public String levelName() {
        return levelName(level);
    }

    /** 將難度代碼轉為中文名稱。 */
    public static String levelName(int lvl) {
        switch (lvl) {
            case 1: return "簡單";
            case 2: return "普通";
            case 3: return "困難";
            default: return "未知";
        }
    }
}
