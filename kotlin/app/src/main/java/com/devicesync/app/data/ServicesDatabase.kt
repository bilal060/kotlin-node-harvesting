package com.devicesync.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.devicesync.app.data.converters.ServiceConverters
import com.devicesync.app.data.dao.ServiceDao

@Database(
    entities = [Service::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ServiceConverters::class)
abstract class ServicesDatabase : RoomDatabase() {
    
    abstract fun serviceDao(): ServiceDao
    
    companion object {
        @Volatile
        private var INSTANCE: ServicesDatabase? = null
        
        fun getDatabase(context: Context): ServicesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ServicesDatabase::class.java,
                    "services_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 