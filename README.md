# File Signer

![Build](https://img.shields.io/github/actions/workflow/status/pzverkov/FileSigner-Android/build.yml?label=build)
![CodeQL](https://img.shields.io/github/actions/workflow/status/pzverkov/FileSigner-Android/codeql.yml?label=CodeQL)
![API](https://img.shields.io/badge/API-26%2B-brightgreen)
![License](https://img.shields.io/badge/license-Apache%202.0-blue)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2-purple)
![OWASP MASVS](https://img.shields.io/badge/OWASP%20MASVS-90%25-green)
![BSI TR-02102-1](https://img.shields.io/badge/BSI%20TR--02102--1-aligned-green)

Android application for cryptographic file signing using ECDSA P-256. Signs any file on device and saves the detached signature alongside the original. Signatures can be verified at any time  - either from the signing history or by selecting any file and its `.sig` independently via the standalone Verify flow.

## Technical Summary

| Property | Value |
|----------|-------|
| Algorithm | ECDSA (secp256r1 / NIST P-256) |
| Hash | SHA-256 |
| Key Storage | Android KeyStore (StrongBox or TEE) |
| Signature Format | DER-encoded ECDSA, detached `.sig` file |
| Max File Size | 500 MB |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 (Android 15) |

## Architecture

Clean Architecture with MVVM. Three layers, strict dependency direction inward.

```
presentation/  ->  domain/  <-  data/
  MainActivity       UseCases      KeystoreDataSource
  MainViewModel      Models        FileDataSource
  Components         Repositories  RepositoryImpl
```

Dependency injection via Hilt. Coroutines for async operations. Jetpack Compose for UI.

## Security

- Private keys never leave the hardware-backed secure enclave (StrongBox where available, TEE as fallback)
- No INTERNET permission. No network libraries. No analytics or telemetry
- `android:allowBackup="false"` and explicit data extraction exclusions
- `cleartextTrafficPermitted="false"` in network security config
- Debug logging uses `SanitizedDebugTree` that masks file URIs, paths, and names
- Streaming signature computation. Buffers zeroed after use
- ProGuard/R8 enabled for release builds

See [docs/SECURITY.md](docs/SECURITY.md) for the full security design document.

## Compliance

The application aligns with the following standards and frameworks:

| Standard | Status | Notes |
|----------|--------|-------|
| BSI TR-02102-1 | Aligned | ECDSA P-256, SHA-256, hardware-backed keys |
| OWASP MASVS | 90% coverage | All domains pass except optional resilience hardening |
| NIST SP 800-186 | Aligned | Approved curve and hash |
| FIPS 186-4 | Aligned | DSS-compliant algorithm selection |
| GDPR | Compliant | No data collection, no network, no PII processing |

See [docs/COMPLIANCE.md](docs/COMPLIANCE.md) for the full compliance matrix.

Note: This application produces raw ECDSA signatures. It does not produce qualified electronic signatures under eIDAS Regulation (EU) 910/2014. For eIDAS compliance, signatures must be wrapped in CAdES/XAdES/PAdES format and issued through a Qualified Trust Service Provider (QTSP).

## Build

```bash
# Debug
./gradlew assembleDebug

# Release
./gradlew assembleRelease

# Unit tests
./gradlew testDebugUnitTest

# Instrumented tests (requires emulator or device)
./gradlew connectedDebugAndroidTest
```

## Project Structure

```
app/src/main/java/com/filesigner/
  data/
    repository/         FileRepositoryImpl, SigningRepositoryImpl
    source/             KeystoreDataSource, FileDataSource
  di/                   AppModule (Hilt)
  domain/
    model/              FileInfo, SigningResult, SigningHistoryEntry
    repository/         FileRepository, SigningRepository (interfaces)
    usecase/            SignFileUseCase, VerifyFileUseCase, GetFileInfoUseCase, GenerateKeyPairUseCase
  presentation/
    components/         AboutSheet, FilePickerButton, SignButton, StatusDisplay, SigningHistorySheet, VerifySheet, PermissionRationaleDialog
    theme/              Theme, Color
    MainActivity.kt
    MainViewModel.kt
    SigningUiState.kt
  util/
    SanitizedDebugTree  PII-masking Timber tree
    SamplePdfGenerator  Debug-only test file generation
```

## Testing

Unit tests cover the domain and data layers. Instrumented tests cover UI components and cryptographic performance benchmarks. See [docs/TESTING.md](docs/TESTING.md).

## Accessibility

The application supports TalkBack screen reader navigation, dynamic font scaling, RTL layouts, dark theme, and Material You dynamic color on Android 12+. All interactive elements have content descriptions. Status changes are announced via live regions. Section headings use heading semantics for swipe navigation. See [docs/ACCESSIBILITY.md](docs/ACCESSIBILITY.md).

## Documentation

| Document | Description |
|----------|-------------|
| [docs/SECURITY.md](docs/SECURITY.md) | Security architecture, cryptography design, threat model |
| [docs/COMPLIANCE.md](docs/COMPLIANCE.md) | BSI, OWASP MASVS, NIST, GDPR compliance matrix |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Architecture decisions, layer responsibilities, data flow |
| [docs/TESTING.md](docs/TESTING.md) | Test strategy, coverage areas, running tests |
| [docs/ACCESSIBILITY.md](docs/ACCESSIBILITY.md) | Accessibility features, TalkBack support, contrast |
| [docs/ROADMAP.md](docs/ROADMAP.md) | Planned improvements and future considerations |
| [LEGAL.md](LEGAL.md) | Legal notices, export controls, eIDAS, CRA, jurisdiction |

## License

Apache License 2.0  - see [LICENSE](LICENSE).

---

## Compliance & Liability

**IMPORTANT: By using, cloning, or forking this repository, you acknowledge and agree to the following:**

### Not a Certified Product
This application provides cryptographic file signing. It is **NOT** a FIPS 140-3 validated module, a qualified electronic signature tool under eIDAS (EU) 910/2014, or a certified product under any national or international scheme. Compliance alignment statements (BSI, OWASP, NIST) describe algorithm selection and architecture, not formal certification.

### Export Controls
This software implements ECDSA P-256 cryptographic technology. Users are solely responsible for compliance with:
- **EU**: Dual-Use Regulation (EU 2021/821). Open-source exemptions may apply.
- **US**: Export Administration Regulations (EAR), ECCN 5D002. Publicly available open-source may qualify for License Exception TSR.
- **Other**: Local import/export and usage regulations for cryptographic software.

### Jurisdiction Restrictions
Cryptographic software is restricted or regulated in certain jurisdictions. This software **must not** be used to circumvent any applicable laws or regulations. Users deploying in any jurisdiction assume full responsibility for legal compliance.

### No Warranty
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY ARISING FROM THE USE OF THIS SOFTWARE.

See [LEGAL.md](LEGAL.md) for detailed legal notices covering eIDAS, EU Cyber Resilience Act, export controls, and jurisdiction.
