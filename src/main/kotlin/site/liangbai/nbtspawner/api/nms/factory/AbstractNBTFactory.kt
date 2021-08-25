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

abstract class AbstractNBTFactory<USAGE> : Iterable<Pair<USAGE, Any>>{
    abstract val handle: Any

    abstract fun <T> findAs(index: USAGE): T?

    fun <T> getAs(index: USAGE): T = findAs<T>(index)!!

    operator fun get(index: USAGE) = findAs<Any>(index)

    abstract operator fun set(index: USAGE, value: Any)

    abstract fun add(index: USAGE, value: Any)

    abstract fun keys(): List<USAGE>

    abstract fun size(): Int

    abstract fun clear()

    abstract fun remove(index: USAGE)

    abstract fun indexOf(any: Any): USAGE?

    abstract operator fun contains(index: USAGE): Boolean

    override fun iterator(): Iterator<Pair<USAGE, Any>> {
        return object : Iterator<Pair<USAGE, Any>> {
            private var index: Int = 0
            private val keys by lazy {
                keys()
            }

            override fun hasNext(): Boolean {
                return index < size()
            }

            override fun next(): Pair<USAGE, Any> {
                val key = keys[index++]
                return Pair(key, this@AbstractNBTFactory[key]!!)
            }
        }
    }

    override fun toString(): String {
        return handle.toString()
    }

    override fun hashCode(): Int {
        return handle.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return handle == other
    }
}