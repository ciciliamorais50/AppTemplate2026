package com.ifpr.androidapptemplate.baseclasses

import com.google.firebase.database.IgnoreExtraProperties

data class Item(
    val uid: String? = "",
    val endereco: String? = "",
    val valor: String? = "",
    val categoria: String? = ""
)