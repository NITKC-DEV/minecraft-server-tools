# Minecraft Server Tools
Minecraftのサーバー公開に便利なツール

Minecraftのポート開放(UPnPを使用したNAT越え)ができます

JDKの自動インストールもできる！
## 動作環境
Windows,Linux

Java 8 or later

## 使用法
一部未実装
```text
mstools <Subcommands> <Options>
Subcommands: 
    sync - ワールドを同期するます
    debug - デバッグ用

Options: 
    --help, -h -> Usage info 
```
## Roadmap
- Githubを使用した同期
- DDNSの設定

## Build
`./gradlew jpackage`

`build/jpackage/{windows,linux}/`にインストーラーやJarが生成されます。
