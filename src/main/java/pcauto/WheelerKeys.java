package pcauto;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Keyboard actions that can be assigned to the Wheeler wheels and buttons in keyboard mode.
 * Values are Arduino Keyboard library key codes; the device presses them over USB HID.
 */
public class WheelerKeys {

    static final int CTRL = 0x80, SHIFT = 0x81, OPTION = 0x82, COMMAND = 0x83;
    static final int UP = 0xDA, DOWN = 0xD9, LEFT = 0xD8, RIGHT = 0xD7;
    static final int TAB = 0xB3, RETURN = 0xB0, ESC = 0xB1, BACKSPACE = 0xB2, DELETE = 0xD4;
    static final int PAGE_UP = 0xD3, PAGE_DOWN = 0xD6, HOME = 0xD2, END = 0xD5, SPACE = ' ';

    public static final String NONE = "None";

    /** Action name -> key codes (modifiers first). Order is the order shown in the dropdowns. */
    public static final Map<String, int[]> ACTIONS = new LinkedHashMap<>();

    static {
        ACTIONS.put(NONE, new int[]{});
        ACTIONS.put("Up Arrow", new int[]{UP});
        ACTIONS.put("Down Arrow", new int[]{DOWN});
        ACTIONS.put("Left Arrow", new int[]{LEFT});
        ACTIONS.put("Right Arrow", new int[]{RIGHT});
        ACTIONS.put("Tab", new int[]{TAB});
        ACTIONS.put("Shift+Tab", new int[]{SHIFT, TAB});
        ACTIONS.put("Return", new int[]{RETURN});
        ACTIONS.put("Escape", new int[]{ESC});
        ACTIONS.put("Space", new int[]{SPACE});
        ACTIONS.put("Backspace", new int[]{BACKSPACE});
        ACTIONS.put("Delete", new int[]{DELETE});
        ACTIONS.put("Page Up", new int[]{PAGE_UP});
        ACTIONS.put("Page Down", new int[]{PAGE_DOWN});
        ACTIONS.put("Home", new int[]{HOME});
        ACTIONS.put("End", new int[]{END});
        ACTIONS.put("Command", new int[]{COMMAND});
        ACTIONS.put("Control", new int[]{CTRL});
        ACTIONS.put("Option", new int[]{OPTION});
        ACTIONS.put("Shift", new int[]{SHIFT});
        ACTIONS.put("Ctrl+Command", new int[]{CTRL, COMMAND});
        ACTIONS.put("Ctrl+Option (VoiceOver)", new int[]{CTRL, OPTION});
        ACTIONS.put("VO+Right", new int[]{CTRL, OPTION, RIGHT});
        ACTIONS.put("VO+Left", new int[]{CTRL, OPTION, LEFT});
        ACTIONS.put("VO+Space", new int[]{CTRL, OPTION, SPACE});
        ACTIONS.put("Command+Tab", new int[]{COMMAND, TAB});
    }

    /** One assignable slot: preference key, label in the UI, default action. */
    public static class Slot {
        public final String key, label, def;

        Slot(String key, String label, String def) {
            this.key = key;
            this.label = label;
            this.def = def;
        }
    }

    public static final Slot[] SLOTS = {
        new Slot("kb_w1_pos", "Wheel 1 up (+)", "Up Arrow"),
        new Slot("kb_w1_neg", "Wheel 1 down (-)", "Down Arrow"),
        new Slot("kb_w2_pos", "Wheel 2 up (+)", "Left Arrow"),
        new Slot("kb_w2_neg", "Wheel 2 down (-)", "Right Arrow"),
        new Slot("kb_w3_pos", "Wheel 3 up (+)", NONE),
        new Slot("kb_w3_neg", "Wheel 3 down (-)", NONE),
        new Slot("kb_b1_short", "Button 1 press", "Command"),
        new Slot("kb_b2_short", "Button 2 press", "Tab"),
        new Slot("kb_b1_long", "Button 1 long press (hold on/off)", "Ctrl+Option (VoiceOver)"),
        new Slot("kb_b2_long", "Button 2 long press (hold on/off)", "Shift"),
        new Slot("kb_both", "Both buttons (held while pressed)", "Shift+Tab"),
    };

    public static int[] codes(String action) {
        int[] c = ACTIONS.get(action);
        return c == null ? new int[]{} : c;
    }
}
