package com.example.firebaseubi.logica

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.firebaseubi.databinding.ActivityRegistroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var storageRef: StorageReference
    private lateinit var databaseRef: DatabaseReference
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference
        databaseRef = FirebaseDatabase.getInstance().reference.child("users")

        binding.btnRegresar.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.imageView.setOnClickListener { openImageSelector() }
        binding.btnRegistro.setOnClickListener { registrarUsuario() }
    }

    private fun registrarUsuario() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val nombre = binding.usernameEditText.text.toString().trim()
        val apellido = binding.apellidoEditText.text.toString().trim()
        val cedula = binding.cedulaEditText.text.toString().trim()
        val latitud = binding.latitudEditText.text.toString().trim()
        val longitud = binding.longitudEditText.text.toString().trim()

        // Validar campos
        if (!validarCampos(email, password, nombre, apellido, cedula,latitud, longitud)) {
            return
        }

        val progressDialog = ProgressDialog(this).apply {
            setMessage("Registrando usuario...")
            setCancelable(false)
            show()
        }

        // 1. Crear usuario en Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid ?: return@addOnSuccessListener

                // 2. Si hay imagen, subirla a Storage
                if (imageUri != null) {
                    subirImagenYGuardarDatos(userId, nombre, apellido, cedula, latitud, longitud, progressDialog)
                } else {
                    // Si no hay imagen, guardar datos directamente
                    guardarDatosUsuario(userId, nombre, apellido, cedula, latitud, longitud, progressDialog)
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                val mensaje = when (e) {
                    is FirebaseAuthWeakPasswordException -> "La contraseña debe tener al menos 6 caracteres"
                    is FirebaseAuthInvalidCredentialsException -> "Email inválido"
                    is FirebaseAuthUserCollisionException -> "Este email ya está registrado"
                    else -> "Error en el registro: ${e.message}"
                }
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
            }
    }

    private fun subirImagenYGuardarDatos(
        userId: String,
        nombre: String,
        apellido: String,
        cedula: String,
        latitud: String,
        longitud: String,
        progressDialog: ProgressDialog
    ) {
        // Crear referencia a la imagen usando el userId
        val imageRef = storageRef.child("profileImages/$userId.jpg")

        imageUri?.let { uri ->
            // Subir imagen a Firebase Storage
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    // Una vez subida la imagen, guardar los datos del usuario
                    guardarDatosUsuario(userId, nombre, apellido, cedula, latitud, longitud, progressDialog)
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Error al subir imagen: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun guardarDatosUsuario(
        userId: String,
        nombre: String,
        apellido: String,
        cedula: String,
        latitud: String,
        longitud: String,
        progressDialog: ProgressDialog
    ) {
        val email = mAuth.currentUser?.email ?: "Correo no disponible"

        // Convertimos latitud y longitud a Double
        val lat = latitud.toDoubleOrNull() ?: 0.0 // Valor predeterminado si es nulo
        val lon = longitud.toDoubleOrNull() ?: 0.0 // Valor predeterminado si es nulo

        // Crear el mapa de datos del usuario, con latitud y longitud como Double
        val userData = hashMapOf(
            "nombre" to nombre,
            "apellido" to apellido,
            "cedula" to cedula,
            "latitud" to lat, // Ahora es Double
            "longitud" to lon, // Ahora es Double
            "correo" to email,
            "estado" to "Disponible"
        )

        // Guardar datos en Realtime Database
        databaseRef.child(userId).setValue(userData)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                mAuth.signOut()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }



    private fun validarCampos(email: String, password: String, nombre: String, apellido: String, cedula: String, latitud: String, longitud: String): Boolean {
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditText.error = "Email inválido"
            return false
        }
        if (password.isEmpty() || password.length < 6) {
            binding.passwordEditText.error = "La contraseña debe tener al menos 6 caracteres"
            return false
        }
        if (nombre.isEmpty()) {
            binding.usernameEditText.error = "Nombre requerido"
            return false
        }
        if (apellido.isEmpty()) {
            binding.apellidoEditText.error = "Apellido requerido"
            return false
        }
        if (cedula.isEmpty()) {
            binding.cedulaEditText.error = "Cédula requerida"
            return false
        }
        // Validar latitud
        val lat = latitud.toDoubleOrNull()
        if (lat == null || lat < -90 || lat > 90) {
            binding.latitudEditText.error = "Latitud debe estar entre -90 y 90"
            return false
        }
        // Validar longitud
        val lon = longitud.toDoubleOrNull()
        if (lon == null || lon < -180 || lon > 180) {
            binding.longitudEditText.error = "Longitud debe estar entre -180 y 180"
            return false
        }
        return true
    }


    // Métodos para manejar la selección de imagen
    private fun openImageSelector() {
        val options = arrayOf("Tomar Foto", "Seleccionar de Galería")
        AlertDialog.Builder(this)
            .setTitle("Seleccionar Imagen")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkAndRequestCameraPermission()
                    1 -> checkAndRequestGalleryPermission()
                }
            }
            .show()
    }

    // Los métodos de permisos y manejo de cámara/galería permanecen iguales...

    private fun checkAndRequestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showPermissionExplanationDialog(
                    "Permiso de Cámara",
                    "Se necesita acceso a la cámara para tomar la foto de perfil.",
                    Manifest.permission.CAMERA,
                    CAMERA_REQUEST_CODE
                )
            }
            else -> {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
            }
        }
    }

    private fun checkAndRequestGalleryPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                openGallery()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                showPermissionExplanationDialog(
                    "Permiso de Galería",
                    "Se necesita acceso a la galería para seleccionar la foto de perfil.",
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    GALLERY_REQUEST_CODE
                )
            }
            else -> {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), GALLERY_REQUEST_CODE)
            }
        }
    }

    private fun showPermissionExplanationDialog(title: String, message: String, permission: String, requestCode: Int) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Aceptar") { _, _ ->
                ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val photo = result.data?.extras?.get("data") as Bitmap
            val uri = saveBitmapToUri(photo)
            imageUri = uri
            binding.imageView.setImageURI(uri)
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            binding.imageView.setImageURI(imageUri)
        }
    }

    private fun saveBitmapToUri(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Profile_${System.currentTimeMillis()}", null)
        return Uri.parse(path)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
                }
            }
            GALLERY_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(this, "Permiso de galería denegado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 1001
        private const val GALLERY_REQUEST_CODE = 1002
    }
}

