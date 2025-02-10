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

package plugily.projects.villagedefense.kits;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import plugily.projects.minigamesbox.classic.api.event.player.PlugilyPlayerChooseKitEvent;
import plugily.projects.minigamesbox.classic.arena.PluginArena;
import plugily.projects.minigamesbox.classic.handlers.items.SpecialItem;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.kits.basekits.Kit;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.classic.utils.helper.ItemUtils;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.minigamesbox.classic.utils.version.events.api.PlugilyPlayerInteractEvent;
import plugily.projects.minigamesbox.inventory.common.item.SimpleClickableItem;
import plugily.projects.minigamesbox.inventory.normal.NormalFastInv;
import plugily.projects.villagedefense.Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitsMenu implements Listener {

  private final Main plugin;
  private final Map<KitCategory, List<Kit>> registeredKits = new HashMap<>();

  public KitsMenu(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  public void registerKit(KitCategory category, Kit kit) {
    registeredKits.computeIfAbsent(category, v -> new ArrayList<>()).add(kit);
  }

  public void openMenu(Player player) {
    NormalFastInv gui = new NormalFastInv(44, new MessageBuilder("KIT_KIT_MENU_TITLE").asKey().build());
    gui.setBorderItem(new SimpleClickableItem(new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE).name(" ").build(), e -> e.setCancelled(true)));
    for (Map.Entry<KitCategory, List<Kit>> entry : registeredKits.entrySet()) {
      int startSlot = entry.getKey().getStartSlot();
      for (int i = 0; i < 6; i++) {
        gui.setItem(startSlot + i, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build(), e -> e.setCancelled(true));
      }
      gui.setItem(startSlot - 1,
        new ItemBuilder(entry.getKey().getIcon())
          .name(plugin.getLanguageManager().getLanguageMessage(entry.getKey().getLanguageAccessor() + ".Name"))
          .lore(plugin.getLanguageManager().getLanguageList(entry.getKey().getLanguageAccessor() + ".Lore"))
          .build(),
        e -> e.setCancelled(true)
      );
      for (Kit kit : entry.getValue()) {
        gui.setItem(startSlot, new SimpleClickableItem(new ItemBuilder(kit.getItemStack()).flags(ItemFlag.HIDE_ATTRIBUTES).build(), event -> {
          event.setCancelled(true);
          if (!(event.isLeftClick() || event.isRightClick()) || !(event.getWhoClicked() instanceof Player) || !ItemUtils.isItemStackNamed(event.getCurrentItem())) {
            return;
          }
          PluginArena arena = plugin.getArenaRegistry().getArena(player);
          if (arena == null) {
            return;
          }
          PlugilyPlayerChooseKitEvent chooseKitEvent = new PlugilyPlayerChooseKitEvent(player, kit, arena);
          Bukkit.getPluginManager().callEvent(chooseKitEvent);
          if (chooseKitEvent.isCancelled()) {
            return;
          }
          plugin.getUserManager().getUser(player).setKit(kit);
          new MessageBuilder("KIT_CHOOSE").asKey().value(kit.getName()).player(player).sendPlayer();
        }));
        startSlot++;
      }
    }
    gui.refresh();
    gui.open(player);
  }

  //copy from core
  @EventHandler
  public void onSpecialItem(PlugilyPlayerInteractEvent event) {
    if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.PHYSICAL) {
      return;
    }
    Player player = event.getPlayer();

    ItemStack itemStack = VersionUtils.getItemInHand(player);
    if (!ItemUtils.isItemStackNamed(itemStack)) {
      return;
    }

    SpecialItem relatedSpecialItem = plugin.getSpecialItemManager().getRelatedSpecialItem(itemStack);
    if (relatedSpecialItem == plugin.getSpecialItemManager().getInvalidItem()) {
      return;
    }
    event.setCancelled(true);
    if (relatedSpecialItem.getPermission() != null && !relatedSpecialItem.getPermission().isEmpty()) {
      if (!plugin.getBukkitHelper().hasPermission(player, relatedSpecialItem.getPermission())) {
        return;
      }
    }

    if (plugin.getSpecialItemManager().getSpecialItem("KIT_SELECTOR_MENU").getPath().equals(relatedSpecialItem.getPath())) {
      openMenu(player);
    }
  }

  @AllArgsConstructor
  @Getter
  public enum KitCategory {
    DAMAGE_DEALER(Material.CHERRY_SIGN, 11, "Gold-Messages.Kit-Selector.Damage-Section"),
    SUPPORT(Material.BAMBOO_SIGN, 20, "Gold-Messages.Kit-Selector.Support-Section"),
    TANK(Material.WARPED_SIGN, 29, "Gold-Messages.Kit-Selector.Tank-Section");

    private final Material icon;
    private final int startSlot;
    private final String languageAccessor;
  }

}
