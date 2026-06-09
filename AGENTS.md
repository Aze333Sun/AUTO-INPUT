# AGENTS.md

## Project Overview

JetBrains IDE plugin (IntelliJ Platform) that auto-switches between Chinese and English input methods based on cursor position. Supports IdeaVim. Windows-only (JNA/Win32 API).

## Build & Run

```bash
# Build plugin JAR
mvn package

# Run in sandboxed IDE for testing
mvn intellij-platform:run-ide

# Compile only
mvn compile

# Clean build
mvn clean package
```

No test suite exists. No CI pipeline configured.

## Architecture

Two entry points registered via `plugin.xml` / `idea-vim-extension.xml`:

| Entry Point | When Used | Listener |
|---|---|---|
| `starter.InputMethodPlugin` | IdeaVim NOT installed | `BaseInputMethodDetector` |
| `starter.VimInputMethodPlugin` | IdeaVim installed | `VimInputMethodDetector` |

**Flow:** Editor caret change → `CommentUtils.isInComment()` (PSI-based) → determine `CursorState` (INCODE/INCOMMENT/OUTIDE) → call `InputMethodChecker.pressShift()` if input method needs toggling.

**Key constraint:** `InputMethodChecker` uses JNA to call Windows `imm32.dll` and `user32.dll`. Cannot be refactored to Spring/non-static pattern without performance regression. `pressShift()` has a 100ms debounce.

## Source Layout

```
src/main/java/
  starter/          - Plugin entry points and wiring
  listener/         - Caret/focus/mode listeners
  inputmethod/      - Windows input method detection (JNA)
  enums/            - CursorState, InputState enums
  utils/            - Comment detection (PSI)
```

## Gotchas

- Target platform: IntelliJ IDEA 2024.3.3+ (`sinceBuild=243.3`, `untilBuild=253.*`)
- Java source/target: 17/21
- IdeaVim dependency is optional — detected at runtime via `Class.forName()`
- JNA dependencies (jna, jna-platform) are bundled with the plugin
- Qodana configured for code analysis (JVM linter, JDK 21)
