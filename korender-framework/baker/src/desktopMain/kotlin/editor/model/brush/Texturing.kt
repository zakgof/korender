package editor.model.brush

data class Texturing(
    val u: Axis = Axis(),
    val v: Axis = Axis(),
    val fitToFace: Boolean
) {
    data class Axis(
        val scale: Float = 1f,
        val offset: Float = 0f
    )
}
