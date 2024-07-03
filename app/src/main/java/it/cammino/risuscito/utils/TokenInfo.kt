package it.cammino.risuscito.utils

class TokenInfo(
    val sub: String
) {
    override fun toString(): String {
        return "sub: $sub"
    }
}