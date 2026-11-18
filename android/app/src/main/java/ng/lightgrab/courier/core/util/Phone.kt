package ng.lightgrab.courier.core.util

/** Normalises Nigerian numbers (080…, 80…, 234…) to E.164 (+234…). */
object NigerianPhone {
    fun toE164(raw: String): String? {
        val digits = raw.filter { it.isDigit() || it == '+' }.removePrefix("+")
        val national = when {
            digits.startsWith("234") -> digits.drop(3)
            digits.startsWith("0") -> digits.drop(1)
            else -> digits
        }
        return if (national.length == 10 && national.first() in "789") "+234$national" else null
    }
}
