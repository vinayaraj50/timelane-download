package com.timelane.core.di

import android.app.Application
import androidx.room.Room
import com.timelane.core.undo.UndoManager
import com.timelane.data.local.EventDao
import com.timelane.data.local.TaskDao
import com.timelane.data.local.TimeLaneDatabase
import com.timelane.data.repository.EventRepositoryImpl
import com.timelane.data.repository.TaskRepositoryImpl
import com.timelane.domain.repository.EventRepository
import com.timelane.domain.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): TimeLaneDatabase {
        return Room.databaseBuilder(
            app,
            TimeLaneDatabase::class.java,
            "timelane_db"
        ).fallbackToDestructiveMigration() // For MVP simplicity
         .build()
    }

    @Provides
    @Singleton
    fun provideEventDao(db: TimeLaneDatabase): EventDao {
        return db.eventDao()
    }

    @Provides
    @Singleton
    fun provideTaskDao(db: TimeLaneDatabase): TaskDao {
        return db.taskDao()
    }

    @Provides
    @Singleton
    fun provideEventRepository(repo: EventRepositoryImpl): EventRepository {
        return repo
    }

    @Provides
    @Singleton
    fun provideTaskRepository(repo: TaskRepositoryImpl): TaskRepository {
        return repo
    }

    @Provides
    @Singleton
    fun provideDataStore(app: Application): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { app.preferencesDataStoreFile("settings") }
        )
    }

    @Provides
    @Singleton
    fun provideUndoManager(): UndoManager {
        return UndoManager()
    }
}
