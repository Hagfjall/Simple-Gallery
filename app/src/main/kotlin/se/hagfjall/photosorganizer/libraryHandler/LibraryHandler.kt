package se.hagfjall.photosorganizer.libraryHandler

import se.hagfjall.photosorganizer.mediaRenamer.GetDateMethod
import se.hagfjall.photosorganizer.mediaRenamer.IMediaService
import se.hagfjall.photosorganizer.mediaRenamer.IMediaRenamer
import java.nio.file.Path

class LibraryHandler(
    val mediaService: IMediaService,
    val mediaRenamer: IMediaRenamer
) {

    /**
     * Lists all files in the folder that doesn't have any EXIF data. Basically filter to return only files lacking EXIF data.
     * @arg files List of files to check
     * @arg includeFilesWithDateInFilename If true, files with date in filename will be included in the search (if lacking EXIF-data)
     * @return
     */
    fun GetAllFileMissingDate(
        files: List<Path>,
        includeFilesWithDateInFilename: Boolean
    ): List<Path> {
        var result = mutableListOf<Path>()
        for (file in files) {
            if (!includeFilesWithDateInFilename && mediaRenamer.HasDateInName(file)) {
                continue
            }
//            if(mediaService.getDate(file, GetDateMethod.EXIF) == null) {
//                result.add(file)
//            }
        }
        return result
    }
}