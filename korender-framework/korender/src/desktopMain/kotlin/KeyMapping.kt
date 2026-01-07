package com.zakgof.korender

import androidx.compose.ui.input.key.Key
import java.awt.event.KeyEvent.*

internal class KeyMapping(val key: String, val composeKey: Key)

internal val KEY_MAPPING: Map<Int, KeyMapping> = mapOf(

    // Letters
    VK_A to KeyMapping("A", Key.A),
    VK_B to KeyMapping("B", Key.B),
    VK_C to KeyMapping("C", Key.C),
    VK_D to KeyMapping("D", Key.D),
    VK_E to KeyMapping("E", Key.E),
    VK_F to KeyMapping("F", Key.F),
    VK_G to KeyMapping("G", Key.G),
    VK_H to KeyMapping("H", Key.H),
    VK_I to KeyMapping("I", Key.I),
    VK_J to KeyMapping("J", Key.J),
    VK_K to KeyMapping("K", Key.K),
    VK_L to KeyMapping("L", Key.L),
    VK_M to KeyMapping("M", Key.M),
    VK_N to KeyMapping("N", Key.N),
    VK_O to KeyMapping("O", Key.O),
    VK_P to KeyMapping("P", Key.P),
    VK_Q to KeyMapping("Q", Key.Q),
    VK_R to KeyMapping("R", Key.R),
    VK_S to KeyMapping("S", Key.S),
    VK_T to KeyMapping("T", Key.T),
    VK_U to KeyMapping("U", Key.U),
    VK_V to KeyMapping("V", Key.V),
    VK_W to KeyMapping("W", Key.W),
    VK_X to KeyMapping("X", Key.X),
    VK_Y to KeyMapping("Y", Key.Y),
    VK_Z to KeyMapping("Z", Key.Z),

    // Digits
    VK_0 to KeyMapping("0", Key.Zero),
    VK_1 to KeyMapping("1", Key.One),
    VK_2 to KeyMapping("2", Key.Two),
    VK_3 to KeyMapping("3", Key.Three),
    VK_4 to KeyMapping("4", Key.Four),
    VK_5 to KeyMapping("5", Key.Five),
    VK_6 to KeyMapping("6", Key.Six),
    VK_7 to KeyMapping("7", Key.Seven),
    VK_8 to KeyMapping("8", Key.Eight),
    VK_9 to KeyMapping("9", Key.Nine),

    // Function keys
    VK_F1 to KeyMapping("F1", Key.F1),
    VK_F2 to KeyMapping("F2", Key.F2),
    VK_F3 to KeyMapping("F3", Key.F3),
    VK_F4 to KeyMapping("F4", Key.F4),
    VK_F5 to KeyMapping("F5", Key.F5),
    VK_F6 to KeyMapping("F6", Key.F6),
    VK_F7 to KeyMapping("F7", Key.F7),
    VK_F8 to KeyMapping("F8", Key.F8),
    VK_F9 to KeyMapping("F9", Key.F9),
    VK_F10 to KeyMapping("F10", Key.F10),
    VK_F11 to KeyMapping("F11", Key.F11),
    VK_F12 to KeyMapping("F12", Key.F12),

    // Control & navigation
    VK_ENTER to KeyMapping("ENTER", Key.Enter),
    VK_ESCAPE to KeyMapping("ESCAPE", Key.Escape),
    VK_BACK_SPACE to KeyMapping("BACKSPACE", Key.Backspace),
    VK_TAB to KeyMapping("TAB", Key.Tab),
    VK_SPACE to KeyMapping("SPACE", Key.Spacebar),

    VK_INSERT to KeyMapping("INSERT", Key.Insert),
    VK_DELETE to KeyMapping("DELETE", Key.Delete),
    VK_HOME to KeyMapping("HOME", Key.Home),
    // VK_END to KeyMapping("END", Key.End), // TODO
    VK_PAGE_UP to KeyMapping("PAGEUP", Key.PageUp),
    VK_PAGE_DOWN to KeyMapping("PAGEDOWN", Key.PageDown),

    VK_LEFT to KeyMapping("LEFT", Key.DirectionLeft),
    VK_RIGHT to KeyMapping("RIGHT", Key.DirectionRight),
    VK_UP to KeyMapping("UP", Key.DirectionUp),
    VK_DOWN to KeyMapping("DOWN", Key.DirectionDown),

    // Modifiers
    VK_SHIFT to KeyMapping("SHIFT", Key.ShiftLeft),
    VK_CONTROL to KeyMapping("CONTROL", Key.CtrlLeft),
    VK_ALT to KeyMapping("ALT", Key.AltLeft),
    VK_META to KeyMapping("META", Key.MetaLeft),
    VK_CAPS_LOCK to KeyMapping("CAPSLOCK", Key.CapsLock),
    VK_NUM_LOCK to KeyMapping("NUMLOCK", Key.NumLock),
    VK_SCROLL_LOCK to KeyMapping("SCROLLLOCK", Key.ScrollLock),

    // Numpad
    VK_NUMPAD0 to KeyMapping("NUMPAD0", Key.NumPad0),
    VK_NUMPAD1 to KeyMapping("NUMPAD1", Key.NumPad1),
    VK_NUMPAD2 to KeyMapping("NUMPAD2", Key.NumPad2),
    VK_NUMPAD3 to KeyMapping("NUMPAD3", Key.NumPad3),
    VK_NUMPAD4 to KeyMapping("NUMPAD4", Key.NumPad4),
    VK_NUMPAD5 to KeyMapping("NUMPAD5", Key.NumPad5),
    VK_NUMPAD6 to KeyMapping("NUMPAD6", Key.NumPad6),
    VK_NUMPAD7 to KeyMapping("NUMPAD7", Key.NumPad7),
    VK_NUMPAD8 to KeyMapping("NUMPAD8", Key.NumPad8),
    VK_NUMPAD9 to KeyMapping("NUMPAD9", Key.NumPad9),

    VK_ADD to KeyMapping("NUMPADADD", Key.NumPadAdd),
    VK_SUBTRACT to KeyMapping("NUMPADSUBTRACT", Key.NumPadSubtract),
    VK_MULTIPLY to KeyMapping("NUMPADMULTIPLY", Key.NumPadMultiply),
    VK_DIVIDE to KeyMapping("NUMPADDIVIDE", Key.NumPadDivide),
    //   VK_DECIMAL to KeyMapping("NUMPADDECIMAL", Key.NumPadDecimal), // TODO

    // Punctuation
    VK_COMMA to KeyMapping(",", Key.Comma),
    VK_PERIOD to KeyMapping(".", Key.Period),
    VK_SLASH to KeyMapping("/", Key.Slash),
    VK_BACK_SLASH to KeyMapping("\\", Key.Backslash),
    VK_SEMICOLON to KeyMapping(";", Key.Semicolon),
    VK_EQUALS to KeyMapping("=", Key.Equals),
    VK_MINUS to KeyMapping("-", Key.Minus),
    VK_OPEN_BRACKET to KeyMapping("[", Key.LeftBracket),
    VK_CLOSE_BRACKET to KeyMapping("]", Key.RightBracket),
    VK_QUOTE to KeyMapping("'", Key.Apostrophe),
    VK_BACK_QUOTE to KeyMapping("BACKTICK", Key.Grave), // TODO
)
