package dev.tapngo.app.utils.inventreeutils

import android.util.Log
import com.google.gson.JsonObject
import dev.tapngo.app.server
import dev.tapngo.app.utils.httputils.Cookie
import dev.tapngo.app.utils.httputils.CookieType
import dev.tapngo.app.utils.httputils.HttpRequest
import dev.tapngo.app.utils.httputils.RequestMethod
import java.io.IOException
import android.os.Handler
import android.os.Looper
import dev.tapngo.app.utils.inventreeutils.components.ItemData
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
        fun sendLoginRequest(email: String, username: String, password: String, callback: (String?) -> Unit) {
            Log.d("LoginScreen", "Sending login request")
            thread {
                val cookies = mutableListOf<Cookie>()

                cookies.add(Cookie(CookieType.CONTENT_TYPE, "application/json; utf-8"))

                val body = JsonObject()
                body.addProperty("email", email)
                body.addProperty("username", username)
                body.addProperty("password", password)

                try {
                    val request = HttpRequest(RequestMethod.POST, "http://$server/api/auth/login/", cookies, body, String::class.java)

                    val response = request.getResponse()
                    Log.d("LoginScreen", "Response Code: ${response.code}")

                    if (response.code == HttpURLConnection.HTTP_OK) {
                        val jsonObject = response.getAsJson()
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
    }
}

