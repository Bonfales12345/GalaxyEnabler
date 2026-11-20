# Nowbar Fix

Even though Now Brief shows up in the settings menu, it never actually shows up on the lock screen Now Bar.

## Why does this happen?

The Now Bar on the lock screen is controlled by `AODService`. To show Now Brief cards, it tries to talk to the new SmartSuggestions app (`7.3.30.9`) from the S26.

The problem is that this new SmartSuggestions app asks for a special permission (`BIND_GENUI_SERVICE`). Because the S23 Ultra came with an older version, its system files do not have this permission. So Android blocks it and gives a permission error.

## How to fix it

We need to make a small bridge app that gives the missing permission to the system.

1. **Copy `AODService` from your phone to your PC:**

```bash
adb pull /system/priv-app/AODService_v80/AODService_v80.apk
```

   *(Why? Because `AODService` and `SystemUI` are in the same system group. To add our bridge app to this group, we need to copy Samsung's signature from `AODService`.)*

2. **Create and install the bridge app:**
   Run the Python script **[tools/GenUIBridge.py](../tools/GenUIBridge.py)**. It copies the signature and builds `GenUIBridge.apk` with the missing permission. Install this APK on your phone.

3. **Update AlwaysOnDisplay:**
   Download and install **AlwaysOnDisplay `8.9.66.1`** from APKMirror (the exact version is important).

After that, the Now Bar restarts. Since it now has the permission it needed, it connects properly and the Now Brief card finally shows up on your lock screen.
