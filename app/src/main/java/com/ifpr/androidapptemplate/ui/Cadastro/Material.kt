package com.ifpr.androidapptemplate.ui.cadastro

data class Material(
    val codigo: String = "",
    val classe: String = "",
    val descricao: String = "",
    val campos: Map<String, String> = emptyMap(),
    val criadoEm: Long = 0L,
    val localizacao: String = "",
    val status: String = "pendente"
)