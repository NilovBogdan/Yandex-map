package bogdan.nilov.yandexmap.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import bogdan.nilov.yandexmap.dto.Map

@Entity
data class MapEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val lat: String,
    val lon: String
){
    fun toDto() = Map(id = id, name = name, lat = lat, lon = lon)

    companion object{
        fun fromDto(map: Map) = MapEntity(map.id, map.name, map.lat, map.lon)
    }
}
