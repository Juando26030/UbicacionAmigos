package com.example.firebaseubi.logica

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firebaseubi.R
import com.example.firebaseubi.Usuario
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.math.*

class AmigoUbicacion : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var googleMap: GoogleMap
    private var latitudUsuario: Double = 0.0
    private var longitudUsuario: Double = 0.0
    private lateinit var nombreUsuario: String
    private var amigoLatitud: Double = 0.0
    private var amigoLongitud: Double = 0.0
    private lateinit var nombreAmigo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_amigo_ubicacion)

        // Configurar Firebase Auth y Database
        mAuth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference.child("users")

        // Obtener el mapa asíncronamente
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Obtener los datos del amigo desde el Intent
        nombreAmigo = intent.getStringExtra("nombre") ?: "Amigo"
        amigoLatitud = intent.getDoubleExtra("latitud", 0.0)
        amigoLongitud = intent.getDoubleExtra("longitud", 0.0)

        // Obtener los datos del usuario autenticado
        obtenerDatosUsuario()
    }

    private fun obtenerDatosUsuario() {
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            databaseRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val usuario = snapshot.getValue(Usuario::class.java)
                    if (usuario != null) {
                        nombreUsuario = usuario.nombre ?: "Usuario"
                        latitudUsuario = usuario.latitud ?: 0.0
                        longitudUsuario = usuario.longitud ?: 0.0

                        // Mostrar los datos en los TextViews
                        findViewById<TextView>(R.id.nombreTextView).text = "Acosando a: $nombreAmigo"

                        // Agregar los marcadores si el mapa ya está listo
                        if (::googleMap.isInitialized) {
                            agregarMarcadores()
                        }
                    } else {
                        Log.e("AmigoUbicacion", "El objeto Usuario es null")
                        Toast.makeText(this@AmigoUbicacion, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("AmigoUbicacion", "Error al leer datos: ${error.message}")
                    Toast.makeText(this@AmigoUbicacion, "Error al obtener datos de usuario.", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Agregar los marcadores si los datos del usuario ya se obtuvieron
        if (latitudUsuario != 0.0 && longitudUsuario != 0.0) {
            agregarMarcadores()
        }
    }

    private fun agregarMarcadores() {
        // Crear LatLng para el usuario autenticado y el amigo
        val usuarioLocation = LatLng(latitudUsuario, longitudUsuario)
        val amigoLocation = LatLng(amigoLatitud, amigoLongitud)

        // Añadir un marcador rojo en la ubicación del usuario autenticado
        googleMap.addMarker(
            MarkerOptions()
                .position(usuarioLocation)
                .title(nombreUsuario)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        // Añadir un marcador verde en la ubicación del amigo
        googleMap.addMarker(
            MarkerOptions()
                .position(amigoLocation)
                .title(nombreAmigo)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )

        // Calcular y mostrar la distancia entre el usuario y el amigo
        val distancia = calcularDistancia(usuarioLocation, amigoLocation)
        findViewById<TextView>(R.id.distanciaTextView).text = "Distancia entre tu amigo y tú: $distancia km"

        // Ajustar la cámara para mostrar ambos marcadores
        val bounds = LatLngBounds.builder()
            .include(usuarioLocation)
            .include(amigoLocation)
            .build()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100)) // 100 es el padding para los bordes
    }

    private fun calcularDistancia(loc1: LatLng, loc2: LatLng): Double {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(loc2.latitude - loc1.latitude)
        val dLon = Math.toRadians(loc2.longitude - loc1.longitude)

        val lat1 = Math.toRadians(loc1.latitude)
        val lat2 = Math.toRadians(loc2.latitude)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                sin(dLon / 2) * sin(dLon / 2) * cos(lat1) * cos(lat2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return String.format("%.2f", earthRadiusKm * c).toDouble() // Redondea a 2 decimales
    }
}
