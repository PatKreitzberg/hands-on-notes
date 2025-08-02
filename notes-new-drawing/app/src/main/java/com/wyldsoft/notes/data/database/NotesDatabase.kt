package com.wyldsoft.notes.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wyldsoft.notes.data.database.converters.Converters
import com.wyldsoft.notes.data.database.dao.*
import com.wyldsoft.notes.data.database.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        FolderEntity::class,
        NotebookEntity::class,
        NoteEntity::class,
        NoteNotebookCrossRef::class,
        ShapeEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class NotesDatabase : RoomDatabase() {
    
    abstract fun folderDao(): FolderDao
    abstract fun notebookDao(): NotebookDao
    abstract fun noteDao(): NoteDao
    abstract fun shapeDao(): ShapeDao
    
    companion object {
        @Volatile
        private var INSTANCE: NotesDatabase? = null
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add viewport columns to notes table
                database.execSQL("ALTER TABLE notes ADD COLUMN viewportScale REAL NOT NULL DEFAULT 1.0")
                database.execSQL("ALTER TABLE notes ADD COLUMN viewportOffsetX REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE notes ADD COLUMN viewportOffsetY REAL NOT NULL DEFAULT 0.0")
            }
        }
        
        fun getDatabase(context: Context): NotesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotesDatabase::class.java,
                    "notes_database"
                )
                .addMigrations(MIGRATION_1_2)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Create root folder on first database creation
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                val rootFolder = FolderEntity(
                                    id = "root",
                                    name = "Root",
                                    parentFolderId = null
                                )
                                database.folderDao().insert(rootFolder)
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}