package com.example.colorit.di

import android.content.Context
import com.example.colorit.data.AppDatabase
import com.example.colorit.data.ArtworkDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideArtworkDao(database: AppDatabase): ArtworkDao {
        return database.artworkDao()
    }
}
