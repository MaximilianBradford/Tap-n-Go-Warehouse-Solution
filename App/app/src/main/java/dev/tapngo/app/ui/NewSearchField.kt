package dev.tapngo.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.compose.primaryDark
import com.example.compose.primaryLight
import dev.tapngo.app.utils.setBothThemeColor

@Composable
fun NewSearchField(searchQuery: MutableState<String>, onSubmit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = searchQuery.value,
            onValueChange = { searchQuery.value = it },
            label = { Text("Search Here") },
            modifier = Modifier
                .weight(1f) // Makes the text field take available space
                .padding(end = 8.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            )
        )

        OutlinedButton(
            onClick = onSubmit,
            border = BorderStroke(
                1.dp, setBothThemeColor(
                    lightColor = primaryLight,
                    darkColor = primaryDark
                )
            ),
            modifier = Modifier
                .height(40.dp) // Smaller height for compact button
                .padding(vertical = 4.dp)
        ) {
            Text(
                "Submit",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SearchFieldPreview() {
    val searchQuery = remember { mutableStateOf("") }

    MaterialTheme {
        NewSearchField(searchQuery) {

        }
    }
}