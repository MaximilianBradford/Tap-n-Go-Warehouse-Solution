package dev.tapngo.app.utils.httputils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser


// This is a response for an HTTP request
// The only reason I have a generic is for images.
class Response<T>(val code: Int, val body: T) {

    fun getRequestBody(): T {
        return body
    }

    // If this gets called on a response that isn't a string, you will break some things.
    fun getAsJson(): JsonElement? {
        if (body is JsonObject) {
            return body
        }
        if (body is String) {
            return JsonParser.parseString(body as String)
        }
        return null
    }

    fun isSuccessful(): Boolean {
        return code in 200..299
    }
}