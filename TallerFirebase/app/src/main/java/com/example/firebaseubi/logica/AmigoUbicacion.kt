package com.example.firebaseubi.logica

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.firebaseubi.R

class AmigoUbicacion : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_amigo_ubicacion)

        // Obtener los datos enviados
        val nombre = intent.getStringExtra("nombre")
        val latitud = intent.getDoubleExtra("latitud", 0.0)
        val longitud = intent.getDoubleExtra("longitud", 0.0)

        val nombreTxt = findViewById<TextView>(R.id.nombreTextView)
        val latitudTxt = findViewById<TextView>(R.id.latitudTextView)
        val longitudTxt = findViewById<TextView>(R.id.longitudTextView)

        nombreTxt.text = nombre
        latitudTxt.text = latitud.toString()
        longitudTxt.text = longitud.toString()
    }
}
