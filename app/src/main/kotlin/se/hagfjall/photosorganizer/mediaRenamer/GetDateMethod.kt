package se.hagfjall.photosorganizer.mediaRenamer

enum class GetDateMethod
{
    NONE,
    EXIF,
    MODIFIED,
}

enum class GetDateMethodResult
{
    NONE,
    EXIF,
    MODIFIED,
    DATE_ALREADY_IN_FILENAME
}