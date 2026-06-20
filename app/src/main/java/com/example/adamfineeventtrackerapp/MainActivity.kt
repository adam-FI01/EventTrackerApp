package com.example.adamfineeventtrackerapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.adamfineeventtrackerapp.data.MyEvent
import com.example.adamfineeventtrackerapp.data.UserAccount
import com.example.adamfineeventtrackerapp.ui.theme.AdamFineEventTrackerAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdamFineEventTrackerAppTheme(darkTheme = true, dynamicColor = false) {
                val myNavController = rememberNavController()
                
                var eventList by remember { mutableStateOf(listOf(
                    MyEvent(1, "CS-360 project 2", "Sunday", "School"),
                    MyEvent(2, "Gym", "Tuesday", "Campus Gym")
                )) }

                var userList by remember { mutableStateOf(listOf(
                    UserAccount("admin", "admin123")
                )) }

                NavHost(navController = myNavController, startDestination = "login_screen") {
                    composable("login_screen") {
                        LoginView(
                            users = userList,
                            onSuccess = { myNavController.navigate("grid_screen") },
                            onRegister = { newUser, newPass ->
                                if (userList.any { it.user == newUser }) {
                                    false
                                } else {
                                    userList = userList + UserAccount(newUser, newPass)
                                    true
                                }
                            }
                        )
                    }

                    composable("grid_screen") {
                        EventGridView(
                            events = eventList,
                            onAdd = { name, date, loc ->
                                val nextId = (eventList.maxOfOrNull { it.eventId } ?: 0) + 1
                                eventList = eventList + MyEvent(nextId, name, date, loc)
                            },
                            onDelete = { id ->
                                eventList = eventList.filter { it.eventId != id }
                            },
                            goToSettings = { myNavController.navigate("sms_screen") }
                        )
                    }

                    composable("sms_screen") {
                        SmsSettingsView(onBack = { myNavController.popBackStack() })
                    }
                }
            }
        }
    }
}

@Composable
fun LoginView(
    users: List<UserAccount>,
    onSuccess: () -> Unit,
    onRegister: (String, String) -> Boolean
) {
    var userText by remember { mutableStateOf("") }
    var passText by remember { mutableStateOf("") }
    val context = LocalContext.current

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(25.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            @Suppress("MagicNumber")
            Text("Event Tracker Login", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF90CAF9))
            Spacer(Modifier.height(30.dp))

            OutlinedTextField(
                value = userText,
                onValueChange = { userText = it },
                label = { Text("Enter Username") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = passText,
                onValueChange = { passText = it },
                label = { Text("Enter Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(25.dp))

            Button(
                onClick = {
                    val found = users.any { it.user == userText && it.pass == passText }
                    if (found) {
                        onSuccess()
                    } else {
                        Toast.makeText(context, "Invalid username or password!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("LOG IN")
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    if (userText.isNotBlank() && passText.isNotBlank()) {
                        val added = onRegister(userText, passText)
                        if (added) {
                            Toast.makeText(context, "Account Created! You can now log in.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Username already exists!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Fill in both fields to register!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("NEW ACCOUNT / REGISTER")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventGridView(
    events: List<MyEvent>,
    onAdd: (String, String, String) -> Unit,
    onDelete: (Int) -> Unit,
    goToSettings: () -> Unit
) {
    var dialogOpen by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf("") }
    var locInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Events List") },
                actions = {
                    IconButton(onClick = goToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            @Suppress("MagicNumber")
            FloatingActionButton(onClick = { dialogOpen = true }, containerColor = Color(0xFF4CAF50)) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(10.dp)) {
            Text("Upcoming Schedule", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(5.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(events) { ev ->
                    Card(
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            @Suppress("MagicNumber")
                            Text(ev.eventName, fontWeight = FontWeight.Bold, color = Color(0xFF90CAF9))
                            Text("Date: ${ev.eventDate}", fontSize = 12.sp)
                            Text("Loc: ${ev.eventLocation}", fontSize = 12.sp)
                            
                            IconButton(
                                onClick = { onDelete(ev.eventId) },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }

    if (dialogOpen) {
        AlertDialog(
            onDismissRequest = { dialogOpen = false },
            title = { Text("New Event Entry") },
            text = {
                Column {
                    TextField(value = nameInput, onValueChange = { nameInput = it }, label = { Text("What is it?") })
                    TextField(value = dateInput, onValueChange = { dateInput = it }, label = { Text("When is it?") })
                    TextField(value = locInput, onValueChange = { locInput = it }, label = { Text("Where is it?") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    onAdd(nameInput, dateInput, locInput)
                    dialogOpen = false
                    nameInput = ""; dateInput = ""; locInput = ""
                }) { Text("ADD IT") }
            }
        )
    }
}

@Composable
fun SmsSettingsView(onBack: () -> Unit) {
    val context = LocalContext.current
    var isAllowed by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED)
    }

    val smsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        isAllowed = granted
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("SMS Notifications", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))
            
            Box(
                modifier = Modifier.background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(10.dp)).padding(15.dp)
            ) {
                Text(
                    if (isAllowed) "Status: ENABLED\n\nYou will get texts for upcoming events!" 
                    else "Status: DISABLED\n\nYou won't get any notifications unless you allow SMS."
                )
            }

            Spacer(Modifier.height(30.dp))

            if (!isAllowed) {
                Button(onClick = { smsLauncher.launch(Manifest.permission.SEND_SMS) }) {
                    Text("ALLOW SMS UPDATES")
                }
            } else {
                Button(onClick = { /* already on */ }, enabled = false) {
                    Text("ALREADY ALLOWED")
                }
            }

            TextButton(onClick = onBack) {
                Text("Back to Event List")
            }
        }
    }
}
