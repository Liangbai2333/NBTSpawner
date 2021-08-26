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

import org.bukkit.entity.Player
import site.liangbai.nbtspawner.api.nms.factory.AbstractNBTFactory
import site.liangbai.nbtspawner.api.nms.factory.NBTTagFactory
import site.liangbai.nbtspawner.api.nms.factory.NBTTagListFactory
import site.liangbai.nbtspawner.util.readNBT
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
            execute<Player> { sender, _, argument ->
                val factory = sender.readNBT()

                sender.sendBook(generateNBTInfo(factory, message, argument))
            }
        }

        execute<Player> { sender, _, _ ->
            val factory = sender.readNBT()

            sender.sendBook(generateNBTInfo(factory, message))
        }
    }

    @CommandBody
    val item = subCommand {
        val message = """
                        
                        
                        
                        &c&l这里是手中物品NBT编辑界面
                        
                        
                        &b&l        请翻页
                        
                        
                        
                    """.trimIndent().colored()

        dynamic(optional = true) {
            execute<Player> { sender, _, argument ->
                val factory = sender.inventory.itemInMainHand.readNBT()

                sender.sendBook(generateNBTInfo(factory, message, argument))
            }
        }

        execute<Player> { sender, _, _ ->
            val factory = sender.inventory.itemInMainHand.readNBT()

            sender.sendBook(generateNBTInfo(factory, message))
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

    @CommandBody
    val internal = subCommand {
        literal("block") {

        }
    }

    private fun <USAGE : Any> generateNBTInfo(fac: AbstractNBTFactory<USAGE>, introduce: String, parser: String? = null, runCommand: String? = null) = buildBook {
        write(
            introduce
        )

        var factory = fac

        if (parser != null) {
            val paths = parser.split("/").filter { it.isNotEmpty() }

            for (path in paths) {
                when (factory) {
                    is NBTTagListFactory -> factory = factory.findAs<AbstractNBTFactory<USAGE>>(path.toInt())!!
                    is NBTTagFactory -> factory = factory.findAs<AbstractNBTFactory<USAGE>>(path)!!
                }
            }
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
        return TellrawJson()
            .append("&c[&6$name&c]: &c${
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
}