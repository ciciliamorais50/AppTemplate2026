package com.ifpr.androidapptemplate.ui.usuario

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Usuario

class PerfilUsuarioFragment : Fragment() {

    private lateinit var userProfileImageView: ImageView
    private lateinit var registerNameEditText: EditText
    private lateinit var registerEmailEditText: EditText
    private lateinit var registerEnderecoEditText: EditText
    private lateinit var registerPasswordEditText: EditText
    private lateinit var registerConfirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var sairButton: Button
    private lateinit var usersReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_perfil_usuario, container, false)

        auth = FirebaseAuth.getInstance()

        userProfileImageView = view.findViewById(R.id.userProfileImageView)
        registerNameEditText = view.findViewById(R.id.registerNameEditText)
        registerEmailEditText = view.findViewById(R.id.registerEmailEditText)
        registerEnderecoEditText = view.findViewById(R.id.registerEnderecoEditText)
        registerPasswordEditText = view.findViewById(R.id.registerPasswordEditText)
        registerConfirmPasswordEditText = view.findViewById(R.id.registerConfirmPasswordEditText)
        registerButton = view.findViewById(R.id.salvarButton)
        sairButton = view.findViewById(R.id.sairButton)

        usersReference = FirebaseDatabase.getInstance().getReference("users")

        val user = auth.currentUser

        if (user != null) {
            sairButton.visibility = View.VISIBLE
            registerPasswordEditText.visibility = View.GONE
            registerConfirmPasswordEditText.visibility = View.GONE
            registerEmailEditText.isEnabled = false
        }

        user?.let {
            val photoUrl = it.photoUrl

            if (photoUrl != null && photoUrl.toString().isNotEmpty()) {
                Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.mipmap.ic_default_user)
                    .error(R.mipmap.ic_default_user)
                    .into(userProfileImageView)
            } else {
                userProfileImageView.setImageResource(R.mipmap.ic_default_user)
            }
        }

        registerButton.setOnClickListener {
            updateUser()
        }

        sairButton.setOnClickListener {
            signOut()
        }

        return view
    }

    private fun signOut() {
        auth.signOut()
        Toast.makeText(context, "Logout realizado com sucesso!", Toast.LENGTH_SHORT).show()
        requireActivity().finish()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userFirebase = auth.currentUser
        if (userFirebase != null) {
            registerNameEditText.setText(userFirebase.displayName)
            registerEmailEditText.setText(userFirebase.email)

            recuperarDadosUsuario(userFirebase.uid)
        }
    }

    fun recuperarDadosUsuario(usuarioKey: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("users")

        databaseReference.child(usuarioKey)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val usuario = snapshot.getValue(Usuario::class.java)
                        usuario?.let {
                            registerEnderecoEditText.setText(it.endereco ?: "")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Erro ao recuperar dados: ${error.message}")
                }
            })
    }

    private fun updateUser() {
        val name = registerNameEditText.text.toString().trim()
        val endereco = registerEnderecoEditText.text.toString().trim()

        val user = auth.currentUser

        if (user != null) {
            updateProfile(user, name, endereco)
        } else {
            Toast.makeText(context, "Usuário não encontrado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfile(user: FirebaseUser, displayName: String, endereco: String) {

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()

        val usuario = Usuario(
            user.uid,
            displayName,
            user.email,
            endereco
        )

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveUserToDatabase(usuario)
                } else {
                    Toast.makeText(
                        context,
                        "Erro ao atualizar perfil",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun saveUserToDatabase(usuario: Usuario) {
        usersReference.child(usuario.key!!)
            .setValue(usuario)
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    "Usuário atualizado com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()

                // 🔥 REMOVIDO o popBackStack (causava retorno ao login)
            }
            .addOnFailureListener {
                Toast.makeText(
                    context,
                    "Erro ao salvar usuário",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}