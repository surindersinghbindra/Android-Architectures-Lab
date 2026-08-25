package com.ekobits.demoapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ekobits.demoapp.data.local.UserDao
import com.ekobits.demoapp.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
