# Split Keyboard for Android Tablet

A native floating split touch keyboard built for Android tablets, featuring dual thumb-typing panels and a **100% touch-transparent center gap**.

---

## Direct APK Download from GitHub

Once you push this repository to your GitHub account:

### Method 1: Download from GitHub Releases (Recommended)
1. Go to your repository on GitHub using your tablet browser (e.g. Chrome).
2. Tap **Releases** on the right side of the repository page (or navigate to `https://github.com/<your-username>/<repo-name>/releases`).
3. Tap **`SplitKeyboard-debug.apk`** under Assets to download it directly to your tablet.
4. Tap the downloaded file in your notifications or Downloads folder to install!

### Method 2: Download from GitHub Actions
1. On GitHub, tap the **Actions** tab at the top.
2. Under "Workflows", tap **Build and Release Split Keyboard APK**.
3. Tap the latest workflow run (marked with a green checkmark).
4. Scroll down to the **Artifacts** section at the bottom.
5. Tap **`SplitKeyboard-debug-apk`** to download the ZIP file containing the APK directly to your tablet.

---

## How to Push from AI Studio to GitHub

You can export or push this complete project directly to your GitHub account from Google AI Studio:
1. In the top-right toolbar of AI Studio Build, tap the **Share / Export** menu (or Project Settings).
2. Select **"Push to GitHub"** (or **"Export to GitHub"**).
3. Connect your GitHub account and specify your desired repository name (e.g., `split-keyboard-tablet`).
4. Once pushed, the included `.github/workflows/build-apk.yml` workflow will automatically run and build your APK on GitHub's cloud servers!

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

If building locally via command line:
```bash
./gradlew assembleDebug
```
The APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`
