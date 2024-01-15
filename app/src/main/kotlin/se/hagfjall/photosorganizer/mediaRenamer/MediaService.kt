package se.hagfjall.photosorganizer.mediaRenamer
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date

class MediaService : IMediaService {
    override fun getDate(file: InputStream, vararg methods: GetDateMethod): Date? {
        for (method in methods) {
            when (method) {
                GetDateMethod.EXIF -> {
                    val date = getExifDateTime(file)
                    if (date != null) {
                        return date
                    }
                }

                GetDateMethod.MODIFIED -> {
                    val date = getModifiedDateTime(file)
                    if (date != null) {
                        return date
                    }
                }

                else -> {
                    return null
                }
            }
        }
        return null
    }

    override fun getGpsData(file: InputStream): Location? {
        return getExifGpsData(file)
    }

    override fun getGpsData(filePath: String): Location? {
        return getExifGpsData(File(filePath).inputStream())
    }

    private fun getExifGpsData(file: InputStream): Location? {
        val exif = ExifInterface(file)
        val lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
        val latRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
        val lon = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
        val lonRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)
        if (lat != null && latRef != null &&
            lon != null && lonRef != null
        ) {
            val latitude = convertGPSCoordinatesToDouble(lat, latRef)
            val longitude = convertGPSCoordinatesToDouble(lon, lonRef)
            return Location(longitude = longitude, latitude = latitude)
        } else {
            return null
        }
    }

    private fun convertGPSCoordinatesToDouble(value: String, reference: String): Double {
        val latitude = value.split(",")
        val degrees = latitude[0].split("/")[0].toDouble() / latitude[0].split("/")[1].toDouble()
        val minutes = latitude[1].split("/")[0].toDouble() / latitude[1].split("/")[1].toDouble()
        val seconds = latitude[2].split("/")[0].toDouble() / latitude[2].split("/")[1].toDouble()
        val result = degrees + (minutes / 60) + (seconds / 3600)
        return if (reference == "S" || reference == "W") {
            -result
        } else {
            result
        }
    }

    private fun getExifDateTime(file: InputStream): Date? {
        val exif = ExifInterface(file)
        val date = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
        return if (date != null && date != ":: ::") {
            SimpleDateFormat("yyyy:MM:d H:m:s").parse(date)
        } else {
            null
        }
    }

    private fun getModifiedDateTime(path: InputStream): Date? {
        return null
    }
}
