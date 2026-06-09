# 🔐 Encoder / Decoder

A shift cipher encoder/decoder built with **Java Swing**, demonstrating OOP principles with a modern dark/light mode UI.

## How to Run

```bash
javac Encoder.java
java Encoder
```

## Features
- Encode text using any offset character
- Decode encoded strings automatically
- Dark / Light mode (auto-detects system preference)
- Press Enter or click to submit
- Inline error validation
- Fully resizable window

## OOP Concepts
| Concept | Where |
|---|---|
| Encapsulation | `CipherEngine` (private reference table), `ThemeManager` (private isDark) |
| Abstraction | `BaseForm` abstract base class |
| Inheritance | `EncodeForm` and `DecodeForm` extend `BaseForm` |
| Polymorphism | Both forms override `handleSubmit()` with different behaviour |

## Author
Zin Bo Htet Aung
