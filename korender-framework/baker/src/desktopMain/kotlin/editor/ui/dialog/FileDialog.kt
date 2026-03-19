package com.zakgof.korender.baker.editor.ui.dialog

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
    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        handler(chooser.selectedFile)
    }
}