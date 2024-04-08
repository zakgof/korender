
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.camera.DefaultCamera
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y

class FrozenCamera {

    fun camera(targetTransform: Transform): Camera {

        val target = targetTransform.mat4() * Vec3.ZERO
        val position = targetTransform.mat4() * Vec3(0f, 5f, 20f)

        val direction = (target - position).normalize()
        val right = (direction % 1.y).normalize()
        val up = (right % direction).normalize()
        return DefaultCamera(position, direction, up)
    }

}
