package com.simplemobiletools.gallery.pro.models

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.Immutable
import com.bumptech.glide.signature.ObjectKey
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import java.io.File

open class FileDirItemDto(
    val path: String,
    val name: String = "",
    var isDirectory: Boolean = false,
    var children: Int = 0,
    var size: Long = 0L,
    var gpsLatitude: Double = 0.0,
    var gpsLongitude: Double = 0.0,
    var modified: Long = 0L,
    var mediaStoreId: Long = 0L
) :
    Comparable<FileDirItemDto> {
    companion object {
        var sorting = 0
    }

    override fun toString() =
        "FileDirItemDto(path=$path, name=$name, isDirectory=$isDirectory, children=$children, size=$size, modified=$modified, gpsLatitude=$gpsLatitude, gpsLongitude=$gpsLongitude, mediaStoreId=$mediaStoreId)"

    override fun compareTo(other: FileDirItemDto): Int {
        return if (isDirectory && !other.isDirectory) {
            -1
        } else if (!isDirectory && other.isDirectory) {
            1
        } else {
            var result: Int
            when {
                sorting and SORT_BY_NAME != 0 -> {
                    result = if (sorting and SORT_USE_NUMERIC_VALUE != 0) {
                        AlphanumericComparator().compare(name.normalizeString().lowercase(), other.name.normalizeString().lowercase())
                    } else {
                        name.normalizeString().lowercase().compareTo(other.name.normalizeString().lowercase())
                    }
                }

                sorting and SORT_BY_SIZE != 0 -> result = when {
                    size == other.size -> 0
                    size > other.size -> 1
                    else -> -1
                }

                sorting and SORT_BY_DATE_MODIFIED != 0 -> {
                    result = when {
                        modified == other.modified -> 0
                        modified > other.modified -> 1
                        else -> -1
                    }
                }

                else -> {
                    result = getExtension().lowercase().compareTo(other.getExtension().lowercase())
                }
            }

            if (sorting and SORT_DESCENDING != 0) {
                result *= -1
            }
            result
        }
    }

    fun getExtension() = if (isDirectory) name else path.substringAfterLast('.', "")

    fun getBubbleText(context: Context, dateFormat: String? = null, timeFormat: String? = null) = when {
        sorting and SORT_BY_SIZE != 0 -> size.formatSize()
        sorting and SORT_BY_DATE_MODIFIED != 0 -> modified.formatDate(context, dateFormat, timeFormat)
        sorting and SORT_BY_EXTENSION != 0 -> getExtension().lowercase()
        else -> name
    }

    fun getProperSize(context: Context, countHidden: Boolean): Long {
        return when {
            context.isRestrictedSAFOnlyRoot(path) -> context.getAndroidSAFFileSize(path)
            context.isPathOnOTG(path) -> context.getDocumentFile(path)?.getItemSize(countHidden) ?: 0
            isNougatPlus() && path.startsWith("content://") -> {
                try {
                    context.contentResolver.openInputStream(Uri.parse(path))?.available()?.toLong() ?: 0L
                } catch (e: Exception) {
                    context.getSizeFromContentUri(Uri.parse(path))
                }
            }

            else -> File(path).getProperSize(countHidden)
        }
    }

    fun getProperFileCount(context: Context, countHidden: Boolean): Int {
        return when {
            context.isRestrictedSAFOnlyRoot(path) -> context.getAndroidSAFFileCount(path, countHidden)
            context.isPathOnOTG(path) -> context.getDocumentFile(path)?.getFileCount(countHidden) ?: 0
            else -> File(path).getFileCount(countHidden)
        }
    }

    fun getDirectChildrenCount(context: Context, countHiddenItems: Boolean): Int {
        return when {
            context.isRestrictedSAFOnlyRoot(path) -> context.getAndroidSAFDirectChildrenCount(path, countHiddenItems)
            context.isPathOnOTG(path) -> context.getDocumentFile(path)?.listFiles()?.filter { if (countHiddenItems) true else !it.name!!.startsWith(".") }?.size
                ?: 0

            else -> File(path).getDirectChildrenCount(context, countHiddenItems)
        }
    }

    fun getLastModified(context: Context): Long {
        return when {
            context.isRestrictedSAFOnlyRoot(path) -> context.getAndroidSAFLastModified(path)
            context.isPathOnOTG(path) -> context.getFastDocumentFile(path)?.lastModified() ?: 0L
            isNougatPlus() && path.startsWith("content://") -> context.getMediaStoreLastModified(path)
            else -> File(path).lastModified()
        }
    }

    fun getParentPath() = path.getParentPath()

    fun getDuration(context: Context) = context.getDuration(path)?.getFormattedDuration()

    fun getFileDurationSeconds(context: Context) = context.getDuration(path)

    fun getArtist(context: Context) = context.getArtist(path)

    fun getAlbum(context: Context) = context.getAlbum(path)

    fun getTitle(context: Context) = context.getTitle(path)

    fun getResolution(context: Context) = context.getResolution(path)

    fun getVideoResolution(context: Context) = context.getVideoResolution(path)

    fun getImageResolution(context: Context) = context.getImageResolution(path)

    fun getPublicUri(context: Context) = context.getDocumentFile(path)?.uri ?: ""

    fun getSignature(): String {
        val lastModified = if (modified > 1) {
            modified
        } else {
            File(path).lastModified()
        }

        return "$path-$lastModified-$size"
    }

    fun getKey() = ObjectKey(getSignature())

    fun assembleContentUri(): Uri {
        val uri = when {
            path.isImageFast() -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            path.isVideoFast() -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else -> MediaStore.Files.getContentUri("external")
        }

        return Uri.withAppendedPath(uri, mediaStoreId.toString())
    }
}

fun FileDirItemDto.asReadOnly() = FileDirItemReadOnly(
    path = path,
    name = name,
    isDirectory = isDirectory,
    children = children,
    size = size,
    modified = modified,
    mediaStoreId = mediaStoreId
)

fun FileDirItemReadOnly.asFileDirItem() = FileDirItemDto(
    path = path,
    name = name,
    isDirectory = isDirectory,
    children = children,
    size = size,
    modified = modified,
    mediaStoreId = mediaStoreId
)

@Immutable
class FileDirItemReadOnly(
    path: String,
    name: String = "",
    isDirectory: Boolean = false,
    children: Int = 0,
    size: Long = 0L,
    gpsLatitude: Double = 0.0,
    gpsLongitude: Double = 0.0,
    modified: Long = 0L,
    mediaStoreId: Long = 0L
) : FileDirItemDto(path, name, isDirectory, children, size, gpsLatitude, gpsLongitude, modified, mediaStoreId)
