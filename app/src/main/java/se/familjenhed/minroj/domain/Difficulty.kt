package se.familjenhed.minroj.domain

enum class Difficulty(
    val rows: Int,
    val cols: Int,
    val mines: Int,
    val label: String
) {
    SMALL(9, 9, 10, "Liten"),
    MEDIUM(16, 16, 40, "Mellan"),
    LARGE(16, 30, 99, "Stor")
}
