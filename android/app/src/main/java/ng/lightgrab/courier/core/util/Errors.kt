package ng.lightgrab.courier.core.util

import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import java.io.IOException

class UserFacingException(message: String) : Exception(message)

/** Turns any failure into a short sentence a courier can act on. */
fun Throwable.userMessage(): String = when (this) {
    is UserFacingException -> message ?: "Something went wrong."
    is RestException -> description?.takeIf { it.isNotBlank() } ?: error.ifBlank { "The server refused the request." }
    is HttpRequestException, is IOException -> "No connection. Check your signal and try again."
    else -> message?.takeIf { it.isNotBlank() } ?: "Something went wrong. Please try again."
}
