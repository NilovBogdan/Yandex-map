package bogdan.nilov.yandexmap.activity


import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import bogdan.nilov.yandexmap.BuildConfig
import bogdan.nilov.yandexmap.OnInteractiveListener
import bogdan.nilov.yandexmap.R
import bogdan.nilov.yandexmap.adapter.ListAdapter
import bogdan.nilov.yandexmap.databinding.ActivityMainBinding
import bogdan.nilov.yandexmap.viewModel.MapsViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.GeoObjectTapListener
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.GeoObjectSelectionMetadata
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.VisibleRegionUtils
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.Address
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.search.ToponymObjectMetadata
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.RemoteError
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(),
    UserLocationObjectListener {
    private lateinit var mapView: MapView
    lateinit var binding: ActivityMainBinding
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationMapkit: UserLocationLayer
    lateinit var searchManager: SearchManager
    lateinit var searchSession: Session


    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                mapView = findViewById(R.id.mapview)
                val mapKit: MapKit = MapKitFactory.getInstance()
                locationMapkit = mapKit.createUserLocationLayer(mapView.mapWindow)
                locationMapkit.isVisible = true
            }
        }
    val viewModel: MapsViewModel by viewModels()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapView = findViewById(R.id.mapview)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        var startLocation: Point
        searchManager =
            SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
        val drivingRouter =
            DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.COMBINED)
        val searchOptions = SearchOptions().apply {
            resultPageSize = 32
        }
        val drivingOptions = DrivingOptions().apply {
            routesCount = 2
        }
        val vehicleOptions = VehicleOptions()

        lifecycleScope.launch { // Проверка разрешений
            when {
                ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    val mapKit: MapKit = MapKitFactory.getInstance()
                    locationMapkit = mapKit.createUserLocationLayer(mapView.mapWindow)
                    val task = fusedLocationProviderClient.lastLocation
                    task.addOnSuccessListener {
                        if (it != null) {
                            startLocation = Point(it.latitude, it.longitude)
                            mapView.mapWindow.map.move(
                                CameraPosition(startLocation, 15f, 0f, 0f),
                                Animation(Animation.Type.SMOOTH, 10f), null
                            )
                        }
                    }
                    locationMapkit.isVisible = true

                }

                else -> requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }


        val adapter = ListAdapter(object : OnInteractiveListener {
            override fun removeById(id: Long, lat: Double, lon: Double) {
                mapView.mapWindow.map.mapObjects.clear()
                viewModel.removeById(id)
            }

            @SuppressLint("MissingPermission")
            override fun transition(       // Выставление метки в нужно место на карте и маршрут движения к нему по клику на название
                id: Long,
                name: String,
                lat: Double,
                lon: Double
            ) {
                mapView.mapWindow.map.mapObjects.clear()
                val point = Point(lat, lon)
                val pin = ImageProvider.fromResource(this@MainActivity, R.drawable.pin)
                mapView.mapWindow.map.mapObjects.addPlacemark()
                    .apply {
                        geometry = point
                        setIcon(pin)
                    }
                mapView.mapWindow.map.move(CameraPosition(point, 15f, 0f, 0f))
                val task = fusedLocationProviderClient.lastLocation
                task.addOnSuccessListener {
                    if (it != null) {
                        startLocation = Point(it.latitude, it.longitude)
                        val points = buildList {
                            add(
                                RequestPoint(
                                    startLocation,
                                    RequestPointType.WAYPOINT,
                                    null,
                                    null,
                                    null
                                )
                            )
                            add(
                                RequestPoint(
                                    point,
                                    RequestPointType.WAYPOINT,
                                    null,
                                    null,
                                    null
                                )
                            )
                        }
                        drivingRouter.requestRoutes(
                            points,
                            drivingOptions,
                            vehicleOptions,
                            drivingRouteListener
                        )
                    }
                }
            }

            override fun edit(id: Long, name: String, lat: Double, lon: Double) {
                mapView.mapWindow.map.mapObjects.clear()
                val point = Point(lat, lon)
                val pin = ImageProvider.fromResource(this@MainActivity, R.drawable.pin)
                mapView.mapWindow.map.mapObjects.addPlacemark()
                    .apply {
                        geometry = point
                        setIcon(pin)
                    }
                binding.listview.visibility = View.INVISIBLE
                binding.saveGroup.visibility = View.VISIBLE
                binding.newPlace.visibility = View.VISIBLE

                val editInputListener = object : InputListener {
                    override fun onMapTap(map: Map, point: Point) {
                        mapView.mapWindow.map.mapObjects.clear()
                        val newLat = point.latitude.toString()
                        val newLon = point.longitude.toString()
                        mapView.mapWindow.map.mapObjects.addPlacemark()
                            .apply {
                                geometry = point
                                setIcon(pin)
                            }
                        binding.save.setOnClickListener {
                            val newName = binding.placeName.text.toString()
                            if (newName == "") {
                                binding.placeName.startAnimation(
                                    AnimationUtils.loadAnimation(
                                        this@MainActivity,
                                        R.anim.animation
                                    )
                                )
                            } else {
                                viewModel.save(id, newName, newLat, newLon)
                                binding.saveGroup.visibility = View.INVISIBLE
                                binding.newPlace.visibility = View.INVISIBLE
                            }

                        }
                        binding.close.setOnClickListener {
                            binding.saveGroup.visibility = View.INVISIBLE
                            binding.newPlace.visibility = View.INVISIBLE
                        }

                    }

                    override fun onMapLongTap(p0: Map, p1: Point) {}

                }
                binding.save.setOnClickListener {
                    binding.newPlace.startAnimation(
                        AnimationUtils.loadAnimation(this@MainActivity, R.anim.animation)
                    )
                }
                binding.close.setOnClickListener {
                    binding.saveGroup.visibility = View.INVISIBLE
                    binding.newPlace.visibility = View.INVISIBLE
                    return@setOnClickListener
                }
                mapView.mapWindow.map.addInputListener(editInputListener)
            }

        })
        binding.listview.adapter = adapter


        mapView.mapWindow.map.addTapListener(tapListener)
        mapView.mapWindow.map.addInputListener(inputListener)

        val mapKit = MapKitFactory.getInstance()
        val trafficJams = mapKit.createTrafficLayer(mapView.mapWindow)
        trafficJams.isTrafficVisible = false
        var opened = false

        binding.listViewButton.setOnClickListener { // Отображение списка мест
            if (opened) {
                binding.listview.visibility = View.INVISIBLE
                opened = false
            } else {
                binding.listview.visibility = View.VISIBLE
                opened = true
            }
        }

        binding.removeLocationButton.setOnClickListener { // Удаление всех меток из списка
            mapView.mapWindow.map.mapObjects.clear()
        }

        binding.trafficJamsButton.setOnClickListener { //Отображение загруженности дорог
            if (!trafficJams.isTrafficVisible) {
                trafficJams.isTrafficVisible = true
                binding.trafficJamsButton.setBackgroundResource(R.drawable.simpleblue)
            } else {
                trafficJams.isTrafficVisible = false
                binding.trafficJamsButton.setBackgroundResource(R.drawable.blueoff)
            }
        }

        binding.userLocationButton.setOnClickListener { // Возвращает камеру на местоположение пользователя
            val task = fusedLocationProviderClient.lastLocation
            task.addOnSuccessListener {
                if (it != null) {
                    startLocation = Point(it.latitude, it.longitude)
                    mapView.mapWindow.map.move(
                        CameraPosition(startLocation, 15f, 0f, 0f),
                        Animation(Animation.Type.LINEAR, 2f), null
                    )
                }
            }
        }

        binding.searchEdit.setOnEditorActionListener { v, actionId, event -> // Поиск мест
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val text = binding.searchEdit.text.toString()
                searchManager.submit(
                    text,
                    VisibleRegionUtils.toPolygon(mapView.mapWindow.map.visibleRegion),
                    searchOptions,
                    searchSessionListener
                )
            }
            true
        }





        viewModel.data.observe(this) { maps ->
            adapter.list = maps

        }
    }

    val searchSessionListener = object : Session.SearchListener {
        override fun onSearchResponse(response: Response) {
            mapView.mapWindow.map.mapObjects.clear()
            val mapObjects: MapObjectCollection = mapView.mapWindow.map.mapObjects
            for (searchResult in response.collection.children) {
                val resultLocation = searchResult.obj!!.geometry[0].point!!
                mapObjects.addPlacemark { placeMark: PlacemarkMapObject ->
                    placeMark.geometry = resultLocation
                    placeMark.setIcon(ImageProvider.fromResource(this@MainActivity, R.drawable.pin))
                }

            }
        }

        override fun onSearchError(p0: com.yandex.runtime.Error) {
            var errorMessage = "Unknown error!"
            if (p0 is RemoteError) {
                errorMessage = "Remote Error!"
            } else if (p0 is NetworkError) {
                errorMessage = "Network error!"
            }
            makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
        }


    }

    val drivingRouteListener = object : DrivingSession.DrivingRouteListener {
        override fun onDrivingRoutes(drivingRoutes: MutableList<DrivingRoute>) {
            for (route in drivingRoutes) {
                mapView.mapWindow.map.mapObjects.addPolyline(route.geometry)
            }
        }

        override fun onDrivingRoutesError(p0: com.yandex.runtime.Error) {
            var errorMessage = "Unknown error!"
            if (p0 is RemoteError) {
                errorMessage = "Remote Error!"
            } else if (p0 is NetworkError) {
                errorMessage = "Network error!"
            }
            makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
        }


    }


    private val tapListener =
        GeoObjectTapListener { geoObjectTapEvent -> // Выделение здания синим цветом
            val selectionMetadata = geoObjectTapEvent
                .geoObject
                .metadataContainer
                .getItem(GeoObjectSelectionMetadata::class.java)
            mapView.mapWindow.map.selectGeoObject(selectionMetadata)
            false
        }

    private val searchListener =
        object : Session.SearchListener { // Получение информации о месте клика на карте
            override fun onSearchResponse(response: Response) {
                val street = response.collection.children.firstOrNull()?.obj
                    ?.metadataContainer
                    ?.getItem(ToponymObjectMetadata::class.java)
                    ?.address
                    ?.components
                    ?.firstOrNull { it.kinds.contains(Address.Component.Kind.STREET) }
                    ?.name ?: "Информация об улице не найдена"

                Toast.makeText(applicationContext, street, Toast.LENGTH_SHORT).show()
            }

            override fun onSearchError(p0: com.yandex.runtime.Error) {
                var errorMessage = "Неизвестная Ошибка!"
                if (p0 is RemoteError) {
                    errorMessage = "Беспрводная ошибка!"
                } else if (p0 is NetworkError) {
                    errorMessage = "Проблема с интрнетом!"
                }
                Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
            }


        }


    private val inputListener = object : InputListener { // Обработка клика по карте
        override fun onMapTap(map: Map, point: Point) {
            searchSession = searchManager.submit(point, 20, SearchOptions(), searchListener)
        }

        override fun onMapLongTap(map: Map, point: Point) {
            AlertDialog.Builder(this@MainActivity)
                .setTitle("Отметить данное место?")
                .setPositiveButton("Да") { _: DialogInterface, _: Int ->
                    mapView.mapWindow.map.mapObjects.clear()
                    val pin = ImageProvider.fromResource(this@MainActivity, R.drawable.pin)
                    mapView.mapWindow.map.mapObjects.addPlacemark()// Выставление метки в нужно место на карте
                        .apply {
                            geometry = point
                            setIcon(pin)
                        }
                    binding.saveGroup.visibility = View.VISIBLE
                    binding.save.setOnClickListener { // Добавление метки в базу данных
                        val name = binding.placeName.text.toString()
                        val lat = point.latitude.toString()
                        val lon = point.longitude.toString()
                        viewModel.save(0, name, lat, lon)
                        binding.saveGroup.visibility = View.INVISIBLE
                    }
                    binding.close.setOnClickListener {
                        binding.saveGroup.visibility = View.INVISIBLE
                        mapView.mapWindow.map.mapObjects.clear()
                    }


                }
                .setNegativeButton("НЕТ") { _: DialogInterface, _: Int -> }
                .show()
        }


    }


    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()

    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onObjectAdded(userLocationView: UserLocationView) {

    }

    override fun onObjectRemoved(p0: UserLocationView) {

    }

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {

    }

}