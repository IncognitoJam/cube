package com.github.incognitojam.cube.game.block

import com.github.incognitojam.cube.game.item.Items
import com.github.incognitojam.cube.game.world.Direction

class BlockStone(id: Byte) : Block(id) {

    override fun getItem() = Items.STONE

    override fun getTextureId(direction: Direction, itemDrop: Boolean) = 1

}