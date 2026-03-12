# Accessibility

## Overview

The application supports Android accessibility services including TalkBack, Switch Access, and Voice Access. All interactive elements are accessible and all status changes are announced.

## Features

### Content Descriptions

Every interactive element and meaningful icon has a content description:

| Component | Element | Description Source |
|-----------|---------|-------------------|
| MainActivity | About icon button | String resource `R.string.about` |
| MainActivity | History button icon | String resource `R.string.history` |
| MainActivity | Dev menu button icon | String resource `R.string.dev_generate_sample_pdf` |
| StatusDisplay | Status cards | Dynamic description reflecting current state |
| SignButton | Loading spinner | "Signing in progress" |
| SigningHistorySheet | Document icon | String resource `R.string.history` |
| SigningHistorySheet | Verify button icon | String resource `R.string.verify` |
| SigningHistorySheet | Verification banner icon | Dynamic description matching banner text |
| PermissionRationaleDialog | Folder icon | String resource `R.string.permission_rationale_title` |

Icons that are purely decorative within elements that already carry semantic meaning (e.g., icons inside buttons with visible text labels) follow Material Design guidance and may use `contentDescription = null` to avoid redundant announcements.

### Heading Semantics

Section headings use `semantics { heading() }` so TalkBack users can navigate between sections using swipe gestures:

- AboutSheet: App name heading, "Open-Source Licenses" heading, "Developer" heading
- SigningHistorySheet: "Signing History" heading

### Live Regions

`StatusDisplay` uses `liveRegion = LiveRegionMode.Polite` on the status card. When the signing state changes (e.g., from "File selected" to "Signing in progress" to "Signature successful"), TalkBack announces the new status without requiring the user to navigate to the element.

### Touch Targets

All interactive elements meet the Material Design 3 minimum touch target of 48dp:

| Element | Size |
|---------|------|
| FilePickerButton | 56dp height, full width |
| SignButton | 56dp height, full width |
| History button | 48dp height (OutlinedButton default), full width |
| IconButton (About) | 48dp (Material3 default) |
| TextButton (links in AboutSheet) | 48dp minimum height (Material3 enforced) |

### Theme Support

| Feature | Status |
|---------|--------|
| Light theme | Supported (custom LightColorScheme) |
| Dark theme | Supported (custom DarkColorScheme, follows system setting) |
| Dynamic color (Material You) | Supported on Android 12+ |
| RTL layout | Supported (`android:supportsRtl="true"`) |
| Font scaling | Supported (Material3 typography scales with system font size) |

### High Contrast

The application uses Material Design 3 color roles that maintain sufficient contrast ratios in both light and dark themes. The custom color schemes were selected with WCAG AA contrast compliance in mind:

- Light theme: Primary (#1976D2) on white background provides 4.5:1+ contrast
- Dark theme: Primary (#90CAF9) on dark background (#121212) provides 4.5:1+ contrast
- Error states use dedicated error color pairs with high contrast

Android 14+ "high contrast text" system setting is respected automatically by the Compose framework.

## Testing Accessibility

Manual testing with TalkBack:
1. Enable TalkBack in device Settings
2. Navigate through the main screen using swipe gestures
3. Verify all buttons announce their purpose
4. Sign a file and verify status changes are announced
5. Open About sheet and verify heading navigation works
6. Verify all links are announced as buttons

Automated testing:
- `testTag` modifiers are applied to key elements for `onNodeWithTag()` assertions
- `AboutSheetTest.kt` verifies content rendering and click callbacks
- `ButtonAlignmentTest.kt` verifies touch target sizes
