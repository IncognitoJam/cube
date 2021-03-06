package com.github.incognitojam.cube.game.collider

import com.github.incognitojam.cube.engine.graphics.mesh.ColouredMesh
import com.github.incognitojam.cube.engine.maths.floorInt
import com.github.incognitojam.cube.game.entity.Entity
import com.github.incognitojam.cube.game.world.Location
import com.github.incognitojam.cube.game.world.World
import org.joml.Math
import org.joml.Vector3f
import org.joml.Vector3fc
import org.joml.Vector3i

class Collider(private val width: Float, private val height: Float, position: Vector3fc? = null) {

    private val halfWidth = width / 2F

    var minPoint = Vector3f()
    var maxPoint = Vector3f()

    var collidingBlockPos = Vector3i()
    var minBlock = Vector3i()
    var maxBlock = Vector3i()

    lateinit var mesh: ColouredMesh

    init {
        position?.let { updateCollider(it) }
    }

    fun initialise() {
        val positions = floatArrayOf(
                // front
                -halfWidth, 0f, halfWidth,
                halfWidth, 0f, halfWidth,
                halfWidth, height, halfWidth,
                -halfWidth, height, halfWidth,
                // back
                -halfWidth, 0f, -halfWidth,
                halfWidth, 0f, -halfWidth,
                halfWidth, height, -halfWidth,
                -halfWidth, height, -halfWidth
        )

        val colours = floatArrayOf(
                1f, 1f, 1f,
                1f, 1f, 1f,
                1f, 1f, 1f,
                1f, 1f, 1f,
                1f, 1f, 1f,
                1f, 1f, 1f,
                1f, 1f, 1f,
                1f, 1f, 1f
        )

        val indices = intArrayOf(
                0, 1, 2,
                2, 3, 0,
                1, 5, 6,
                6, 2, 1,
                7, 6, 5,
                5, 4, 7,
                4, 0, 3,
                3, 7, 4,
                4, 5, 1,
                1, 0, 4,
                3, 2, 6,
                6, 7, 3
        )

        mesh = ColouredMesh(positions, colours, indices)
        mesh.initialise()
    }

    fun delete() = mesh.delete(true)

    fun doesCollide(entitySelf: Entity, position: Vector3fc, world: World): Boolean {
        return doesCollideWorld(position, world) || doesCollideEntity(entitySelf, position, world) != null
    }

    fun doesCollideWorld(position: Vector3fc, world: World): Boolean {
        return checkBlockCollision(position) { blockPosition -> Location(world, blockPosition).block?.solid == true }
    }

    fun doesCollideEntity(entitySelf: Entity, position: Vector3fc, world: World): Entity? {
        updateCollider(position)
        return world.getEntities().filter { it != entitySelf }.firstOrNull { entity ->
            entity.collider.doesColliderIntersect(this)
        }
    }

    private fun doesColliderIntersect(other: Collider): Boolean {
        if (minPoint.x > other.maxPoint.x) return false
        if (maxPoint.x < other.minPoint.x) return false
        if (minPoint.y > other.maxPoint.y) return false
        if (maxPoint.y < other.minPoint.y) return false
        if (minPoint.z > other.maxPoint.z) return false
        if (maxPoint.z < other.minPoint.z) return false

        return true
    }

    fun getIntersectLocation(other: Collider): Vector3fc? {
        if (!doesColliderIntersect(other)) return null

        val startX = Math.max(other.minPoint.x, minPoint.x)
        val startY = Math.max(other.minPoint.y, minPoint.y)
        val startZ = Math.max(other.minPoint.z, minPoint.z)

        val endX = Math.min(other.maxPoint.x, maxPoint.x)
        val endY = Math.min(other.maxPoint.y, maxPoint.y)
        val endZ = Math.min(other.maxPoint.z, maxPoint.z)

        return Vector3f((startX - endX) / 2f, (startY - endY) / 2f, (startZ - endZ) / 2f)
    }

    private fun checkBlockCollision(position: Vector3fc, callback: (Vector3i) -> Boolean): Boolean {
        updateCollider(position)

        for (x in minBlock.x..maxBlock.x) {
            for (y in minBlock.y..maxBlock.y) {
                for (z in minBlock.z..maxBlock.z) {
                    val blockPos = Vector3i(x, y, z)
                    if (callback.invoke(blockPos)) {
                        collidingBlockPos = blockPos
                        return true
                    }
                }
            }
        }

        collidingBlockPos = Vector3i()
        return false
    }

    private fun updateCollider(position: Vector3fc) {
        minPoint = Vector3f(position).sub(halfWidth, 0f, halfWidth)
        maxPoint = Vector3f(position).add(halfWidth, 0f, halfWidth)

        minBlock = Vector3i(minPoint.x.floorInt(), minPoint.y.floorInt(), minPoint.z.floorInt())
        maxBlock = Vector3i(maxPoint.x.floorInt(), maxPoint.y.floorInt(), maxPoint.z.floorInt())
    }

}