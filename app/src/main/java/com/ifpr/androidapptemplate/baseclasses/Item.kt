package com.ifpr.androidapptemplate.baseclasses

data class Item(
    val uid: String? = null,
    val identificador: String? = null,
    val valor: String? = null,
    val categoria: String? = null
) {
    constructor() : this(null, null, null, null)
}