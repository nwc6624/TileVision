# TileVision AR Regression Test Checklist

## Test Environment
- **Device**: [Enter device model]
- **OS Version**: [Enter Android version]
- **Build**: [Enter build version/date]
- **Tester**: [Enter name]
- **Date**: [Enter test date]

---

## 1. AR Polygon Measurement - Basic Functionality

### ✅ Test 1.1: Minimum Valid Polygon (3 Vertices)
- [ ] Place exactly 3 vertices on a flat floor surface
- [ ] **Expected**: Continue button enabled
- [ ] **Expected**: Area displays correctly
- [ ] **Expected**: No validation errors

### ✅ Test 1.2: Rectangle Polygon (4 Vertices)
- [ ] Place 4 vertices forming a rectangle on flat floor
- [ ] **Expected**: Continue button enabled
- [ ] **Expected**: Area displays correctly (check: width × height)
- [ ] **Expected**: Polygon renders correctly in AR

### ✅ Test 1.3: Complex Polygon (8 Vertices)
- [ ] Place 8 vertices in a complex shape on flat floor
- [ ] **Expected**: Continue button enabled
- [ ] **Expected**: Area computes correctly
- [ ] **Expected**: All edges render properly
- [ ] **Expected**: No performance issues

---

## 2. AR Polygon Measurement - Irregular Shapes

### ✅ Test 2.1: Concave Polygon
- [ ] Place vertices forming a concave (bow-tie or star) shape
- [ ] **Expected**: Area computes correctly
- [ ] **Expected**: Continue button enabled
- [ ] **Expected**: Polygon renders without self-intersection issues
- [ ] **Expected**: No validation errors

### ✅ Test 2.2: Convex Polygon
- [ ] Place vertices forming a convex shape
- [ ] **Expected**: Area computes correctly
- [ ] **Expected**: Continue button enabled
- [ ] **Expected**: All edges render properly

---

## 3. AR Polygon Measurement - Edge Cases

### ✅ Test 3.1: Incomplete Polygon (2 Points Only)
- [ ] Place only 2 vertices
- [ ] **Expected**: Continue button **DISABLED** (60% opacity)
- [ ] **Expected**: Hint shows: "Add 1 more point to close a surface"
- [ ] **Expected**: No area displayed

### ✅ Test 3.2: Single Point
- [ ] Place only 1 vertex
- [ ] **Expected**: Continue button **DISABLED**
- [ ] **Expected**: No area displayed

### ✅ Test 3.3: No Points
- [ ] Start fresh measurement (0 vertices)
- [ ] **Expected**: Continue button **DISABLED**
- [ ] **Expected**: Undo button hidden
- [ ] **Expected**: Area bubble hidden

---

## 4. Flashlight Functionality

### ✅ Test 4.1: Flashlight Toggle - Main Measurement
- [ ] Open MeasureActivity (AR polygon mode)
- [ ] Tap flashlight button
- [ ] **Expected**: Flashlight turns ON
- [ ] **Expected**: Icon changes to "on" state
- [ ] Tap flashlight button again
- [ ] **Expected**: Flashlight turns OFF
- [ ] **Expected**: Icon changes to "off" state

### ✅ Test 4.2: Flashlight Auto-Off on Pause - Main Measurement
- [ ] Turn flashlight ON in MeasureActivity
- [ ] Press Home button (app goes to background)
- [ ] **Expected**: Flashlight turns OFF automatically
- [ ] Return to app
- [ ] **Expected**: Flashlight remains OFF
- [ ] **Expected**: Icon shows "off" state

### ✅ Test 4.3: Flashlight Toggle - Tile Sample
- [ ] Open TileSampleMeasureActivity
- [ ] Tap flashlight button
- [ ] **Expected**: Flashlight turns ON
- [ ] **Expected**: Icon changes to "on" state
- [ ] Tap flashlight button again
- [ ] **Expected**: Flashlight turns OFF
- [ ] **Expected**: Icon changes to "off" state

