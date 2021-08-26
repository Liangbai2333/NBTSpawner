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
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld
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
        return findFactory(
            buildNBT {
                if (majorLegacy >= 11200) {
                    craftEntity.handle.save(this)
                } else if (majorLegacy in 10800..11200) {
                    craftEntity.handle.invokeMethod<Any>("e", this)
                } else {
                    throw IllegalStateException("unsupported minecraft version $majorLegacy")
                }
            }
        )
    }

    @Throws(IllegalArgumentException::class)
    override fun readItemStack(itemStack: ItemStack): AbstractNBTFactory<String> {
        return if (itemStack is CraftItemStack) {
            val nmsItemStack = itemStack.findHandle() ?: throw IllegalArgumentException("could load NBT from empty item stack")
            findFactory(
                buildNBT {
                    nmsItemStack.save(this)
                }
            )
        } else {
            val nmsItemStack = CraftItemStack.asNMSCopy(itemStack)
            findFactory(
                buildNBT {
                    nmsItemStack.save(this)
                }
            )
        }
    }

    @Throws(IllegalStateException::class)
    override fun readBlock(block: Block): AbstractNBTFactory<String>? {
        val tileEntity = block.asTileEntity() ?: return null

        return findFactory(
            buildNBT {
                if (majorLegacy >= 10900) {
                    tileEntity.save(this)
                } else if (majorLegacy in 10800..10900) {
                    tileEntity.invokeMethod<Any>("b", this)
                } else {
                    throw IllegalStateException("unsupported minecraft version $majorLegacy")
                }
            }
        )
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

    @Throws(IllegalStateException::class)
    override fun writeItemStack(itemStack: ItemStack, nbtFactory: AbstractNBTFactory<String>) {
        fun net.minecraft.server.v1_16_R3.ItemStack.loadOrCopy(nbt: NBTTagCompound): net.minecraft.server.v1_16_R3.ItemStack {
            if (majorLegacy >= 11600) {
                return this::class.java.invokeConstructor(nbt)
            } else if (majorLegacy in 11200..11600) {
                invokeMethod<Any>("load", nbt)
            } else if (majorLegacy in 10800..11200) {
                invokeMethod<Any>("c", nbt)
            } else {
                throw IllegalStateException("unsupported minecraft version $majorLegacy")
            }

            return this
        }

        if (itemStack is CraftItemStack) {
            val nmsItemStack = itemStack.findHandle()

            val loaded = nmsItemStack?.loadOrCopy(nbtFactory.handle as NBTTagCompound)

            if (loaded != nmsItemStack) {
                val copiedItemStack = CraftItemStack.asBukkitCopy(loaded)
                itemStack.itemMeta = copiedItemStack.itemMeta
            }
        } else {
            var nmsItemStack = CraftItemStack.asNMSCopy(itemStack)
            nmsItemStack = nmsItemStack.loadOrCopy(nbtFactory.handle as NBTTagCompound)
            val copiedItemStack = CraftItemStack.asBukkitCopy(nmsItemStack)
            itemStack.setItemMeta(copiedItemStack.itemMeta)
        }
    }

    @Throws(IllegalStateException::class)
    override fun writeBlock(block: Block, nbtFactory: AbstractNBTFactory<String>) {
        val tileEntity = block.asTileEntity() ?: return

        val nbt = nbtFactory.handle as NBTTagCompound

        tileEntity.also {
            if (majorLegacy >= 11600) {
                tileEntity.load(null, nbt)
            } else if (majorLegacy in 11300..11600) {
                tileEntity.invokeMethod<Any>("load", nbt)
            } else if (majorLegacy in 10800..11300) {
                tileEntity.invokeMethod<Any>("a", nbt)
            } else {
                throw IllegalStateException("unsupported minecraft version $majorLegacy")
            }
        }
    }

    @Throws(IllegalArgumentException::class)
    override fun write(target: Any, nbtFactory: AbstractNBTFactory<String>) {
        when (target) {
            is Entity -> writeEntity(target, nbtFactory)
            is ItemStack -> writeItemStack(target, nbtFactory)
            is Block -> writeBlock(target, nbtFactory)
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

    private fun buildNBT(builder: NBTTagCompound.() -> Unit) = NBTTagCompound().apply(builder)

    private fun emptyNBT() = NBTTagCompound()

    private fun Block.asTileEntity(): TileEntity? {
        val craftWorld = world as CraftWorld
        val nmsWorld = craftWorld.handle
        return nmsWorld.getTileEntity(BlockPosition(x, y, z))
    }

    private fun CraftItemStack.findHandle() = getProperty<net.minecraft.server.v1_16_R3.ItemStack>("handle")
}