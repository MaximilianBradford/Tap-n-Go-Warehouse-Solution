package dev.tapngo.app.utils.inventreeutils.components

import android.util.Log
import dev.tapngo.app.authToken
import dev.tapngo.app.server
import dev.tapngo.app.utils.httputils.Cookie
import dev.tapngo.app.utils.httputils.CookieType
import dev.tapngo.app.utils.httputils.HttpRequest
import dev.tapngo.app.utils.httputils.RequestMethod
import java.io.IOException
import java.net.HttpURLConnection
import kotlin.concurrent.thread

class ItemData(val id: Int) {
    // Basic item data.
    var sku: String? = null
    var description: String? = null
    var imageUrl: String? = null
    var imageData: ByteArray? = null

    // Fetch item data from the server
    // THIS does run on another thread. however if this request fails, the app locks.
    init {
        Log.d("MainActivity" , id.toString())
        thread {
            val cookies = mutableListOf<Cookie>()
            cookies.add(Cookie(CookieType.AUTHORIZATION, "Token $authToken"))
            try {
                // I literally yoinked this from my network tab.
                val request = HttpRequest(RequestMethod.GET, "http://$server/api/stock/$id/?part_detail=true&location_detail=true", cookies, null, String::class.java)

                val response = request.getResponse()
                Log.d("MainActivity", "Response Code: ${response.code}")

                // if the first response is okay, we can then the image
                if (response.code == HttpURLConnection.HTTP_OK) {
                    val jsonObject = response.getAsJson()
                    val desc = jsonObject!!.get("part_detail").asJsonObject.get("description").asString
                    val sku = jsonObject.get("SKU").asString
                    var imgUrl = jsonObject.get("part_detail").asJsonObject.get("image").asString

                    // I expect there to never be a full url returned from above.. But who knows.
                    if (!imgUrl.startsWith("http://") && !imgUrl.startsWith("https://")) {
                        imgUrl = "http://$server$imgUrl"
                    }

                    // Get the image!!!
                    val imageRequest = HttpRequest(RequestMethod.GET, imgUrl, cookies, null, ByteArray::class.java)

                    if(imageRequest.getResponse().code == HttpURLConnection.HTTP_OK) {
                        val imageData = imageRequest.getResponse().body

                        this.sku = sku
                        this.description = desc
                        this.imageUrl = imgUrl
                        this.imageData = imageData
                    } else {
                        Log.e("MainActivity", "Error: Image request failed with response code ${imageRequest.getResponse().code}")
                    }
                } else {
                    Log.e("MainActivity", "Error: Response JSON is null")
                }

            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("MainActivity", "Error sending request", e)
            }
        }
    }
}