package city
import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.TouchEvent
import com.zakgof.korender.context.FrameContext
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z

class ChaseCamera {

    private val distance = 1.0f

    private var offset: Vec3 = 1.z

    private var deltaX: Float = 0f
    private var deltaY: Float = 0f
    private var dragStartEvent: TouchEvent? = null
    private var dragStartCamToTarget: Vec3? = null

    fun camera(targetPosition: Vec3, targetLook: Vec3, fc: FrameContext): CameraDeclaration {
        val cameraPos = targetPosition + targetLook * (-distance) + 0.3f.y
        val cameraDir = (targetPosition - cameraPos).normalize()
        val cameraRight = cameraDir % 1.y
        val cameraUp = cameraRight % cameraDir
        return fc.camera(cameraPos, cameraDir, cameraUp)
    }

    fun touch(touchEvent: TouchEvent) {
        if (touchEvent.type == TouchEvent.Type.DOWN) {
        }
        if (touchEvent.type == TouchEvent.Type.UP) {
        }
        if (touchEvent.type == TouchEvent.Type.MOVE && dragStartEvent != null) {
        }
    }

}
