# Release Build Guide

This guide explains how to configure and build a signed release APK for the Trainer App.

## Prerequisites

- Android SDK installed
- JDK 17 or newer
- `keytool` utility (included with JDK)

## Step 1: Generate a Keystore

A keystore is required to sign your release APK. Generate one using the following command:

```bash
# Create keystore directory
mkdir -p keystore

# Generate keystore (change the values as needed)
keytool -genkey -v \
  -keystore keystore/trainerapp-release.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias trainerapp
```

You will be prompted for:
- **Keystore password**: Choose a strong password (remember this!)
- **Key password**: Can be the same as keystore password
- **Distinguished Name fields**: Your name, organization, etc.

### Important Security Notes

⚠️ **Keep these files secure and backed up:**
- `keystore/trainerapp-release.jks` - The keystore file
- Keystore password and key password

❌ **Never commit these to version control!**
- The keystore file is already in `.gitignore`
- Store passwords in a secure password manager

## Step 2: Configure Signing Properties

Copy the template and fill in your values:

```bash
cp keystore.properties.template keystore.properties
```

Edit `keystore.properties`:

```properties
storePassword=YOUR_KEYSTORE_PASSWORD_HERE
keyPassword=YOUR_KEY_PASSWORD_HERE
keyAlias=trainerapp
storeFile=../keystore/trainerapp-release.jks
```

⚠️ **This file is in `.gitignore` and should never be committed!**

## Step 3: Build Release APK

### Using Gradle

```bash
# Build release APK (unsigned if no keystore)
./gradlew assembleRelease

# Build and sign release APK (if keystore configured)
./gradlew assembleRelease

# Build App Bundle for Play Store
./gradlew bundleRelease
```

### Using Android Studio

1. **Build → Generate Signed Bundle / APK**
2. Select **APK** or **Android App Bundle**
3. Choose your keystore file
4. Enter passwords
5. Select **release** build variant
6. Click **Finish**

## Step 4: Locate Build Outputs

After building, find your files here:

```
app/build/outputs/
├── apk/
│   ├── debug/
│   │   └── app-debug.apk
│   └── release/
│       └── app-release.apk
└── bundle/
    └── release/
        └── app-release.aab
```

## Build Variants

### Debug Build
- **Suffix**: `.debug` appended to package name
- **Version**: Has `-debug` suffix
- **Signing**: Signed with debug key (auto-generated)
- **Optimizations**: None (faster builds)
- **Use**: Development and testing

```bash
./gradlew assembleDebug
./gradlew installDebug  # Install on connected device
```

### Release Build
- **Suffix**: None (production package name)
- **Version**: Clean version number
- **Signing**: Signed with release key (if configured)
- **Optimizations**: ProGuard/R8 enabled (minified, optimized)
- **Use**: Production deployment

```bash
./gradlew assembleRelease
```

## ProGuard/R8 Configuration

The release build uses R8 for:
- **Code shrinking**: Removes unused code
- **Obfuscation**: Makes reverse engineering harder
- **Optimization**: Improves performance

ProGuard rules are in `app/proguard-rules.pro`.

### Testing Release Builds

Always test release builds before publishing:

```bash
# Install release APK
adb install app/build/outputs/apk/release/app-release.apk

# Or use Gradle
./gradlew installRelease
```

**Test thoroughly:**
- All features work correctly
- No crashes from R8 optimization
- Performance is good
- Database migrations work

## CI/CD Configuration

For automated builds, use environment variables:

```bash
# Set in CI/CD environment
export KEYSTORE_PASSWORD="your_password"
export KEY_PASSWORD="your_password"
export KEY_ALIAS="trainerapp"
export KEYSTORE_FILE="/path/to/keystore.jks"
```

Update `build.gradle.kts` to read from environment:

```kotlin
signingConfigs {
    create("release") {
        storeFile = file(System.getenv("KEYSTORE_FILE") ?: keystoreProperties["storeFile"])
        storePassword = System.getenv("KEYSTORE_PASSWORD") ?: keystoreProperties["storePassword"]
        keyAlias = System.getenv("KEY_ALIAS") ?: keystoreProperties["keyAlias"]
        keyPassword = System.getenv("KEY_PASSWORD") ?: keystoreProperties["keyPassword"]
    }
}
```

## Version Management

Update version for each release in `app/build.gradle.kts`:

```kotlin
defaultConfig {
    versionCode = 2      // Increment for each release
    versionName = "1.1"  // Semantic version
}
```

**Version Code Rules:**
- Must increment with each release
- Google Play requires higher version code for updates
- Never reuse a version code

**Version Name:**
- User-facing version string
- Use semantic versioning: MAJOR.MINOR.PATCH
- Example: `1.0.0`, `1.1.0`, `2.0.0`

## Troubleshooting

### "Keystore file not found"
- Check the `storeFile` path in `keystore.properties`
- Ensure the keystore file exists at that location
- Use relative path from project root

### "Incorrect keystore password"
- Verify password in `keystore.properties`
- Re-generate keystore if password is lost (⚠️ can't update existing app!)

### "Release APK not signed"
- Ensure `keystore.properties` exists
- Check that all properties are correctly set
- Verify keystore file permissions

### R8/ProGuard issues
- Check `app/proguard-rules.pro` for missing keep rules
- Test release build thoroughly
- Use `-printconfiguration` to debug rules

## Play Store Publishing

For Google Play Store submission:

1. **Build App Bundle** (preferred):
   ```bash
   ./gradlew bundleRelease
   ```

2. **Upload to Play Console**:
   - Go to Play Console
   - Select your app
   - Release → Production/Beta/Alpha
   - Upload `app-release.aab`

3. **Fill in store listing**:
   - Screenshots
   - Description
   - Privacy policy
   - Content rating

## Security Checklist

Before releasing:

- [ ] Keystore backed up securely
- [ ] Passwords stored in password manager
- [ ] `keystore.properties` NOT in git
- [ ] ProGuard rules reviewed
- [ ] No debug logs in release build
- [ ] No test API keys or credentials
- [ ] SSL certificate pinning (if applicable)
- [ ] Release APK tested thoroughly

## Useful Commands

```bash
# Check APK information
aapt dump badging app/build/outputs/apk/release/app-release.apk

# Verify APK signature
apksigner verify app/build/outputs/apk/release/app-release.apk

# Check APK size
ls -lh app/build/outputs/apk/release/app-release.apk

# Analyze APK contents
bundletool build-apks --bundle=app-release.aab --output=app.apks

# Clean build
./gradlew clean

# Build all variants
./gradlew assemble
```

## Resources

- [Android App Signing](https://developer.android.com/studio/publish/app-signing)
- [Configure Build Variants](https://developer.android.com/studio/build/build-variants)
- [Shrink, obfuscate, and optimize](https://developer.android.com/studio/build/shrink-code)
- [Play Console Help](https://support.google.com/googleplay/android-developer)

---

**Remember**: Never lose your keystore or passwords. Without them, you cannot update your app on Google Play!
