
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.input.TouchEvent
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.projection.FrustumProjection
import com.zakgof.korender.projection.Projection
import kotlin.math.min

class ChaseCamera (initialTarget: Transform) {

    var target = initialTarget * Vec3.ZERO
    var position = initialTarget * Vec3(0f, 2f, 10f)

    private var deltaX: Float = 0f
    private var deltaY: Float = 0f
    private var dragStartEvent: TouchEvent? = null
    private var dragStartCamToTarget: Vec3? = null

    fun camera(targetTransform: Transform, projection: Projection, width: Int, height: Int, hf: HeightField, dt: Float): Camera {

        target = targetTransform * Vec3.ZERO
        val frustum =  projection as FrustumProjection

        if (dragStartCamToTarget != null) {

            val startDirection = dragStartCamToTarget!!.normalize()
            val startRight = (startDirection % 1.y).normalize()
            val startUp = (startRight % startDirection).normalize()


            position = target + dragStartCamToTarget!! +
                    startRight * (deltaX / width * frustum.width * 8.0f) +
                    startUp * (deltaY / height * frustum.height * 8.0f)
        } else {
            val expectedPosition = targetTransform * Vec3(0f, 2f, 10f)
            val expectedJump = expectedPosition - position
            position += expectedJump.normalize() * min(expectedJump.length(), 50f * dt)
        }

        val r = frustum.near * 0.5f + (frustum.width*frustum.width+frustum.height*frustum.height) * 0.125f / frustum.near
        position = hf.surfaceIfBelow(position, r)

        val direction = (target - position).normalize()
        val right = (direction % 1.y).normalize()
        val up = (right % direction).normalize()
        return DefaultCamera(position, direction, up)
    }

    fun touch(touchEvent: TouchEvent) {
        if (touchEvent.type == TouchEvent.Type.DOWN) {
            dragStartEvent = touchEvent
            dragStartCamToTarget = position - target
        }
        if (touchEvent.type == TouchEvent.Type.UP) {
            dragStartEvent = null
            dragStartCamToTarget = null
            deltaX = 0f
            deltaY = 0f
        }
        if (touchEvent.type == TouchEvent.Type.MOVE && dragStartEvent != null) {
            deltaX = touchEvent.x - dragStartEvent!!.x
            deltaY = touchEvent.y - dragStartEvent!!.y
        }

    }

}
