package com.example.firebaseubi

import android.net.Uri

// Clase Usuario con los atributos nombre, latitud, longitud, y la URI de la imagen
data class Usuario(
    val nombre: String? = null,
    val estado: String? = null,
    val imagenUri: Uri? = null, // Usar Uri para la imagen en lugar de String
    val latitud: Double? = null,
    val longitud: Double? = null
)
