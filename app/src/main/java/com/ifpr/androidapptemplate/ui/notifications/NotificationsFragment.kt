package com.ifpr.androidapptemplate.ui.notifications

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.ui.cadastro.AprovacaoRepository
import com.ifpr.androidapptemplate.ui.cadastro.ClassesMaterial
import com.ifpr.androidapptemplate.ui.cadastro.NotificacaoHelper
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {

    private val repository = AprovacaoRepository()
    private var papelAtual = "cadastrador"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        NotificacaoHelper.criarCanal(requireContext())

        val tvPapel = view.findViewById<TextView>(R.id.tvPapel)
        val btnTrocar = view.findViewById<Button>(R.id.btnTrocarPapel)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvVazio = view.findViewById<TextView>(R.id.tvVazio)
        val container = view.findViewById<LinearLayout>(R.id.containerNotificacoes)

        lifecycleScope.launch {
            papelAtual = repository.getPapelUsuario()
            tvPapel.text = if (papelAtual == "aprovador") "Aprovador" else "Cadastrador"
            carregarNotificacoes(progressBar, tvVazio, container)
        }

        btnTrocar.setOnClickListener {
            val opcoes = arrayOf("Cadastrador", "Aprovador")
            AlertDialog.Builder(requireContext())
                .setTitle("Selecionar papel")
                .setItems(opcoes) { _, which ->
                    val novoPapel = if (which == 0) "cadastrador" else "aprovador"
                    lifecycleScope.launch {
                        repository.setPapelUsuario(novoPapel)
                        papelAtual = novoPapel
                        tvPapel.text = if (novoPapel == "aprovador") "Aprovador" else "Cadastrador"
                        carregarNotificacoes(progressBar, tvVazio, container)
                    }
                }
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        val progressBar = view?.findViewById<ProgressBar>(R.id.progressBar) ?: return
        val tvVazio = view?.findViewById<TextView>(R.id.tvVazio) ?: return
        val container = view?.findViewById<LinearLayout>(R.id.containerNotificacoes) ?: return
        lifecycleScope.launch {
            papelAtual = repository.getPapelUsuario()
            carregarNotificacoes(progressBar, tvVazio, container)
        }
    }

    private suspend fun carregarNotificacoes(
        progressBar: ProgressBar,
        tvVazio: TextView,
        container: LinearLayout
    ) {
        progressBar.visibility = View.VISIBLE
        container.removeAllViews()
        tvVazio.visibility = View.GONE

        if (papelAtual == "aprovador") {
            carregarPendentes(progressBar, tvVazio, container)
        } else {
            carregarMeusMateriais(progressBar, tvVazio, container)
        }
    }

    private suspend fun carregarPendentes(
        progressBar: ProgressBar,
        tvVazio: TextView,
        container: LinearLayout
    ) {
        val resultado = repository.listarPendentes()
        progressBar.visibility = View.GONE

        resultado.onSuccess { lista ->
            if (lista.isEmpty()) {
                tvVazio.visibility = View.VISIBLE
                tvVazio.text = "Nenhum material pendente de aprovação"
                return
            }

            NotificacaoHelper.notificarPendentes(requireContext(), lista.size)

            for (item in lista) {
                val card = criarCardPendente(item.id, item.codigo, item.classe, item.descricao, container)
                container.addView(card)
            }
        }.onFailure {
            tvVazio.visibility = View.VISIBLE
            tvVazio.text = "Erro ao carregar: ${it.message}"
        }
    }

    private suspend fun carregarMeusMateriais(
        progressBar: ProgressBar,
        tvVazio: TextView,
        container: LinearLayout
    ) {
        val resultado = repository.listarMeusMateriais()
        progressBar.visibility = View.GONE

        resultado.onSuccess { lista ->
            if (lista.isEmpty()) {
                tvVazio.visibility = View.VISIBLE
                tvVazio.text = "Você ainda não cadastrou nenhum material"
                return
            }

            for (item in lista) {
                val card = criarCardStatus(item.codigo, item.classe, item.descricao, item.status, item.comentario)
                container.addView(card)
            }
        }.onFailure {
            tvVazio.visibility = View.VISIBLE
            tvVazio.text = "Erro ao carregar: ${it.message}"
        }
    }

    private fun criarCardPendente(
        id: String, codigo: String, classe: String, descricao: String,
        container: LinearLayout
    ): View {
        val card = layoutInflater.inflate(R.layout.item_notificacao, container, false)

        card.findViewById<TextView>(R.id.tvCodigo).text = codigo
        card.findViewById<TextView>(R.id.tvDescricao).text = descricao.ifEmpty { "—" }
        card.findViewById<TextView>(R.id.tvClasse).text = ClassesMaterial.porId(classe)?.nome ?: classe
        card.findViewById<TextView>(R.id.tvStatus).apply {
            text = "● PENDENTE"
            setTextColor(0xFFf59e0b.toInt())
        }

        val btnAprovar = card.findViewById<Button>(R.id.btnAprovar)
        val btnRejeitar = card.findViewById<Button>(R.id.btnRejeitar)
        val layoutBotoes = card.findViewById<View>(R.id.layoutBotoes)
        layoutBotoes.visibility = View.VISIBLE

        btnAprovar.setOnClickListener {
            pedirComentario("Aprovar material") { comentario ->
                lifecycleScope.launch {
                    repository.aprovar(id, comentario)
                    container.removeView(card)
                    Toast.makeText(requireContext(), "Material aprovado!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnRejeitar.setOnClickListener {
            pedirComentario("Rejeitar material") { comentario ->
                lifecycleScope.launch {
                    repository.rejeitar(id, comentario)
                    container.removeView(card)
                    Toast.makeText(requireContext(), "Material rejeitado.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return card
    }

    private fun criarCardStatus(
        codigo: String, classe: String, descricao: String,
        status: String, comentario: String
    ): View {
        val card = layoutInflater.inflate(
            R.layout.item_notificacao,
            requireView().findViewById(R.id.containerNotificacoes),
            false
        )

        card.findViewById<TextView>(R.id.tvCodigo).text = codigo
        card.findViewById<TextView>(R.id.tvDescricao).text = descricao.ifEmpty { "—" }
        card.findViewById<TextView>(R.id.tvClasse).text = ClassesMaterial.porId(classe)?.nome ?: classe
        card.findViewById<View>(R.id.layoutBotoes).visibility = View.GONE

        val tvStatus = card.findViewById<TextView>(R.id.tvStatus)
        when (status) {
            "aprovado" -> {
                tvStatus.text = "● APROVADO"
                tvStatus.setTextColor(0xFF22c55e.toInt())
            }
            "rejeitado" -> {
                tvStatus.text = "● REJEITADO"
                tvStatus.setTextColor(0xFFef4444.toInt())
            }
            else -> {
                tvStatus.text = "● AGUARDANDO"
                tvStatus.setTextColor(0xFFf59e0b.toInt())
            }
        }

        if (comentario.isNotEmpty()) {
            val tvComentario = card.findViewById<TextView>(R.id.tvComentario)
            tvComentario.visibility = View.VISIBLE
            tvComentario.text = "\"$comentario\""
        }

        return card
    }

    private fun pedirComentario(titulo: String, onConfirm: (String) -> Unit) {
        val et = EditText(requireContext()).apply {
            hint = "Comentário (opcional)"
            setPadding(40, 20, 40, 20)
        }
        AlertDialog.Builder(requireContext())
            .setTitle(titulo)
            .setView(et)
            .setPositiveButton("Confirmar") { _, _ -> onConfirm(et.text.toString().trim()) }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}