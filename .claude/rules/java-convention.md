# 프로젝트 Java 코딩 컨벤션

프로젝트의 Checkstyle·IntelliJ 포매터 설정에서 추출한 Java 코딩 규격이다. 코드 작성 시 아래 규칙을 그대로 적용한다.

> **적용 대상:** `**/*.java`
> 규칙 본문은 Checkstyle 설정 용어와 1:1 대응을 위해 영어 원문을 유지한다.

---

## Naming Conventions

- **Packages**: Lowercase, dot-separated (e.g., `com.example.app.module`). Pattern: `^[a-z]+(\.[a-z][a-z0-9]*)*$`
- **Classes/Interfaces**: UpperCamelCase. Pattern: `^[A-Z][a-zA-Z0-9]*$`
- **Methods**: lowerCamelCase. Pattern: `^[a-z][a-z0-9][a-zA-Z0-9_]*$` (최소 2자 강제 — 단일 문자 메소드명 금지)
- **Variables/Parameters**: lowerCamelCase. Pattern: `^[a-z][a-zA-Z0-9][a-zA-Z0-9]*$` (최소 2자 강제 — 단일 문자 변수명 `i`, `x` 등 금지)
- **Abbreviations**:
  - Treat abbreviations as words (e.g., `XmlHttpRequest` not `XMLHTTPRequest`).
  - Exception: `DAO`, `BO` are allowed as uppercase (e.g., `UserDAO`).
  - Max consecutive uppercase letters: 1 (except allowed list).

---

## Formatting

- **Indentation**: Use **TAB** characters, not spaces.
  - Tab width: 4
- **Line Length**: Max **140** characters.
- **Line Endings**: LF (Unix style).
- **Encoding**: UTF-8.
- **Trailing Spaces**: Must be removed.
- **Modifiers**: Follow JLS order (`public static final...`).

---

## Imports

- **Avoid Star Imports**: Do not use `import java.util.*;`. Explicitly import classes.
- **Order**:
  1. Static imports
  2. `java.*`
  3. `javax.*`
  4. `org.*`
  5. `net.*`
  6. `com.*` (프로젝트 베이스 패키지 제외)
  7. Other packages
  8. 프로젝트 베이스 패키지 `*` (예: `com.yourcompany.app.*`)
- **Spacing**: Blank lines between import groups.

---

## Code Style

- **Braces (K&R Style)**:
  - Opening brace on the same line.
  - `else`, `catch`, `finally` on the same line as the closing brace of the previous block.
  - **Required**: Braces are mandatory for all control structures (`if`, `else`, `for`, `do`, `while`), even for single statements.
- **Statements**: One statement per line. One variable declaration per line.
- **Arrays**: Place brackets after the type, not the variable (e.g., `String[] args`).
- **Long Literals**: Use uppercase `L` suffix (e.g., `100L`).
- **Annotations**:
  - Place annotations on their own line for classes, methods, and fields.
  - Single parameterless annotations can be on the same line.

---

## Whitespace

- **Spaces Required**:
  - Around binary/ternary operators (`=`, `+`, `?`, etc.).
  - After keywords (`if`, `for`, `while`, `switch`, `try`, `catch`, `synchronized`).
  - After commas and semicolons.
- **No Spaces**:
  - Between method name and opening parenthesis.
  - After casting parentheses (e.g., `(String)object`).
  - Before/after generic angle brackets (e.g., `List<String>`).
  - Around unary operators (`++`, `--`, `!`).