### ✅ Test 4.4: Flashlight Auto-Off on Pause - Tile Sample
- [ ] Turn flashlight ON in TileSampleMeasureActivity
- [ ] Press Home button (app goes to background)
- [ ] **Expected**: Flashlight turns OFF automatically
- [ ] Return to app
- [ ] **Expected**: Flashlight remains OFF
- [ ] **Expected**: Icon shows "off" state

---

## 5. Debug Visuals

### ✅ Test 5.1: No Debug Lines in Polygon Mode
- [ ] Measure area in polygon mode
- [ ] **Expected**: **NO** vertical blue debug lines
- [ ] **Expected**: **NO** plane grid visible
- [ ] **Expected**: **NO** feature points visible
- [ ] **Expected**: Only polygon edges + fill visible

### ✅ Test 5.2: Clean Rendering
- [ ] Place vertices on floor
- [ ] **Expected**: Edges connect smoothly vertex-to-vertex
- [ ] **Expected**: No visual artifacts
- [ ] **Expected**: Edges lie flat on plane (not vertical)
- [ ] **Expected**: Fill renders semi-transparent (alpha ~0.25)

---

## 6. Save & Continue Flow

### ✅ Test 6.1: Continue Without Save
- [ ] Place valid polygon (3+ vertices)
- [ ] Tap "Use" button (Continue)
- [ ] **Expected**: Navigates to TileCalculatorActivity
- [ ] **Expected**: Area pre-filled in calculator
- [ ] **Expected**: Polygon state not saved to projects

### ✅ Test 6.2: Save Then Continue
- [ ] Place valid polygon (3+ vertices)
- [ ] Tap "Save" button
- [ ] **Expected**: Snackbar shows "Measurement saved"
- [ ] **Expected**: Polygons remains visible
- [ ] **Expected**: Continue button still enabled
- [ ] Tap "Use" button
- [ ] **Expected**: Navigates to TileCalculatorActivity
- [ ] **Expected**: Area pre-filled correctly
- [ ] Open SavedProjectsActivity
- [ ] **Expected**: Measurement appears in list

### ✅ Test 6.3: Undo Last Point
- [ ] Place 3+ vertices
- [ ] Tap "Undo" button
- [ ] **Expected**: Last vertex removed
- [ ] **Expected**: Continue button disabled if <3 vertices
- [ ] **Expected**: Area updates correctly
- [ ] **Expected**: Haptic feedback on tap

### ✅ Test 6.4: Reset Measurement
- [ ] Place multiple vertices
- [ ] Long-press or tap reset option
- [ ] **Expected**: All vertices cleared
- [ ] **Expected**: Continue/Save buttons disabled
- [ ] **Expected**: Area bubble hidden
- [ ] **Expected**: Plane unlocked

---

## 7. Units Consistency

### ✅ Test 7.1: Imperial Units Display
- [ ] Set units to Imperial in Settings
- [ ] Measure polygon area
- [ ] **Expected**: Area bubble shows ft²
- [ ] **Expected**: Bottom panel big number shows ft²
- [ ] **Expected**: Values match exactly (no discrepancies)

### ✅ Test 7.2: Metric Units Display
- [ ] Set units to Metric in Settings
- [ ] Measure polygon area
- [ ] **Expected**: Area bubble shows m²
- [ ] **Expected**: Bottom panel big number shows m²
- [ ] **Expected**: Values match exactly (no discrepancies)

### ✅ Test 7.3: Units Switch Consistency
- [ ] Measure area in Imperial (ft²)
- [ ] Note the displayed value
- [ ] Switch to Metric in Settings
- [ ] Return to measurement
- [ ] **Expected**: Value converted correctly
- [ ] **Expected**: Units changed to m²
- [ ] **Expected**: All UI elements show same value

### ✅ Test 7.4: Calculator Pre-fill Consistency
- [ ] Measure area in Imperial
- [ ] Tap Continue
- [ ] **Expected**: Calculator shows correct ft² value
- [ ] Go back, measure same area in Metric
- [ ] Tap Continue
- [ ] **Expected**: Calculator shows correct m² value
- [ ] **Expected**: Values are consistent

---

## 8. Visual Polish & UX

