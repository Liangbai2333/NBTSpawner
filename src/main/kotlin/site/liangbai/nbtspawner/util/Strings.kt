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

fun String.findLast(parseChar: String = "/"): String {
    if (!contains(parseChar)) {
        return this
    }

    return split(parseChar).last()
}

fun String.cast(ref: Class<*>): Any {
    val type = ref.kotlin.javaPrimitiveType ?: return this

    return when (type) {
        String::class.java -> this
        Int::class.java -> toInt()
        Long::class.java -> toLong()
        Short::class.java -> toShort()
        Boolean::class.java -> toBoolean()
        Byte::class.java -> toByte()
        Float::class.java -> toFloat()
        Double::class.java -> toDouble()
        else -> this
    }
}