package se.hagfjall.photosorganizer.mediaRenamer

interface IFileHandler {
    fun RenameFiles(files : List<RenameCommand>) {}
}