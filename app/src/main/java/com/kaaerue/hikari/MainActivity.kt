
package com.kaaerue.hikari

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaaerue.hikari.ui.theme.HikariTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HikariRoot()
        }
    }
}

// Hikari Root

@Composable
fun HikariRoot() {
    val context = LocalContext.current

    var themeMode by remember {
        mutableStateOf(
            context.getSharedPreferences("hikari_settings", 0)
                .getString("theme_mode", "System") ?: "System"
        )
    }

    var dynamicColor by remember {
        mutableStateOf(
            context.getSharedPreferences("hikari_settings", 0)
                .getBoolean("dynamic_color", true)
        )
    }

    val prefs = remember {
    context.getSharedPreferences("hikari_settings", 0)
}

var profileName by remember {
    mutableStateOf(
        prefs.getString("profile_name", "") ?: ""
    )
}

var personalGreeting by remember {
    mutableStateOf(
        prefs.getString(
            "personal_greeting",
            "Welcome back! Have a great day."
        ) ?: "Welcome back! Have a great day."
    )
}

    val darkTheme = when (themeMode) {
        "Light" -> false
        "Dark" -> true
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    HikariTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
    ) {
        HikariApp(
    themeMode = themeMode,
    dynamicColor = dynamicColor,     profileName = profileName,
    personalGreeting = personalGreeting,

    onProfileNameChange = { name ->
        profileName = name
        prefs.edit().putString("profile_name", name).apply()
    },

    onGreetingChange = { greeting ->
        personalGreeting = greeting
        prefs.edit().putString("personal_greeting", greeting).apply()
    },
    onThemeChange = { newMode ->
        themeMode = newMode
        context.getSharedPreferences("hikari_settings", 0)
            .edit()
            .putString("theme_mode", newMode)
            .apply()
    },
    onDynamicColorChange = { enabled ->
        dynamicColor = enabled
        context.getSharedPreferences("hikari_settings", 0)
            .edit()
            .putBoolean("dynamic_color", enabled)
            .apply()
    }
)
    }
}

// MAIN APP


@Composable
fun HikariApp(
    themeMode: String,
    dynamicColor: Boolean,
    onThemeChange: (String) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    profileName: String,
    personalGreeting: String,
    onProfileNameChange: (String) -> Unit,
    onGreetingChange: (String) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        "Home",
        "Habits",
        "Money",
        "Notes",
        "Settings"
    )

    val icons = listOf(
    Icons.Filled.Home,
    Icons.Filled.CheckCircle,
    Icons.Filled.AccountBalanceWallet,
    Icons.Filled.EditNote,
    Icons.Filled.Settings
)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                        },
                        icon = {
    Icon(
        imageVector = icons[index],
        contentDescription = title
    )
},
                        label = {
                            Text(title)
                        }
                    )
                }
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    onNavigate = { selectedTab = it },
                    profileName = profileName,
                    personalGreeting = personalGreeting
                )

                1 -> HabitScreen()

                2 -> MoneyScreen()

                3 -> NotesScreen()

                4 -> SettingsScreen(
    themeMode = themeMode,
    dynamicColor = dynamicColor,
    onThemeChange = onThemeChange,
    onDynamicColorChange = onDynamicColorChange,
    profileName = profileName,
    personalGreeting = personalGreeting,
    onProfileNameChange = onProfileNameChange,
    onGreetingChange = onGreetingChange
)
            }
        }
    }
}


// HOME


