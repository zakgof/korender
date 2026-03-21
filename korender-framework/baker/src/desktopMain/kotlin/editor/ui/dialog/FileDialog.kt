package editor.ui.dialog

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

fun fileDialog(title: String, save: Boolean, lastDir: String?, typeTitle: String, typeExtension: String, handler: (File) -> Unit) {
    val chooser = JFileChooser()
    chooser.dialogType = if (save) JFileChooser.SAVE_DIALOG else JFileChooser.OPEN_DIALOG
    chooser.dialogTitle = title
    chooser.currentDirectory = lastDir?.let { File(it) }
    chooser.fileFilter = FileNameExtensionFilter(
        "$typeTitle (*.$typeExtension)", typeExtension
    )
    val res = if (save) chooser.showSaveDialog(null) else chooser.showOpenDialog(null)
    if (res == JFileChooser.APPROVE_OPTION) {
        var file = chooser.selectedFile
        if (save && !file.endsWith(".$typeExtension"))
            file = File(file.parentFile, file.name + "." + typeExtension)
        handler(file)
    }
}