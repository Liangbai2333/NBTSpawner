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
import site.liangbai.nbtspawner.api.nms.factory.NBTTagFactory
import site.liangbai.nbtspawner.api.nms.factory.NBTTagListFactory
import site.liangbai.nbtspawner.util.forEach
import site.liangbai.nbtspawner.util.readNBT
import taboolib.common.platform.command.*
import taboolib.module.chat.TellrawJson
import taboolib.module.chat.colored
import taboolib.platform.util.sendBook
import java.util.*

@CommandHeader("nbtspawner", aliases = ["nbt", "ns"], permission = "nbtspawner.admin", permissionDefault = PermissionDefault.OP)
internal object Command {
    @CommandBody
    val me = subCommand {
        execute<Player> { sender, context, argument ->
            val factory = sender.readNBT()

            sender.sendBook {
                write(
                    """
                        
                        
                        
                        &c&l       这里是个人NBT编辑界面
                        &b&l             请翻页
                        
                        
                        
                    """.trimIndent().colored()
                )

                val keys = factory.keys()

                loop@ for (i in keys.indices step 10) {
                    val message = TellrawJson()

                    for (j in i until i + 10) {
                        if (j >= keys.size) break@loop
                        message.also {
                            val key = keys[j]
                            val value = factory[key]!!
                            it.append(generateNBTDataInfo(key, value)).newLine()
                        }
                    }

                    write(message)
                }
            }
        }
    }

    @CommandBody
    val item = subCommand {

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

    }

    private fun generateNBTDataInfo(name: String, value: Any, parent: String? = null): TellrawJson {
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
    }
}