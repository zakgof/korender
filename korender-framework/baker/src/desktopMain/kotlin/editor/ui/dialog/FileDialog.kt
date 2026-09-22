package editor.ui.dialog

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.openFileSaver
import java.io.File

suspend fun fileDialog(title: String, save: Boolean, lastDir: String?, typeTitle: String, typeExtensions: List<String>, handler: suspend (File) -> Unit) {
    val directory: PlatformFile? = lastDir?.takeIf { it.isNotBlank() }?.let { PlatformFile(it) }
    val dialogSettings = FileKitDialogSettings(title = title)
    if (save) {
        val defaultExtension = typeExtensions.singleOrNull()
        val file = FileKit.openFileSaver(
            suggestedName = "untitled",
            defaultExtension = defaultExtension,
            allowedExtensions = typeExtensions.toSet(),
            directory = directory,
            dialogSettings = dialogSettings
        )
        file?.let { handler(it.file) }
    } else {
        val file = FileKit.openFilePicker(
            type = FileKitType.File(typeExtensions),
            directory = directory,
            dialogSettings = dialogSettings
        )
        file?.let { handler(it.file) }
    }
}
