/*
 * Village Defense - Protect villagers from hordes of zombies
 * Copyright (c) 2025  Plugily Projects - maintained by Tigerpanzer_02 and contributors
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

package plugily.projects.villagedefense.kits.terminator;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.kits.utils.KitSpecifications;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TerminatorAugmentsGui {

  private static final String AUGMENT_SELECT0R_TEMP = "VD_AUGMENT_SELECTOR_TEMP";
  private final Main plugin;

  public TerminatorAugmentsGui(Main plugin) {
    this.plugin = plugin;
  }

  public void openGui(Player player) {
    openGui(player, null);
  }

  private void openGui(Player player, List<TerminatorAugment> chosenAugments) {
    User user = plugin.getUserManager().getUser(player);
    List<TerminatorAugment> augments = chosenAugments == null ? doRollRandomAugments(user) : chosenAugments;
    int augmentsAmount = 0;
    if (player.hasMetadata(TerminatorAugment.AUGMENTS_COUNT_METADATA_KEY)) {
      augmentsAmount = player.getMetadata(TerminatorAugment.AUGMENTS_COUNT_METADATA_KEY).get(0).asInt();
    }
    String message = "&a&lCHOOSE AUGMENT &8(&7%amount%/&73 unlocked&8)".replace("%amount%", String.valueOf(augmentsAmount));
    ChestGui gui = new ChestGui(3, ChatColor.translateAlternateColorCodes('&', message));
    gui.setOnGlobalClick(e -> e.setCancelled(true));
    gui.setOnClose(e -> {
      if (player.hasMetadata(AUGMENT_SELECT0R_TEMP)) {
        player.removeMetadata(AUGMENT_SELECT0R_TEMP, plugin);
        new MessageBuilder("KIT_CONTENT_TERMINATOR_GAME_ITEM_TERMINUS_AUGMENT_GUI_AUGMENT_APPLIED").asKey().send(player);
        return;
      }
      new MessageBuilder("KIT_CONTENT_TERMINATOR_GAME_ITEM_TERMINUS_AUGMENT_GUI_CHOOSE_AN_AUGMENT").asKey().send(player);
      player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1f, 1f);
      Bukkit.getScheduler().runTaskLater(plugin, () -> openGui(player, augments), 5);
    });
    StaticPane pane = new StaticPane(0, 0, 9, 3);
    TerminatorAugment first = augments.get(0);
    pane.addItem(new GuiItem(
      new ItemBuilder(first.icon().clone())
        .name(first.name())
        .lore(first.lore())
        .lore("")
        .lore(org.bukkit.ChatColor.translateAlternateColorCodes('&', "&8(Click to upgrade)"))
        .build(),
      e -> doApplyAugment(user, first)
    ), 2, 1);
    TerminatorAugment second = augments.get(1);
    pane.addItem(new GuiItem(
      new ItemBuilder(second.icon().clone())
        .name(second.name())
        .lore(second.lore())
        .lore("")
        .lore(org.bukkit.ChatColor.translateAlternateColorCodes('&', "&8(Click to upgrade)"))
        .build(),
      e -> doApplyAugment(user, second)
    ), 4, 1);
    TerminatorAugment third = augments.get(2);
    pane.addItem(new GuiItem(
      new ItemBuilder(third.icon().clone())
        .name(third.name())
        .lore(third.lore())
        .lore("")
        .lore(org.bukkit.ChatColor.translateAlternateColorCodes('&', "&8(Click to upgrade)"))
        .build(),
      e -> doApplyAugment(user, third)
    ), 6, 1);
    gui.addPane(pane);
    gui.show(player);
  }

  private List<TerminatorAugment> doRollRandomAugments(User user) {
    List<TerminatorAugment> augments = TerminatorAugmentRegistry.getAugments()
      .stream()
      .filter(a -> a.appliesFrom() == KitSpecifications.getTimeState((Arena) user.getArena()) && a.synergyApplies().apply((Arena) user.getArena()))
      .filter(a -> !user.getPlayer().hasMetadata(a.getMetadataKey()))
      .collect(Collectors.toList());
    Collections.shuffle(augments);
    List<TerminatorAugment> rolledAugments = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      if (augments.size() <= i) {
        break;
      }
      rolledAugments.add(augments.get(i));
    }
    return rolledAugments;
  }

  private void doApplyAugment(User user, TerminatorAugment augment) {
    user.getPlayer().setMetadata(AUGMENT_SELECT0R_TEMP, new FixedMetadataValue(plugin, true));
    int totalAugments = 0;
    if (user.getPlayer().hasMetadata(TerminatorAugment.AUGMENTS_COUNT_METADATA_KEY)) {
      totalAugments = user.getPlayer().getMetadata(TerminatorAugment.AUGMENTS_COUNT_METADATA_KEY).get(0).asInt();
    }
    totalAugments++;
    user.getPlayer().setMetadata(TerminatorAugment.AUGMENTS_COUNT_METADATA_KEY, new FixedMetadataValue(plugin, totalAugments));
    augment.onApply().accept(user);
    user.getPlayer().closeInventory();
  }

}
