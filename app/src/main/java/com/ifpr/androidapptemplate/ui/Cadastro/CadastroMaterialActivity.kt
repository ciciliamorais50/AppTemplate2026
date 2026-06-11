package com.ifpr.androidapptemplate.ui.cadastro

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.ifpr.androidapptemplate.databinding.ActivityCadastroMaterialBinding
import kotlinx.coroutines.launch

class CadastroMaterialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroMaterialBinding
    private val repository = MaterialRepository()
    private var classeSelecionada: ClasseMaterial? = null
    private val camposViews = mutableMapOf<String, TextInputEditText>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroMaterialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Cadastro de Material"

        val classeId = intent.getStringExtra("classe_id")
        if (classeId != null) {
            classeSelecionada = ClassesMaterial.porId(classeId)
            classeSelecionada?.let { montarFormulario(it) }
            binding.spinnerClasse.visibility = View.GONE
            binding.labelClasse.visibility = View.GONE
        } else {
            configurarSpinnerClasse()
        }

        binding.btnSalvar.setOnClickListener {
            salvarMaterial()
        }
    }

    private fun configurarSpinnerClasse() {
        val nomes = ClassesMaterial.classes.map { "${it.icone} ${it.nome}" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nomes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerClasse.adapter = adapter

        binding.spinnerClasse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                classeSelecionada = ClassesMaterial.classes[position]
                classeSelecionada?.let { montarFormulario(it) }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun montarFormulario(classe: ClasseMaterial) {
        binding.containerCampos.removeAllViews()
        camposViews.clear()

        for (campo in classe.campos) {
            val label = TextView(this).apply {
                text = if (campo.obrigatorio) "${campo.label} *" else campo.label
                setTextColor(0xFF888888.toInt())
                textSize = 11f
                letterSpacing = 0.05f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.topMargin = dpToPx(12) }
            }

            val et = TextInputEditText(this).apply {
                setTextColor(0xFFf0f0ff.toInt())
                setHintTextColor(0xFF444466.toInt())
                setBackgroundResource(android.R.color.transparent)
                setPadding(0, dpToPx(4), 0, dpToPx(8))
                textSize = 15f
                if (campo.tipo == TipoCampo.NUMERO) {
                    inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                            android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                }
            }

            val divider = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1)
                ).also { it.bottomMargin = dpToPx(4) }
                setBackgroundColor(0xFF2a2a4a.toInt())
            }

            binding.containerCampos.addView(label)
            binding.containerCampos.addView(et)
            binding.containerCampos.addView(divider)
            camposViews[campo.chave] = et
        }
    }

    private fun salvarMaterial() {
        val classe = classeSelecionada ?: run {
            Toast.makeText(this, "Selecione uma classe", Toast.LENGTH_SHORT).show()
            return
        }

        val camposObrigatorios = classe.campos.filter { it.obrigatorio }
        for (campo in camposObrigatorios) {
            val valor = camposViews[campo.chave]?.text?.toString()?.trim()
            if (valor.isNullOrEmpty()) {
                Toast.makeText(this, "${campo.label} é obrigatório", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val campos = mutableMapOf<String, String>()
        for ((chave, editText) in camposViews) {
            val valor = editText.text?.toString()?.trim()
            if (!valor.isNullOrEmpty()) {
                campos[chave] = valor
            }
        }

        val descricao = camposViews["descricao"]?.text?.toString()?.trim() ?: ""

        val material = Material(
            classe = classe.id,
            descricao = descricao,
            campos = campos
        )

        binding.btnSalvar.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val resultado = repository.salvarMaterial(material)
            resultado.onSuccess { codigo ->
                Toast.makeText(
                    this@CadastroMaterialActivity,
                    "Material cadastrado: $codigo",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }.onFailure { erro ->
                Toast.makeText(
                    this@CadastroMaterialActivity,
                    "Erro ao salvar: ${erro.message}",
                    Toast.LENGTH_LONG
                ).show()
                binding.btnSalvar.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) onBackPressedDispatcher.onBackPressed()
        return super.onOptionsItemSelected(item)
    }
}