package com.kaaerue.hikari

import android.content.Context
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

object BackupStorage {

    private const val BACKUP_VERSION = 1

    suspend fun createBackup(context: Context): String {
        val habits = HabitStorage.getHabits(context).first()
        val transactions = MoneyStorage.getTransactions(context).first()
        val notes = NotesStorage.getNotes(context).first()

        val habitArray = JSONArray()
        habits.forEach {
            habitArray.put(
                JSONObject().apply {
                    put("name", it.name)
                    put("isDone", it.isDone)
                }
            )
        }

        val transactionArray = JSONArray()
        transactions.forEach {
            transactionArray.put(
                JSONObject().apply {
                    put("id", it.id)
                    put("title", it.title)
                    put("amount", it.amount)
                    put("type", it.type)
                    put("category", it.category)
                    put("date", it.date)
                }
            )
        }

        val notesArray = JSONArray()
        notes.forEach {
            notesArray.put(
                JSONObject().apply {
                    put("id", it.id)
                    put("title", it.title)
                    put("content", it.content)
                    put("date", it.date)
                }
            )
        }

        return JSONObject().apply {
            put("app", "Hikari")
            put("backupVersion", BACKUP_VERSION)
            put("backupDate", System.currentTimeMillis())
            put("data", JSONObject().apply {
                put("habits", habitArray)
                put("transactions", transactionArray)
                put("notes", notesArray)
            })
        }.toString(4)
    }
    suspend fun restoreBackup(
        context: Context,
        json: String
    ) {
        val root = JSONObject(json)

        require(root.getString("app") == "Hikari") {
            "File backup bukan milik Hikari."
        }

        require(root.getInt("backupVersion") == BACKUP_VERSION) {
            "Versi backup tidak didukung."
        }

        val data = root.getJSONObject("data")

        val habitArray = data.getJSONArray("habits")
        val transactionArray = data.getJSONArray("transactions")
        val notesArray = data.getJSONArray("notes")

        val habits = List(habitArray.length()) { index ->
            val item = habitArray.getJSONObject(index)

            HabitItem(
                name = item.getString("name"),
                isDone = item.getBoolean("isDone")
            )
        }

        val transactions = List(transactionArray.length()) { index ->
            val item = transactionArray.getJSONObject(index)

            MoneyTransaction(
                id = item.getString("id"),
                title = item.getString("title"),
                amount = item.getLong("amount"),
                type = item.getString("type"),
                category = item.getString("category"),
                date = item.getLong("date")
            )
        }

        val notes = List(notesArray.length()) { index ->
            val item = notesArray.getJSONObject(index)

            NoteItem(
                id = item.getString("id"),
                title = item.getString("title"),
                content = item.getString("content"),
                date = item.getLong("date")
            )
        }

        HabitStorage.saveHabits(context, habits)
        MoneyStorage.saveTransactions(context, transactions)
        NotesStorage.saveNotes(context, notes)
    }
}
