package dev.tapngo.app.utils.inventreeutils.components

import android.util.Log
import dev.tapngo.app.authToken
import dev.tapngo.app.server
import dev.tapngo.app.utils.httputils.Cookie
import dev.tapngo.app.utils.httputils.CookieType
import dev.tapngo.app.utils.httputils.HttpRequest
import dev.tapngo.app.utils.httputils.RequestMethod
import java.net.HttpURLConnection

class ItemListData(val id: Int, val sku: String, val description: String, var thumbnailUrl: String, val quantity: Int) {
    lateinit var thumbnail: ByteArray

    init {
        // I expect there to never be a full url returned from above.. But who knows.
        if (!thumbnailUrl.startsWith("http://") && !thumbnailUrl.startsWith("https://")) {
            thumbnailUrl = "http://$server$thumbnailUrl"
        }

        val cookies = mutableListOf<Cookie>()
        cookies.add(Cookie(CookieType.AUTHORIZATION, "Token $authToken"))

        // Get the image!!!
        val imageRequest = HttpRequest(RequestMethod.GET, thumbnailUrl, cookies, null, ByteArray::class.java)

        if(imageRequest.getResponse().code == HttpURLConnection.HTTP_OK) {
            val imageData = imageRequest.getResponse().body

            this.thumbnail = imageData
        } else {
            Log.e("MainActivity", "Error: Image request failed with response code ${imageRequest.getResponse().code}")
        }
    }
}