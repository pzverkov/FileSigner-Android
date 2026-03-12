# Roadmap

## Planned Improvements

### Short Term

**Signature Format Specification**
Define and document the `.sig` file format formally. Currently raw DER-encoded ECDSA bytes. A header with version, algorithm identifier, and key fingerprint would improve interoperability and forward compatibility.

**Key Rotation Support**
The key alias is versioned (`file_signer_ecdsa_p256_v1`). Implement key rotation workflow: generate new key, re-sign recent files if requested, deprecate old alias.

**Persistent Signing History**
Move signing history from in-memory list to Room database. Enables history survival across app restarts and provides audit trail capability.

### Medium Term

**Biometric Authentication (Optional)**
Add opt-in biometric gating for signing operations via `setUserAuthenticationRequired(true)` on the KeyGenParameterSpec. Useful for enterprise deployments where signing authorization matters.

**Batch Signing**
Support selecting and signing multiple files in a single operation. Requires UI changes to FilePickerButton (multi-select) and progress tracking for batch operations.

**Signature File Sharing**
Add share intent for completed signatures so users can send `.sig` files via email, messaging, or cloud storage directly from the success screen.

### Long Term

**eIDAS Compliance Path**
For EU regulatory contexts, implement CAdES signature wrapping and integrate with a Qualified Trust Service Provider (QTSP) for certificate binding. This requires INTERNET permission and server-side infrastructure.

**RFC 3161 Timestamp Authority**
Integrate optional TSA for legally-binding timestamps on signatures. Adds network dependency but provides non-repudiation with temporal proof.

**Post-Quantum Cryptography**
Monitor Android platform support for post-quantum signature algorithms (e.g., ML-DSA/Dilithium). When available in Android KeyStore, add as an alternative algorithm option alongside ECDSA P-256.

## Future Considerations

### Anomaly Detection for Enterprise (Not a Priority)

For enterprise deployments with many devices signing files, on-device AI could detect anomalous signing patterns: unusual file types, signing frequency spikes, files from unexpected storage locations, or signing attempts outside normal operating hours. This would require an on-device ML model (potentially using Android ML Kit or the Android AI SDK on supported hardware) trained on baseline signing behavior.

This is recorded as a future consideration, not a planned feature. The current single-user, single-device architecture does not benefit from anomaly detection. It becomes relevant only if the app evolves toward managed enterprise deployment with centralized policy.

### Audit Log with Tamper Protection

For SOC 2 auditability, implement a local audit log where each entry includes a chain hash: `SHA-256(previous_entry_hash + current_entry_data)`. This makes it detectable if any log entry is modified or deleted. Requires Room database for persistence.
