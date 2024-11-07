package com.example.firebaseubi.logica

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebaseubi.R
import com.example.firebaseubi.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ListaAmigos : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var userListAdapter: UserListAdapter
    private lateinit var recyclerView: RecyclerView
    private val usersList = mutableListOf<Usuario>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_amigos)

        mAuth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference.child("users")

        // Configurar el RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inicializar el adaptador con el clic en el botón
        userListAdapter = UserListAdapter(usersList) { usuario ->
            // Al hacer clic en el botón, se abre AmigoUbicacion con la info del usuario
            val intent = Intent(this, AmigoUbicacion::class.java).apply {
                putExtra("nombre", usuario.nombre)
                putExtra("latitud", usuario.latitud)
                putExtra("longitud", usuario.longitud)
            }
            startActivity(intent)
        }

        recyclerView.adapter = userListAdapter

        // Cargar los usuarios disponibles
        cargarUsuariosDisponibles()
    }

    private fun cargarUsuariosDisponibles() {
        databaseRef.orderByChild("estado").equalTo("Disponible")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    usersList.clear()
                    for (userSnapshot in snapshot.children) {
                        if (userSnapshot.key != mAuth.currentUser?.uid) {
                            val nombre = userSnapshot.child("nombre").value?.toString()
                            val correo = userSnapshot.child("correo").value?.toString()
                            val latitud = userSnapshot.child("latitud").value?.toString()?.toDoubleOrNull()
                            val longitud = userSnapshot.child("longitud").value?.toString()?.toDoubleOrNull()
                            val imagenUrl = userSnapshot.child("imagenUri").value?.toString()

                            if (nombre != null && latitud != null && longitud != null) {
                                val usuario = Usuario(
                                    nombre = nombre,
                                    estado = "Disponible",
                                    imagenUri = imagenUrl?.let { Uri.parse(it) },
                                    latitud = latitud,
                                    longitud = longitud
                                )
                                usersList.add(usuario)
                            }
                        }
                    }
                    userListAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejar error si ocurre
                }
            })
    }
}
