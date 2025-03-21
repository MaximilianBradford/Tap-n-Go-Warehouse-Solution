package dev.tapngo.app

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.components.BuildConfig
import dev.tapngo.app.utils.dynamicColor
import dev.tapngo.app.utils.inventreeutils.InvenTreeUtils


// Login screen component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController) {
    // Some states for login screen information
    // Yes, I did just hardcode the credentials... I'm not typing out my password every time I test this
    var username by remember { mutableStateOf("admin") }
    var password by remember { mutableStateOf("admin") }
    var email by remember { mutableStateOf("contact@danielj.dev") }
    var errorMessage by remember { mutableStateOf("") }

    // This column just slaps stuff in the center of the screen.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {

        Text(
            text = "LOGIN", style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = dynamicColor(
                lightColor = { Color.Black },
                darkColor = { Color.White }
            )
        )
        Spacer(modifier = Modifier.height(32.dp))
        Image(
            painter = painterResource(id = R.drawable.banner_login), // Replace with your image
            contentDescription = "Banner Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Adjust height as needed
                .clip(RoundedCornerShape(12.dp)), // Optional rounded corners
            contentScale = ContentScale.Crop // Ensures image covers the area properly
        )
        Spacer(modifier = Modifier.height(32.dp))
        // Fields for text input
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            textStyle = TextStyle()
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
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Login", color = Color.White,
                fontSize = 18.sp,  // Increase font size
                fontWeight = FontWeight.Bold )
        }

        // Error handling
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }

    }
}


@Composable
@Preview(showBackground = true, showSystemUi = true)
fun LoginScreenPreview() {
    LoginScreen(navController = rememberNavController())
}