# TermSudoku 簡報

終端數獨 TermSudoku（Java 期末分組作品 G907）的展示簡報。走場景 3 簡報工作流重做：
`ppt-classify`（判型）→ 引導定案 → `ppt-narrative-review`（審敘事）→ `ui-ux-pro-max`（定風格）→ HTML → `impeccable`（打磨）→ 匯出 PDF。

- **類型**：主 Teaching/How-to + 次 Narrative
- **規格**：9 頁 16:9，深色終端機風（Dark Mode OLED，accent 綠 #22C55E，JetBrains Mono + IBM Plex Sans + Noto Sans TC）
- **交付**：HTML slideshow（←/→ 翻頁、F 全螢幕）＋ 匯出 PDF（作業限 .ppt/.pdf）
- **時長**：~12 分鐘，含現場 live demo（第 4/5/6 頁）

## 交付版本（2026-06-02）

**正式交付 = HTML→PDF 版**：`3-成品/index.html`＋`TermSudoku-簡報.pdf`。
用**真實終端截圖 + 真實出題器流程圖 + 精確結構化內容**，深色終端風，無 AI 亂碼。

> Canva（Claude Design）路線曾試過：自動生圖全是亂碼假圖，事後雖用真截圖替換修過，
> 整體品質仍不如手刻 HTML 版，**已放棄**。Canva 設計仍留在帳號內（design id `DAHLmusn1MQ`），
> 本機匯出已刪。為 Canva 上傳而推到 repo 的 `docs/presentation/*.png` 截圖留作 repo 用圖。

## 三幕結構

```
TermSudoku簡報/
├─ 1-研究素材/      終端截圖、出題器流程圖.svg
├─ 2-分析骨架/      骨架定案.md、Claude-Design-brief.md（逐頁文案 + 設計方向）
├─ 3-成品/
│   ├─ index.html          ← 簡報本體（深色終端風 slideshow，←/→ 翻頁、F 全螢幕）
│   ├─ TermSudoku-簡報.pdf  ← 繳交用 PDF（每頁一張）
│   └─ assets/             簡報引用的圖（真截圖 + 流程圖）
└─ README.md
```

## 9 頁

1. 封面·題目  2. 動機（提問鉤子）  3. 系統概覽·畫面設計  4. 互動 demo ① 操作+即時驗證
5. 互動 demo ② 自動求解+分難度排行榜  6. 技術亮點·隨機出題器（流程圖+唯一解保證）
7. 對應六大主題  8. 心得建議  9. GitHub + 執行

## 重新產生 PDF

改完 `3-成品/index.html` 後：

```bash
cd 3-成品
"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" \
  --headless --disable-gpu --no-pdf-header-footer \
  --print-to-pdf="TermSudoku-簡報.pdf" "file://$PWD/index.html"
```

列印 CSS 已設 `@page 1280×720`，每張投影片自成一頁。

## 狀態：定稿（2026-06-07）

- **第 8 頁三份心得齊全**：陳曼婷、黃建瑋（組員原文，小修錯字）、李佳勳（採草稿，本人確認「草稿就可」）。
- live demo（4/5/6 頁）現場操作前先跑一次；失敗的後備＝`1-研究素材/` 的截圖。
