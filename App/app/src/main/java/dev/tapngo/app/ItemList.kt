package dev.tapngo.app
//Claude used to debug glitch where pagenum changing would not update the page.

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.shape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.compose.primaryDark
import dev.tapngo.app.utils.httputils.Cookie
import dev.tapngo.app.utils.httputils.CookieType
import dev.tapngo.app.utils.httputils.HttpRequest
import dev.tapngo.app.utils.httputils.RequestMethod
import dev.tapngo.app.utils.inventreeutils.components.ItemData
import dev.tapngo.app.utils.inventreeutils.components.ItemListData
import dev.tapngo.app.utils.itemUtils.updateItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection


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

@Composable
fun <T : Any> EndlessLazyColumn(
    //loading: Boolean = false,
    items: List<T>,
    itemKey: (T) -> Any,
    itemContent: @Composable (T) -> Unit,
    loadingItem: @Composable () -> Unit,
    loadMore: () -> Unit,
    isLoadingMore: MutableState<Boolean> = remember { mutableStateOf(false) }
) {
    val listState = rememberLazyListState()
    val buffer = 10
    val reachedBottom: Boolean by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem?.index != 0 && lastVisibleItem?.index == listState.layoutInfo.totalItemsCount - buffer
        }
    }

    LaunchedEffect(reachedBottom) {
        if (reachedBottom && !isLoadingMore.value) loadMore()
    }

    LazyColumn(state = listState) {
        items(
            items = items,
            key = { item: T -> itemKey(item) }
        ) { item ->
            itemContent(item)
        }

        Log.d("LoadingDebug", "Before loading check in LazyColumn, isLoading: $isLoadingMore")
        if (isLoadingMore.value) {
            Log.d("LoadingDebug", "Adding loading item to LazyColumn")
            item {
                loadingItem()
            }
        }

    }

}

@SuppressLint("UnrememberedMutableState")
@Composable
fun ItemList(
    navController: NavController,
) {

    //as much as i hate to admit it, I had to get help from Claude on how to properly complete this function,
    // specifically how to pass the selected value back to main to be used for navigation.
    val coroutineScope = rememberCoroutineScope()
    val itemList = remember { mutableStateListOf<ItemListData>() }
    val offNum = remember { mutableStateOf(0) }
    var searchQuery = remember { mutableStateOf("") }
    val isLoadingMore = remember { mutableStateOf(false) }

    suspend fun populateList(
        offNum: Int,
        searchfunc: String
    ) {
        itemList.clear()
        isLoadingMore.value = true
        try {
            val items = withContext(Dispatchers.IO) { getItemList(offNum, searchfunc) }
            itemList.addAll(items)
        } catch (e: IllegalStateException) {
            Log.d("populateList", "error encountered $e" )
        }
        isLoadingMore.value = false
    }

    suspend fun extendList(
        offNum: Int,
        searchfunc: String
    ) {
        isLoadingMore.value = true
        val items = withContext(Dispatchers.IO) { getItemList(offNum, searchfunc) }
        itemList.addAll(items)
        isLoadingMore.value = false
    }



    @Composable
    fun SearchField(searchQuery: MutableState<String>) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)

        ) {
            TextField(
                value = searchQuery.value,
                onValueChange = {
                    searchQuery.value = it
                },
                label = { Text("Search Here") }
            )
            OutlinedButton(onClick = {
                coroutineScope.launch {
                    populateList(offNum.value, searchQuery.value)
                }
            },
                shape = RectangleShape,
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth(),
            ) { Text("Submit") }
        }

    }


    SearchField(searchQuery)
    Column(
        Modifier.fillMaxWidth()
    ) {
        if (itemList.size == 0){
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center

            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.nodata),
                        contentDescription = "Image Description",
                        modifier = Modifier.size(100.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )

                    Text("No Items Found!")
                }

            }
        } else {
            EndlessLazyColumn(
                items = itemList,
                itemKey = { item: ItemListData -> item.id },
                itemContent = { item: ItemListData ->
                    ListItem(
                        itemListData = item,
                        onItemClick = {
                            try {
                                updateItem(ItemData(item.id, null))
                                navController.navigate("checkout/${item.sku}")
                            } catch (e: Exception) {
                                Log.e("Navigation", "Failed to navigate: ${e.message}")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                },
                loadingItem = { LoadingItem() },
                loadMore = {
                    offNum.value += 50
                    coroutineScope.launch {
                        extendList(offNum.value, searchQuery.value)
                    }
                },
                isLoadingMore = isLoadingMore
            )
        }


    }
//    Column(
//        Modifier.fillMaxSize()
//    ) {
//        LazyColumn(
//            Modifier.weight(1f)
//        ) {
//
//            items(itemList) { listitem ->
//                ListItem(
//                    itemListData = listitem,
//                    onItemClick = {
//                        try {
//                            onItemSelected(listitem)
//                            navController.navigate("checkout/${listitem.sku}")
//                        } catch (e: Exception){
//                            Log.e("Navigation", "Failed to navigate: ${e.message}")
//                        }
//
//                    }
//                )
//            }
//        }
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.End
//        ){
//            var previousButtonActive = true
//            if (offNum.value <= 0){
//                previousButtonActive = false
//            }
//            var nextButtonActive = true
//            if (itemList.size <= 9){
//                nextButtonActive = false
//            }
//            Button(onClick = {
//                offNum.value+=10
//                Log.d("Next Button", "Next Button called")
//                Log.d("Pagenum", "${offNum}")
//                coroutineScope.launch {
//                    populateList(offNum.value, searchQuery.value)
//                }
//            },
//                enabled = nextButtonActive
//            ) {Text("Next")}
//            Button(
//                onClick = {
//                offNum.value-=10
//                Log.d("Previous Button", "Previous Button called")
//                Log.d("Pagenum", "${offNum}")
//                coroutineScope.launch {
//                    populateList(offNum.value, searchQuery.value)
//                }
//            },
//                enabled = previousButtonActive
//                ) {Text("Previous") }
//        }
//    }


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

@Composable
fun LoadingItem() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

}


fun getItemList(
    offNum: Int,
    searchfunc: String
): List<ItemListData> {
    val cookies = mutableListOf<Cookie>()
    cookies.add(Cookie(CookieType.AUTHORIZATION, "Token $authToken"))
    Log.d(
        "Request Call",
        "http://$server/api/part/?search=${searchfunc}&offset=${offNum}&limit=25&cascade=1&category=null&category_detail=true&location_detail=true"
    )
    val request = HttpRequest(
        RequestMethod.GET,
        "http://$server/api/part/?search=${searchfunc}&offset=${offNum}&limit=25&cascade=1&category=null&category_detail=true&location_detail=true",
        cookies,
        null,
        String::class.java

    )

    val response = request.getResponse()
    if (response.code != HttpURLConnection.HTTP_OK) {
        Log.d("ItemList", "Error: Request failed with response code ${response.code}")
        return listOf()
    }

    val jsonObject = response.getAsJson()!!.asJsonObject
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
        Log.d("ItemList Data", "${obj} ${id} ${thumbnailUrl} ${inStock} ${sku} ${description} ")
    }
    return itemList
}