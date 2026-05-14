package com.ifpr.androidapptemplate.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {

    // Texto padrão da tela
    private val _text = MutableLiveData<String>().apply {
        value = "Conversor de Unidades"
    }
    val text: LiveData<String> = _text

    // Valor digitado pelo usuário
    val valorDigitado = MutableLiveData<String>("")

    // Resultado da conversão
    val resultadoConversao = MutableLiveData<String>("0.00")

    // Unidade origem e destino
    val unidadeOrigem = MutableLiveData<String>("Metros (m)")
    val unidadeDestino = MutableLiveData<String>("Centímetros (cm)")

    // Identificação do item
    val identificacao = MutableLiveData<String>("")
}