# Wheeler

Code and 3D files for the Wheeler device.

Paper: [Wheeler: A three-wheeled input device for usable, efficient, and versatile non-visual interaction](https://dl.acm.org/doi/abs/10.1145/3654777.3676396)

## Folders

| Folder | What it is |
|---|---|
| `src/main/java/pcauto/` | PCAuto, the PC app that talks to the device |
| `jars/` | Libraries PCAuto needs |
| `Wheeler_Firmware/wheeler_final_w_debounce/` | Latest firmware |
| `vs studio/HelloWord2/` | Word add-in source |
| `vs studio/pub3/` | Word add-in installer |
| `3D/` | 3D files for the device |

## PCAuto

Main class: `src/main/java/pcauto/PCAuto.java`. Needs Java 21 or newer.

### Run from IntelliJ

1. Open the `PCAuto` folder. It loads as a Maven project; `pom.xml` points to the jars in `jars/`.
2. Right-click `src/main/java/pcauto/PCAuto.java` and choose Run 'PCAuto.main()'.

### Run from the command line

macOS / Linux:

```
mkdir -p out/classes
javac -d out/classes -cp "jars/*" $(find src/main/java -name "*.java")
java --enable-native-access=ALL-UNNAMED -cp "out/classes:jars/*" pcauto.PCAuto
```

On Windows, use `;` instead of `:` in the classpath: `-cp "out/classes;jars/*"`.

On macOS, give the app that runs Java (Terminal or IntelliJ) Accessibility permission in System Settings > Privacy & Security > Accessibility. Without it the keyboard shortcut does not work.

### Use

1. Plug in the device.
2. Home tab: pick the port (shown as `Wheeler (...)`) and click `connect`.
3. Pick a mode:
   - Mouse mode: tick `Use as Mouse` in the Mouse tab. Wheel 1 moves the cursor up/down, wheel 2 left/right, wheel 3 changes speed. Button 1 is left click, button 2 is right click.
   - Keyboard mode: `Use as Mouse` off. Wheels and buttons press the keys set in the Keyboard tab.
   - Word mode: `Use as Mouse` off and the Word add-in running. Wheels move through the Word ribbon (see below).
4. Cmd+M switches between mouse and keyboard mode.

Button gestures (both modes, set in the Keyboard tab):

- Hold a button for 1 second: turns its hold key on, hold again to turn it off. Default: button 1 = Ctrl+Option (VoiceOver), button 2 = Shift.
- Press both buttons: holds Shift+Tab until released.

## Firmware

Latest firmware: `Wheeler_Firmware/wheeler_final_w_debounce/wheeler_final_w_debounce.ino`

Upload it with the Arduino IDE:

1. Open `wheeler_final_w_debounce.ino`.
2. Install the `Mouse` and `Keyboard` libraries (Library Manager) if they are missing.
3. Board: Arduino Leonardo. Pick the device's port.
4. Close PCAuto and the Serial Monitor (they hold the port), then click Upload.

Wheel direction, long press time and haptic length are set with `#define`s at the top of the file (`ENC1_DIR`, `LONG_PRESS_MS`, `HAPTIC_MS`).

The previous firmware is in `Wheeler_Firmware/wheeler_final_w_debounce_backup/`.

## Word add-in

Windows only (Word for Windows).

- Source: `vs studio/HelloWord2/` (open `HelloWord2.sln` in Visual Studio)
- Installer: `vs studio/pub3/setup.exe`

To launch:

1. Install the add-in by running `vs studio/pub3/setup.exe`.
2. Start PCAuto on the same PC and connect the device.
3. Turn `Use as Mouse` off.
4. Open Word. The add-in connects to PCAuto on port 4455.

Controls in Word:

- Wheel 3: ribbon tabs
- Wheel 2: groups in the tab
- Wheel 1: buttons in the group
- Button 1: press the selected button
- Button 2: Esc

The first button 1 press after Word starts opens the add-in's form.

## 3D files

`3D/`

- `3D/device.stl`: full device
- `3D/parts_7.stl`: the seven parts in one file
- `3D/rotor/parts/`: the parts as separate files (`1_wheel_base.stl`, `2_box.stl`, `3_rotor_head_full_gear.stl`, `4_bar_con.stl`, `5_curve_base.stl`, `6_button_2_small.stl`, `7_top_modify.stl`)
- `.scad` files are the OpenSCAD sources, `blender/` has the Blender files
