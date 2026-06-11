package com.ifpr.androidapptemplate.ui.usuario

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.ui.cadastro.AprovacaoRepository
import com.ifpr.androidapptemplate.ui.cadastro.MaterialRepository
import com.ifpr.androidapptemplate.ui.login.LoginActivity
import kotlinx.coroutines.launch

class PerfilUsuarioFragment : Fragment() {

    private val aprovacaoRepo = AprovacaoRepository()
    private val materialRepo = MaterialRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_perfil_usuario, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser

        val tvNome = view.findViewById<TextView>(R.id.tvNome)
        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
        val tvPapel = view.findViewById<TextView>(R.id.tvPapel)
        val tvIniciais = view.findViewById<TextView>(R.id.tvIniciais)
        val tvTotalMateriais = view.findViewById<TextView>(R.id.tvTotalMateriais)
        val btnTrocarPapel = view.findViewById<Button>(R.id.btnTrocarPapel)
        val btnSair = view.findViewById<Button>(R.id.btnSair)

        val nome = user?.displayName ?: user?.email?.substringBefore("@") ?: "Usuário"
        val email = user?.email ?: ""
        tvNome.text = nome
        tvEmail.text = email
        tvIniciais.text = nome.firstOrNull()?.uppercase() ?: "U"

        lifecycleScope.launch {
            val papel = aprovacaoRepo.getPapelUsuario()
            tvPapel.text = if (papel == "aprovador") "Aprovador" else "Cadastrador"

            val resultado = materialRepo.listarMeusMateriais()
            resultado.onSuccess { lista ->
                tvTotalMateriais.text = "${lista.size}"
            }
        }

        btnTrocarPapel.setOnClickListener {
            val opcoes = arrayOf("Cadastrador", "Aprovador")
            AlertDialog.Builder(requireContext())
                .setTitle("Selecionar papel")
                .setItems(opcoes) { _, which ->
                    val novoPapel = if (which == 0) "cadastrador" else "aprovador"
                    lifecycleScope.launch {
                        aprovacaoRepo.setPapelUsuario(novoPapel)
                        tvPapel.text = if (novoPapel == "aprovador") "Aprovador" else "Cadastrador"
                        Toast.makeText(requireContext(), "Papel atualizado!", Toast.LENGTH_SHORT).show()
                    }
                }
                .show()
        }

        btnSair.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}