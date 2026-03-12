# Security Design Document

## 1. Cryptographic Design

### 1.1 Algorithm Selection

| Component | Choice | Rationale |
|-----------|--------|-----------|
| Asymmetric Algorithm | ECDSA | Compact signatures, efficient on mobile hardware |
| Curve | secp256r1 (NIST P-256) | BSI TR-02102-1 approved, NIST SP 800-186 recommended, broadly supported |
| Hash Function | SHA-256 | Collision resistant, hardware-accelerated on ARMv8+ |
| Signature Encoding | DER | Standard ECDSA output format from Android KeyStore |
| Key Size | 256-bit | Equivalent to 128-bit symmetric security. Sufficient through 2030+ per BSI/NIST guidance |

### 1.2 Key Management

**Generation:**
- Keys are generated via `KeyPairGenerator` backed by Android KeyStore provider
- `KeyGenParameterSpec` restricts usage to `PURPOSE_SIGN | PURPOSE_VERIFY`
- StrongBox (dedicated secure element) is attempted first on Android 9+ (API 28)
- If StrongBox is unavailable, keys fall back to TEE (Trusted Execution Environment)
- Key alias is versioned (`file_signer_ecdsa_p256_v1`) for future rotation support

**Storage:**
- Private keys are stored in Android KeyStore and never leave the secure enclave
- `KeyStore.getKey()` returns a reference, not raw key material
- Keys are not extractable: no `getEncoded()` on private key will return data
- Keys survive app updates but not factory resets

**Access Control:**
- `setUserAuthenticationRequired(false)`: signing does not require biometric/PIN
- `setInvalidatedByBiometricEnrollment(false)`: key survives biometric changes
- These settings are intentional for a file utility app. Biometric gating can be added if required.

### 1.3 Signing Process

```
1. Open file as InputStream
2. Initialize Signature object with SHA256withECDSA
3. Stream file through Signature.update() in 8 KB chunks
4. Call Signature.sign() to produce DER-encoded ECDSA signature
5. Zero the read buffer
6. Save signature bytes as [filename].sig via MediaStore
```

The streaming approach prevents loading entire files into memory. The 500 MB limit is enforced before signing begins.

### 1.4 Verification Process

```
1. Open original file as InputStream
2. Read corresponding .sig file
3. Initialize Signature with public key in VERIFY mode
4. Stream file through Signature.update()
5. Call Signature.verify() with loaded signature bytes
6. Return Valid / Invalid / Error
```

### 1.5 Entropy and Randomness

ECDSA requires cryptographically secure random values at two points: private key generation and per-signature nonce (`k`) derivation. Weak or repeated randomness at either point is catastrophic  - a reused `k` value across two signatures allows algebraic recovery of the private key (as demonstrated in the 2010 PlayStation 3 key extraction).

**Entropy sources:**

| Operation | Random value | Source | Where it runs |
|-----------|-------------|--------|---------------|
| Key generation | Private key scalar `d` | Hardware TRNG | Inside StrongBox secure element or TEE |
| Signing | Per-signature nonce `k` | Hardware TRNG | Inside StrongBox secure element or TEE |

Both random values are generated entirely within the hardware security module. The application code never supplies, seeds, or influences the entropy. `KeyPairGenerator.generateKeyPair()` and `Signature.sign()` delegate to the Android KeyStore provider, which executes the cryptographic operations inside the secure enclave.

**Why user-supplied randomness is not used:**

Human-generated or application-generated entropy is categorically weaker than hardware TRNG. The Android KeyStore architecture enforces this by design  - the private key and signing nonce are never exposed to the application process. Even if the app's memory is compromised, the attacker cannot influence or observe the random values used in key generation or signing.

**RFC 6979 (deterministic ECDSA) note:**

RFC 6979 defines a method to derive the nonce `k` deterministically from the private key and message hash, eliminating dependence on RNG quality entirely. Whether the underlying hardware implements RFC 6979 or standard random `k` is chip-vendor-dependent (Qualcomm, Samsung, Google Titan each have independent implementations). The Android KeyStore API does not expose this detail. In either case, the nonce is generated inside the secure enclave with no application-level influence, making the practical security equivalent for both approaches.

**Entropy health:**

