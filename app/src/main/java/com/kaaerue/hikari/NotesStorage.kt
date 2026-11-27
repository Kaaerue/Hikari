
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

private val Context.notesDataStore by preferencesDataStore(
    name = "hikari_notes"
)

data class NoteItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val date: Long = System.currentTimeMillis()
)

object NotesStorage {

    private val NOTES_KEY = stringPreferencesKey("notes")

    private fun encode(notes: List<NoteItem>): String {
        val array = JSONArray()

        notes.forEach { note ->
            val item = JSONObject()
            item.put("id", note.id)
            item.put("title", note.title)
            item.put("content", note.content)
            item.put("date", note.date)
            array.put(item)
        }

        return array.toString()
    }

    private fun decode(json: String): List<NoteItem> {
        val array = JSONArray(json)

        return List(array.length()) { index ->
            val item = array.getJSONObject(index)

            NoteItem(
                id = item.getString("id"),
                title = item.getString("title"),
                content = item.getString("content"),
                date = item.getLong("date")
            )
        }
    }

    fun getNotes(context: Context): Flow<List<NoteItem>> {
        return context.notesDataStore.data.map { preferences ->
            decode(preferences[NOTES_KEY] ?: "[]")
        }
    }

    suspend fun saveNotes(
        context: Context,
        notes: List<NoteItem>
    ) {
        context.notesDataStore.edit { preferences ->
            preferences[NOTES_KEY] = encode(notes)
        }
    }
}
