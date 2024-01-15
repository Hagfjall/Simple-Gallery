package se.hagfjall.photosorganizer.mediaRenamer

data class Location(val latitude: Double, val longitude: Double) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is Location) {
            return false
        }

        return (latitude.round(5) == other.latitude.round(5)) &&
                (longitude.round(5) == other.longitude.round(5))
    }

    fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }
}