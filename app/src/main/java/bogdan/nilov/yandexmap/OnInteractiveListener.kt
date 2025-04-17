package bogdan.nilov.yandexmap

interface OnInteractiveListener {
    fun removeById(id: Long, lat: Double, lon: Double){}
    fun transition(id: Long, name: String, lat: Double, lon: Double){}
    fun edit(id: Long, name: String, lat: Double, lon: Double){}
}