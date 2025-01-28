package dev.tapngo.app.utils.httputils

// Used since there's about 8000 types of cookies InvenTree uses
enum class CookieType(private val type: String) {
    AUTHORIZATION("Authorization"),
    CSRFTOKEN("X-CSRFToken"),
    CONTENT_TYPE("Content-Type");

    override fun toString(): String {
        return type
    }
}