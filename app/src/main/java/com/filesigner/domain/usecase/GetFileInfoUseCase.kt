package com.filesigner.domain.usecase

import com.filesigner.domain.model.FileInfo
import com.filesigner.domain.repository.FileRepository
import javax.inject.Inject

class GetFileInfoUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    suspend operator fun invoke(uri: String): Result<FileInfo> {
        return fileRepository.getFileInfo(uri)
    }
}
