import com.zakgof.korender.FrameInfo
import com.zakgof.korender.image.Images
import com.zakgof.korender.math.Vec3

class Controller {

    val elevationRatio = 300.0f
    val hfImage = Images.image("/hf-rg16-512.png")
    val hf = RgImageHeightField(hfImage, 20.0f, elevationRatio)
    val characterManager = CharacterManager(hf, hf.surface(Vec3.ZERO, -1.0f))
    val enemyManager = EnemyManager(hf)
    val explosionManager = ExplosionManager()
    val missileManager = MissileManager(hf, explosionManager)
    val chaseCamera = ChaseCamera(characterManager.transform())

    private val collisionDetector = CollisionDetector()

    fun update(frameInfo: FrameInfo) {
        explosionManager.update(frameInfo.time, frameInfo.dt)
        characterManager.update(frameInfo.dt)
        missileManager.update(frameInfo.time, frameInfo.dt)
        enemyManager.update(characterManager.transform() * Vec3.ZERO, frameInfo.time, frameInfo.dt)

        collisionDetector.clear()
        collisionDetector.update(CharacterManager::class, characterManager, CharacterManager::transform, 1f)
        enemyManager.heads.map { collisionDetector.update(EnemyManager.Head::class, it, EnemyManager.Head::transform, 1.5f) }
        missileManager.missiles.map { collisionDetector.update(MissileManager.Missile::class, it, MissileManager.Missile::transform) }

        collisionDetector.detect(CharacterManager::class, EnemyManager.Head::class) {
            characterManager.hit()
            enemyManager.hit(it.second)
            explosionManager.boom(it.second.transform().mat4() * Vec3.ZERO, 24f, frameInfo.time)
        }
        collisionDetector.detect(MissileManager.Missile::class, EnemyManager.Head::class) {
            enemyManager.hit(it.second)
            missileManager.missileHitEnemy(it.first, frameInfo.time)
            characterManager.incrementScore(10)
        }
        collisionDetector.go()
    }

}