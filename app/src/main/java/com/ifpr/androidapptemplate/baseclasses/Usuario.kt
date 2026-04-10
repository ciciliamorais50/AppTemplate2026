package com.ifpr.androidapptemplate.baseclasses

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Usuario(
    val key: String? = null,
    val nome: String? = null,
    val email: String? = null,
    val endereco: String? = null
)