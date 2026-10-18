package com.example.ime

enum class KeyType {
    CHAR,
    SHIFT,
    BACKSPACE,
    ENTER,
    SPACE,
    SYMBOLS_TOGGLE,
    HIDE_KEYBOARD,
    SETTINGS
}

enum class ShiftState {
    OFF,
    SHIFTED,
    CAPS_LOCKED
}

enum class KeyboardMode {
    LETTERS,
    SYMBOLS
}

data class KeyModel(
    val type: KeyType,
    val primary: String,
    val shifted: String = primary.uppercase(),
    val weight: Float = 1.0f,
    val altHint: String? = null
)

object KeyboardLayout {

    fun getLeftPanelRows(
        mode: KeyboardMode,
        showNumberRow: Boolean
    ): List<List<KeyModel>> {
        val rows = mutableListOf<List<KeyModel>>()

        if (showNumberRow && mode == KeyboardMode.LETTERS) {
            rows.add(
                listOf(
                    KeyModel(KeyType.CHAR, "1"),
                    KeyModel(KeyType.CHAR, "2"),
                    KeyModel(KeyType.CHAR, "3"),
                    KeyModel(KeyType.CHAR, "4"),
                    KeyModel(KeyType.CHAR, "5")
                )
            )
        }

        when (mode) {
            KeyboardMode.LETTERS -> {
                rows.add(
                    listOf(
                        KeyModel(KeyType.CHAR, "q", altHint = "1"),
                        KeyModel(KeyType.CHAR, "w", altHint = "2"),
                        KeyModel(KeyType.CHAR, "e", altHint = "3"),
                        KeyModel(KeyType.CHAR, "r", altHint = "4"),
                        KeyModel(KeyType.CHAR, "t", altHint = "5")
                    )
                )
                rows.add(
                    listOf(
                        KeyModel(KeyType.CHAR, "a", altHint = "@"),
                        KeyModel(KeyType.CHAR, "s", altHint = "#"),
                        KeyModel(KeyType.CHAR, "d", altHint = "$"),
                        KeyModel(KeyType.CHAR, "f", altHint = "%"),
                        KeyModel(KeyType.CHAR, "g", altHint = "&")
                    )
                )
                rows.add(
                    listOf(
                        KeyModel(KeyType.SHIFT, "⇧", weight = 1.25f),
                        KeyModel(KeyType.CHAR, "z", altHint = "*"),
                        KeyModel(KeyType.CHAR, "x", altHint = "-"),
                        KeyModel(KeyType.CHAR, "c", altHint = "+"),
                        KeyModel(KeyType.CHAR, "v", altHint = "=")
                    )
                )
                rows.add(
                    listOf(
                        KeyModel(KeyType.SYMBOLS_TOGGLE, "?123", weight = 1.25f),
                        KeyModel(KeyType.CHAR, ",", weight = 0.85f),
                        KeyModel(KeyType.SPACE, "Space", weight = 2.0f)
                    )
                )
            }
            KeyboardMode.SYMBOLS -> {
                rows.add(
                    listOf(
                        KeyModel(KeyType.CHAR, "1"),
                        KeyModel(KeyType.CHAR, "2"),
                        KeyModel(KeyType.CHAR, "3"),
                        KeyModel(KeyType.CHAR, "4"),
                        KeyModel(KeyType.CHAR, "5")
                    )
                )
                rows.add(
                    listOf(
                        KeyModel(KeyType.CHAR, "@"),
                        KeyModel(KeyType.CHAR, "#"),
                        KeyModel(KeyType.CHAR, "$"),
                        KeyModel(KeyType.CHAR, "%"),
                        KeyModel(KeyType.CHAR, "&")
                    )
                )
                rows.add(
                    listOf(
                        KeyModel(KeyType.CHAR, "~", weight = 1.25f),
                        KeyModel(KeyType.CHAR, "*"),
                        KeyModel(KeyType.CHAR, "-"),
                        KeyModel(KeyType.CHAR, "+"),
                        KeyModel(KeyType.CHAR, "=")
                    )
                )
                rows.add(
                    listOf(
                        KeyModel(KeyType.SYMBOLS_TOGGLE, "ABC", weight = 1.25f),
                        KeyModel(KeyType.CHAR, "(", weight = 0.85f),
                        KeyModel(KeyType.SPACE, "Space", weight = 2.0f)
                    )
                )
            }
        }
        return rows
    }

