package com.ifpr.androidapptemplate.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.androidapptemplate.baseclasses.Item
import com.ifpr.androidapptemplate.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        // Referência correta: itens > ID_DO_USUARIO
        database = FirebaseDatabase.getInstance().getReference("itens")

        configurarSpinners()
        configurarLogicaConversao()

        binding.salvarItemButton.setOnClickListener {
            salvarConversaoNoFirebase()
        }

        return binding.root
    }

    private fun configurarSpinners() {
        val unidades = arrayOf("Metros (m)", "Centímetros (cm)", "Quilômetros (km)", "Gramas (g)", "Quilos (kg)")

        // Usamos um layout padrão do Android que geralmente tem texto escuro
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, unidades)

        binding.spinnerOrigem.adapter = adapter
        binding.spinnerDestino.adapter = adapter



    }

    private fun configurarLogicaConversao() {
        binding.etValorMedida.addTextChangedListener { executarCalculo() }
    }

    private fun executarCalculo() {
        val valorStr = binding.etValorMedida.text.toString()
        if (valorStr.isEmpty()) {
            binding.tvResultadoConversao.text = "0.00"
            return
        }

        val valor = valorStr.toDoubleOrNull() ?: 0.0
        val de = binding.spinnerOrigem.selectedItem.toString()
        val para = binding.spinnerDestino.selectedItem.toString()
        var resultado = valor

        when {
            de.contains("Metros") && para.contains("Centímetros") -> resultado = valor * 100
            de.contains("Centímetros") && para.contains("Metros") -> resultado = valor / 100
            de.contains("Quilômetros") && para.contains("Metros") -> resultado = valor * 1000
            de.contains("Metros") && para.contains("Quilômetros") -> resultado = valor / 1000
            de.contains("Quilos") && para.contains("Gramas") -> resultado = valor * 1000
            de.contains("Gramas") && para.contains("Quilos") -> resultado = valor / 1000
        }

        binding.tvResultadoConversao.text = "%.2f".format(resultado)
    }

    private fun salvarConversaoNoFirebase() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val identificacao = binding.etIdentificacao.text.toString().trim()
        val valorDigitado = binding.etValorMedida.text.toString().trim()
        val resultadoCalculado = binding.tvResultadoConversao.text.toString()
        val unidadeDe = binding.spinnerOrigem.selectedItem.toString()
        val unidadePara = binding.spinnerDestino.selectedItem.toString()

        if (identificacao.isEmpty() || valorDigitado.isEmpty()) {
            Toast.makeText(context, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        val resumoConversao = "$valorDigitado $unidadeDe ➔ $resultadoCalculado $unidadePara"

        // Criando o objeto para o Firebase
        val item = Item(
            uid = userId,
            endereco = identificacao,
            valor = resumoConversao,
            categoria = "Conversão"
        )

        // Salva dentro de itens > userId > ID_GERADO_AUTOMATICO
        database.child(userId).push().setValue(item)
            .addOnSuccessListener {
                Toast.makeText(context, "✅ Salvo com sucesso!", Toast.LENGTH_SHORT).show()
                binding.etIdentificacao.text.clear()
                binding.etValorMedida.text.clear()
                binding.tvResultadoConversao.text = "0.00"
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "❌ Erro ao salvar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}