# Split Keyboard for Android Tablet

A native floating split touch keyboard built for Android tablets, featuring dual thumb-typing panels and a **100% touch-transparent center gap**.

---

## Direct APK Download (Fastest Method)

You can download the compiled APK directly from your GitHub repository without waiting for builds or navigating releases:

### 🚀 Direct Download Link:
In your tablet browser (e.g., Chrome), open:
```text
https://github.com/<YOUR-USERNAME>/<YOUR-REPO-NAME>/raw/main/release/SplitKeyboard-debug.apk
```
*(Replace `<YOUR-USERNAME>` and `<YOUR-REPO-NAME>` with your actual GitHub username and repository name).*

Tap this link on your tablet to immediately download `SplitKeyboard-debug.apk` directly to your Downloads folder!

---

### Alternative Download Methods

#### Method 1: From GitHub Releases
1. In your tablet browser, navigate to:
   `https://github.com/<YOUR-USERNAME>/<YOUR-REPO-NAME>/releases`
2. Under **Assets**, tap **`SplitKeyboard-debug.apk`**.

#### Method 2: From GitHub Actions Artifacts
1. On GitHub, tap the **Actions** tab at the top.
2. Tap the latest workflow run: **"Build and Release Split Keyboard APK"**.
3. Scroll to the bottom to the **Artifacts** section and tap **`SplitKeyboard-debug-apk`**.

---

## Tablet Quick Start & Activation

1. **Install APK:**
   - Open `SplitKeyboard-debug.apk` on your OnePlus tablet.
   - If prompted, allow "Install unknown apps" from your browser/file manager.
2. **Step 1 - Enable in Android Settings:**
   - Launch the Split Keyboard app.
   - Tap **"Enable in System Settings"** (or open *Settings → System → Languages & Input → On-screen keyboard*).
   - Switch **Split Keyboard (Tablet)** to **ON**.
3. **Step 2 - Select Active Keyboard:**
   - Tap **"Switch Active Keyboard"** and select **Split Keyboard (Tablet)**.
4. **Ergonomic Adjustment:**
   - Tap and drag the top headers (`⠇⠇ LEFT` / `RIGHT ⠇⠇`) up or down to align the thumb panels with your natural hand grip.

---

## Architecture & Touch-Through Technology

This keyboard is implemented as a native Android `InputMethodService`:
- **`TOUCHABLE_INSETS_REGION`:** The window manager is provided with the exact union of the left and right card bounding boxes. Any touch event occurring in the center falls completely through to the underlying application (browser, code editor, notes, canvas).
- **Full Focus Fidelity:** Because it is an authentic IME rather than a floating window overlay (`SYSTEM_ALERT_WINDOW`), the active app retains text cursor focus and standard `InputConnection` text editing without requiring invasive accessibility permissions.
- **Landscape Extract Prevention:** `onEvaluateFullscreenMode()` returns `false`, preventing Android from obscuring the screen with a fullscreen extract box in tablet landscape.

---

## Manual Local Build

```bash
./gradlew assembleDebug
```
The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.
