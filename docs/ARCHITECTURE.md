# Architecture

## Overview

The application follows Clean Architecture with three layers. Dependencies point inward: presentation depends on domain, data depends on domain, domain depends on nothing.

```
+---------------------+
|    Presentation     |    Compose UI, ViewModel, State
+---------------------+
          |
          v
+---------------------+
|      Domain         |    UseCases, Models, Repository interfaces
+---------------------+
          ^
          |
+---------------------+
|       Data          |    Repository implementations, DataSources
+---------------------+
```

## Layer Responsibilities

### Domain Layer

The domain layer contains business logic with zero Android framework dependencies (except `android.net.Uri` for file references).

**Use Cases:**
- `SignFileUseCase` -- orchestrates the signing workflow: key check, file validation, stream signing, signature save
- `VerifyFileUseCase` -- orchestrates verification: file stream, signature load, cryptographic verify
- `GetFileInfoUseCase` -- resolves file metadata from URI
- `GenerateKeyPairUseCase` -- ensures signing key exists in KeyStore

**Models:**
- `SigningResult` -- sealed class with Success/Error variants. Error variants carry typed information (FileNotFound, PermissionDenied, SigningFailed, etc.)
- `FileInfo` -- file metadata (URI, name, size, MIME type, display size)
- `SigningHistoryEntry` -- in-memory record of a completed signing operation

**Repository Interfaces:**
- `SigningRepository` -- key generation, stream signing, stream verification
- `FileRepository` -- file info resolution, stream opening, signature saving

### Data Layer

Implements repository interfaces with Android SDK components.

**KeystoreDataSource:**
- Wraps Android KeyStore for ECDSA P-256 key operations
- Attempts StrongBox hardware backing, falls back to TEE
- Streaming sign/verify with buffer cleanup
- Singleton scope (single KeyStore instance)

**FileDataSource:**
- Wraps ContentResolver for file access
- Uses MediaStore for signature file creation on Android Q+
- Legacy file I/O fallback for older API levels
- Singleton scope

### Presentation Layer

Jetpack Compose UI with unidirectional data flow.

**MainViewModel:**
- Manages `MainUiState` via `StateFlow`
- Delegates to use cases for business operations
- Uses `SavedStateHandle` to survive process death (selected file URI only)
- Cancellation support for long-running signing operations

**State:**
- `SigningUiState` -- sealed interface: Idle, FileSelected, Signing, Success, Error
- `VerificationUiState` -- sealed interface: Idle, Verifying, Valid, Invalid, Error
- `PermissionState` -- enum: NotRequested, Granted, Denied, ShowRationale, PermanentlyDenied

**Components:**
- `StatusDisplay` -- animated status card with live region for TalkBack
- `FilePickerButton` / `SignButton` -- primary action buttons with proper touch targets
- `AboutSheet` / `SigningHistorySheet` -- modal bottom sheets
- `PermissionRationaleDialog` -- permission explanation dialog

## Dependency Injection

Hilt provides compile-time verified DI.

`AppModule` binds:
- `ContentResolver` (from application context)
- `FileRepository` to `FileRepositoryImpl`
- `SigningRepository` to `SigningRepositoryImpl`
- `SamplePdfGenerator` (debug-only utility)

All data sources are `@Singleton` scoped. Use cases are unscoped (new instance per injection, stateless).

## Data Flow: Signing Operation

```
User taps "Sign"
  -> MainViewModel.onSignFile()
    -> SignFileUseCase.invoke(fileUri)
      -> SigningRepository.hasSigningKey()
      -> FileRepository.getFileInfo(uri)       // validate size
      -> FileRepository.openFileStream(uri)    // get InputStream
      -> SigningRepository.signStream(stream)   // ECDSA sign
      -> FileRepository.saveSignature(uri, bytes) // write .sig
    <- SigningResult.Success(originalUri, signatureUri, base64)
  <- _uiState.update { Success(...) }
UI recomposes with success status
```

## Threading Model

- UI: Main thread (Compose)
- ViewModel: `viewModelScope` (Main dispatcher, cancellable)
- Repository: `Dispatchers.IO` via `withContext`
- KeyStore operations: blocking on IO dispatcher
- File I/O: blocking on IO dispatcher

All coroutine cancellation is cooperative. `signingJob` reference allows user-initiated cancellation.
