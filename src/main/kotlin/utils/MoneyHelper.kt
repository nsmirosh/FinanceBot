package nick.mirosh.utils

fun parseSumText(sumText: String): Int =
    if (sumText.contains(".")) {
        val parts = sumText.split(".")
        if (parts.size != 2) {
            throw IllegalArgumentException("Invalid sum format: $sumText")
        }
        val integerPart = parts[0].toInt()
        val cents = parts[1].toInt()
        integerPart * 100 + cents
    } else {
        sumText.toInt() * 100
    }


fun parseIntToText(amount: Int): String {
    if (amount == 0) return "0.00"

    val integerPart = amount / 100
    val centsPart = amount % 100
    return if (centsPart < 10) "$integerPart.0$centsPart" else "$integerPart.$centsPart"
}
