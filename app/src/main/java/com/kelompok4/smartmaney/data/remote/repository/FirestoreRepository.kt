package com.kelompok4.smartmaney.data.remote.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.kelompok4.smartmaney.data.local.entity.BudgetCategoryEntity
import com.kelompok4.smartmaney.data.local.entity.BudgetMetaEntity
import com.kelompok4.smartmaney.data.local.entity.ProfileEntity
import com.kelompok4.smartmaney.data.local.entity.TransactionEntity
import com.kelompok4.smartmaney.data.local.entity.WalletMetaEntity
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    private fun userRef(uid: String) = db.collection("users").document(uid)

    suspend fun uploadTransaction(uid: String, entity: TransactionEntity) {
        userRef(uid).collection("transactions")
            .document(entity.id.toString())
            .set(
                mapOf(
                    "id" to entity.id,
                    "title" to entity.title,
                    "amount" to entity.amount,
                    "type" to entity.type,
                    "category" to entity.category,
                    "note" to entity.note,
                    "paymentMethod" to entity.paymentMethod,
                    "createdAtMillis" to entity.createdAtMillis
                )
            ).await()
    }

    suspend fun deleteTransaction(uid: String, transactionId: Long) {
        userRef(uid).collection("transactions")
            .document(transactionId.toString())
            .delete().await()
    }

    suspend fun fetchAllTransactions(uid: String): List<TransactionEntity> {
        return userRef(uid).collection("transactions")
            .get().await()
            .documents.mapNotNull { doc ->
                runCatching {
                    TransactionEntity(
                        id = doc.getLong("id") ?: 0L,
                        title = doc.getString("title") ?: "",
                        amount = (doc.getLong("amount") ?: 0L).toInt(),
                        type = doc.getString("type") ?: "",
                        category = doc.getString("category") ?: "",
                        note = doc.getString("note") ?: "",
                        paymentMethod = doc.getString("paymentMethod") ?: "",
                        createdAtMillis = doc.getLong("createdAtMillis") ?: 0L
                    )
                }.getOrNull()
            }
    }

    suspend fun uploadWalletMeta(uid: String, entity: WalletMetaEntity) {
        userRef(uid).collection("wallet_meta").document("singleton")
            .set(mapOf("initialBalance" to entity.initialBalance)).await()
    }

    suspend fun fetchWalletMeta(uid: String): WalletMetaEntity? {
        val doc = userRef(uid).collection("wallet_meta").document("singleton").get().await()
        if (!doc.exists()) return null
        return runCatching {
            WalletMetaEntity(initialBalance = (doc.getLong("initialBalance") ?: 0L).toInt())
        }.getOrNull()
    }

    suspend fun uploadBudgetMeta(uid: String, entity: BudgetMetaEntity) {
        userRef(uid).collection("budget_meta").document("singleton")
            .set(mapOf("totalBudget" to entity.totalBudget)).await()
    }

    suspend fun fetchBudgetMeta(uid: String): BudgetMetaEntity? {
        val doc = userRef(uid).collection("budget_meta").document("singleton").get().await()
        if (!doc.exists()) return null
        return runCatching {
            BudgetMetaEntity(totalBudget = (doc.getLong("totalBudget") ?: 0L).toInt())
        }.getOrNull()
    }

    suspend fun uploadBudgetCategory(uid: String, entity: BudgetCategoryEntity) {
        userRef(uid).collection("budget_categories").document(entity.id)
            .set(
                mapOf(
                    "id" to entity.id,
                    "name" to entity.name,
                    "allocated" to entity.allocated
                )
            ).await()
    }

    suspend fun fetchAllBudgetCategories(uid: String): List<BudgetCategoryEntity> {
        return userRef(uid).collection("budget_categories")
            .get().await()
            .documents.mapNotNull { doc ->
                runCatching {
                    BudgetCategoryEntity(
                        id = doc.getString("id") ?: doc.id,
                        name = doc.getString("name") ?: "",
                        allocated = (doc.getLong("allocated") ?: 0L).toInt()
                    )
                }.getOrNull()
            }
    }

    suspend fun uploadProfile(uid: String, entity: ProfileEntity) {
        userRef(uid).collection("profile").document("singleton")
            .set(
                mapOf(
                    "fullName" to entity.fullName,
                    "email" to entity.email,
                    "notificationsEnabled" to entity.notificationsEnabled,
                    "darkModeEnabled" to entity.darkModeEnabled
                )
            ).await()
    }

    suspend fun fetchProfile(uid: String): ProfileEntity? {
        val doc = userRef(uid).collection("profile").document("singleton").get().await()
        if (!doc.exists()) return null
        return runCatching {
            ProfileEntity(
                fullName = doc.getString("fullName") ?: "",
                email = doc.getString("email") ?: "",
                notificationsEnabled = doc.getBoolean("notificationsEnabled") ?: true,
                darkModeEnabled = doc.getBoolean("darkModeEnabled") ?: false
            )
        }.getOrNull()
    }
}