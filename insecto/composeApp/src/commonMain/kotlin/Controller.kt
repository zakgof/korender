
import com.zakgof.korender.CameraDeclaration
import com.zakgof.korender.FrameInfo
import com.zakgof.korender.TouchEvent
import com.zakgof.korender.context.KorenderContext
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Controller(kc: KorenderContext) {

    val elevationRatio = 300.0f
    var game: Game? = null

    init {
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            val hfImage = kc.loadImage("terrain/hf-rg16-512.png").await()
            val hf = RgImageHeightField(hfImage, 20.0f, elevationRatio)
            game = Game(hf)
        }
    }

    class Game(private val hf: RgImageHeightField) {

        var session: GameSession = GameSession(hf)

        fun restart(touchEvent: TouchEvent) {
            if (touchEvent.type == TouchEvent.Type.DOWN) { // TODO ????
                session = GameSession(hf)
            }
        }
    }

    class GameSession(val hf: RgImageHeightField) {

        // TODO : var is poor
        val characterManager = CharacterManager(hf, hf.surface(Vec3(3274f, 0f, 4596f)))
        val enemyManager = EnemyManager(hf)
        val skullManager = SkullManager(hf)
        val explosionManager = ExplosionManager()
        val missileManager = MissileManager(hf, explosionManager)
        val chaseCamera = ChaseCamera(characterManager.transform())

        var gameOver: Boolean = false
        var gameOverTime: Float = 0f

        private val collisionDetector = CollisionDetector()

        fun update(frameInfo: FrameInfo) {
            explosionManager.update(frameInfo.time, frameInfo.dt)
            characterManager.update(frameInfo.dt)
            missileManager.update(frameInfo.time, frameInfo.dt)
            enemyManager.update(characterManager.transform() * Vec3.ZERO, frameInfo.dt)
            skullManager.update(characterManager.transform() * Vec3.ZERO, frameInfo.time, frameInfo.dt)

            collisionDetector.clear()
            collisionDetector.update(CharacterManager::class, characterManager, CharacterManager::transform, 1f)
            enemyManager.heads.map { collisionDetector.update(EnemyManager.Head::class, it, EnemyManager.Head::transform, 1.5f) }
            missileManager.missiles.map { collisionDetector.update(MissileManager.Missile::class, it, MissileManager.Missile::transform) }
            skullManager.skulls.map { collisionDetector.update(SkullManager.Skull::class, it, SkullManager.Skull::transform, 7.0f) }

            collisionDetector.detect(CharacterManager::class, EnemyManager.Head::class) {
                if (!gameOver) {
                    gameOver = true
                    gameOverTime = frameInfo.time
                }
                enemyManager.hit(it.second)
                explosionManager.boom(it.second.transform().offset(), 1f, 15f, frameInfo.time)
            }
            collisionDetector.detect(MissileManager.Missile::class, EnemyManager.Head::class) {
                enemyManager.hit(it.second)
                missileManager.destroyMissile(it.first)
                explosionManager.boom(it.second.transform().offset(), 1f, 12f, frameInfo.time)
                characterManager.incrementScore(10)
            }
            collisionDetector.detect(MissileManager.Missile::class, SkullManager.Skull::class) {
                skullManager.hit(it.second)
                missileManager.destroyMissile(it.first)
                explosionManager.boom(it.second.transform.offset(), 8f, 20f, frameInfo.time)
                characterManager.incrementScore(100)
            }
            collisionDetector.go()
        }

        fun camera(kc: KorenderContext, bugTransform: Transform, dt: Float, time: Float): CameraDeclaration {
            return if (gameOver && time - gameOverTime > 2f) {
                FlyAwayCamera().camera(kc, bugTransform, time - gameOverTime - 2f)
            } else {
                chaseCamera.camera(bugTransform, kc, hf, dt)
            }
        }
    }

}