package com.example.firebaseubi.logica

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firebaseubi.R
import com.example.firebaseubi.Usuario
import com.example.firebaseubi.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference.child("users")

        binding.btnIniciarSesion.setOnClickListener {
            val email = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            signInUser(email, password)
        }

        // Botón de registrarse (redirecciona a RegistroActivity)
        binding.btnRegistrarse.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        // Verificar si el usuario ya está autenticado
        val currentUser = mAuth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            // Obtener datos del usuario autenticado
            val userId = currentUser.uid
            databaseRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val usuario = snapshot.getValue(Usuario::class.java)
                    if (usuario != null) {
                        Log.e("MainActivity", "Datos del usuario obtenidos: nombre=${usuario.nombre}, latitud=${usuario.latitud}, longitud=${usuario.longitud}")

                        if (usuario.latitud != null && usuario.longitud != null) {
                            // Pasar los datos del usuario a MapaUbicaciones
                            val intent = Intent(this@MainActivity, MapaUbicaciones::class.java).apply {
                                putExtra("nombre", usuario.nombre)
                                putExtra("latitud", usuario.latitud)
                                putExtra("longitud", usuario.longitud)
                            }
                            Log.e("MainActivity", "Enviando datos al Intent: nombre=${usuario.nombre}, latitud=${usuario.latitud}, longitud=${usuario.longitud}")
                            startActivity(intent)
                            finish() // Finaliza MainActivity para evitar regresar
                        } else {
                            Log.e("MainActivity", "Latitud o longitud del usuario son null")
                            Toast.makeText(this@MainActivity, "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("MainActivity", "El objeto Usuario es null")
                        Toast.makeText(this@MainActivity, "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MainActivity", "Error al leer datos: ${error.message}")
                    Toast.makeText(this@MainActivity, "Error al obtener datos de usuario.", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // Limpiar los campos si el usuario no está autenticado
            binding.usernameEditText.text?.clear()
            binding.passwordEditText.text?.clear()
        }
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = binding.usernameEditText.text.toString()
        if (email.isEmpty()) {
            binding.usernameInputLayout.error = "Requerido"
            valid = false
        } else {
            binding.usernameInputLayout.error = null
        }

        val password = binding.passwordEditText.text.toString()
        if (password.isEmpty()) {
            binding.passwordInputLayout.error = "Requerido"
            valid = false
        } else {
            binding.passwordInputLayout.error = null
        }
        return valid
    }

    private fun signInUser(email: String, password: String) {
        if (!validateForm()) return

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.e("MainActivity", "signInWithEmail: success")
                    val user = mAuth.currentUser
                    updateUI(user)
                } else {
                    Log.e("MainActivity", "signInWithEmail: failure", task.exception)
                    Toast.makeText(
                        baseContext, "Autenticación fallida.",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }
            }
    }
}
