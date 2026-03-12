package com.filesigner.domain.usecase

import com.filesigner.domain.repository.SigningRepository
import javax.inject.Inject

class GenerateKeyPairUseCase @Inject constructor(
    private val signingRepository: SigningRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return signingRepository.generateKeyPairIfNeeded()
    }
}
