package editor.state

import editor.model.Model

class History {

    private val MAX_SIZE = 500

    private val undoStack = ArrayDeque<Model>()
    private val redoStack = ArrayDeque<Model>()

    fun push(model: Model) {

        if (model === undoStack.lastOrNull())
            return

        undoStack.addLast(model)
        redoStack.clear()
        if (undoStack.size > MAX_SIZE) {
            undoStack.removeFirst() // drop oldest
        }
    }

    fun undo(): Model? {
        val model = undoStack.removeLastOrNull() ?: return null
        redoStack.addLast(model)
        return model
    }

    fun redo(): Model? {
        val model = redoStack.removeLastOrNull() ?: return null
        undoStack.addLast(model)
        return model
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }

    fun canUndo() = undoStack.isNotEmpty()

    fun canRedo() = redoStack.isNotEmpty()
}