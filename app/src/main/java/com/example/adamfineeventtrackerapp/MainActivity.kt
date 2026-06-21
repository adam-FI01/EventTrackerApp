package com.example.adamfineeventtrackerapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*
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
import androidx.compose.material.icons.filled.Edit
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
import com.example.adamfineeventtrackerapp.data.DatabaseHelper
import com.example.adamfineeventtrackerapp.ui.theme.AdamFineEventTrackerAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = DatabaseHelper(this)
        
        setContent {
            AdamFineEventTrackerAppTheme(darkTheme = true, dynamicColor = false) {
                val myNavController = rememberNavController()
                
                NavHost(navController = myNavController, startDestination = "login_screen") {
                    composable("login_screen") {
                        LoginView(
                            db = db,
                            onSuccess = { myNavController.navigate("grid_screen") }
                        )
                    }

                    composable("grid_screen") {
                        EventGridView(
                            db = db,
                            goToSettings = { myNavController.navigate("sms_screen") }
                        )
                    }

                    composable("sms_screen") {
                        SmsSettingsView(
                            onBack = { myNavController.popBackStack() },
                            onSignOut = {
                                myNavController.navigate("login_screen") {
                                    popUpTo("login_screen") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

class EventReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventName = intent.getStringExtra("eventName") ?: "Event"

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                val smsManager: SmsManager = context.getSystemService(SmsManager::class.java)
                smsManager.sendTextMessage("5554", null, "Reminder: $eventName is starting!", null, null)
            } catch (e: Exception) {
            }
        }
    }
}

@Composable
fun LoginView(
    db: DatabaseHelper,
    onSuccess: () -> Unit
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
                    if (db.checkUser(userText, passText)) {
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
                        if (db.userExists(userText)) {
                            Toast.makeText(context, "Username already exists!", Toast.LENGTH_SHORT).show()
                        } else {
                            db.addUser(userText, passText)
                            Toast.makeText(context, "Account Created! You can now log in.", Toast.LENGTH_SHORT).show()
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
    db: DatabaseHelper,
    goToSettings: () -> Unit
) {
    val context = LocalContext.current
    var events by remember { mutableStateOf(db.getAllEvents()) }
    var dialogOpen by remember { mutableStateOf(false) }
    var editDialogOpen by remember { mutableStateOf(false) }
    
    var nameInput by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf("") }
    var timeInput by remember { mutableStateOf("") }
    var locInput by remember { mutableStateOf("") }
    
    var currentEditId by remember { mutableIntStateOf(-1) }

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
                            Text(ev.eventName, fontWeight = FontWeight.Bold, color = Color(0xFF90CAF9))
                            Text("Date: ${ev.eventDate}", fontSize = 12.sp)
                            Text("Time: ${ev.eventTime}", fontSize = 12.sp)
                            Text("Loc: ${ev.eventLocation}", fontSize = 12.sp)
                            
                            Row(modifier = Modifier.align(Alignment.End)) {
                                IconButton(onClick = {
                                    currentEditId = ev.eventId
                                    nameInput = ev.eventName
                                    dateInput = ev.eventDate
                                    timeInput = ev.eventTime
                                    locInput = ev.eventLocation
                                    editDialogOpen = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = {
                                    db.deleteEvent(ev.eventId)
                                    events = db.getAllEvents()
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                }
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
                    TextField(value = dateInput, onValueChange = { dateInput = it }, label = { Text("When (Ex: 10/27/2025)") })
                    TextField(value = timeInput, onValueChange = { timeInput = it }, label = { Text("Time (Ex: 14:30)") })
                    TextField(value = locInput, onValueChange = { locInput = it }, label = { Text("Where is it?") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    val newId = db.addEvent(nameInput, dateInput, timeInput, locInput)
                    events = db.getAllEvents()
                    
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                        try {
                            val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())
                            val dateObj = sdf.parse("$dateInput $timeInput")
                            
                            if (dateObj != null) {
                                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                val intent = Intent(context, EventReminderReceiver::class.java).apply {
                                    putExtra("eventName", nameInput)
                                }
                                val pendingIntent = PendingIntent.getBroadcast(
                                    context,
                                    newId.toInt(),
                                    intent,
                                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                                )
                                
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                    if (alarmManager.canScheduleExactAlarms()) {
                                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dateObj.time, pendingIntent)
                                    } else {
                                        alarmManager.set(AlarmManager.RTC_WAKEUP, dateObj.time, pendingIntent)
                                    }
                                } else {
                                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, dateObj.time, pendingIntent)
                                }

                                Toast.makeText(context, "Saved! SMS alert scheduled.", Toast.LENGTH_SHORT).show()
                            }
                        } catch (_: Exception) {
                            Toast.makeText(context, "Format error. Use MM/dd/yyyy and HH:mm", Toast.LENGTH_LONG).show()
                        }
                    }

                    dialogOpen = false
                    nameInput = ""; dateInput = ""; timeInput = ""; locInput = ""
                }) { Text("ADD IT") }
            }
        )
    }

    if (editDialogOpen) {
        AlertDialog(
            onDismissRequest = { editDialogOpen = false },
            title = { Text("Update Event") },
            text = {
                Column {
                    TextField(value = nameInput, onValueChange = { nameInput = it }, label = { Text("What is it?") })
                    TextField(value = dateInput, onValueChange = { dateInput = it }, label = { Text("When (Ex: 10/27/2025)") })
                    TextField(value = timeInput, onValueChange = { timeInput = it }, label = { Text("Time (Ex: 14:30)") })
                    TextField(value = locInput, onValueChange = { locInput = it }, label = { Text("Where is it?") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    db.updateEvent(currentEditId, nameInput, dateInput, timeInput, locInput)
                    events = db.getAllEvents()
                    
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                        try {
                            val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())
                            val dateObj = sdf.parse("$dateInput $timeInput")
                            if (dateObj != null) {
                                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                val intent = Intent(context, EventReminderReceiver::class.java).apply {
                                    putExtra("eventName", nameInput)
                                }
                                val pendingIntent = PendingIntent.getBroadcast(
                                    context,
                                    currentEditId,
                                    intent,
                                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                                )
                                alarmManager.set(AlarmManager.RTC_WAKEUP, dateObj.time, pendingIntent)
                            }
                        } catch (_: Exception) {}
                    }
                    
                    editDialogOpen = false
                    nameInput = ""; dateInput = ""; timeInput = ""; locInput = ""
                }) { Text("UPDATE") }
            }
        )
    }
}

@Composable
fun SmsSettingsView(onBack: () -> Unit, onSignOut: () -> Unit) {
    val context = LocalContext.current
    var isAllowed by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED)
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
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
                    else "Status: DISABLED\n\nYou won't get any texts unless you allow SMS."
                )
            }

            Spacer(Modifier.height(30.dp))

            if (!isAllowed) {
                Button(onClick = { 
                    launcher.launch(Manifest.permission.SEND_SMS)
                }) {
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

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = onSignOut,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f))
            ) {
                Text("SIGN OUT", color = Color.White)
            }
        }
    }
}
