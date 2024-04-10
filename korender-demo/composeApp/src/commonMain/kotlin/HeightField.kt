import com.zakgof.korender.math.Vec3

interface HeightField {
    fun elevation(x: Float, z: Float): Float
    fun normal(x: Float, y: Float): Vec3
    fun surface(position: Vec3): Vec3 = Vec3(position.x, elevation(position.x, position.z), position.z)
}
