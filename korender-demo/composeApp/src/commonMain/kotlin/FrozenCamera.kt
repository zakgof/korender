
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.projection.FrustumProjection
import com.zakgof.korender.projection.Projection

class FrozenCamera {

    fun camera(targetTransform: Transform, projection: Projection, heightfield: HeightField): Camera {



        val target = targetTransform.mat4() * Vec3.ZERO
        var position = targetTransform.mat4() * Vec3(0f, 2f, 30f)

        val near = (projection as FrustumProjection).near
        val width = projection.width
        val height = projection.height

        // TODO : this does not work
        val p1 = targetTransform.mat4() * Vec3(0f - width, 2f - height, 20f - near)
        val p2 = targetTransform.mat4() * Vec3(0f        , 2f - height, 20f - near)
        val p3 = targetTransform.mat4() * Vec3(0f + width, 2f - height, 20f - near)

        val deep = listOf(p1, p2, p3).maxOf { heightfield.elevation(it.x, it.z) - it.y }
        if (deep > 0)
            position += deep.y

        val direction = (target - position).normalize()
        val right = (direction % 1.y).normalize()
        val up = (right % direction).normalize()
        return DefaultCamera(position, direction, up)
    }

}
