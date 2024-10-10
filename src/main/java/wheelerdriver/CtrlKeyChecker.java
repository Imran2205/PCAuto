package wheelerdriver;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class CtrlKeyChecker {
    private static boolean ctrlPressed = false;

    public static void initialize() {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(e.getMessage());
            System.exit(1);
        }

        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                if (e.getKeyCode() == NativeKeyEvent.VC_CONTROL) {
                    ctrlPressed = true;
                }
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent e) {
                if (e.getKeyCode() == NativeKeyEvent.VC_CONTROL) {
                    ctrlPressed = false;
                }
            }

            @Override
            public void nativeKeyTyped(NativeKeyEvent e) {
                // Not used
            }
        });
    }

    public static boolean isCtrlPressed() {
        return ctrlPressed;
    }
}