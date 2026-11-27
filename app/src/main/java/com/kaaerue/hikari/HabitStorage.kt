
package com.kaaerue.hikari

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.habitDataStore by preferencesDataStore(
    name = "hikari_habits"
)

data class HabitItem(
    val name: String,
    val isDone: Boolean = false
)

object HabitStorage {

    private val HABITS_KEY = stringPreferencesKey("habits")

    private fun encode(habits: List<HabitItem>): String {
        val array = JSONArray()

        habits.forEach { habit ->
            val item = JSONObject()
            item.put("name", habit.name)
            item.put("isDone", habit.isDone)
            array.put(item)
        }

        return array.toString()
    }

    private fun decode(json: String): List<HabitItem> {
        val array = JSONArray(json)

        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            HabitItem(
                name = item.getString("name"),
                isDone = item.getBoolean("isDone")
            )
        }
    }

    fun getHabits(context: Context): Flow<List<HabitItem>> {
        return context.habitDataStore.data.map { preferences ->
            decode(preferences[HABITS_KEY] ?: "[]")
        }
    }

    suspend fun initializeDefaults(context: Context) {
        context.habitDataStore.edit { preferences ->
            if (preferences[HABITS_KEY] == null) {
                preferences[HABITS_KEY] = encode(
                    listOf(
                        HabitItem("Morning Walk"),
                        HabitItem("Read a Book"),
                        HabitItem("Drink Water")
                    )
                )
            }
        }
    }

    suspend fun saveHabits(
        context: Context,
        habits: List<HabitItem>
    ) {
        context.habitDataStore.edit { preferences ->
            preferences[HABITS_KEY] = encode(habits)
        }
    }
}
