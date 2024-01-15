package se.hagfjall.photosorganizer.mediaRenamer

import java.nio.file.Path

interface IMediaRenamer {
    /**
     * This function returns How it got the result and potentially new paths
     */
    fun RenameToDropboxFormat(
        path: Path, vararg fetchTypes: GetDateMethod
    ): Pair<GetDateMethodResult, Path?>

    /**
     * Requires that the file is already in Dropbox format. Library root should not include any year.
     * Example: /home/user/temp/2019-01-01 12.00.00.jpg with library root /home/user/Pictures
     * will create /home/users/Pictures/2019/2019-01/2019-01-01 12.00.00.jpg
     */
    fun OrganizeInLibraryFormat(filename : Path, libraryRoot : Path
        ): RenameCommand?

    // Returns True if the path doesn't match against the regex
    fun HasDateInName(path: Path): Boolean {
        return regex.containsMatchIn(path.fileName.toString())
    }
    companion object {
        val regex = Regex("((\\d{4})-\\d{2})-\\d{2}\\s\\d{2}\\.\\d{2}\\.\\d{2}")
    }


}