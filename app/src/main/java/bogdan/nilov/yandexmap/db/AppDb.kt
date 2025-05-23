package bogdan.nilov.yandexmap.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import bogdan.nilov.yandexmap.dao.MapDao
import bogdan.nilov.yandexmap.entity.MapEntity

@Database(entities = [MapEntity::class], version = 1)
abstract class AppDb : RoomDatabase() {
    abstract val mapDao: MapDao

    companion object {
        @Volatile
        private var instance: AppDb? = null

        fun getInstance(context: Context): AppDb {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context, AppDb::class.java, "app.db",
        )
            .allowMainThreadQueries()
            .build()
    }
}