package com.example.firebaseubi.logica

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.firebaseubi.R
import com.example.firebaseubi.databinding.ActivityCambiarEstadoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CambiarEstado : AppCompatActivity() {
    private lateinit var binding: ActivityCambiarEstadoBinding
    private lateinit var databaseRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCambiarEstadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        userId = mAuth.currentUser?.uid
        databaseRef = FirebaseDatabase.getInstance().reference.child("users").child(userId!!)

        cargarEstadoActual()

        binding.btnDisponible.setOnClickListener { cambiarEstado("Disponible") }
        binding.btnNoDisponible.setOnClickListener { cambiarEstado("No disponible") }
    }

    private fun cargarEstadoActual() {
        databaseRef.child("estado").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val estado = snapshot.value.toString()
                    binding.estadoActualTextView.text = "Estado actual: $estado"
                } else {
                    binding.estadoActualTextView.text = "Estado actual: No disponible"
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar el estado actual.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cambiarEstado(nuevoEstado: String) {
        databaseRef.child("estado").setValue(nuevoEstado)
            .addOnSuccessListener {
                binding.estadoActualTextView.text = "Estado actual: $nuevoEstado"
                Toast.makeText(this, "Estado cambiado a $nuevoEstado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cambiar el estado.", Toast.LENGTH_SHORT).show()
            }
    }
}