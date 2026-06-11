package com.ifpr.androidapptemplate.ui.dashboard

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.ui.cadastro.ClassesMaterial
import com.ifpr.androidapptemplate.ui.cadastro.DetalheMaterialActivity
import com.ifpr.androidapptemplate.ui.cadastro.Material
import com.ifpr.androidapptemplate.ui.cadastro.MaterialRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private val repository = MaterialRepository()
    private var todosMateriais = listOf<Material>()
    private var buscaJob: Job? = null
    private var classeSelecionada: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etBusca = view.findViewById<EditText>(R.id.etBusca)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvVazio = view.findViewById<TextView>(R.id.tvVazio)
        val tvLabel = view.findViewById<TextView>(R.id.tvLabelResultados)
        val container = view.findViewById<LinearLayout>(R.id.containerResultados)
        val containerChips = view.findViewById<LinearLayout>(R.id.containerChips)

        montarChips(containerChips, etBusca, container, tvVazio, tvLabel)
        carregarTodos(progressBar, tvVazio)

        etBusca.addTextChangedListener {
            buscaJob?.cancel()
            buscaJob = lifecycleScope.launch {
                delay(300)
                filtrar(etBusca.text.toString().trim(), container, tvVazio, tvLabel)
            }
        }
    }

    private fun montarChips(
        containerChips: LinearLayout,
        etBusca: EditText,
        container: LinearLayout,
        tvVazio: TextView,
        tvLabel: TextView
    ) {
        val opcoes = mutableListOf("Todas")
        opcoes.addAll(ClassesMaterial.classes.map { it.nome })

        for (opcao in opcoes) {
            val chip = TextView(requireContext()).apply {
                text = opcao
                textSize = 12f
                setTypeface(null, Typeface.BOLD)
                setPadding(dpToPx(14), dpToPx(7), dpToPx(14), dpToPx(7))
                setTextColor(0xFFa78bfa.toInt())
                background = criarBgChip(false)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.marginEnd = dpToPx(8) }

                setOnClickListener {
                    classeSelecionada = if (opcao == "Todas") null
                    else ClassesMaterial.classes.find { it.nome == opcao }?.id

                    for (i in 0 until containerChips.childCount) {
                        val c = containerChips.getChildAt(i) as TextView
                        c.background = criarBgChip(c == this)
                        c.setTextColor(
                            if (c == this) 0xFFffffff.toInt()
                            else 0xFFa78bfa.toInt()
                        )
                    }
                    filtrar(etBusca.text.toString().trim(), container, tvVazio, tvLabel)
                }
            }

            if (opcao == "Todas") {
                chip.background = criarBgChip(true)
                chip.setTextColor(0xFFffffff.toInt())
            }

            containerChips.addView(chip)
        }
    }

    private fun criarBgChip(selecionado: Boolean): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            cornerRadius = dpToPx(20).toFloat()
            if (selecionado) {
                setColor(0xFF7c3aed.toInt())
            } else {
                setColor(0xFF12122a.toInt())
                setStroke(dpToPx(1), 0xFF2a2a4a.toInt())
            }
        }
    }

    private fun carregarTodos(progressBar: ProgressBar, tvVazio: TextView) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val resultados = mutableListOf<Material>()
            for (classe in ClassesMaterial.classes) {
                val r = repository.listarPorClasse(classe.id)
                r.onSuccess { resultados.addAll(it) }
            }
            todosMateriais = resultados.sortedByDescending { it.criadoEm }
            progressBar.visibility = View.GONE
            tvVazio.text = "Digite algo para buscar"
        }
    }

    private fun filtrar(
        query: String,
        container: LinearLayout,
        tvVazio: TextView,
        tvLabel: TextView
    ) {
        var lista = todosMateriais

        classeSelecionada?.let { id ->
            lista = lista.filter { it.classe == id }
        }

        if (query.isNotEmpty()) {
            lista = lista.filter { material ->
                material.codigo.contains(query, ignoreCase = true) ||
                        material.campos.values.any { it.contains(query, ignoreCase = true) } ||
                        material.descricao.contains(query, ignoreCase = true)
            }
        }

        container.removeAllViews()

        if (query.isEmpty() && classeSelecionada == null) {
            tvVazio.visibility = View.VISIBLE
            tvVazio.text = "Digite algo para buscar"
            tvLabel.visibility = View.GONE
            return
        }

        if (lista.isEmpty()) {
            tvVazio.visibility = View.VISIBLE
            tvVazio.text = "Nenhum material encontrado"
            tvLabel.visibility = View.GONE
            return
        }

        tvVazio.visibility = View.GONE
        tvLabel.visibility = View.VISIBLE
        tvLabel.text = "${lista.size} RESULTADO${if (lista.size > 1) "S" else ""}"

        for (material in lista) {
            val item = layoutInflater.inflate(R.layout.item_material_lista, container, false)

            item.findViewById<TextView>(R.id.tvCodigo).text = material.codigo
            item.findViewById<TextView>(R.id.tvDescricao).text =
                material.campos["descricao"] ?: material.descricao.ifEmpty { "—" }
            item.findViewById<TextView>(R.id.tvFabricante).text =
                ClassesMaterial.porId(material.classe)?.nome ?: material.classe

            item.setOnClickListener {
                val intent = Intent(requireContext(), DetalheMaterialActivity::class.java)
                intent.putExtra("material_codigo", material.codigo)
                intent.putExtra("classe_id", material.classe)
                for ((k, v) in material.campos) {
                    intent.putExtra("campo_$k", v)
                }
                startActivity(intent)
            }

            container.addView(item)
        }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()
}