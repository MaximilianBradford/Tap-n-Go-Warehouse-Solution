package dev.tapngo.app


import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import dev.tapngo.app.utils.httputils.Cookie
import dev.tapngo.app.utils.httputils.CookieType
import dev.tapngo.app.utils.httputils.HttpRequest
import dev.tapngo.app.utils.httputils.RequestMethod
import dev.tapngo.app.utils.inventreeutils.components.ItemListData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection

@Composable
fun ItemList() {
    
    val itemList = remember { mutableStateListOf<ItemListData>() }

    LaunchedEffect(Unit) {
        val items = withContext(Dispatchers.IO) { getItemList() }
        itemList.addAll(items)
    }

    LazyColumn {
        items(itemList) { item ->
            ListItem(itemListData = item)
        }
    }
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