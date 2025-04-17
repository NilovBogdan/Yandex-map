package bogdan.nilov.yandexmap.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import bogdan.nilov.yandexmap.db.AppDb
import bogdan.nilov.yandexmap.dto.Map
import bogdan.nilov.yandexmap.repository.MapRepository
import bogdan.nilov.yandexmap.repository.MapRepositoryRoomImpl

class MapsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MapRepository = MapRepositoryRoomImpl(
        AppDb.getInstance(application).mapDao
    )

    val data = repository.getAll()
    fun save(id: Long, name: String, lat: String, lon: String) {
        val map = Map(id = id, name = name, lat = lat, lon = lon)
        repository.save(map)
    }

    fun removeById(id: Long) {
        repository.removeById(id)
    }

}