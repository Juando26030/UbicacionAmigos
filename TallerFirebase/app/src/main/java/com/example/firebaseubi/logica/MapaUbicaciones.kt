package com.example.firebaseubi.logica

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.firebaseubi.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class MapaUbicaciones : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa_ubicaciones)

        // Initialize MapView
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permissions if not already granted
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
            )
            return
        }

        // Enable user's location on the map
        map.isMyLocationEnabled = true

        // Obtener los datos del usuario pasados por el intent
        val nombre = intent.getStringExtra("nombre")
        val latitud = intent.getDoubleExtra("latitud", Double.NaN)
        val longitud = intent.getDoubleExtra("longitud", Double.NaN)

        Log.d("MapaUbicaciones", "Datos recibidos: nombre=$nombre, latitud=$latitud, longitud=$longitud")

        if (!latitud.isNaN() && !longitud.isNaN()) {
            // Crear un LatLng y agregar un marcador rojo para el usuario autenticado
            val userLocation = LatLng(latitud, longitud)
            map.addMarker(
                MarkerOptions()
                    .position(userLocation)
                    .title(nombre ?: "Ubicación del usuario")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)) // Marcador rojo
            )
            // Mover la cámara a la ubicación del usuario
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
        } else {
            Log.e("MapaUbicaciones", "Latitud o longitud no válida")
            Toast.makeText(this, "Coordenadas no encontradas.", Toast.LENGTH_SHORT).show()
        }

        // Agregar marcadores azules para las posiciones del JSON
        addMarkersFromJson()
    }

    private fun addMarkersFromJson() {
        try {
            // Leer archivo JSON desde assets
            val inputStream = assets.open("locations.json")
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = bufferedReader.use { it.readText() }

            // Parsear el JSON
            val jsonObject = JSONObject(jsonString)
            val locationsArray = jsonObject.getJSONArray("locationsArray")

            // Iterar sobre los elementos del array y añadir marcadores
            for (i in 0 until locationsArray.length()) {
                val location = locationsArray.getJSONObject(i)
                val lat = location.getDouble("latitude")
                val lng = location.getDouble("longitude")
                val name = location.getString("name")

                val position = LatLng(lat, lng)
                map.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(name)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)) // Marcador azul
                )
            }

            // Enfocar la cámara en el primer marcador
            if (locationsArray.length() > 0) {
                val firstLocation = locationsArray.getJSONObject(0)
                val firstLat = firstLocation.getDouble("latitude")
                val firstLng = firstLocation.getDouble("longitude")
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(firstLat, firstLng), 12f))
            }

        } catch (e: Exception) {
            Log.e("MapaUbicaciones", "Error al leer el JSON", e)
            Toast.makeText(this, "Error al cargar ubicaciones", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
