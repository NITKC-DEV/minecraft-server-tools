# リポジトリの初期化(鯖管理者向け)
生成した`mstools.config.json`には機密情報が含まれているので、`.gitignore`で除外しましょう。

```gitignore
mstools.config.json

#オプション: 不必要なフォルダも別途除外
./Logs
```