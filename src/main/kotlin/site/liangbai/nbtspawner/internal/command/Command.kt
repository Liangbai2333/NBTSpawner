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

package site.liangbai.nbtspawner.internal.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import site.liangbai.nbtspawner.api.nbt.factory.AbstractNBTFactory
import site.liangbai.nbtspawner.api.nbt.factory.NBTTagFactory
import site.liangbai.nbtspawner.api.nbt.factory.NBTTagListFactory
import site.liangbai.nbtspawner.internal.command.Command.commandSet
import site.liangbai.nbtspawner.util.*
import taboolib.common.platform.command.*
import taboolib.module.chat.TellrawJson
import taboolib.module.chat.colored
import taboolib.platform.util.buildBook
import taboolib.platform.util.sendBook

@CommandHeader("nbtspawner", aliases = ["nbt", "ns"], permission = "nbtspawner.admin", permissionDefault = PermissionDefault.OP)
internal object Command {
    @CommandBody
    val me = subCommand {
        val message = """
                        
                        
                        
                        &c&l 这里是个人NBT编辑界面
                        
                        
                        &b&l        请翻页
                        
                        
                        
                    """.trimIndent().colored()

        dynamic(optional = true) {
            generateSet<Player> { path, argument ->
                commandSet(readNBT(), this, path, argument)
            }

            execute<Player> { sender, _, argument ->
                val factory = sender.readNBT()

                sender.sendBook(generateNBTInfoBook(factory, message, argument))
            }
        }

        execute<Player> { sender, _, _ ->
            val factory = sender.readNBT()

            sender.sendBook(generateNBTInfoBook(factory, message))
        }
    }

    @CommandBody
    val item = subCommand {
        val message = """
                        
                        
                        
                        &c&l这里是手中物品NBT编辑界面
                        
                        
                        &b&l        请翻页
                        
                        
                        
                    """.trimIndent().colored()

        dynamic(optional = true) {
            generateSet<Player> { path, argument ->
                val item = inventory.itemInMainHand
                commandSet(item.readNBT(), item, path, argument)
            }

            execute<Player> { sender, _, argument ->
                val factory = sender.inventory.itemInMainHand.readNBT()

                sender.sendBook(generateNBTInfoBook(factory, message, argument))
            }
        }

        execute<Player> { sender, _, _ ->
            val factory = sender.inventory.itemInMainHand.readNBT()

            sender.sendBook(generateNBTInfoBook(factory, message))
        }
    }

    @CommandBody
    val target = subCommand {

    }

    @CommandBody
    val block = subCommand {

    }

    @CommandBody
    val targetBlock = subCommand {

    }

    @CommandBody
    val player = subCommand {
        dynamic {
            execute<Player> { sender, context, argument ->

            }
        }
    }

    private inline fun <reified T> CommandBuilder.CommandComponent.generateSet(crossinline func: T.(String, String) -> Unit) = literal("set", optional = true) {
        dynamic {
            execute<T> { sender, context, argument ->
                sender.func(context.argument(-2)!!, argument)
            }
        }
    }

    private fun CommandSender.commandSet(factory: AbstractNBTFactory<String>, target: Any, path: String, argument: String) {
        val subFactory = findSubFactory(factory, path, offset = 1, checkFirst = true)
        val attr = path.findLast()
        val type = subFactory[attr]?.javaClass ?: String::class.java
        subFactory[attr] = argument.cast(type)
        factory.write(target)
        sendMessage("&a成功将 &e$path &a设定为: &c$argument".colored())
    }

    private fun <USAGE : Any> findSubFactory(mainFactory: AbstractNBTFactory<USAGE>, parser: String, offset: Int = 0, checkFirst: Boolean = false, parseChar: String = "/"): AbstractNBTFactory<USAGE> {
        var factory = mainFactory

        if (!parser.contains(parseChar) && checkFirst) {
            return factory
        }

        var paths = parser.split(parseChar).filter { it.isNotEmpty() }

        paths = paths.subList(0, paths.size - offset)

        for (path in paths) {
            when (factory) {
                is NBTTagListFactory -> factory = factory.findAs<AbstractNBTFactory<USAGE>>(path.toInt())!!
                is NBTTagFactory -> factory = factory.findAs<AbstractNBTFactory<USAGE>>(path)!!
            }
        }

        return factory
    }

    private fun <USAGE : Any> generateNBTInfoBook(fac: AbstractNBTFactory<USAGE>, introduce: String, parser: String? = null, runCommand: String? = null) = buildBook {
        write(
            introduce
        )

        var factory = fac

        if (parser != null) {
            factory = findSubFactory(factory, parser)
        }
        val keys = factory.keys()

        loop@ for (i in keys.indices step 7) {
            val message = TellrawJson()

            for (j in i until i + 7) {
                if (j >= keys.size) {
                    write(message)
                    break@loop
                }
                message.also {
                    val key = keys[j]
                    val value = factory[key]!!
                    it.append(generateNBTDataInfo(key.toString(), value, runCommand)).newLine()
                }
            }

            write(message)
        }
    }

    private fun generateNBTDataInfo(name: String, value: Any, runCommand: String? = null): TellrawJson {
        val type = value.javaClass.simpleName.first().uppercase()

        return TellrawJson()
            .append("&${type.lowercase()[0].withColorToken()}[$type] &c[&6$name&c]: &c${
                when (value) {
                    is NBTTagFactory, is NBTTagListFactory -> "&d鼠标放置后显示"
                    is ByteArray -> value.contentToString()
                    is LongArray -> value.contentToString()
                    is IntArray -> value.contentToString()
                    is Array<*> -> value.contentToString()
                    else -> value
                }
            }".colored())
            .hoverText(value.toString())
            .also {
                if (runCommand != null) it.runCommand(runCommand
                    .replace("{key}", name)
                )
            }
    }

    private fun Char.withColorToken() = when (this) {
        'i' -> 'a'
        'f' -> '1'
        'l' -> '6'
        's' -> '2'
        'n', '[', ']' -> '4'
        else -> this
    }
}