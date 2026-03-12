package com.filesigner.di

import com.filesigner.util.ReleasePdfGenerator
import com.filesigner.util.SamplePdfGenerator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PdfGeneratorModule {

    @Binds
    @Singleton
    abstract fun bindSamplePdfGenerator(impl: ReleasePdfGenerator): SamplePdfGenerator
}
