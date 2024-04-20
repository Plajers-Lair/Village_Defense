/*
 * Village Defense - Protect villagers from hordes of zombies
 * Copyright (c) 2024  Plugily Projects - maintained by Tigerpanzer_02 and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package plugily.projects.villagedefense.commands.arguments.admin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plugily.projects.minigamesbox.classic.commands.arguments.data.CommandArgument;
import plugily.projects.minigamesbox.classic.commands.arguments.data.LabelData;
import plugily.projects.minigamesbox.classic.commands.arguments.data.LabeledCommandArgument;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.classic.utils.misc.complement.ComplementAccessor;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.minigamesbox.number.NumberUtils;
import plugily.projects.villagedefense.commands.arguments.ArgumentsRegistry;

import java.util.List;

/**
 * @author Plajer
 * <p>
 * Created at 24.11.2018
 */
public class SetWaveUnlockArgument {

  public SetWaveUnlockArgument(ArgumentsRegistry registry) {
    registry.mapArgument("villagedefenseadmin", new LabeledCommandArgument("setwaveunlock", "villagedefense.admin.setwaveunlock", CommandArgument.ExecutorType.PLAYER,
      new LabelData("/vda setwaveunlock &6<number>", "/vda setwaveunlock <number>",
        "&7Set wave at which item is purchasable in the shop\n&6Permission: &7villagedefense.admin.setwaveunlock")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        if(args.length == 1 || !NumberUtils.isInteger(args[1])) {
          new MessageBuilder(ChatColor.RED + "Please type wave number!").prefix().send(sender);
          return;
        }

        Player player = (Player) sender;
        ItemStack item = VersionUtils.getItemInHand(player);
        if(item == null || item.getType() == Material.AIR) {
          new MessageBuilder("COMMANDS_HOLD_ANY_ITEM").asKey().player(player).sendPlayer();
          return;
        }

        ItemMeta meta = item.getItemMeta();
        if(meta == null || !meta.hasLore()) {
          VersionUtils.setItemInHand(player, new ItemBuilder(item)
            .lore(Integer.parseInt(args[1]) + " wave_lock").build());
          new MessageBuilder("COMMANDS_COMMAND_EXECUTED").asKey().player(player).sendPlayer();
          return;
        }

        //check any price from lore
        List<String> lore = ComplementAccessor.getComplement().getLore(meta);
        for(String search : lore) {
          if (search.endsWith("wave_lock")) {
            lore.remove(search);
            break;
          }
        }
        lore.add(0, Integer.parseInt(args[1]) + " wave_lock");
        ComplementAccessor.getComplement().setLore(meta, lore);
        item.setItemMeta(meta);
        new MessageBuilder("COMMANDS_COMMAND_EXECUTED").asKey().player(player).sendPlayer();
      }
    });
  }

}
