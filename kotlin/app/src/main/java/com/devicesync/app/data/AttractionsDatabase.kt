package com.devicesync.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.devicesync.app.data.converters.AttractionConverters
import com.devicesync.app.data.dao.AttractionDao

@Database(
    entities = [Attraction::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(AttractionConverters::class)
abstract class AttractionsDatabase : RoomDatabase() {
    
    abstract fun attractionDao(): AttractionDao
    
    companion object {
        @Volatile
        private var INSTANCE: AttractionsDatabase? = null
        
        fun getDatabase(context: Context): AttractionsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AttractionsDatabase::class.java,
                    "attractions_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 