package dev.tapngo.app.utils.httputils

import com.google.gson.JsonObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log

// I originally wanted to do something like axios, but it was twice as long, so I made the RequestMethod enum for this.
class HttpRequest<T>(val method: RequestMethod, val url: String, val cookies: List<Cookie>, val body: JsonObject?, val responseType: Class<T>) {

    private var response: Response<T>

    // This performs the request when initialized.
    init {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(url)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = method.name
            // Cookies are just properties.
            // Probably safe to not touch this ever again?
            cookies.forEach { cookie ->
                connection.setRequestProperty(cookie.getKey().toString(), cookie.getValue())
            }
            // For post this is set to true since the body is being sent for processing.
            connection.doOutput = method == RequestMethod.POST

            // I didn't need a timeout before my ISP dunked on itself earlier today.
            // But it's been helpful for seeing when requests are unable to reach the server in a what is considerably long time.
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            // Only send body with post
            if (body != null && method == RequestMethod.POST) {
                OutputStreamWriter(connection.outputStream).use {
                    it.write(body.toString())
                    it.flush()
                }
            }

            // Handle response
            val responseCode = connection.responseCode

            // if successful, read the response
            val responseBody: T = if (responseCode in 200..299) {
                // If the response type is a string, just use bufferedReader instead of trying to decode bytes
                if (responseType == String::class.java) {
                    connection.inputStream.bufferedReader().use { it.readText() as T }
                } else if (responseType == ByteArray::class.java) { // Otherwise HANDLE THEM BYTES!!!
                    connection.inputStream.readBytes() as T
                } else {
                    // I expect this to never be thrown.
                    throw IllegalArgumentException("Unsupported response type")
                }
            } else {
                // Getting the error if request fails
                if (responseType == String::class.java) {
                    connection.errorStream?.bufferedReader()?.use { it.readText() as T } ?: "" as T
                } else {
                    throw IllegalArgumentException("Unsupported response type")
                }
            }
            response = Response(responseCode, responseBody)
        } catch (e: Exception) {
            Log.e("HttpRequest", "Error during HTTP request", e)
            response = Response(HttpURLConnection.HTTP_INTERNAL_ERROR, e.message as T ?: "Unknown error" as T)
        } finally {
            connection?.disconnect()
        }
    }

    fun getResponse(): Response<T> {
        return response
    }

    fun getRequestMethod(): RequestMethod {
        return method
    }

    fun getRequestUrl(): String {
        return url
    }

    fun getRequestCookies(): List<Cookie> {
        return cookies
    }

    fun getRequestBody(): JsonObject? {
        return body
    }
}