@Composable
fun HomeScreen(
    onNavigate: (Int) -> Unit,
    profileName: String,
    personalGreeting: String
) {
    val context = LocalContext.current

    val habits by HabitStorage.getHabits(context)
        .collectAsState(initial = emptyList())

    val transactions by MoneyStorage.getTransactions(context)
        .collectAsState(initial = emptyList())

    val notes by NotesStorage.getNotes(context)
        .collectAsState(initial = emptyList())

    val completed = habits.count { it.isDone }

    val progress = if (habits.isEmpty()) 0f
        else completed.toFloat() / habits.size

    val income = transactions
        .filter { it.type == "Income" }
        .sumOf { it.amount }

    val expense = transactions
        .filter { it.type == "Expense" }
        .sumOf { it.amount }

    val balance = income - expense

    val total = income + expense

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    val greeting = when (hour) {
        in 5..11 -> "Good morning ☀️"
        in 12..16 -> "Good afternoon 🌤️"
        in 17..20 -> "Good evening 🌆"
        else -> "Good night 🌙"
    }

    val today = SimpleDateFormat(
        "EEEE, dd MMMM yyyy",
        Locale("id", "ID")
    ).format(Date())

    val recentTransactions = transactions
        .sortedByDescending { it.date }
        .take(4)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Text(
    if (profileName.isBlank()) greeting
    else "$greeting, $profileName!",
    fontSize = 27.sp,
    fontWeight = FontWeight.Bold
)

Text(
    personalGreeting,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)

        Spacer(Modifier.height(6.dp))

        Text(
            today,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.height(26.dp))

        Text(
            "Financial Overview",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "Total Balance",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    formatRupiah(balance),
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    "Your current financial balance",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("Income")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        formatRupiah(income),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("Expense")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        formatRupiah(expense),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Cash Flow",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("Income vs Expense")

                Spacer(Modifier.height(16.dp))

                Text("Income", fontWeight = FontWeight.Medium)

                Spacer(Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = {
                        if (total == 0L) 0f
                        else income.toFloat() / total.toFloat()
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))

                Text("Expense", fontWeight = FontWeight.Medium)

                Spacer(Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = {
                        if (total == 0L) 0f
                        else expense.toFloat() / total.toFloat()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "Based on all recorded transactions",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Daily Habits",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Today's Progress")
                    Text(
                        "$completed / ${habits.size}",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "${(progress * 100).toInt()}% completed",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(10.dp))

                TextButton(onClick = { onNavigate(1) }) {
                    Text("View Habits →")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "My Notes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        "${notes.size} saved notes",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextButton(onClick = { onNavigate(3) }) {
                    Text("Open →")
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Recent Transactions",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                if (recentTransactions.isEmpty()) {
                    Text(
                        "No transactions yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    recentTransactions.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    item.title,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    item.category,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                (if (item.type == "Income") "+" else "-") +
                                    formatRupiah(item.amount),
                                fontWeight = FontWeight.Bold,
                                color = if (item.type == "Income")
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            )
                        }

                        if (index != recentTransactions.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }
                }

                TextButton(onClick = { onNavigate(2) }) {
                    Text("View Money Tracker →")
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Quick Actions ⚡",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onNavigate(1) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Habits")
            }

            OutlinedButton(
                onClick = { onNavigate(2) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Money")
            }

            OutlinedButton(
                onClick = { onNavigate(3) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Notes")
            }
        }

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "A little reminder 💗",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(Modifier.height(8.dp))

                Text("Small steps every day make a big difference.")
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}



// Notes Screen

@Composable
fun NotesScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val notes by NotesStorage
        .getNotes(context)
        .collectAsState(initial = emptyList())

    var showDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var editingNoteId by remember { mutableStateOf<String?>(null) }

    fun resetEditor() {
        title = ""
        content = ""
        editingNoteId = null
        showDialog = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "My Notes 📝",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Your thoughts, saved safely.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                resetEditor()
                showDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ New Note")
        }

        Spacer(Modifier.height(16.dp))

        if (notes.isEmpty()) {
            Text("No notes yet. Create your first note!")
        }

        notes.sortedByDescending { it.date }.forEach { note ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        note.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(note.content)

                    Spacer(Modifier.height(8.dp))

                    Row {
                        TextButton(
                            onClick = {
                                title = note.title
                                content = note.content
                                editingNoteId = note.id
                                showDialog = true
                            }
                        ) {
                            Text("Edit")
                        }

                        TextButton(
                            onClick = {
                                scope.launch {
                                    NotesStorage.saveNotes(
                                        context,
                                        notes.filterNot { it.id == note.id }
                                    )
                                }
                            }
                        ) {
                            Text(
                                "Delete",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { resetEditor() },
            title = {
                Text(
                    if (editingNoteId == null) {
                        "New Note"
                    } else {
                        "Edit Note"
                    }
                )
            },
            text = {
                Column {
                    OutlinedTextField(
    value = title,
    onValueChange = { title = it },
    label = { Text("Title") },
    singleLine = true
)                    

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Write something...") },
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (title.isNotBlank()) {
                            val updatedNotes = if (editingNoteId == null) {
                                notes + NoteItem(
                                    title = title.trim(),
                                    content = content.trim()
                                )
                            } else {
                                notes.map { note ->
                                    if (note.id == editingNoteId) {
                                        note.copy(
                                            title = title.trim(),
                                            content = content.trim()
                                        )
                                    } else {
                                        note
                                    }
                                }
                            }

                            scope.launch {
                                NotesStorage.saveNotes(
                                    context,
                                    updatedNotes
                                )
                            }

                            resetEditor()
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { resetEditor() }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}



// DASHBOARD CARD

@Composable
fun DashboardCard(
    title: String,
    subtitle: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 14.dp)
            )
        }
    }
}

// HABIT TRACKER

@Composable
fun HabitScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val habits by HabitStorage.getHabits(context)
        .collectAsState(initial = emptyList())

    var newHabit by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        HabitStorage.initializeDefaults(context)
    }

    val completed = habits.count { it.isDone }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "My Habits 🌱",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Build your daily habits.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        Text(
            "Progress: $completed / ${habits.size}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = {
                if (habits.isEmpty()) 0f
                else completed.toFloat() / habits.size
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Add New Habit")
        }

        Spacer(Modifier.height(12.dp))

        habits.forEachIndexed { index, habit ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = habit.isDone,
                        onCheckedChange = { checked ->
                            val updated = habits.toMutableList()
                            updated[index] = habit.copy(isDone = checked)

                            scope.launch {
                                HabitStorage.saveHabits(
                                    context,
                                    updated
                                )
                            }
                        }
                    )

                    Text(
                        habit.name,
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (habit.isDone) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        }
                    )

                    TextButton(
                        onClick = {
                            val updated = habits.filterIndexed {
                                i, _ -> i != index
                            }

                            scope.launch {
                                HabitStorage.saveHabits(
                                    context,
                                    updated
                                )
                            }
                        }
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            title = {
                Text("New Habit")
            },
            text = {
                OutlinedTextField(
                    value = newHabit,
                    onValueChange = { newHabit = it },
                    label = { Text("Habit name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newHabit.isNotBlank()) {
                            val updated = habits + HabitItem(
                                name = newHabit.trim()
                            )

                            scope.launch {
                                HabitStorage.saveHabits(
                                    context,
                                    updated
                                )
                            }

                            newHabit = ""
                            showDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// MONEY TRACKER


@Composable
fun MoneyScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val transactions by MoneyStorage
        .getTransactions(context)
        .collectAsState(initial = emptyList())

    var showDialog by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf<String?>(null) }

    var transactionType by remember { mutableStateOf("Expense") }
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }

    var expanded by remember { mutableStateOf(false) }
    var deleteTarget by remember {
        mutableStateOf<MoneyTransaction?>(null)
    }

    val income = transactions
        .filter { it.type == "Income" }
        .sumOf { it.amount }

    val expenses = transactions
        .filter { it.type == "Expense" }
        .sumOf { it.amount }

    val balance = income - expenses

    val categories = listOf(
        "Food", "Transport", "Education",
        "Entertainment", "Savings", "Other"
    )

    fun resetForm() {
        editingId = null
        title = ""
        amount = ""
        transactionType = "Expense"
        category = "Food"
        showDialog = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "My Money 💰",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Manage your finances.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Total Balance")
                Spacer(Modifier.height(8.dp))
                Text(
                    formatRupiah(balance),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(Modifier.weight(1f)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Income", color = MaterialTheme.colorScheme.primary)
                    Text(formatRupiah(income), fontWeight = FontWeight.Bold)
                }
            }

            Card(Modifier.weight(1f)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Expense", color = MaterialTheme.colorScheme.error)
                    Text(formatRupiah(expenses), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                resetForm()
                showDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Add Transaction")
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Recent Transactions",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        if (transactions.isEmpty()) {
            Text("No transactions yet.")
        }

        transactions.sortedByDescending { it.date }.forEach { transaction ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(transaction.title, fontWeight = FontWeight.Medium)
                    Text(
                        "${transaction.category} • ${transaction.type}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        (if (transaction.type == "Income") "+" else "-") +
                            formatRupiah(transaction.amount),
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.type == "Income")
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                }

                TextButton(
                    onClick = {
                        editingId = transaction.id
                        title = transaction.title
                        amount = transaction.amount.toString()
                        transactionType = transaction.type
                        category = transaction.category
                        showDialog = true
                    }
                ) {
                    Text("Edit")
                }

                TextButton(
                    onClick = {
                        deleteTarget = transaction
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }

            HorizontalDivider()
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { resetForm() },
            title = {
                Text(if (editingId == null) "New Transaction" else "Edit Transaction")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = transactionType == "Expense",
                            onClick = { transactionType = "Expense" },
                            label = { Text("Expense") }
                        )

                        FilterChip(
                            selected = transactionType == "Income",
                            onClick = { transactionType = "Income" },
                            label = { Text("Income") }
                        )
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Description") },
                        singleLine = true
                    )

                    
OutlinedTextField(
    value = amount,
    onValueChange = { input ->
        val digits = input.filter { it.isDigit() }
        amount = digits.reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()
    },
    label = { Text("Amount (Rp)") },
    keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number
    ),
    singleLine = true
)


                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Category: $category")
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            categories.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item) },
                                    onClick = {
                                        category = item
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val value = amount.replace(".","").toLongOrNull()

                        if (title.isNotBlank() && value != null && value > 0) {
                            val updated = transactions.map { old ->
                                if (old.id == editingId) {
                                    old.copy(
                                        title = title.trim(),
                                        amount = value,
                                        type = transactionType,
                                        category = category
                                    )
                                } else old
                            }

                            val result = if (editingId == null) {
                                updated + MoneyTransaction(
                                    title = title.trim(),
                                    amount = value,
                                    type = transactionType,
                                    category = category
                                )
                            } else updated

                            scope.launch {
                                MoneyStorage.saveTransactions(context, result)
                            }

                            resetForm()
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { resetForm() }) {
                    Text("Cancel")
                }
            }
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Transaction?") },
            text = { Text("Delete \"${target.title}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updated = transactions.filterNot {
                            it.id == target.id
                        }

                        scope.launch {
                            MoneyStorage.saveTransactions(context, updated)
                        }

                        deleteTarget = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}


// RUPIAH FORMAT

private fun formatRupiah(value: Long): String {
    return "Rp" + "%,d".format(value).replace(',', '.')
}

// Settings Screen

@Composable
fun SettingsScreen(
    themeMode: String,
    dynamicColor: Boolean,
    onThemeChange: (String) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    profileName: String,
    personalGreeting: String,
    onProfileNameChange: (String) -> Unit,
    onGreetingChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            "Settings ⚙️",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
    "Customize your Hikari experience.",
    color = MaterialTheme.colorScheme.onSurfaceVariant
)

Spacer(Modifier.height(24.dp))

Text(
    "Personalization",
    fontSize = 19.sp,
    fontWeight = FontWeight.Bold
)

Spacer(Modifier.height(12.dp))

Card(Modifier.fillMaxWidth()) {
    Column(
        Modifier.padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "Your Profile",
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = profileName,
            onValueChange = onProfileNameChange,
            label = { Text("Your Name") },
            placeholder = { Text("Enter your name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = personalGreeting,
            onValueChange = onGreetingChange,
            label = { Text("Personal Greeting") },
            placeholder = { Text("Have a great day!") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3
        )

        Text(
            "Your preferences are saved automatically.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

Spacer(Modifier.height(24.dp))

        Text("Appearance", fontSize = 19.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(12.dp))

        Card(Modifier.fillMaxWidth()) {
    Column(Modifier.padding(18.dp)) {

        Text("Theme", fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Light", "Dark", "System").forEach { mode ->
                FilterChip(
                    selected = themeMode == mode,
                    onClick = { onThemeChange(mode) },
                    label = { Text(mode) }
                )
            }
        }

        HorizontalDivider(
            Modifier.padding(vertical = 14.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Dynamic Color",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Uses colors from your device wallpaper.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = dynamicColor,
                onCheckedChange = onDynamicColorChange
            )
        }
    }
}

        Spacer(Modifier.height(24.dp))

        Text("Application", fontSize = 19.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(12.dp))

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Text("Hikari", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Personal Life Companion")
                Spacer(Modifier.height(12.dp))
                Text("Version 1.0")
                Text("Made with Kotlin and Jetpack Compose.")
            }
        }
    }
}
