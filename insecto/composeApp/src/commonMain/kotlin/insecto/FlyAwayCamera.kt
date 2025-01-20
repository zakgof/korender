package insecto
import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import kotlin.math.min

class FlyAwayCamera {
    fun camera(kc: KorenderContext, characterTransform: Transform, time: Float): CameraDeclaration {
        val cameraPos = characterTransform * (Vec3(0f, 10f, 10f) * min(1.0f + time * 3.0f, 50f))
        val look = (characterTransform * Vec3.ZERO - cameraPos).normalize()
        val right = look % 1.y
        val up = (right % look).normalize()
        return kc.camera(cameraPos, look, up)
    }

}
