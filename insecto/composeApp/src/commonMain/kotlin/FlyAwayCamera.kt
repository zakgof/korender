
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import kotlin.math.min

class FlyAwayCamera {
    fun camera(characterTransform: Transform, time: Float): Camera {
        val cameraPos = characterTransform * (Vec3(0f, 10f, 10f) * min(1.0f + time * 3.0f, 50f))
        val look = (characterTransform * Vec3.ZERO - cameraPos).normalize()
        val right = look % 1.y
        val up = (right % look).normalize()
        return DefaultCamera(cameraPos, look, up)
    }

}
