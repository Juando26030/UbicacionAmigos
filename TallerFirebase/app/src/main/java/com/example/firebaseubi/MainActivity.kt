package com.example.firebaseubi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firebaseubi.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

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
            // Redirigir a PantallaPrincipalActivity si el usuario está autenticado
            val intent = Intent(this, PantallaPrincipalActivity::class.java)
            intent.putExtra("user", currentUser.email)
            startActivity(intent)
            finish() // Finaliza MainActivity para evitar regresar
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
                    Log.d("MainActivity", "signInWithEmail: success")
                    val user = mAuth.currentUser
                    updateUI(user)
                } else {
                    Log.w("MainActivity", "signInWithEmail: failure", task.exception)
                    Toast.makeText(
                        baseContext, "Autenticación fallida.",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }
            }
    }
}