package bogdan.nilov.yandexmap.repository

import androidx.lifecycle.LiveData
import bogdan.nilov.yandexmap.dto.Map

interface MapRepository {
    fun getAll(): LiveData<List<Map>>
    fun save(map: Map)
    fun removeById(id: Long)
    fun removeAll()
}