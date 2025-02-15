package com.zakgof.korender.examples

class PriorityQueue<T>(private val priority: (T) -> Float) {

    private val heap = mutableListOf<T>()
    private val indexMap = mutableMapOf<T, Int>()

    fun add(element: T) {
        heap.add(element)
        indexMap[element] = heap.size - 1
        siftUp(heap.size - 1)
    }

    fun poll(): T? {
        if (heap.isEmpty()) return null

        val root = heap[0]
        val last = heap.removeAt(heap.size - 1)
        if (heap.isNotEmpty()) {
            heap[0] = last
            siftDown(0)
        }

        indexMap.remove(root)
        return root
    }

    fun contains(element: T) = indexMap.containsKey(element)

    fun remove(element: T): Boolean {
        val index = indexMap[element] ?: return false
        val last = heap.removeAt(heap.size - 1)

        if (index != heap.size) {
            heap[index] = last
            indexMap[last] = index
            siftUp(index)
            siftDown(index)
        }

        indexMap.remove(element)
        return true
    }

    fun peek(): T? = heap.firstOrNull()

    fun size(): Int = heap.size

    private fun siftUp(index: Int) {
        var currentIndex = index
        while (currentIndex > 0) {
            val parentIndex = (currentIndex - 1) / 2
            if (priority(heap[currentIndex]) >= priority(heap[parentIndex])) break
            swap(currentIndex, parentIndex)
            currentIndex = parentIndex
        }
    }

    private fun siftDown(index: Int) {
        var currentIndex = index
        val size = heap.size
        while (2 * currentIndex + 1 < size) {
            var childIndex = 2 * currentIndex + 1
            if (childIndex + 1 < size && priority(heap[childIndex + 1]) < priority(heap[childIndex])) {
                childIndex++
            }

            if (priority(heap[currentIndex]) <= priority(heap[childIndex])) break

            swap(currentIndex, childIndex)
            currentIndex = childIndex
        }
    }

    private fun swap(i: Int, j: Int) {
        val temp = heap[i]
        heap[i] = heap[j]
        heap[j] = temp
        indexMap[heap[i]] = i
        indexMap[heap[j]] = j
    }
}
