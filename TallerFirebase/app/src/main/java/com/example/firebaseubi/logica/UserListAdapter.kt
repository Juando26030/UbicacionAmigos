package com.example.firebaseubi.logica

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebaseubi.R
import com.example.firebaseubi.Usuario

class UserListAdapter(
    private val users: List<Usuario>,
    private val onUserClick: (Usuario) -> Unit // Función lambda para manejar el clic
) : RecyclerView.Adapter<UserListAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreTextView: TextView = itemView.findViewById(R.id.nombreTextView)
        val perfilImagenView: ImageView = itemView.findViewById(R.id.imageView)
        val button: Button = itemView.findViewById(R.id.button) // Botón para "acosar"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.usuario_card, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.nombreTextView.text = user.nombre

        // Cargar la imagen desde Uri con Glide
        user.imagenUri?.let { uri ->
            Glide.with(holder.itemView.context)
                .load(uri)
                .placeholder(R.color.black) // Imagen temporal mientras carga
                .error(R.color.red) // Imagen de error en caso de fallo
                .into(holder.perfilImagenView)
        }

        // Configurar el botón "Acosar a mi amigo"
        holder.button.setOnClickListener {
            onUserClick(user)
        }
    }

    override fun getItemCount(): Int = users.size
}
