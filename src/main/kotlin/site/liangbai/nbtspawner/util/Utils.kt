/*
 * RealHomeHunt
 * Copyright (C) 2021  Liangbai
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package site.liangbai.nbtspawner.util

import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import site.liangbai.nbtspawner.api.nms.NMS
import site.liangbai.nbtspawner.api.nms.factory.AbstractNBTFactory

fun <USAGE> AbstractNBTFactory<USAGE>.forEach(action: (USAGE, Any) -> Unit) {
    forEach { action(it.first, it.second) }
}

fun Any.wrappedAsNBTBase() = NMS.INSTANCE.wrappedAsNBTBase(this)

fun <DATA> Any.unwrappedAsValue() = NMS.INSTANCE.unwrappedAsValue<DATA>(this)

fun Entity.readNBT() = NMS.INSTANCE.readEntity(this)

fun ItemStack.readNBT() = NMS.INSTANCE.readItemStack(this)

fun Block.readNBT() = NMS.INSTANCE.readBlock(this)

fun Entity.writeNBT(factory: AbstractNBTFactory<String>) {
    NMS.INSTANCE.writeEntity(this, factory)
}

fun ItemStack.writeNBT(factory: AbstractNBTFactory<String>) {
    NMS.INSTANCE.writeItemStack(this, factory)
}

fun Block.writeNBT(factory: AbstractNBTFactory<String>) {
    NMS.INSTANCE.writeBlock(this, factory)
}