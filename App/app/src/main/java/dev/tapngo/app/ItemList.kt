package dev.tapngo.app


import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.tapngo.app.utils.httputils.Cookie
import dev.tapngo.app.utils.httputils.CookieType
import dev.tapngo.app.utils.httputils.HttpRequest
import dev.tapngo.app.utils.httputils.RequestMethod
import dev.tapngo.app.utils.inventreeutils.components.ItemData
import dev.tapngo.app.utils.inventreeutils.components.ItemListData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection

//ToDo:  Finish Search Function
//@Composable
//fun ScrollContent(innerPadding: PaddingValues) {
//    val range = 1..100
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxSize(),
//        contentPadding = innerPadding,
//        verticalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        items(range.count()) { index ->
//            Text(text = "- List item number ${index + 1}")
//        }
//    }
//}
// God forgive me if this makes everything insecure.
@SuppressLint("UnrememberedMutableState")
@Composable
fun ItemList(
    navController: NavController,
    item: ItemData?,
    onItemSelected: (ItemListData) -> Unit
) {

    //as much as i hate to admit it, I had to get help from chatgpt on how to properly complete this function,
    // specifically how to pass the selected value back to main to be used for navigation.
    val itemList = remember { mutableStateListOf<ItemListData>() }
    LazyColumn {
        items(itemList) { listitem ->
            ListItem(
                itemListData = listitem,
                onItemClick = {
                    try {
                        onItemSelected(listitem)
                        navController.navigate("checkout/${listitem.sku}")
                    } catch (e: Exception){
                        Log.e("Navigation", "Failed to navigate: ${e.message}")
                    }

            }
            )
        }
    }

    LaunchedEffect(Unit) {
        val items = withContext(Dispatchers.IO) { getItemList() }
        itemList.addAll(items)
    }


    //For Search Function
    //    Scaffold(
//        topBar = {
//
//        },
//    ) { innerPadding ->
//        ScrollContent(innerPadding)
//    }
}


fun getItemList(): List<ItemListData> {
    val cookies = mutableListOf<Cookie>()
    cookies.add(Cookie(CookieType.AUTHORIZATION, "Token $authToken"))

    val request = HttpRequest(
        RequestMethod.GET,
        "http://$server/api/part/?search=&offset=0&limit=25&cascade=1&category=null&category_detail=true&location_detail=true",
        cookies,
        null,
        String::class.java
    )

    val response = request.getResponse()
    if (response.code != HttpURLConnection.HTTP_OK) {
        Log.d("ItemList", "Error: Request failed with response code ${response.code}")
        return listOf()
    }

    val jsonObject = response.getAsJson()
    val results = jsonObject!!.get("results").asJsonArray
    val itemList = mutableListOf<ItemListData>()
    results.forEach {
        val obj = it.asJsonObject
        val id = obj.get("pk").asInt
        val thumbnailUrl = obj.get("thumbnail").asString
        val inStock = obj.get("in_stock").asInt
        val sku = obj.get("name").asString
        val description = obj.get("description").asString
        itemList.add(ItemListData(id, sku, description, thumbnailUrl, inStock))
    }
    return itemList
}