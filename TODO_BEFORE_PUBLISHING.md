# TODO: Before Publishing to Google Play Store

## 🚨 CRITICAL (Must Fix)

### 1. AR Activity Exports & Security
- **ISSUE**: `MeasureActivity` is exported=true when it shouldn't be
  - Line 40 in AndroidManifest.xml
  - Only activities that need to be launched by external apps should be exported
  - **Fix**: Change to `android:exported="false"`
- **Fix**: Only `HomeActivity` (launcher) should be exported=true

### 2. ProGuard Rules for ARCore/Sceneform
- **ISSUE**: Current ProGuard rules are minimal
- **Needed**: Add rules to prevent ARCore/Sceneform from being stripped
- **Add to proguard-rules.pro**:
```proguard
# ARCore
-keep class com.google.ar.core.** { *; }
-dontwarn com.google.ar.core.**

# Sceneform
-keep class com.google.ar.sceneform.** { *; }
-keep class com.google.ar.sceneform.animation.** { *; }
-keep class com.google.ar.sceneform.rendering.** { *; }

# Keep custom classes
-keep class de.westnordost.streetmeasure.** { *; }
-keep class * implements com.google.ar.core.Anchor { *; }
```

### 3. Build Configuration
- **ISSUE**: `isShrinkResources = false` in release build
- **Current**: Line 38 in app/build.gradle.kts
- **Fix**: Change to `isShrinkResources = true` for smaller APK

### 4. Version Info Mismatch
- **ISSUE**: Version mismatch between build.gradle and AndroidManifest
  - build.gradle: versionCode=6, versionName="1.5"
  - AndroidManifest: versionName="1.0.0", versionCode="1"
- **Fix**: Use only build.gradle version, remove from AndroidManifest

### 5. Test Release Build
- **CRITICAL**: Test a release build with minify and shrink enabled
- Run: `./gradlew assembleRelease`
- Install: `adb install app/build/outputs/apk/release/app-release.apk`
- **Test all features** especially AR measurement

---

## ⚠️ IMPORTANT (Should Fix)

### 6. Privacy Policy
- Create a proper privacy policy HTML page
- Current: Just a placeholder file
- **Required by Google Play** for apps that handle user data
- Should state what data is collected (none, everything is local)
- Add link in Settings

### 7. Error Handling & User Feedback
- Add try-catch around AR hit testing (already done in some places)
- Add Toast messages when AR tracking fails
- Add "Skip AR" button visibility handling (already done)
- Add haptic feedback on successful measurements

### 8. App Icon Quality
- Ensure all density versions are provided
- Check adaptive icon works correctly
- Test on various Android versions

### 9. Content Descriptions
- Check all ImageButtons have contentDescription
- Test with TalkBack enabled

### 10. Performance
- Test on low-end devices (if possible)
- Monitor memory usage during AR measurements
- Check for memory leaks in AR activities

---

## ✅ OPTIONAL (Nice to Have)

### 11. Analytics/Crash Reporting
- Consider adding Firebase Crashlytics for crash reporting
- Or use open source Sentry
- Helpful for monitoring user issues

### 12. Beta Testing
- Use Google Play Console internal testing track
- Invite testers before public release
- Collect feedback on real devices

### 13. Screenshots & Store Listing
- Take screenshots for 5" phone (1080x1920)
- Take screenshots for 7" tablet (1024x600)
- Write compelling app description (already in README)
- Add feature graphics (1024x500)

### 14. App Video
- Record demo video showing AR measurement
- Upload to YouTube, embed in Play Store

### 15. Accessibility
- Test with screen readers
- Ensure high contrast mode works
- Check text sizes are readable

---

## 📋 Testing Checklist

### Functional Testing
- [ ] Measure project area with AR
- [ ] Measure tile sample with AR
- [ ] Save project measurement
- [ ] Save tile sample
- [ ] Open calculator with saved data
- [ ] Calculate tiles needed
- [ ] Save job summary
- [ ] View saved jobs
- [ ] Delete job summary
- [ ] Export PDF
- [ ] Share PDF
- [ ] Change theme (light/dark/system)
- [ ] Delete all data

### AR-Specific Testing
- [ ] Test with good lighting
- [ ] Test with poor lighting
- [ ] Test with moving device
- [ ] Test on different surface types
- [ ] Test ARCore plane detection
- [ ] Test "Skip AR" button

### Edge Cases
- [ ] App killed by system, reopened
- [ ] Measure with no ARCore support (should show error)
- [ ] Calculate with zero area
- [ ] Delete last remaining item
- [ ] Rotate device during measurement
- [ ] Background app, return later

### Performance
- [ ] App startup time
- [ ] AR session initialization
- [ ] Memory usage during long session
- [ ] Battery drain monitoring

---

## 🔧 Quick Fixes Needed

1. **Change exported=false for MeasureActivity** (5 min)
2. **Fix ProGuard rules** (10 min)
3. **Enable shrinkResources** (1 min)
4. **Remove duplicate version info** (2 min)
5. **Test release build** (30 min)

**Total: ~1 hour for critical fixes**

---

## 📦 Final Release Steps

1. ✅ Complete critical fixes above
2. ✅ Run full testing checklist
3. ✅ Create release build: `./gradlew assembleRelease`
4. ✅ Test release build thoroughly
5. ✅ Sign APK/AAB (if not using Play App Signing)
6. ✅ Upload to Google Play Console
7. ✅ Add screenshots and description
8. ✅ Complete content rating questionnaire
9. ✅ Set pricing and availability
10. ✅ Submit for review
11. 🎉 Wait for approval (usually 1-3 days)
