package com.filesigner.di

import android.content.ContentResolver
import android.content.Context
import com.filesigner.data.repository.FileRepositoryImpl
import com.filesigner.data.repository.SigningRepositoryImpl
import com.filesigner.data.source.FileDataSource
import com.filesigner.data.source.KeystoreDataSource
import com.filesigner.domain.repository.FileRepository
import com.filesigner.domain.repository.SigningRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSigningRepository(impl: SigningRepositoryImpl): SigningRepository

    @Binds
    @Singleton
    abstract fun bindFileRepository(impl: FileRepositoryImpl): FileRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    @Provides
    @Singleton
    fun provideKeystoreDataSource(): KeystoreDataSource {
        return KeystoreDataSource()
    }

    @Provides
    @Singleton
    fun provideFileDataSource(contentResolver: ContentResolver): FileDataSource {
        return FileDataSource(contentResolver)
    }
}
