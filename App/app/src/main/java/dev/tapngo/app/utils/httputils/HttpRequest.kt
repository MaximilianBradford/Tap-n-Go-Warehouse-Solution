package dev.tapngo.app.utils.httputils

import android.util.Log
import com.google.gson.JsonObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class HttpRequest<T>(
    val method: RequestMethod,
    val url: String,
    val cookies: List<Cookie>,
    val body: JsonObject?,
    val responseType: Class<T>
) {

    private var response: Response<T>

    init {
        var connection: HttpURLConnection? = null
        Log.d("HttpRequest", "Sending request to $url")
        try {
            val url = URL(url)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = method.name
            connection.setRequestProperty("Content-Type", "application/json")
            cookies.forEach { cookie ->
                connection.setRequestProperty(cookie.getKey().toString(), cookie.getValue())
            }
            connection.doOutput = method == RequestMethod.POST
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.instanceFollowRedirects = true

            if (body != null && method == RequestMethod.POST) {
                Log.d("HttpRequest", "Sending body: $body")
                OutputStreamWriter(connection.outputStream).use {
                    it.write(body.toString())
                    it.flush()
                }
            } else {
                Log.d("HttpRequest", "No body to send or method is not POST")
            }

            val responseCode = connection.responseCode
            Log.d("HttpRequest", "Response code: $responseCode")

            val responseBody: T = if (responseCode in 200..299) {
                if (responseType == String::class.java) {
                    connection.inputStream.bufferedReader().use { it.readText() as T }
                } else if (responseType == ByteArray::class.java) {
                    connection.inputStream.readBytes() as T
                } else {
                    throw IllegalArgumentException("Unsupported response type")
                }
            } else {
                if (responseType == String::class.java) {
                    connection.errorStream?.bufferedReader()?.use { it.readText() as T } ?: "" as T
                } else {
                    throw IllegalArgumentException("Unsupported response type")
                }
            }
            response = Response(responseCode, responseBody)
            if (responseType == String::class.java) {
                Log.d("HttpRequest", "Response: ${response.getAsJson().toString()}")
            } else {
                Log.d("HttpRequest", "Response is not a string")
            }
        } catch (e: Exception) {
            Log.e("HttpRequest", "Error during HTTP request", e)
            response = Response(
                HttpURLConnection.HTTP_INTERNAL_ERROR,
                e.message as T ?: "Unknown error" as T
            )
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