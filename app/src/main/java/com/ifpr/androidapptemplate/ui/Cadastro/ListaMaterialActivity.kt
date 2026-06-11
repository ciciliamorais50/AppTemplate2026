package com.ifpr.androidapptemplate.ui.cadastro

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ifpr.androidapptemplate.R
import kotlinx.coroutines.launch

class ListaMaterialActivity : AppCompatActivity() {

    private val repository = MaterialRepository()
    private lateinit var classeId: String
    private lateinit var classeNome: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_material)

        classeId = intent.getStringExtra("classe_id") ?: return
        classeNome = intent.getStringExtra("classe_nome") ?: classeId

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = classeNome

        val fab = findViewById<FloatingActionButton>(R.id.fabNovoCadastro)
        fab.setOnClickListener {
            val intent = Intent(this, CadastroMaterialActivity::class.java)
            intent.putExtra("classe_id", classeId)
            startActivity(intent)
        }

        carregarMateriais()
    }

    override fun onResume() {
        super.onResume()
        carregarMateriais()
    }

    private fun carregarMateriais() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val container = findViewById<LinearLayout>(R.id.containerMateriais)
        val tvVazio = findViewById<TextView>(R.id.tvVazio)

        progressBar.visibility = View.VISIBLE
        container.removeAllViews()
        tvVazio.visibility = View.GONE

        lifecycleScope.launch {
            val resultado = repository.listarPorClasse(classeId)
            progressBar.visibility = View.GONE

            resultado.onSuccess { lista ->
                if (lista.isEmpty()) {
                    tvVazio.visibility = View.VISIBLE
                } else {
                    val inflater = layoutInflater
                    for (material in lista) {
                        val item = inflater.inflate(R.layout.item_material_lista, container, false)

                        item.findViewById<TextView>(R.id.tvCodigo).text = material.codigo
                        item.findViewById<TextView>(R.id.tvDescricao).text =
                            material.campos["descricao"] ?: material.descricao.ifEmpty { "—" }
                        item.findViewById<TextView>(R.id.tvFabricante).text =
                            material.campos["fabricante"] ?: ""

                        item.setOnClickListener {
                            val intent = Intent(this@ListaMaterialActivity, DetalheMaterialActivity::class.java)
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
            }.onFailure {
                Toast.makeText(this@ListaMaterialActivity, "Erro ao carregar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) onBackPressedDispatcher.onBackPressed()
        return super.onOptionsItemSelected(item)
    }
}