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
import dev.tapngo.app.utils.inventreeutils.components.ItemData
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


        // This gets the item data from the server
        // Extremely unsafe, if request fails this can lock the thread... Which is currently the main thread...
        // TODO - MORE THREADS!!!
        fun getItemData(id: Int): ItemData {
            val itemData = ItemData(id)
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
            callback: (String?) -> Unit
        ) {
            Log.d("LoginScreen", "Sending login request")
            thread {
                val cookies = mutableListOf<Cookie>()

                cookies.add(Cookie(CookieType.CONTENT_TYPE, "application/json; utf-8"))

                val body = JsonObject()
                body.addProperty("email", email)
                body.addProperty("username", username)
                body.addProperty("password", password)

                try {
                    val request = HttpRequest(
                        RequestMethod.POST,
                        "http://$server/api/auth/login/",
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
                            callback(key)
                        }
                    } else {
                        Log.e("LoginScreen", "Login failed with response code ${response.code}")
                        Handler(Looper.getMainLooper()).post {
                            callback(null)
                        }
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.e("LoginScreen", "Error sending login request", e)
                    Handler(Looper.getMainLooper()).post {
                        callback(null)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("LoginScreen", "Unexpected error", e)
                    Handler(Looper.getMainLooper()).post {
                        callback(null)
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
                "http://$server/api/stock/location/?structural=false&offset=$off&limit=$limit",
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
                "http://$server/api/stock/?part=$id&in_stock=true&allow_variants=true&part_detail=true&location_detail=true",
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
                        "http://$server/api/stock/transfer/",
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
    }
}

