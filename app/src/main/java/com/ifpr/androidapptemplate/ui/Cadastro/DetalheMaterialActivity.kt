package com.ifpr.androidapptemplate.ui.cadastro

import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ifpr.androidapptemplate.R

class DetalheMaterialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhe_material)

        val codigo = intent.getStringExtra("material_codigo") ?: ""
        val classeId = intent.getStringExtra("classe_id") ?: ""
        val classe = ClassesMaterial.porId(classeId)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = codigo

        findViewById<TextView>(R.id.tvCodigo).text = codigo
        findViewById<TextView>(R.id.tvClasse).text = classe?.nome ?: classeId

        val container = findViewById<LinearLayout>(R.id.containerCampos)

        val camposDefinidos = classe?.campos ?: emptyList()
        for (campo in camposDefinidos) {
            val valor = intent.getStringExtra("campo_${campo.chave}")
            if (!valor.isNullOrEmpty()) {
                val itemView = layoutInflater.inflate(R.layout.item_campo_detalhe, container, false)
                itemView.findViewById<TextView>(R.id.tvLabel).text = campo.label
                itemView.findViewById<TextView>(R.id.tvValor).text = valor
                container.addView(itemView)
            }
        }

        if (container.childCount == 0) {
            val tv = TextView(this).apply {
                text = "Nenhum campo preenchido."
                textSize = 13f
                setTextColor(0xFF555577.toInt())
                setPadding(0, 16, 0, 0)
            }
            container.addView(tv)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) onBackPressedDispatcher.onBackPressed()
        return super.onOptionsItemSelected(item)
    }
}