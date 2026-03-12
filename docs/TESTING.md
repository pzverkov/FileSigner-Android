# Testing

## Test Strategy

Testing is organized in two tiers:

1. **Unit tests** (`app/src/test/`) -- fast, run on JVM, mock Android dependencies
2. **Instrumented tests** (`app/src/androidTest/`) -- run on device/emulator, test real Android components

## Running Tests

```bash
# All unit tests
./gradlew testDebugUnitTest

# All instrumented tests (requires emulator or connected device)
./gradlew connectedDebugAndroidTest

# Specific test class
./gradlew testDebugUnitTest --tests "com.filesigner.data.repository.SigningRepositoryImplTest"

# Specific instrumented test
./gradlew connectedDebugAndroidTest --tests "com.filesigner.ui.AboutSheetTest"
```

## Unit Tests

| Test File | Coverage Area |
|-----------|---------------|
| `KeystoreDataSourceTest.kt` | Signing and verification of byte arrays and streams, buffer handling |
| `FileDataSourceTest.kt` | File info resolution, stream opening, signature file creation |
| `SigningRepositoryImplTest.kt` | Key generation idempotency, signing delegation, verification delegation, public key export |
| `GetFileInfoUseCaseTest.kt` | URI-to-FileInfo resolution, error handling |
| `SignFileUseCaseTest.kt` | Full signing workflow: key check, size validation, stream signing, save. Error paths for each failure mode. |
| `MainViewModelTest.kt` | State transitions: Idle to FileSelected to Signing to Success/Error. Cancellation. Permission handling. |

## Instrumented Tests

| Test File | Coverage Area |
|-----------|---------------|
| `ButtonAlignmentTest.kt` | FilePickerButton and SignButton: rendering, icon placement, spacing, touch target size (48dp+), enabled/disabled states, cross-button consistency |
| `AboutSheetTest.kt` | AboutSheetContent rendering: app name, version, description, license section, all 11 library entries, developer section, click callbacks |
| `CryptoPerformanceTest.kt` | ECDSA signing benchmarks across file sizes (1 KB, 10 KB, 100 KB, 1 MB, 10 MB), key generation timing, SHA-256 hashing performance |
| `SigningThroughputTest.kt` | Real-device signing throughput measurement |
| `PdfGeneratorTest.kt` | Debug PDF generation for testing |

## Test Conventions

- Use `createComposeRule()` for Compose UI tests
- Wrap test content in `MaterialTheme { ... }` for proper theming
- Use `testTag` modifiers for node lookup (not text matchers for structural tests)
- Use MockK for mocking in unit tests
- Use Turbine for Flow testing in ViewModel tests
- Test names follow the pattern `component_behavior` (e.g., `signButton_loadingShowsSpinnerHidesIconAndText`)

## Coverage Areas

| Area | Unit | Instrumented |
|------|------|--------------|
| Cryptographic operations | Yes | Yes (benchmarks) |
| File I/O | Yes | No |
| State management | Yes | No |
| UI rendering | No | Yes |
| UI interaction | No | Yes |
| Performance | No | Yes |
