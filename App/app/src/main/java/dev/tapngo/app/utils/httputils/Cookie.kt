package dev.tapngo.app.utils.httputils

// Yes. I made a wrapper for a pair.
class Cookie(key: CookieType, value: String) {
    private var cookie: Pair<CookieType, String> = Pair(key, value)

    fun getKey(): CookieType {
        return cookie.first
    }

    fun getValue(): String {
        return cookie.second
    }
}