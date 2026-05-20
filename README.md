# 終端數獨 TermSudoku

文字介面（終端機）數獨遊戲，以 Java 實作。可載入題庫遊玩、即時檢查衝突、提示一格、以回溯法（backtracking）自動求解，並記錄完成排行榜。

淡江大學資訊管理學系 — Java 程式設計　期末分組作品（G9）

組員：李佳勳（學號：414631225） ／ 黃建瑋（學號：414630326）

## 執行方式

需求：JDK 17 以上（開發環境為 JDK 25）。請在專案根目錄執行，程式會讀取 `resources/puzzles.txt`（找不到時改用內建題目）。

命令列：

```bash
javac -encoding UTF-8 -d out src/*.java
java -cp out TermSudoku
```

IDE：將本資料夾以 Java 專案開啟（IntelliJ 直接 Open 資料夾；Eclipse 以 File → New → Java Project 後將 `src` 設為原始碼資料夾），執行 `TermSudoku` 的 `main`。

## 操作說明（遊戲中）

| 指令 | 作用 | 範例 |
|---|---|---|
| `列 行 數` | 在第「列」列、第「行」行填入數字 1-9 | `3 5 7` |
| `列 行 0` | 清除該格 | `3 5 0` |
| `h` | 提示一格（由求解器給出正確數字） | `h` |
| `s` | 自動求解（觀看回溯解題） | `s` |
| `u` | 復原上一步 | `u` |
| `r` | 重來本題（清空已填，保留題目給定） | `r` |
| `n` | 換一題（同難度隨機） | `n` |
| `?` | 顯示指令說明 | `?` |
| `q` | 返回主選單 | `q` |

數字可用全形或半形，分隔可用空白或逗號。

## 專案結構

```
src/TermSudoku.java   進入點
src/Game.java         主選單與遊玩流程（合成各模組、字串指令解析）
src/Board.java        9×9 盤面（封裝、合法性檢查、盤面渲染）
src/Solver.java       回溯遞迴求解、計算解數量、提示一格
src/Puzzle.java       一道題目（題號／難度／題面）
src/PuzzleBank.java   題庫（收藏、合併排序、二分搜尋）
src/Leaderboard.java  排行榜（收藏、排序、檔案讀寫）
resources/puzzles.txt 題庫（每行格式： 難度|81字元題面|名稱）
```

## 對應本學期主題

| 主題 | 在哪裡實作 |
|---|---|
| 物件封裝合成 | `Board`／`Puzzle` 的私有欄位；`Game` 組合 `Solver`／`PuzzleBank`／`Leaderboard` |
| 收藏 | `PuzzleBank`、`Leaderboard` 的 `List`；復原步驟用 `Deque` |
| 字串 | 題庫行解析、指令解析、盤面渲染 |
| 遞迴 | `Solver` 回溯求解、`countSolutions`、`PuzzleBank` 合併排序 |
| 搜索 | 回溯搜索、`firstByLevel` 二分搜尋、`byId` 線性搜尋 |
| 排序 | 合併排序（依難度）、排行榜依用時排序 |
