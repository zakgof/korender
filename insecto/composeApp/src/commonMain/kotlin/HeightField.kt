import com.zakgof.korender.math.Vec3
import kotlin.math.max

interface HeightField {
    fun elevation(x: Float, z: Float): Float
    fun normal(x: Float, y: Float): Vec3
    fun surface(position: Vec3, height: Float = 0f): Vec3 = Vec3(position.x, elevation(position.x, position.z) + height, position.z)

    fun surfaceIfBelow(position: Vec3, height: Float = 0f) =
        Vec3(position.x, max(position.y, elevation(position.x, position.z) + height), position.z)

}
