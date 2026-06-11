package com.ifpr.androidapptemplate.ui.cadastro

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AprovacaoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val materiaisRef = db.collection("materiais")
    private val usuariosRef = db.collection("usuarios")

    suspend fun getPapelUsuario(): String {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return "cadastrador"
        return try {
            val doc = usuariosRef.document(uid).get().await()
            doc.getString("papel") ?: "cadastrador"
        } catch (e: Exception) {
            "cadastrador"
        }
    }

    suspend fun setPapelUsuario(papel: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        try {
            usuariosRef.document(uid).set(
                mapOf("papel" to papel),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
        } catch (e: Exception) { }
    }

    suspend fun listarPendentes(): Result<List<MaterialPendente>> {
        return try {
            val snap = materiaisRef
                .whereEqualTo("status", "pendente")
                .get()
                .await()

            val lista = snap.documents.map { doc ->
                MaterialPendente(
                    id = doc.id,
                    codigo = doc.getString("codigo") ?: "",
                    classe = doc.getString("classe") ?: "",
                    descricao = (doc.get("campos") as? Map<String, String>)?.get("descricao") ?: "",
                    cadastradoPor = doc.getString("cadastradoPor") ?: "",
                    criadoEm = doc.getLong("criadoEm") ?: 0L
                )
            }
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun aprovar(materialId: String, comentario: String): Result<Unit> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return Result.failure(Exception("Não autenticado"))
        return try {
            materiaisRef.document(materialId).update(
                mapOf(
                    "status" to "aprovado",
                    "aprovadoPor" to uid,
                    "comentario" to comentario,
                    "aprovadoEm" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejeitar(materialId: String, comentario: String): Result<Unit> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return Result.failure(Exception("Não autenticado"))
        return try {
            materiaisRef.document(materialId).update(
                mapOf(
                    "status" to "rejeitado",
                    "aprovadoPor" to uid,
                    "comentario" to comentario,
                    "aprovadoEm" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listarMeusMateriais(): Result<List<MaterialStatus>> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return Result.failure(Exception("Não autenticado"))
        return try {
            val snap = materiaisRef
                .whereEqualTo("cadastradoPor", uid)
                .get()
                .await()

            val lista = snap.documents.map { doc ->
                MaterialStatus(
                    id = doc.id,
                    codigo = doc.getString("codigo") ?: "",
                    classe = doc.getString("classe") ?: "",
                    descricao = (doc.get("campos") as? Map<String, String>)?.get("descricao") ?: "",
                    status = doc.getString("status") ?: "pendente",
                    comentario = doc.getString("comentario") ?: "",
                    criadoEm = doc.getLong("criadoEm") ?: 0L
                )
            }.sortedByDescending { it.criadoEm }
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class MaterialPendente(
    val id: String,
    val codigo: String,
    val classe: String,
    val descricao: String,
    val cadastradoPor: String,
    val criadoEm: Long
)

data class MaterialStatus(
    val id: String,
    val codigo: String,
    val classe: String,
    val descricao: String,
    val status: String,
    val comentario: String,
    val criadoEm: Long
)