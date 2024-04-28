import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec3
import kotlin.reflect.KClass

class CollisionDetector {

    private val xlist = mutableListOf<Entry<out Any>>()
    private val zlist = mutableListOf<Entry<out Any>>()
    private val handlers = mutableMapOf<KClass<out Any>, Map<KClass<out Any>, Handler<Any, Any>>>()

    fun clear() {
        xlist.clear()
        zlist.clear()
        handlers.clear()
    }

    fun <T : Any> update(kClass: KClass<T>, entity: T, transformProvider: (T) -> Transform, radius: Float = 0f) {
        val entry = Entry(kClass, entity, transformProvider(entity) * Vec3.ZERO, radius)
        xlist.add(entry)
        zlist.add(entry)
        xlist.sortBy { it.position.x }
        zlist.sortBy { it.position.z }
    }

    fun <T : Any, O : Any> detect(kClass1: KClass<T>, kClass2: KClass<O>, block: Handler<T, O>) {
        handlers.computeIfAbsent(kClass1) {
            mutableMapOf(kClass2 to (block as Handler<Any, Any>))
        }
    }

    fun go() {
        val xpairs = mutableSetOf<CollisionPair>()
        for (i in 0..<xlist.size - 1) {
            val t = xlist[i]
            val o = xlist[i + 1]
            if (t.position.x - o.position.x < t.radius + o.radius) {
                xpairs.add(CollisionPair(t, o))
            }
        }
        val zpairs = mutableSetOf<CollisionPair>()
        for (i in 0..<zlist.size - 1) {
            val t = zlist[i]
            val o = zlist[i + 1]
            if (t.position.z - o.position.z < t.radius + o.radius) {
                zpairs.add(CollisionPair(t, o))
            }
        }
        val candidatePairs = xpairs intersect zpairs
        for (cp in candidatePairs) {
            if ((cp.first.position - cp.second.position).lengthSquared() < cp.first.radius + cp.second.radius) {
                handlers[cp.first.kClass]?.get(cp.second.kClass)?.invoke(Collision(cp.first.entity, cp.second.entity))
                handlers[cp.second.kClass]?.get(cp.first.kClass)?.invoke(Collision(cp.second.entity, cp.first.entity))
            }
        }
    }

}

class Collision<T : Any, O : Any>(
    val first: T,
    val second: O
)

private class Entry<T : Any>(val kClass: KClass<T>, val entity: T, val position: Vec3, val radius: Float) {

}

typealias Handler<T, O> = (Collision<T, O>) -> Unit

private class CollisionPair(var first: Entry<out Any>, var second: Entry<out Any>) {
    override fun equals(other: Any?) =
        other is CollisionPair &&
                (this.first == other.first && this.second == other.second ||
                        this.first == other.second && this.second == other.first)

    override fun hashCode() = first.hashCode() + second.hashCode()
}

