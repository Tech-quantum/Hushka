package com.techquantum.promptvault

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [PromptEntity::class], version = 5, exportSchema = false)
abstract class PromptDatabase : RoomDatabase() {
    abstract fun promptDao(): PromptDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE prompts ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE prompts ADD COLUMN source TEXT NOT NULL DEFAULT 'local'")
                database.execSQL("ALTER TABLE prompts ADD COLUMN license TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE prompts ADD COLUMN collection TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE prompts ADD COLUMN pinned INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE prompts ADD COLUMN lastUsedAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE prompts ADD COLUMN catalogId TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE prompts ADD COLUMN provider TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE prompts ADD COLUMN modality TEXT NOT NULL DEFAULT ''")
            }
        }

        @Volatile
        private var INSTANCE: PromptDatabase? = null

        fun getInstance(context: Context): PromptDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PromptDatabase::class.java,
                    "promptvault.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
