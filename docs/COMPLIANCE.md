# Compliance Matrix

This document maps the application against recognized security and privacy standards. Last updated: 2026-03-12.

## 1. BSI TR-02102-1 (Cryptographic Mechanisms)

The German Federal Office for Information Security (BSI) publishes TR-02102-1, which specifies recommended cryptographic algorithms and key lengths.

| Requirement | Status | Implementation |
|------------|--------|----------------|
| Approved asymmetric algorithm | PASS | ECDSA |
| Approved curve | PASS | secp256r1 (NIST P-256), approved through 2030+ |
| Minimum key length | PASS | 256-bit EC key (128-bit symmetric equivalent) |
| Approved hash function | PASS | SHA-256, SHA-512 supported |
| No deprecated algorithms | PASS | No MD5, SHA-1, RSA-1024, or 3DES |
| Hardware-backed key storage | PASS | Android KeyStore with StrongBox/TEE |
| Secure random number generation | PASS | Delegated to Android KeyStore (CSPRNG) |

**Assessment:** The cryptographic implementation is fully aligned with BSI TR-02102-1 recommendations for algorithm selection and key management.

## 2. BSI TR-03161 (Mobile Application Security)

BSI TR-03161 defines security requirements for mobile applications in three parts.

### Part 1: General Requirements

| Requirement | Status | Evidence |
|------------|--------|----------|
| Minimal permission usage | PASS | Only storage permissions, scoped to legacy API levels |
| Secure data storage | PASS | No persistent sensitive data. KeyStore for keys. |
| Secure communication | PASS | No network communication. Cleartext blocked. |
| Input validation | PASS | File size validated before processing |
| Secure logging | PASS | SanitizedDebugTree masks PII. No release logging. |
| Code obfuscation | PASS | R8/ProGuard enabled for release |
| Backup protection | PASS | allowBackup=false, extraction rules configured |

### Part 2: Android-Specific Requirements

| Requirement | Status | Evidence |
|------------|--------|----------|
| Target latest SDK | PASS | targetSdk 35 (Android 15) |
| Minimal exported components | PASS | Only MainActivity with LAUNCHER filter |
| No implicit intents for sensitive operations | PASS | Explicit intents for URL viewing only |
| Content provider security | PASS | No content providers defined |
| WebView security | PASS | No WebView usage |
| File provider security | PASS | Uses MediaStore API, no FileProvider needed |

**Assessment:** Aligned with BSI TR-03161 requirements for mobile application security.

## 3. OWASP MASVS v2.0

The OWASP Mobile Application Security Verification Standard defines security requirements across seven domains.

| Domain | Score | Status |
|--------|-------|--------|
| MASVS-STORAGE | 9/9 | PASS |
| MASVS-CRYPTO | 9/9 | PASS |
| MASVS-AUTH | 5/5 | PASS |
| MASVS-NETWORK | 5/5 | PASS |
| MASVS-PLATFORM | 5/5 | PASS |
| MASVS-CODE | 8/9 | PASS |
| MASVS-RESILIENCE | 6/9 | PARTIAL |

**Overall: 47/51 (92%)**

### MASVS-RESILIENCE Details

| Item | Status | Notes |
|------|--------|-------|
| Root detection | N/A | Not implemented. KeyStore integrity provides baseline protection. |
| Debugger detection | N/A | Not implemented. Acceptable for non-financial use case. |
| Emulator detection | N/A | Not implemented. |
| Integrity verification | PASS | Signature verification validates file integrity |
| Code obfuscation | PASS | ProGuard enabled |
| String encryption | PARTIAL | Resource strings not encrypted. Acceptable for open-source app. |

## 4. NIST Standards

| Standard | Status | Details |
|----------|--------|---------|
| NIST SP 800-186 (ECC) | Aligned | P-256 curve is NIST-recommended |
| NIST SP 800-57 (Key Management) | Aligned | 256-bit EC provides 128-bit equivalent security. Suitable through 2030+. |
| FIPS 186-4 (Digital Signature Standard) | Aligned | ECDSA with approved curve and hash |
| NIST SP 800-131A (Transitions) | Aligned | All algorithms meet current strength requirements |

## 5. GDPR (General Data Protection Regulation)

| Requirement | Status | Evidence |
|------------|--------|----------|
| Data minimization | PASS | No personal data collected or processed |
| Purpose limitation | PASS | App purpose is file signing only |
| Storage limitation | PASS | Signing history is in-memory only, not persisted |
| Data protection by design | PASS | No network, no analytics, no telemetry |
| Data protection by default | PASS | Minimal permissions, backup disabled |
| Right to erasure | PASS | No data stored to erase (in-memory only) |
| Data breach notification | N/A | No data collection means no breach scenario |

**Assessment:** Fully compliant. The application collects no personal data and makes no network connections.

## 6. eIDAS Regulation (EU) 910/2014

| Requirement | Status | Notes |
|------------|--------|-------|
| Advanced electronic signature | PARTIAL | Cryptographically linked to signatory via device KeyStore, but lacks certificate binding |
| Qualified electronic signature | NOT MET | Requires QTSP-issued certificate and qualified signature creation device |
| Timestamp token (RFC 3161) | NOT MET | No TSA integration |
| CAdES/XAdES/PAdES format | NOT MET | Raw ECDSA signatures used |
| Certificate chain | NOT MET | No X.509 certificate included with signature |

**Assessment:** The application does not produce eIDAS-compliant electronic signatures. For EU regulatory contexts, signatures must be wrapped in a standard format (CAdES, XAdES, or PAdES) and issued through a Qualified Trust Service Provider. The underlying cryptography is sound and would be acceptable within such a wrapper.

## 7. Regional Compliance Summary

| Region | Framework | Status |
|--------|-----------|--------|
| Germany | BSI TR-02102-1, TR-03161 | Aligned |
| EU | GDPR | Compliant |
| EU | eIDAS | Not aligned (raw signatures) |
| USA | NIST SP 800-186, FIPS 186-4 | Aligned |
| International | OWASP MASVS | 92% coverage |
| International | ISO/IEC 27001 (cryptographic controls) | Aligned for algorithm selection and key management |

## 8. Compliance Gaps and Remediation Path

| Gap | Severity | Remediation |
|-----|----------|-------------|
| No eIDAS qualified signatures | Low (unless required by regulation) | Integrate with QTSP for certificate binding and CAdES wrapping |
| No RFC 3161 timestamps | Low | Add optional TSA integration for legal timestamp tokens |
| No root/debugger detection | Low | Add runtime integrity checks if threat model requires it |
| No key recovery mechanism | Medium | Implement encrypted key backup if device loss is a concern |
| Raw signature format | Low | Define and document a formal signature file format specification |
