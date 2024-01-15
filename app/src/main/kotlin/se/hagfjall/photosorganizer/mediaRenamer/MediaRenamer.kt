package se.hagfjall.photosorganizer.mediaRenamer

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.io.path.Path
import kotlin.io.path.extension

class MediaRenamer : IMediaRenamer {
    constructor(mediaService: IMediaService) {
        this._mediaService = mediaService
    }

    private var _mediaService: IMediaService
    private val _dateFormat = SimpleDateFormat("YYYY-MM-dd HH.mm.ss")

    override fun RenameToDropboxFormat(path: Path, vararg methods: GetDateMethod): Pair<GetDateMethodResult, Path?> {
        if (HasDateInName(path)) {
            return Pair(GetDateMethodResult.DATE_ALREADY_IN_FILENAME, path)
        }
        for (fetchType in methods) {
//            val date = _mediaService.getDate(path, fetchType)
//            if (date != null) {
//
//                return Pair(map(fetchType), getName(path, date))
//            }
        }
        return Pair(GetDateMethodResult.NONE, path)
    }

    /**
     * Requires that the file is already in Dropbox format. Library root should not include any year.
     * Example: /home/user/temp/2019-01-01 12.00.00.jpg with library root /home/user/Pictures
     * will create /home/users/Pictures/2019/2019-01/2019-01-01 12.00.00.jpg
     */
    override fun OrganizeInLibraryFormat(filename: Path, libraryRoot: Path): RenameCommand? {
        val matches = IMediaRenamer.regex.find(filename.fileName.toString()) ?: return null
        val yearAndMonth = matches.groupValues[1]
        val year = matches.groupValues[2]
        return RenameCommand(
            currentFile = filename,
            newFile = Paths.get(libraryRoot.toString(), year, yearAndMonth, filename.fileName.toString())
        )
    }

    // keeps the file extension and all paths, but changes the name of the file to date
    private fun getName(path: Path, date: Date): Path {
        val newFilename = _dateFormat.format(date) + "." + path.extension
        if (path.parent != null) {
            return Path(path.parent.toString() + File.separator + newFilename)
        } else {
            return Path(newFilename)
        }
    }

    private fun map(src: GetDateMethod): GetDateMethodResult {
        val converted = GetDateMethodResult.values().find { it.name.equals(src.toString(), ignoreCase = true) }
        if (converted != null) {
            return converted
        } else {
            return GetDateMethodResult.NONE
        }
    }
}