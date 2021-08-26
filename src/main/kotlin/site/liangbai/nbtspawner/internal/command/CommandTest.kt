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
import site.liangbai.nbtspawner.api.nms.factory.NBTTagFactory.Companion.bindTo
import site.liangbai.nbtspawner.api.nms.factory.NBTTagFactory.Companion.withAutoSave
import site.liangbai.nbtspawner.util.forEach
import site.liangbai.nbtspawner.util.readNBT
import site.liangbai.nbtspawner.util.writeNBT
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand

@CommandHeader("test")
internal object CommandTest {
    @CommandBody
    val main = mainCommand {
        execute<Player> { sender, context, argument ->
            val item = sender.inventory.itemInMainHand
            val nbtFactory = item.readNBT()
            nbtFactory.findAs<NBTTagFactory>("ForgeCaps")!!.findAs<NBTTagFactory>("Parent")!!["size"] = 30
            nbtFactory.findAs<NBTTagFactory>("tag")!!.findAs<NBTTagFactory>("magazine")!!["size"] = 30
            item.writeNBT(nbtFactory)
            sender.sendMessage(nbtFactory.toString())
            sender.sendMessage("")
            sender.sendMessage(item.readNBT().toString())
        }
    }
}