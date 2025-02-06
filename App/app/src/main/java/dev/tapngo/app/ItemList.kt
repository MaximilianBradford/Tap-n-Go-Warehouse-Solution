package dev.tapngo.app
//Claude used to debug glitch where pagenum changing would not update the page.

import android.annotation.SuppressLint
import android.app.DownloadManager.Query
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import kotlin.properties.Delegates

//ToDo:  Finish Search Function
@Composable
fun ScrollContent(innerPadding: PaddingValues) {
    val range = 1..100
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = innerPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(range.count()) { index ->
            Text(text = "- List item number ${index + 1}")
        }
    }
}
// God forgive me if this makes everything insecure.


@SuppressLint("UnrememberedMutableState")
@Composable
fun ItemList(
    navController: NavController,
    item: ItemData?,
    onItemSelected: (ItemListData) -> Unit
) {

    //as much as i hate to admit it, I had to get help from Claude on how to properly complete this function,
    // specifically how to pass the selected value back to main to be used for navigation.
    val coroutineScope = rememberCoroutineScope()
    val itemList = remember { mutableStateListOf<ItemListData>() }
    val offNum = remember { mutableStateOf(0) }
    var searchQuery = remember { mutableStateOf("") }

    suspend fun populateList(
        offNum: Int,
        searchfunc: String
    ){
        itemList.clear()
        val items = withContext(Dispatchers.IO) { getItemList(offNum, searchfunc ) }
        itemList.addAll(items)
    }

    //Claude helped debug why the
    @Composable
    fun SearchField(searchQuery: MutableState<String>) {

        TextField(
            value = searchQuery.value,
            onValueChange = {
                searchQuery.value = it
                coroutineScope.launch {
                    populateList(offNum.value, searchQuery.value)
                }
            },
            label = { Text("Search Here") }
        )
    }


    SearchField(searchQuery)
    Column(
        Modifier.fillMaxSize()
    ) {
        LazyColumn(
            Modifier.weight(1f)
        ) {

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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ){
            var previousButtonActive = true
            if (offNum.value <= 0){
                previousButtonActive = false
            }
            var nextButtonActive = true
            if (itemList.size <= 9){
                nextButtonActive = false
            }
            Button(onClick = {
                offNum.value+=10
                Log.d("Next Button", "Next Button called")
                Log.d("Pagenum", "${offNum}")
                coroutineScope.launch {
                    populateList(offNum.value, searchQuery.value)
                }
            },
                enabled = nextButtonActive
            ) {Text("Next")}
            Button(
                onClick = {
                offNum.value-=10
                Log.d("Previous Button", "Previous Button called")
                Log.d("Pagenum", "${offNum}")
                coroutineScope.launch {
                    populateList(offNum.value, searchQuery.value)
                }
            },
                enabled = previousButtonActive
                ) {Text("Previous") }
        }
    }



    LaunchedEffect(Unit) {
        populateList(0, "")
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


fun getItemList(
    offNum: Int,
    searchfunc: String
): List<ItemListData> {
    val cookies = mutableListOf<Cookie>()
    cookies.add(Cookie(CookieType.AUTHORIZATION, "Token $authToken"))
    Log.d("Request Call", "http://$server/api/part/?search=${searchfunc}&offset=${offNum}&limit=10&cascade=1&category=null&category_detail=true&location_detail=true")
    val request = HttpRequest(
        RequestMethod.GET,
        "http://$server/api/part/?search=${searchfunc}&offset=${offNum}&limit=10&cascade=1&category=null&category_detail=true&location_detail=true",
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