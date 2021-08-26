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

package site.liangbai.nbtspawner.api.nms.factory

import site.liangbai.nbtspawner.util.unwrappedAsValue
import site.liangbai.nbtspawner.util.wrappedAsNBTBase
import taboolib.common.reflect.Reflex.Companion.getProperty

class NBTTagListFactory(private val nbtList: Any) : AbstractNBTFactory<Int>() {
    override val handle: Any
        get() = nbtList

    private val list by lazy {
        nbtList.getProperty<MutableList<Any>>("list")!!
    }

    override fun <T> findAs(index: Int): T? {
        return list[index].unwrappedAsValue<T>()
    }

    override operator fun set(index: Int, value: Any) {
        list[index] = value.wrappedAsNBTBase()
    }

    override fun keys(): List<Int> {
        return (0 until list.size).toList()
    }

    override fun size(): Int {
        return list.size
    }

    override fun contains(index: Int) = index < list.size
    override fun clear() {
        list.clear()
    }

    override fun remove(index: Int) {
        list.removeAt(index)
    }

    override fun indexOf(any: Any): Int {
        return list.map { it.unwrappedAsValue<Any>() }.indexOf(any)
    }

    override fun add(index: Int, value: Any) {
        list.add(value.wrappedAsNBTBase())
    }
}