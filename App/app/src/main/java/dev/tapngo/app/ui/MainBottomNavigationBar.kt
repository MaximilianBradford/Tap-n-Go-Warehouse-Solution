package dev.tapngo.app.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.FormatListNumberedRtl
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.compose.primaryDark
import com.example.compose.primaryLight
import com.example.compose.secondaryDark
import com.example.compose.secondaryLight
import dev.tapngo.app.MainScreenState
import dev.tapngo.app.utils.dynamicColor
import dev.tapngo.app.utils.setBothThemeColor

@Composable
fun BottomNavigationBar(
    navController: NavController,
    currentScreen: MainScreenState,
    mainScreenState: (MainScreenState) -> Unit
) {
    BottomAppBar(
        modifier = Modifier.height(56.dp), // Standard bottom bar height
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp), // Adds spacing from edges
            horizontalArrangement = Arrangement.SpaceEvenly // Evenly distribute buttons
        ) {
            BottomNavItem(
                icon = Icons.Filled.Nfc,
                title = "NFC",
                isSelected = currentScreen == MainScreenState.NFCScan,
                onClick = {
                    mainScreenState(MainScreenState.NFCScan)
                    navController.popBackStack(route = "main", inclusive = false)
                    Log.d("NavBar", "NFC Called")
                }
            )
            BottomNavItem(
                icon = Icons.Filled.FormatListNumberedRtl,
                title = "Items",
                isSelected = currentScreen == MainScreenState.ItemList,
                onClick = {
                    mainScreenState(MainScreenState.ItemList)
                    navController.popBackStack(route = "main", inclusive = false)
                    Log.d("NavBar", "ItemList Called")
                }
            )
            BottomNavItem(
                icon = Icons.Filled.Camera,
                title = "Barcode",
                isSelected = currentScreen == MainScreenState.Barcode,
                onClick = {
                    mainScreenState(MainScreenState.Barcode)
                    navController.popBackStack(route = "main", inclusive = false)
                    Log.d("NavBar", "Barcode Called")
                }
            )
            BottomNavItem(
                icon = Icons.Filled.Work,
                title = "Job",
                isSelected = currentScreen == MainScreenState.Job,
                onClick = {
                    mainScreenState(MainScreenState.Job)
                    navController.popBackStack(route = "main", inclusive = false)
                    Log.d("NavBar", "Job Called")
                }
            )
        }
    }
}
//
//@Composable
//fun BottomNavItem(
//    icon: androidx.compose.ui.graphics.vector.ImageVector,
//    contentDescription: String,
//    onClick: () -> Unit
//) {
//    IconButton(onClick = onClick,) {
//        Icon(
//            icon,
//            contentDescription = contentDescription,
//        )
//    }
//}
@Composable
fun BottomNavItem(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(
                onClick = onClick,
                indication = rememberRipple(bounded = true), // Ripple effect
                interactionSource = remember { MutableInteractionSource() }
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = if (isSelected) setBothThemeColor(lightColor = primaryLight, darkColor = primaryDark) else setBothThemeColor(
                lightColor = secondaryLight,
                darkColor = secondaryDark
            )
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) setBothThemeColor(lightColor = primaryLight, darkColor = primaryDark) else setBothThemeColor(
                lightColor = secondaryLight,
                darkColor = secondaryDark
            )
        )
    }
}

@Composable
@Preview
fun BottomNavigationBarPreview() {
    BottomNavigationBar(navController = rememberNavController(), currentScreen = MainScreenState.ItemList) {

    }
}