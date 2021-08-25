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

package site.liangbai.nbtspawner.api.nms

import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import site.liangbai.nbtspawner.api.nms.factory.AbstractNBTFactory
import taboolib.module.nms.nmsProxy

abstract class NMS {
    abstract fun readEntity(entity: Entity): AbstractNBTFactory<String>

    abstract fun readItemStack(itemStack: ItemStack): AbstractNBTFactory<String>

    abstract fun writeEntity(entity: Entity, nbtFactory: AbstractNBTFactory<String>)

    abstract fun writeItemStack(itemStack: ItemStack, nbtFactory: AbstractNBTFactory<String>)

    abstract fun write(target: Any, nbtFactory: AbstractNBTFactory<String>)

    abstract fun <FAC : AbstractNBTFactory<*>> findFactory(obj: Any): FAC

    abstract fun <DATA> unwrappedAsValue(target: Any): DATA?

    abstract fun wrappedAsNBTBase(value: Any): Any

    companion object {
        @JvmStatic
        val INSTANCE by lazy {
            nmsProxy<NMS>()
        }
    }
}