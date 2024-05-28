import com.zakgof.korender.FrameInfo
import com.zakgof.korender.camera.Camera
import com.zakgof.korender.image.Images
import com.zakgof.korender.input.TouchEvent
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.projection.Projection

class Controller {


    val elevationRatio = 300.0f
    val hfImage = Images.image("/terrain/hf-rg16-512.png")
    val hf = RgImageHeightField(hfImage, 20.0f, elevationRatio)

    // TODO : var is poor
    var characterManager = CharacterManager(hf, hf.surface(Vec3.ZERO, -1.0f))
    var enemyManager = EnemyManager(hf)
    var skullManager = SkullManager()
    var explosionManager = ExplosionManager()
    var missileManager = MissileManager(hf, explosionManager)
    var chaseCamera = ChaseCamera(characterManager.transform())

    var gameOver: Boolean = false
    var gameOverTime: Float = 0f

    private val collisionDetector = CollisionDetector()

    fun restart(touchEvent: TouchEvent) {
        if (touchEvent.type == TouchEvent.Type.DOWN) {
            characterManager = CharacterManager(hf, hf.surface(Vec3.ZERO, -1.0f))
            enemyManager = EnemyManager(hf)
            skullManager = SkullManager()
            explosionManager = ExplosionManager()
            missileManager = MissileManager(hf, explosionManager)
            chaseCamera = ChaseCamera(characterManager.transform())
            gameOver = false
        }
    }

    fun update(frameInfo: FrameInfo) {
        explosionManager.update(frameInfo.time, frameInfo.dt)
        characterManager.update(frameInfo.dt)
        missileManager.update(frameInfo.time, frameInfo.dt)
        enemyManager.update(characterManager.transform() * Vec3.ZERO, frameInfo.time, frameInfo.dt)
        skullManager.update(characterManager.transform() * Vec3.ZERO, frameInfo.time, frameInfo.dt)

        collisionDetector.clear()
        collisionDetector.update(CharacterManager::class, characterManager, CharacterManager::transform, 1f)
        enemyManager.heads.map { collisionDetector.update(EnemyManager.Head::class, it, EnemyManager.Head::transform, 1.5f) }
        missileManager.missiles.map { collisionDetector.update(MissileManager.Missile::class, it, MissileManager.Missile::transform) }

        collisionDetector.detect(CharacterManager::class, EnemyManager.Head::class) {
            if (!gameOver) {
                gameOver = true
                gameOverTime = frameInfo.time
            }
            enemyManager.hit(it.second)
            explosionManager.boom(it.second.transform().mat4() * Vec3.ZERO, 12f, frameInfo.time)
        }
        collisionDetector.detect(MissileManager.Missile::class, EnemyManager.Head::class) {
            enemyManager.hit(it.second)
            missileManager.missileHitEnemy(it.first, frameInfo.time)
            characterManager.incrementScore(10)
        }
        collisionDetector.go()
    }

    fun camera(bugTransform: Transform, projection: Projection, width: Int, height: Int, dt: Float, time: Float): Camera {
        return if (gameOver && time - gameOverTime > 2f) {
            FlyAwayCamera().camera(bugTransform, time - gameOverTime - 2f)
        } else {
            chaseCamera.camera(bugTransform, projection, width, height, hf, dt)
        }
    }

}