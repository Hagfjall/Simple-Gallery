package se.hagfjall.photosorganizer.fileutils

import se.hagfjall.photosorganizer.mediaRenamer.RenameCommand
import java.nio.file.Path

class Renamer(val dryRun: Boolean = false) {
    /**
     * Renames files according to the rename commands
     * @return Pair of success and failed files
     */
    fun RenameFiles(renameCommands: List<RenameCommand>): Pair<List<Path>, List<Path>> {
        val success = mutableListOf<Path>()
        val failed = mutableListOf<Path>()
        for (command in renameCommands) {
            assertDirectoryExist(command.newFile.parent)
            try {
                if (!dryRun) {
                    command.currentFile.toFile().renameTo(command.newFile.toFile())
                }
                success.add(command.newFile)
            } catch (e: Exception) {
                println("Failed to rename file: ${command.currentFile} to ${command.newFile}")
                failed.add(command.currentFile)
            }
        }
        return Pair(success, failed)
    }

    private fun assertDirectoryExist(parent: Path) {
        if (dryRun) {
            return

        }
        if (!parent.toFile().exists()) {
            parent.toFile().mkdirs()
        }
    }
}