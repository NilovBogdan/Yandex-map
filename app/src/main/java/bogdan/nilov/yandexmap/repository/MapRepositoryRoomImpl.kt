package bogdan.nilov.yandexmap.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import bogdan.nilov.yandexmap.dao.MapDao
import bogdan.nilov.yandexmap.dto.Map
import bogdan.nilov.yandexmap.entity.MapEntity

class MapRepositoryRoomImpl(
    private val dao: MapDao
): MapRepository {
    private val maps = emptyList<Map>()
    private val data = MutableLiveData(maps)

    init {
        data.value = maps
    }

    override fun getAll(): LiveData<List<Map>> = dao.getAll().map {
        it.map {map -> map.toDto() }
    }
    override fun save(map: Map) = dao.save(MapEntity.fromDto(map))

    override fun removeById(id: Long) = dao.removeById(id)

    override fun removeAll() = dao.removeAll()
}
