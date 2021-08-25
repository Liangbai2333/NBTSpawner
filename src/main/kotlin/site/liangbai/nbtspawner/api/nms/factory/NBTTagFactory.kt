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

import site.liangbai.nbtspawner.api.nms.AutoPreservable
import site.liangbai.nbtspawner.api.nms.NMS
import site.liangbai.nbtspawner.util.unwrappedAsValue
import site.liangbai.nbtspawner.util.wrappedAsNBTBase
import taboolib.common.reflect.Reflex.Companion.getProperty

class NBTTagFactory(private val nbt: Any, var target: Any? = null, var autoSave: Boolean = false) : AbstractNBTFactory<String>(), AutoPreservable<Any> {
    override val handle: Any
        get() = nbt

    private val map by lazy {
        nbt.getProperty<MutableMap<String, Any>>("map")!!
    }

    override fun <T> findAs(index: String): T? {
        return map[index]?.unwrappedAsValue<T>()
    }

    override operator fun set(index: String, value: Any) {
        map[index] = value.wrappedAsNBTBase()

        save()
    }

    override fun keys(): List<String> {
        return map.keys.toList()
    }

    override fun size(): Int {
        return map.size
    }

    override fun contains(index: String) = map.containsKey(index)
    override fun clear() {
        map.clear()

        save()
    }

    override fun remove(index: String) {
        map.remove(index)

        save()
    }

    override fun indexOf(any: Any): String? {
        for (pair in this) {
            if (pair.second.unwrappedAsValue<Any>() == any) {
                return pair.first
            }
        }

        return null
    }

    override fun add(index: String, value: Any) {
        this[index] = value
    }

    override fun bindTo(target: Any) {
        this.target = target
    }

    override fun save(value: Any) {
        NMS.INSTANCE.write(value, this)
    }

    override fun save() {
        if (!autoSave) return

        target?.let {
            save(it)
        }
    }

    companion object {
        @JvmStatic
        fun AbstractNBTFactory<String>.tryUse(func: NBTTagFactory.() -> Unit) {
            if (this is NBTTagFactory) {
                func()
            }
        }

        @JvmStatic
        fun AbstractNBTFactory<String>.withAutoSave() = this.apply {
            tryUse {
                autoSave = true
            }
        }

        @JvmStatic
        fun AbstractNBTFactory<String>.withNonAutoSave() = this.apply {
            tryUse {
                autoSave = false
            }
        }

        @JvmStatic
        fun AbstractNBTFactory<String>.bindTo(target: Any)  = this.apply {
            tryUse {
                this.bindTo(target)
            }
        }
    }
}