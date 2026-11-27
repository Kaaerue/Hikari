
package com.kaaerue.hikari

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private val Context.moneyDataStore by preferencesDataStore(
    name = "hikari_money"
)

data class MoneyTransaction(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Long,
    val type: String,
    val category: String,
    val date: Long = System.currentTimeMillis()
)

object MoneyStorage {

    private val TRANSACTIONS_KEY =
        stringPreferencesKey("transactions")

    private fun encode(
        transactions: List<MoneyTransaction>
    ): String {
        val array = JSONArray()

        transactions.forEach { transaction ->
            val item = JSONObject()

            item.put("id", transaction.id)
            item.put("title", transaction.title)
            item.put("amount", transaction.amount)
            item.put("type", transaction.type)
            item.put("category", transaction.category)
            item.put("date", transaction.date)

            array.put(item)
        }

        return array.toString()
    }

    private fun decode(json: String): List<MoneyTransaction> {
        val array = JSONArray(json)

        return List(array.length()) { index ->
            val item = array.getJSONObject(index)

            MoneyTransaction(
                id = item.getString("id"),
                title = item.getString("title"),
                amount = item.getLong("amount"),
                type = item.getString("type"),
                category = item.getString("category"),
                date = item.getLong("date")
            )
        }
    }

    fun getTransactions(
        context: Context
    ): Flow<List<MoneyTransaction>> {
        return context.moneyDataStore.data.map { preferences ->
            decode(preferences[TRANSACTIONS_KEY] ?: "[]")
        }
    }

    suspend fun saveTransactions(
        context: Context,
        transactions: List<MoneyTransaction>
    ) {
        context.moneyDataStore.edit { preferences ->
            preferences[TRANSACTIONS_KEY] = encode(transactions)
        }
    }
}
