package com.ifpr.androidapptemplate.ui.usuario

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Usuario
import com.ifpr.androidapptemplate.databinding.FragmentPerfilUsuarioBinding

class PerfilUsuarioFragment : Fragment() {

    private var _binding: FragmentPerfilUsuarioBinding? = null
    private val binding get() = _binding!!

    private lateinit var usersReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // CORREÇÃO: Inicializar o View Binding corretamente
        _binding = FragmentPerfilUsuarioBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        usersReference = FirebaseDatabase.getInstance().getReference("users")

        setupUI()

        return binding.root
    }

    private fun setupUI() {
        val user = auth.currentUser

        if (user != null) {
            binding.sairButton.visibility = View.VISIBLE
            binding.registerPasswordEditText.visibility = View.GONE
            binding.registerConfirmPasswordEditText.visibility = View.GONE
            binding.registerEmailEditText.isEnabled = false

            // Carregar foto
            val photoUrl = user.photoUrl
            if (photoUrl != null && photoUrl.toString().isNotEmpty()) {
                Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.mipmap.ic_default_user)
                    .error(R.mipmap.ic_default_user)
                    .into(binding.userProfileImageView)
            }
        }

        binding.salvarButton.setOnClickListener {
            updateUser()
        }

        binding.sairButton.setOnClickListener {
            signOut()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userFirebase = auth.currentUser
        if(userFirebase != null){
            binding.registerNameEditText.setText(userFirebase.displayName)
            binding.registerEmailEditText.setText(userFirebase.email)
            recuperarDadosUsuario(userFirebase.uid)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun recuperarDadosUsuario(usuarioKey: String) {
        usersReference.child(usuarioKey).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val usuario = snapshot.getValue(Usuario::class.java)
                    usuario?.let {
                        binding.registerEnderecoEditText.setText(it.endereco ?: "")
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Erro: ${error.message}")
            }
        })
    }

    private fun updateUser() {
        val name = binding.registerNameEditText.text.toString().trim()
        val endereco = binding.registerEnderecoEditText.text.toString().trim()
        val user = auth.currentUser

        if (user != null) {
            updateProfile(user, name, endereco)
        } else {
            Toast.makeText(context, "Usuário não logado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfile(user: FirebaseUser, displayName: String, endereco: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()

        val usuario = Usuario(user.uid, displayName, user.email, endereco)

        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                saveUserToDatabase(usuario)
            } else {
                Toast.makeText(context, "Erro ao atualizar perfil.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserToDatabase(usuario: Usuario) {
        usersReference.child(usuario.key.toString()).setValue(usuario)
            .addOnSuccessListener {
                Toast.makeText(context, "Perfil atualizado!", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            }
    }

    private fun signOut() {
        auth.signOut()
        requireActivity().finish()
    }
}