### ✅ Test 8.1: Button States
- [ ] Before placing points
- [ ] **Expected**: Continue/Save buttons disabled (60% opacity)
- [ ] **Expected**: No toast messages on disabled tap
- [ ] Place valid polygon
- [ ] **Expected**: Continue/Save buttons enabled (100% opacity)
- [ ] **Expected**: Haptic feedback on tap

### ✅ Test 8.2: Area Display
- [ ] Place valid polygon
- [ ] **Expected**: Area bubble shows compact format
- [ ] **Expected**: Bottom panel shows large number
- [ ] **Expected**: Both show same value + unit

### ✅ Test 8.3: Disabled State Visual
- [ ] Place 2 points (invalid)
- [ ] **Expected**: Continue/Save at 60% opacity
- [ ] **Expected**: Hint text: "Add 1 more point to close a surface"
- [ ] **Expected**: No toast on disabled button tap

### ✅ Test 8.4: Haptic Feedback
- [ ] Place vertex
- [ ] **Expected**: Short buzz (15ms) on tap
- [ ] Complete valid polygon
- [ ] **Expected**: Longer buzz (30ms) when polygon becomes valid
- [ ] Tap Undo
- [ ] **Expected**: Haptic feedback

---

## 9. Start Up Flow

### ✅ Test 9.1: First Launch
- [ ] Clear app data
- [ ] Launch app
- [ ] **Expected**: Splash screen shows
- [ ] **Expected**: DisclaimerActivity shows
- [ ] **Expected**: "Show this at startup" checkbox checked
- [ ] Tap Continue
- [ ] **Expected**: Navigates to HomeActivity

### ✅ Test 9.2: Disclaimer Disabled
- [ ] Disable disclaimer in Settings
- [ ] Force stop app
- [ ] Launch app
- [ ] **Expected**: Goes directly to HomeActivity (no disclaimer)
- [ ] No SplashActivity visible after launch

### ✅ Test 9.3: Disclaimer with Back Button
- [ ] Enable disclaimer in Settings
- [ ] Launch app
- [ ] Tap back button on disclaimer
- [ ] **Expected**: Navigates to HomeActivity
- [ ] **Expected**: Disclaimer dismissed correctly

---

## 10. Edge Rendering & Plane Projection

### ✅ Test 10.1: Edges on Flat Plane
- [ ] Place vertices on flat floor
- [ ] **Expected**: Edges lie flat on plane
- [ ] **Expected**: No vertical lines
- [ ] **Expected**: Edges connect smoothly

### ✅ Test 10.2: Edges on Angled Plane
- [ ] Place vertices on angled surface
- [ ] **Expected**: Edges follow plane geometry
- [ ] **Expected**: No vertical projections
- [ ] **Expected**: Area computes correctly

### ✅ Test 10.3: Edge Thickness
- [ ] Place vertices and observe edges
- [ ] **Expected**: Edges ~12mm thick (3dp)
- [ ] **Expected**: Visible but not too thick

### ✅ Test 10.4: Fill Transparency
- [ ] Place valid polygon (3+ vertices)
- [ ] **Expected**: Fill renders with alpha ~0.25
- [ ] **Expected**: Floor still visible through fill

---

## 11. Regression Tests

### ✅ Test 11.1: No Crashes
- [ ] Rapidly tap to place multiple vertices
- [ ] **Expected**: No crashes
- [ ] **Expected**: No ANR

### ✅ Test 11.2: Memory Leaks
- [ ] Open/close AR measurement multiple times
- [ ] **Expected**: No memory leaks
- [ ] **Expected**: Performance remains stable

### ✅ Test 11.3: Rotation Handling
- [ ] Place vertices
- [ ] Rotate device
- [ ] **Expected**: AR tracking continues smoothly
- [ ] **Expected**: Polygon renders correctly in new orientation

---

## Test Results Summary

**Total Tests**: 42  
**Passed**: [Enter count]  
**Failed**: [Enter count]  
**Blocked**: [Enter count]  

### Critical Issues Found:
1. [List any critical issues]

### Minor Issues Found:
1. [List any minor issues]

### Notes:
[Any additional observations or comments]

---

## Sign-Off
- **Tester Signature**: _________________
- **Date**: _________________
- **Ready for Release**: ☐ Yes  ☐ No

