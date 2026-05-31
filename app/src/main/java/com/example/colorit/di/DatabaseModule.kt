package com.example.colorit.di

import android.content.Context
import androidx.room.Room
import com.example.colorit.data.database.ColorItDatabase
import com.example.colorit.data.database.DrawingDao
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
    fun provideDatabase(
        @ApplicationContext context: Context
    ): ColorItDatabase {
        return Room.databaseBuilder(
            context,
            ColorItDatabase::class.java,
            "colorit_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideDrawingDao(
        database: ColorItDatabase
    ): DrawingDao {
        return database.drawingDao()
    }
}
