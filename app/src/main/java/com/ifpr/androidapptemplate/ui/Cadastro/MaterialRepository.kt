package com.ifpr.androidapptemplate.ui.cadastro

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MaterialRepository {

    private val db = FirebaseFirestore.getInstance()
    private val materiaisRef = db.collection("materiais")
    private val contadorRef = db.collection("config").document("contador")

    suspend fun salvarMaterial(material: Material): Result<String> {
        return try {
            var codigoGerado = ""
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            db.runTransaction { transaction ->
                val contadorSnap = transaction.get(contadorRef)
                val atual = contadorSnap.getLong("ultimo") ?: 0L
                val proximo = atual + 1
                codigoGerado = "MAT-%05d".format(proximo)

                transaction.set(contadorRef, mapOf("ultimo" to proximo))

                val novoDoc = materiaisRef.document()
                transaction.set(
                    novoDoc, mapOf(
                        "codigo" to codigoGerado,
                        "classe" to material.classe,
                        "descricao" to material.descricao,
                        "campos" to material.campos,
                        "criadoEm" to System.currentTimeMillis(),
                        "status" to "pendente",
                        "cadastradoPor" to uid,
                        "comentario" to "",
                        "localizacao" to mapOf(
                            "endereco" to LocationHolder.endereco,
                            "latitude" to LocationHolder.latitude,
                            "longitude" to LocationHolder.longitude
                        )
                    )
                )
            }.await()

            Result.success(codigoGerado)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listarPorClasse(classeId: String): Result<List<Material>> {
        return try {
            val snap = materiaisRef
                .whereEqualTo("classe", classeId)
                .get()
                .await()

            val lista = snap.documents.map { doc ->
                Material(
                    codigo = doc.getString("codigo") ?: "",
                    classe = doc.getString("classe") ?: "",
                    descricao = doc.getString("descricao") ?: "",
                    campos = (doc.get("campos") as? Map<String, String>) ?: emptyMap(),
                    criadoEm = doc.getLong("criadoEm") ?: 0L,
                    localizacao = (doc.get("localizacao") as? Map<String, Any>)?.let {
                        "${it["endereco"] ?: ""}"
                    } ?: "",
                    status = doc.getString("status") ?: "pendente"
                )
            }.sortedByDescending { it.criadoEm }

            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listarMeusMateriais(): Result<List<Material>> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return Result.failure(Exception("Não autenticado"))
        return try {
            val snap = materiaisRef
                .whereEqualTo("cadastradoPor", uid)
                .get()
                .await()

            val lista = snap.documents.map { doc ->
                Material(
                    codigo = doc.getString("codigo") ?: "",
                    classe = doc.getString("classe") ?: "",
                    descricao = doc.getString("descricao") ?: "",
                    campos = (doc.get("campos") as? Map<String, String>) ?: emptyMap(),
                    criadoEm = doc.getLong("criadoEm") ?: 0L,
                    localizacao = (doc.get("localizacao") as? Map<String, Any>)?.let {
                        "${it["endereco"] ?: ""}"
                    } ?: "",
                    status = doc.getString("status") ?: "pendente"
                )
            }.sortedByDescending { it.criadoEm }
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}