StrongBox-backed devices use a dedicated hardware TRNG that meets NIST SP 800-90B requirements. TEE-backed devices rely on the ARM TrustZone entropy source, which feeds the TEE's internal CSPRNG. On both paths, the entropy quality is a property of the hardware, not the application  - there is no application-level knob to weaken it.

## 2. Data Protection

### 2.1 Data at Rest

| Data | Storage | Protection |
|------|---------|------------|
| Private signing key | Android KeyStore | Hardware-backed (StrongBox/TEE). Not extractable. |
| Signature files (.sig) | Device Downloads folder | User-accessible. Contains only cryptographic signature bytes. |
| Signing history | In-memory only | Lost on process death. Not persisted to disk. |
| Selected file URI | SavedStateHandle | Survives configuration changes. Cleared on state reset. |

### 2.2 Data in Transit

No data leaves the device. The application has no INTERNET permission and no network libraries.

Two URLs are accessible via Intent.ACTION_VIEW (opens external browser, user-initiated only):
- Apache License 2.0 URL (About screen)
- GitHub profile URL (About screen)

### 2.3 Backup Protection

- `android:allowBackup="false"` prevents Android backup of app data
- `data_extraction_rules.xml` explicitly excludes root, database, and sharedpref domains from cloud backup and device transfer
- `backup_rules.xml` excludes sharedpref and database domains

### 2.4 Logging

All logging goes through `SanitizedDebugTree`, a custom Timber tree that:
- Masks `content://` URIs (preserves authority, masks path)
- Masks file system paths (`/storage/...` becomes `/storage/***`)
- Masks file names in key-value pairs (preserves extension only)
- Is only planted in DEBUG builds (release builds have no logging)

Direct log statements in data and presentation layers have been stripped of file names, URIs, and paths. Only operational metadata (file size, byte counts, success/failure status) is logged.

## 3. Network Security

- No `android.permission.INTERNET` declared in AndroidManifest.xml
- `network_security_config.xml` sets `cleartextTrafficPermitted="false"` as defense in depth
- If INTERNET permission is ever added in the future, all HTTP traffic will be blocked by default

## 4. Platform Security

### 4.1 Permissions

| Permission | Scope | Purpose |
|------------|-------|---------|
| `READ_EXTERNAL_STORAGE` | maxSdkVersion="32" | Legacy file access for Android 12 and below |
| `WRITE_EXTERNAL_STORAGE` | maxSdkVersion="28" | Legacy file writing for Android 9 and below |

On Android 10+ (API 29+), the app uses `ActivityResultContracts.OpenDocument()` and MediaStore APIs. No broad storage permissions needed.

### 4.2 Exported Components

Only `MainActivity` is exported, with a single MAIN/LAUNCHER intent filter. No content providers, broadcast receivers, or services are defined.

### 4.3 Code Protection

- R8/ProGuard enabled for release builds with `proguard-android-optimize.txt`
- Custom ProGuard rules preserve `java.security.*` and `android.security.keystore.*` classes
- No WebView components (eliminates XSS, JavaScript injection vectors)

## 5. Threat Model

### 5.1 In Scope

| Threat | Mitigation |
|--------|------------|
| File tampering after signing | ECDSA verification detects any modification |
| Key extraction from device | Android KeyStore prevents key export. StrongBox provides hardware isolation. |
| Log data leakage | SanitizedDebugTree masks PII. No logging in release builds. |
| Backup extraction | Backup disabled. Data extraction rules exclude sensitive domains. |
| Man-in-the-middle | No network communication to intercept |
| Signature forgery | ECDSA P-256 provides 128-bit equivalent security against forgery |
| ECDSA nonce reuse / weak RNG | Nonce generation runs inside StrongBox/TEE hardware TRNG. Application cannot supply or influence entropy. See section 1.5. |

### 5.2 Out of Scope

| Threat | Reasoning |
|--------|-----------|
| Physical device access with root | Rooted device compromises all Android security guarantees including KeyStore on non-StrongBox hardware |
| Side-channel attacks on TEE | Requires physical access and specialized equipment |
| Quantum computing attacks | ECDSA is vulnerable to quantum attacks. Migration to post-quantum algorithms is a future consideration when Android provides PQC KeyStore support. |
| Key recovery after factory reset | Android KeyStore keys do not survive factory reset by design |
