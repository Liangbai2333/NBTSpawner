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

import net.minecraft.server.v1_16_R3.*
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import site.liangbai.nbtspawner.api.nms.factory.AbstractNBTFactory
import site.liangbai.nbtspawner.api.nms.factory.NBTTagFactory
import site.liangbai.nbtspawner.api.nms.factory.NBTTagListFactory
import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.common.reflect.Reflex.Companion.invokeConstructor
import taboolib.common.reflect.Reflex.Companion.invokeMethod
import taboolib.module.nms.MinecraftVersion

class NMSImpl : NMS() {
    private val isUniversal = MinecraftVersion.isUniversal

    private val majorLegacy = MinecraftVersion.majorLegacy

    @Throws(IllegalStateException::class)
    override fun readEntity(entity: Entity): AbstractNBTFactory<String> {
        val craftEntity = entity as CraftEntity
        val nbt  = NBTTagCompound().also {
            if (majorLegacy >= 11200) {
                craftEntity.handle.save(it)
            } else if (majorLegacy in 10800..11200) {
                craftEntity.handle.invokeMethod<Any>("e", it)
            } else {
                throw IllegalStateException("unsupported minecraft version $majorLegacy")
            }
        }
        return findFactory(nbt)
    }

    @Throws(IllegalArgumentException::class)
    override fun readItemStack(itemStack: ItemStack): AbstractNBTFactory<String> {
        if (itemStack is CraftItemStack) {
            val nmsItemStack = itemStack.handle ?: throw IllegalArgumentException("could load NBT from empty item stack")

            return findFactory(nmsItemStack.tag ?: NBTTagCompound())
        } else {
            val nmsItemStack = CraftItemStack.asNMSCopy(itemStack)
            return findFactory(nmsItemStack.tag ?: NBTTagCompound())
        }
    }

    @Throws(IllegalStateException::class)
    override fun writeEntity(entity: Entity, nbtFactory: AbstractNBTFactory<String>) {
        val craftEntity = entity as CraftEntity

        craftEntity.handle.also {
            if (majorLegacy >= 11600) {
                it.load(nbtFactory.handle as NBTTagCompound)
            } else if (majorLegacy in 10800..11600) {
                it.invokeMethod<Any>("f", nbtFactory.handle)
            } else {
                throw IllegalStateException("unsupported minecraft version $majorLegacy")
            }
        }
    }

    override fun writeItemStack(itemStack: ItemStack, nbtFactory: AbstractNBTFactory<String>) {
        if (itemStack is CraftItemStack) {
            val nmsItemStack = itemStack.handle

            if (nmsItemStack != null) {
                nmsItemStack.tag = nbtFactory.handle as NBTTagCompound
            }
        } else {
            val nmsItemStack = CraftItemStack.asNMSCopy(itemStack)
            nmsItemStack.tag = nbtFactory.handle as NBTTagCompound
            val copiedItemStack = CraftItemStack.asBukkitCopy(nmsItemStack)
            itemStack.setItemMeta(copiedItemStack.itemMeta)
        }
    }

    @Throws(IllegalArgumentException::class)
    override fun write(target: Any, nbtFactory: AbstractNBTFactory<String>) {
        when (target) {
            is Entity -> writeEntity(target, nbtFactory)
            is ItemStack -> writeItemStack(target, nbtFactory)
            else -> throw IllegalArgumentException("could not save nbt data to ${target::class.java.simpleName}")
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalArgumentException::class)
    override fun <FAC : AbstractNBTFactory<*>> findFactory(obj: Any) = when (obj) {
        is NBTTagCompound -> NBTTagFactory(obj)
        is NBTTagList -> NBTTagListFactory(obj)
        else -> throw IllegalArgumentException("could not access the type ${obj::class.java.simpleName}")
    } as FAC

    @Suppress("UNCHECKED_CAST")
    override fun <DATA> unwrappedAsValue(target: Any): DATA? = when (target) {
        is NBTTagCompound, is NBTTagList -> findFactory(target)
        else -> try {
            target.getProperty<DATA>("data")
        } catch (e: Throwable) {
            target as DATA
        }
    }

    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    override fun wrappedAsNBTBase(value: Any): Any {
        return value.run { when (majorLegacy) {
            in 11500..99999 -> when (this) {
                is NBTBase -> return this
                is AbstractNBTFactory<*> -> this.handle
                is String -> NBTTagString.a(this)
                is Boolean -> NBTTagByte.a(this)
                is Byte -> NBTTagByte.a(this)
                is Int -> NBTTagInt.a(this)
                is Long -> NBTTagLong.a(this)
                is Short -> NBTTagShort.a(this)
                is Double -> NBTTagDouble.a(this)
                is Float -> NBTTagFloat.a(this)
                is ByteArray -> NBTTagByteArray(this)
                is IntArray -> NBTTagIntArray(this)
                is LongArray -> NBTTagLongArray(this)
                else -> throw IllegalArgumentException("could not wrapped the type: ${this::class.java.simpleName} as any nbt type.")
            }
            in 10800..11500 -> when (this) {
                is NBTBase -> return this
                is AbstractNBTFactory<*> -> this.handle
                is String -> NBTTagString::class.java.invokeConstructor(this)
                is Boolean -> NBTTagByte::class.java.invokeConstructor(this)
                is Byte -> NBTTagByte::class.java.invokeConstructor(this)
                is Int -> NBTTagInt::class.java.invokeConstructor(this)
                is Long -> NBTTagLong::class.java.invokeConstructor(this)
                is Short -> NBTTagShort::class.java.invokeConstructor(this)
                is Double -> NBTTagDouble::class.java.invokeConstructor(this)
                is Float -> NBTTagFloat::class.java.invokeConstructor(this)
                is ByteArray -> NBTTagByteArray::class.java.invokeConstructor(this)
                is IntArray -> NBTTagIntArray::class.java.invokeConstructor(this)
                is LongArray -> {
                    if (majorLegacy >= 11200) {
                        NBTTagLongArray::class.java.invokeConstructor(this)
                    } else throw IllegalStateException("unsupported minecraft version $majorLegacy for Long Array type.")
                }
                else -> throw IllegalArgumentException("could not wrapped the type: ${this::class.java.simpleName} as any nbt type.")
            }
            else -> throw IllegalStateException("unsupported minecraft version $majorLegacy")
        } }
    }
}