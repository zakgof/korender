import com.zakgof.korender.math.Vec3

interface HeightField {
    fun elevation(x: Float, z: Float): Float
    fun normal(x: Float, y: Float): Vec3
}
