package se.hagfjall.photosorganizer.mediaRenamer

import java.io.InputStream
import java.util.Date

interface IMediaService {
    fun getDate(file: InputStream, vararg methods : GetDateMethod) : Date?
    fun getGpsData(file: InputStream): Location?
    fun getGpsData(filePath: String): Location?
}
