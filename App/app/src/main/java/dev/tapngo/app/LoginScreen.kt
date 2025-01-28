package dev.tapngo.app

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.tapngo.app.utils.inventreeutils.InvenTreeUtils

// Login screen component
@Composable
fun LoginScreen(navController: NavHostController) {
    // Some states for login screen information
    // Yes, I did just hardcode the credentials... I'm not typing out my password every time I test this
    var username by remember { mutableStateOf("admin") }
    var password by remember { mutableStateOf("inventree") }
    var email by remember { mutableStateOf("contact@danielj.dev") }
    var errorMessage by remember { mutableStateOf("") }

    // This column just slaps stuff in the center of the screen.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Login", style = MaterialTheme.typography.titleLarge, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        // Fields for text input
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = Color.White) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.White)
        )
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username", color = Color.White) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.White)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", color = Color.White) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            textStyle = TextStyle(color = Color.White)
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Login button
        // Sends credentials to sendLoginRequest function, uses a callback to handle the response
        Button(
            onClick = {
                if (username.isNotEmpty() && password.isNotEmpty()) {
                    Log.d("LoginScreen", "Sending login request - Username: $username, Password: $password")
                    InvenTreeUtils.sendLoginRequest(email, username, password) { key ->
                        if (key != null) {
                            authToken = key
                            navController.navigate("main")
                        } else {
                            errorMessage = "Login failed. Please check your credentials."
                        }
                    }
                } else {
                    errorMessage = "Please enter email, username, and password"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login", color = Color.Black)
        }
        // Error handling
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}