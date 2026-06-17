package mx.utng.mamr.smarthealthmonitor.data.db

import android.content.Context
import androidx.room.*

@Database(
    entities = [LecturaFC::class],
    version  = 1,
    exportSchema = false  // true en producción para migraciones
)
abstract class SmartHealthDB : RoomDatabase() {
    abstract fun lecturaDao(): LecturaFCDao

    companion object {
        @Volatile
        private var INSTANCE: SmartHealthDB? = null

        fun getDatabase(context: Context): SmartHealthDB {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    SmartHealthDB::class.java,
                    "smarthealthmonitor_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