    fun getRightPanelRows(
        mode: KeyboardMode,
        showNumberRow: Boolean
    ): List<List<KeyModel>> {
        val rows = mutableListOf<List<KeyModel>>()

        if (showNumberRow && mode == KeyboardMode.LETTERS) {
            rows.add(
                listOf(
                    KeyModel(KeyType.CHAR, "6"),
                    KeyModel(KeyType.CHAR, "7"),
                    KeyModel(KeyType.CHAR, "8"),
                    KeyModel(KeyType.CHAR, "9"),
                    KeyModel(KeyType.CHAR, "0")
                )
            )
        }

        when (mode) {
            KeyboardMode.LETTERS -> {
                rows.add(
                    listOf(
                        KeyModel(KeyType.CHAR, "y", altHint = "6"),
                        KeyModel(KeyType.CHAR, "u", altHint = "7"),
                        KeyModel(KeyType.CHAR, "i", altHint = "8"),
                        KeyModel(KeyType.CHAR, "o", altHint = "9"),
                        KeyModel(KeyType.CHAR, "p", altHint = "0")
                    )
                )
                rows.add(
                    listOf(
                        KeyModel(KeyType.CHAR, "h"),
                        KeyModel(KeyType.CHAR, "j"),
                        KeyModel(KeyType.CHAR, "k"),
                        KeyModel(KeyType.CHAR, "l"),
                        KeyModel(KeyType.BACKSPACE, "⌫", weight = 1.25f)
                    )
                )
                rows.add(
                    listOf(
                        KeyModel(KeyType.CHAR, "b"),
                        KeyModel(KeyType.CHAR, "n"),
                        KeyModel(KeyType.CHAR, "m"),
                        KeyModel(KeyType.CHAR, ".", weight = 0.85f),
                        KeyModel(KeyType.ENTER, "⏎", weight = 1.25f)
                    )
                )
                rows.add(
                    listOf(
                        KeyModel(KeyType.SPACE, "Space", weight = 2.0f),
                        KeyModel(KeyType.SETTINGS, "⚙", weight = 0.85f),
                        KeyModel(KeyType.HIDE_KEYBOARD, "⌄", weight = 1.15f)
                    )
                )
            }
            KeyboardMode.SYMBOLS -> {
                rows.add(
                    listOf(
                        KeyModel(KeyType.CHAR, "6"),
                        KeyModel(KeyType.CHAR, "7"),
                        KeyModel(KeyType.CHAR, "8"),
                        KeyModel(KeyType.CHAR, "9"),
                        KeyModel(KeyType.CHAR, "0")
                    )
                )
                rows.add(
                    listOf(
                        KeyModel(KeyType.CHAR, "_"),
                        KeyModel(KeyType.CHAR, "/"),
                        KeyModel(KeyType.CHAR, ":"),
                        KeyModel(KeyType.CHAR, ";"),
                        KeyModel(KeyType.BACKSPACE, "⌫", weight = 1.25f)
                    )
                )
                rows.add(
                    listOf(
                        KeyModel(KeyType.CHAR, "\""),
                        KeyModel(KeyType.CHAR, "'"),
                        KeyModel(KeyType.CHAR, "!"),
                        KeyModel(KeyType.CHAR, "?", weight = 0.85f),
                        KeyModel(KeyType.ENTER, "⏎", weight = 1.25f)
                    )
                )
                rows.add(
                    listOf(
                        KeyModel(KeyType.SPACE, "Space", weight = 2.0f),
                        KeyModel(KeyType.CHAR, ")", weight = 0.85f),
                        KeyModel(KeyType.HIDE_KEYBOARD, "⌄", weight = 1.15f)
                    )
                )
            }
        }
        return rows
    }
}
