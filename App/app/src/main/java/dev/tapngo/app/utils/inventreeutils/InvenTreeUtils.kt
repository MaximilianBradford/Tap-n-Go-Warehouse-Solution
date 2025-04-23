package dev.tapngo.app.utils.inventreeutils

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.navigation.NavController
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dev.tapngo.app.authToken
import dev.tapngo.app.server
import dev.tapngo.app.utils.httputils.Cookie
import dev.tapngo.app.utils.httputils.CookieType
import dev.tapngo.app.utils.httputils.HttpRequest
import dev.tapngo.app.utils.httputils.RequestMethod
import dev.tapngo.app.utils.inventreeutils.components.Address
import dev.tapngo.app.utils.inventreeutils.components.ItemData
import dev.tapngo.app.utils.inventreeutils.components.Job
import dev.tapngo.app.utils.inventreeutils.components.JobItem
import dev.tapngo.app.utils.inventreeutils.components.Location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import kotlin.concurrent.thread

class InvenTreeUtils {
    companion object {

        val HTTP_PROTOCOL = "http"

        // This gets the item data from the server
        // Extremely unsafe, if request fails this can lock the thread... Which is currently the main thread...
        // TODO - MORE THREADS!!!
        fun getItemData(id: Int, loc: Int?): ItemData {
            val itemData = ItemData(id, loc)
            while (itemData.sku == null || itemData.description == null || itemData.imageData == null) {
                Thread.sleep(100)
            }
            return itemData
        }

        // Sends a login request to the server
        // I thought it would all remain this simple...
        fun sendLoginRequest(
            email: String,
            username: String,
            password: String,
            callback: (String?, Int?) -> Unit
        ) {
            Log.d("LoginScreen", "Sending login request")
            thread {
                val cookies = mutableListOf<Cookie>()

                cookies.add(Cookie(CookieType.CONTENT_TYPE, "application/json; utf-8"))

                val body = JsonObject()
                body.addProperty("username", username)
                body.addProperty("email", email)
                body.addProperty("password", password)

                try {
                    val request = HttpRequest(
                        RequestMethod.POST,
                        "$HTTP_PROTOCOL://$server/api/auth/login/",
                        cookies,
                        body,
                        String::class.java
                    )


                    val response = request.getResponse()
                    Log.d("LoginScreen", "Response Code: ${response.code}")

                    if (response.code == HttpURLConnection.HTTP_OK) {
                        val jsonObject = response.getAsJson()!!.asJsonObject
                        val key = jsonObject!!.get("key").asString
                        Handler(Looper.getMainLooper()).post {
                            callback(key, response.code)
                        }
                    } else {
                        Log.e("LoginScreen", "Login failed with response code ${response.code}")
                        Handler(Looper.getMainLooper()).post {
                            callback(null, response.code)
                        }
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.e("LoginScreen", "Error sending login request", e)
                    Handler(Looper.getMainLooper()).post {
                        callback(null, null)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("LoginScreen", "Unexpected error", e)
                    Handler(Looper.getMainLooper()).post {
                        callback(null, null)
                    }
                }
            }
        }


        fun getLocationList(count: Int?, offset: Int?): List<Location> {
            val limit: Int = 25!!
            val off: Int = 0!!
            val locations: MutableList<Location> = mutableListOf()

            val cookies = mutableListOf<Cookie>()
            cookies.add(Cookie(CookieType.AUTHORIZATION, "Token $authToken"))
            val request = HttpRequest(
                RequestMethod.GET,
                "$HTTP_PROTOCOL://$server/api/stock/location/?structural=false&offset=$off&limit=$limit",
                cookies,
                null,
                String::class.java
            )

            val response = request.getResponse()

            if (response.code != HttpURLConnection.HTTP_OK) {
                Log.d("LocationList", "Error: Request failed with response code ${response.code}")
                return locations
            }

            val jsonObject = response.getAsJson()!!.asJsonObject
            val jsonArray = jsonObject.getAsJsonArray("results")

            jsonArray.forEach {
                val obj = it.asJsonObject
                val id = obj.get("pk").asInt
                val name = obj.get("name").asString
                val description = obj.get("description").asString
                val pathstring = obj.get("pathstring").asString
                val items = obj.get("items").asInt
                val sublocations = obj.get("sublocations").asInt
                locations.add(Location(id, name, description, pathstring, items, sublocations))
            }
            return locations
        }

        fun getPartLocations(id: Int): List<Location> {
            val locations: MutableList<Location> = mutableListOf()

            val cookies = mutableListOf<Cookie>()
            cookies.add(Cookie(CookieType.AUTHORIZATION, "Token $authToken"))

            val request = HttpRequest(
                RequestMethod.GET,
                "$HTTP_PROTOCOL://$server/api/stock/?part=$id&in_stock=true&allow_variants=true&part_detail=true&location_detail=true",
                cookies,
                null,
                String::class.java
            )

            val response = request.getResponse()

            if (response.code != HttpURLConnection.HTTP_OK) {
                Log.d("LocationList", "Error: Request failed with response code ${response.code}")
                return locations
            }

            val jsonArray = response.getAsJson()!!.asJsonArray

            jsonArray.forEach { part ->
                val obj = part.asJsonObject
                val quan = obj.get("quantity").asInt
                val pk = obj.get("pk").asInt
                val location = obj.get("location_detail").asJsonObject
                val id = location.get("pk").asInt
                val name = location.get("name").asString
                var description: String? = null
                var pathstring: String = location.get("pathstring").asString
                var items: Int? = null
                var sublocations: Int? = null
                if (location.has("description"))
                    description = location.get("description").asString
                if (location.has("items"))
                    items = location.get("items").asInt
                if (location.has("sublocations"))
                    sublocations = location.get("sublocations").asInt
                val loc = Location(id, name, description, pathstring, items, sublocations)
                loc.setQuantity(quan)
                loc.setPk(pk)
                locations.add(loc)
            }

            return locations
        }

        fun transferItemWH(
            from: Location,
            to: Location,
            amount: Int,
            navController: NavController
        ) {
            val cookies = mutableListOf<Cookie>()
            cookies.add(Cookie(CookieType.AUTHORIZATION, "Token $authToken"))
            val body = JsonObject()

            val itemJson = JsonObject()
            itemJson.addProperty("pk", from.getPk())
            itemJson.addProperty("quantity", amount)

            val itemsArray = JsonArray()
            itemsArray.add(itemJson)

            body.add("items", itemsArray)
            body.addProperty("notes", "TapNGo App Transfer")
            body.addProperty("location", to.id)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = HttpRequest(
                        RequestMethod.POST,
                        "$HTTP_PROTOCOL://$server/api/stock/transfer/",
                        cookies,
                        body,
                        String::class.java
                    )
                    val response = request.getResponse()
                    Log.d("TransferItem", "Response Code: ${response.code}")
                    Log.d("TransferItem", "Response: ${response.body}")

                    withContext(Dispatchers.Main) {
                        if (response.code >= 200 && response.code < 300) {
                            Log.d("TransferItem", "Transfer successful")
                            navController.navigate("main")
                        } else {
                            Log.e(
                                "TransferItem",
                                "Transfer failed with response code ${response.code}"
                            )
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.e("TransferItem", "Error sending transfer request", e)
                }
            }
        }

        fun getUserJobs(): List<Job> {
            val jobs: MutableList<Job> = mutableListOf()

            val cookies = mutableListOf<Cookie>()
            cookies.add(Cookie(CookieType.AUTHORIZATION, "Token $authToken"))

            val request = HttpRequest(
                RequestMethod.GET,
                "$HTTP_PROTOCOL://$server/api/job/",
                cookies,
                null,
                String::class.java
            )

            val response = request.getResponse()

            if (response.code != HttpURLConnection.HTTP_OK) {
                Log.d("JobList", "Error: Request failed with response code ${response.code}")
                return jobs
            }

            val jsonArray = response.getAsJson()!!.asJsonArray

            jsonArray.forEach { job ->
                val obj = job.asJsonObject
                val id = obj.get("job_id").asInt
                val name = obj.get("name").asString
                val description = obj.get("description").asString
                val status = obj.get("status").asInt
                val addressJson = obj.get("address").asJsonObject
                val address = Address(
                    addressJson.get("id").asInt,
                    addressJson.get("street").asString,
                    addressJson.get("city").asString,
                    addressJson.get("state").asString,
                    addressJson.get("zip_code").asString,
                    addressJson.get("country").asString
                )

                val items = obj.get("items").asJsonArray
                val jobItems: MutableList<JobItem> = mutableListOf()

                items.forEach { item ->
                    val itemObj = item.asJsonObject
                    val itemId = itemObj.get("stock_item").asInt
                    val itemQty = itemObj.get("quantity").asInt
                    val part = ItemData(itemId, null)
                    val jobItem = JobItem(part, itemQty)
                    jobItems.add(jobItem)
                }

                val job = Job(id, address, status, name, description, jobItems)
                jobs.add(job)
            }

            return jobs
        }

        fun getPartFromStockNo(stockNo: Int): ItemData? {

            val cookies = mutableListOf<Cookie>()
            cookies.add(Cookie(CookieType.AUTHORIZATION, "Token $authToken"))

            val request = HttpRequest(
                RequestMethod.GET,
                "$HTTP_PROTOCOL://$server/api/stock/$stockNo/",
                cookies,
                null,
                String::class.java
            )

            val response = request.getResponse()

            if (response.code != HttpURLConnection.HTTP_OK) {
                Log.d("StockItem", "Error: Request failed with response code ${response.code}")
                return null
            }

            val jsonObject = response.getAsJson()!!.asJsonObject
            val itemData = ItemData(jsonObject.get("part").asInt, jsonObject.get("location").asInt)
            itemData.quantity = jsonObject.get("quantity").asInt
            itemData.stockItemId = jsonObject.get("pk").asInt

            return itemData
        }

        fun getStockPart(itemData: ItemData, loc: Int): ItemData? {

            val cookies = mutableListOf<Cookie>()
            cookies.add(Cookie(CookieType.AUTHORIZATION, "Token $authToken"))

            val request = HttpRequest(
                RequestMethod.GET,
                "$HTTP_PROTOCOL://$server/api/stock/?part=${itemData.id}&location=$loc&SKU=${itemData.sku}",
                cookies,
                null,
                String::class.java
            )

            val response = request.getResponse()

            if (response.code != HttpURLConnection.HTTP_OK) {
                Log.d("StockItem", "Error: Request failed with response code ${response.code}")
                return itemData
            }

            val jsonArray = response.getAsJson()!!.asJsonArray

            if(jsonArray.isEmpty) {
                return null
            }

            val jsonObject = jsonArray[0].asJsonObject
            val stockItemId = jsonObject.get("pk").asInt
            val item = ItemData(itemData.id, loc)
            item.quantity = jsonObject.get("quantity").asInt
            item.stockItemId = stockItemId

            return item
        }

        suspend fun transferItemJob(
            itemData: ItemData,
            to: Job,
            amount: Int,
            navController: NavController
        ) {
            if(itemData.selectedLocation == null) {
                Log.e("TransferItem", "Invalid method usage")
                // Do more error handling or something.
                return
            }

            transferItemJob(itemData, itemData.selectedLocation!!, to, amount, navController)
        }

        suspend fun transferItemJob(
            itemData: ItemData,
            from: Location,
            to: Job,
            amount: Int,
            navController: NavController
        ) {
            val cookies = mutableListOf<Cookie>()
            cookies.add(Cookie(CookieType.AUTHORIZATION, "Token $authToken"))

            val stockItem = withContext(Dispatchers.IO) {
                getStockPart(itemData, from.id)
            }

            if (stockItem == null) {
                Log.e("TransferItem", "Error: Stock item not found")
                return
            }

            val body = JsonObject()
            body.addProperty("job", to.id)
            body.addProperty("stock_item", stockItem.stockItemId!!)
            body.addProperty("quantity", amount)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = HttpRequest(
                        RequestMethod.POST,
                        "$HTTP_PROTOCOL://$server/api/job/${to.id}/items/transfer/",
                        cookies,
                        body,
                        String::class.java
                    )
                    val response = request.getResponse()
                    Log.d("TransferItem", "Response Code: ${response.code}")
                    Log.d("TransferItem", "Response: ${response.body}")

                    withContext(Dispatchers.Main) {
                        if (response.code >= 200 && response.code < 300) {
                            Log.d("TransferItem", "Transfer successful")
                            navController.navigate("main")
                        } else {
                            Log.e("TransferItem", "Transfer failed with response code ${response.code}")
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.e("TransferItem", "Error sending transfer request", e)
                }
            }
        }

    }
}
