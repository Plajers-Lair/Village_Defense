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

package plugily.projects.villagedefense.arena.managers.shop;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XSound;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Tom on 16/08/2014.
 */
public class ShopManager {

  public static final String SHOP_OFFER_METADATA = "VD_SHOP_SPECIAL_OFFER";

  private final String defaultOrbsName;
  private final String defaultWaveLockName = "wave_lock";
  private final String itemLockedMessage;
  private final String specialOfferMessage;

  private final @Getter Set<Integer> newOffersAtWaves = new HashSet<>();
  private final Main plugin;
  private final Arena arena;

  public ShopManager(Arena arena) {
    plugin = arena.getPlugin();
    this.arena = arena;

    defaultOrbsName = new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_SHOP_CURRENCY").asKey().build();
    itemLockedMessage = new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_SHOP_ITEM_LOCKED").asKey().build();
    specialOfferMessage = new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_SHOP_SPECIAL_OFFER").asKey().build();
  }

  public void openShop(Player player, Villager source) {
    User user = plugin.getUserManager().getUser(player);
    if (user == null || user.getArena() == null) {
      return;
    }
    boolean hasSpecialOffer = source.hasMetadata(ShopManager.SHOP_OFFER_METADATA);
    openShop(user, hasSpecialOffer);
  }

  private void openShop(User user, boolean hasPromo) {
    Player player = user.getPlayer();
    StaticPane pane = new StaticPane(0, 0, 9, 5);
    Set<String> ownedMetadatas = new HashSet<>();
    for (ItemStack itemStack : player.getInventory()) {
      if (itemStack == null || !itemStack.hasItemMeta()) {
        continue;
      }
      PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
      if (container.isEmpty()) {
        continue;
      }
      for (ShopItem item : plugin.getArenaShopRegistry().getShopItems()) {
        if (container.has(new NamespacedKey(plugin, item.id()))) {
          ownedMetadatas.add(item.id());
        }
      }
    }
    for (ShopItem item : plugin.getArenaShopRegistry().getShopItems()) {
      boolean owned = ownedMetadatas.contains(item.id());
      if (owned && !item.finalTier() && !item.requiresMetadata()) {
        continue;
      }
      if (item.requiresId() != null && !ownedMetadatas.contains(item.requiresId()) && !owned) {
        continue;
      }
      pane.addItem(new GuiItem(renderItem(item, owned), e -> {
        e.setCancelled(true);
        int orbs = user.getStatistic("ORBS");
        if (owned) {
          XSound.ENTITY_VILLAGER_NO.play(player);
          return;
        }
        if (orbs < item.cost()) {
          new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_SHOP_NOT_ENOUGH_CURRENCY").asKey().player(player).sendPlayer();
          XSound.ENTITY_VILLAGER_NO.play(player);
          return;
        }

        applyOrGiveItem(player, item);
        adjustOrbs(user, item.cost());
        openShop(user, hasPromo);
      }), item.xPos(), item.yPos());
    }
    ChestGui gui = new ChestGui(5, new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_SHOP_GUI").asKey().build());
    gui.addPane(pane);
    gui.show(player);
  }

  private ItemStack renderItem(ShopItem item, boolean owned) {
    plugily.projects.villagedefense.utils.ItemBuilder builder = new plugily.projects.villagedefense.utils.ItemBuilder(item.item().itemStack())
      .addFlag(ItemFlag.HIDE_ATTRIBUTES);
    if (item.requiresId() == null && !owned) {
      builder = builder.appendLore(Arrays.asList("", "§7Purchase this item", "§7for §e§l" + item.cost() + " ORBS", ""));
    } else {
      builder = builder.appendLore(Arrays.asList("", "§7Upgrade item to this tier", "§7for §e§l" + item.cost() + " ORBS", ""));
    }
    if (owned) {
      builder = builder.appendLore(Arrays.asList("", "§c(You already own this item)"));
    }
    return builder.build();
  }

  private void applyOrGiveItem(Player player, ShopItem item) {
    ShopItem.PurchasableItem.Position position = item.item().position();
    ItemStack itemStack = item.item().itemStack();
    if (position == ShopItem.PurchasableItem.Position.INVENTORY) {
      if (item.requiresMetadata() && item.requiresId() != null) {
        replaceItem(player, item);
        return;
      }
      player.getInventory().addItem(itemStack);
    }
    if (position == ShopItem.PurchasableItem.Position.ARMOR_HELMET) {
      player.getInventory().setHelmet(item.item().itemStack());
    } else if (position == ShopItem.PurchasableItem.Position.ARMOR_CHESTPLATE) {
      player.getInventory().setChestplate(item.item().itemStack());
    } else if (position == ShopItem.PurchasableItem.Position.ARMOR_LEGGINGS) {
      player.getInventory().setLeggings(item.item().itemStack());
    } else if (position == ShopItem.PurchasableItem.Position.ARMOR_BOOTS) {
      player.getInventory().setBoots(item.item().itemStack());
    }
  }

  private void replaceItem(Player player, ShopItem item) {
    ItemStack itemStack = item.item().itemStack();
    List<ItemStack> items = Arrays.stream(player.getInventory().getContents())
      .filter(stack -> {
        if (stack == null || !stack.hasItemMeta()) {
          return false;
        }
        PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();
        return container.has(new NamespacedKey(plugin, item.requiresId()));
      }).collect(Collectors.toList());
    if (items.isEmpty()) {
      player.getInventory().addItem(itemStack);
      return;
    }
    ItemStack toReplace = items.get(0);
    player.getInventory().remove(toReplace);
    player.getInventory().addItem(itemStack);
  }

  private void adjustOrbs(User user, int cost) {
    user.adjustStatistic("ORBS", -cost);
    arena.changeArenaOptionBy("TOTAL_ORBS_SPENT", cost);
    XSound.ENTITY_VILLAGER_YES.play(user.getPlayer());
  }

}
