package com.ifpr.androidapptemplate.ui.dashboard

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.androidapptemplate.baseclasses.Item
import com.ifpr.androidapptemplate.R


class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private lateinit var spinnerOrigem: Spinner
    private lateinit var spinnerDestino: Spinner
    private lateinit var etValorMedida: EditText
    private lateinit var etIdentificacao: EditText
    private lateinit var tvResultado: TextView
    private lateinit var btnSalvar: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // pegar views
        spinnerOrigem = view.findViewById(R.id.spinnerOrigem)
        spinnerDestino = view.findViewById(R.id.spinnerDestino)
        etValorMedida = view.findViewById(R.id.etValorMedida)
        etIdentificacao = view.findViewById(R.id.etIdentificacao)
        tvResultado = view.findViewById(R.id.tvResultadoConversao)
        btnSalvar = view.findViewById(R.id.salvarItemButton)

        setupConversor()

        btnSalvar.setOnClickListener {
            salvarNoFirebase()
        }
    }

    private fun setupConversor() {

        val unidades = arrayOf(
            "Metros (m)",
            "Centímetros (cm)",
            "Quilômetros (km)",
            "Gramas (g)",
            "Quilos (kg)"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            unidades
        )

        spinnerOrigem.adapter = adapter
        spinnerDestino.adapter = adapter

        etValorMedida.addTextChangedListener {
            calcular()
        }

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                calcular()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerOrigem.onItemSelectedListener = listener
        spinnerDestino.onItemSelectedListener = listener
    }

    private fun calcular() {

        val valor = etValorMedida.text.toString().toDoubleOrNull() ?: 0.0

        val de = spinnerOrigem.selectedItem?.toString() ?: ""
        val para = spinnerDestino.selectedItem?.toString() ?: ""

        var resultado = valor

        when {
            de.contains("Metros") && para.contains("Centímetros") -> resultado = valor * 100
            de.contains("Centímetros") && para.contains("Metros") -> resultado = valor / 100
            de.contains("Quilômetros") && para.contains("Metros") -> resultado = valor * 1000
            de.contains("Metros") && para.contains("Quilômetros") -> resultado = valor / 1000
            de.contains("Quilos") && para.contains("Gramas") -> resultado = valor * 1000
            de.contains("Gramas") && para.contains("Quilos") -> resultado = valor / 1000
        }

        tvResultado.text = "%.2f".format(resultado)
    }

    private fun salvarNoFirebase() {

        val user = FirebaseAuth.getInstance().currentUser ?: return

        val iden = etIdentificacao.text.toString().trim()
        val valor = etValorMedida.text.toString().trim()
        val resultado = tvResultado.text.toString()

        if (iden.isEmpty() || valor.isEmpty()) {
            Toast.makeText(context, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        val item = Item(
            uid = user.uid,
            identificador = iden,
            valor = "$valor ➜ $resultado",
            categoria = "Conversão"
        )

        FirebaseDatabase.getInstance()
            .getReference("itens")
            .child(user.uid)
            .push()
            .setValue(item)
            .addOnSuccessListener {
                Toast.makeText(context, "Salvo com sucesso!", Toast.LENGTH_SHORT).show()

                etIdentificacao.text.clear()
                etValorMedida.text.clear()
                tvResultado.text = "0.00"
            }
            .addOnFailureListener {
                Toast.makeText(context, "Erro ao salvar!", Toast.LENGTH_SHORT).show()
            }
    }
}