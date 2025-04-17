package bogdan.nilov.yandexmap.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import bogdan.nilov.yandexmap.entity.MapEntity

@Dao
interface MapDao {
    @Query("SELECT * FROM MapEntity ORDER BY id DESC")
    fun getAll(): LiveData<List<MapEntity>>

    fun save(map: MapEntity) = if (map.id != 0L) edit(map.id, map.name, map.lat, map.lon) else insert(map)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(map: MapEntity)

    @Query("UPDATE MapEntity SET name = :name, lat = :lat, lon = :lon WHERE id = :id")
    fun edit(id: Long, name: String, lat: String, lon: String)

    @Query("DELETE FROM MapEntity WHERE id = :id")
    fun removeById(id: Long)

    @Query("DELETE FROM MapEntity")
    fun removeAll()
}