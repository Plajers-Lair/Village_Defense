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

package plugily.projects.villagedefense.arena.managers;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.minigamesbox.classic.utils.configuration.ConfigUtils;
import plugily.projects.minigamesbox.classic.utils.helper.ItemUtils;
import plugily.projects.minigamesbox.classic.utils.misc.complement.ComplementAccessor;
import plugily.projects.minigamesbox.classic.utils.serialization.LocationSerializer;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XSound;
import plugily.projects.minigamesbox.inventory.normal.NormalFastInv;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Created by Tom on 16/08/2014.
 */
public class ShopManager {

  public static final String SHOP_OFFER_METADATA = "VD_SHOP_SPECIAL_OFFER";

  private final String defaultOrbsName;
  private final String defaultWaveLockName = "wave_lock";
  private final String defaultGolemItemName;
  private final String defaultWolfItemName;
  private final String itemLockedMessage;
  private final String specialOfferMessage;

  private final Set<Integer> newOffersAtWaves = new HashSet<>();
  private final Main plugin;
  private final FileConfiguration config;
  private final Arena arena;
  private boolean shopValidated = false;

  public ShopManager(Arena arena) {
    plugin = arena.getPlugin();
    config = ConfigUtils.getConfig(plugin, "arenas");
    this.arena = arena;

    defaultOrbsName = new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_SHOP_CURRENCY").asKey().build();
    defaultGolemItemName = new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_SHOP_GOLEM_ITEM", false).asKey().build();
    defaultWolfItemName = new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_SHOP_WOLF_ITEM", false).asKey().build();
    itemLockedMessage = new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_SHOP_ITEM_LOCKED").asKey().build();
    specialOfferMessage = new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_SHOP_SPECIAL_OFFER").asKey().build();

    if(config.isSet("instances." + arena.getId() + ".shop")) {
      validateShop();
      if (shopValidated) {
        loadShopChestWaveAnnouncements();
      }
    }
  }

  /**
   * Default name of golem spawn item from language.yml
   *
   * @return the default golem item name
   */
  public String getDefaultGolemItemName() {
    return defaultGolemItemName;
  }

  /**
   * Default name of wolf spawn item from language.yml
   *
   * @return the default wolf item name
   */
  public String getDefaultWolfItemName() {
    return defaultWolfItemName;
  }

  public Set<Integer> getNewOffersAtWaves() {
    return newOffersAtWaves;
  }

  private void loadShopChestWaveAnnouncements() {
    ItemStack[] contents = ((Chest) LocationSerializer.getLocation(config.getString("instances." + arena.getId() + ".shop"))
      .getBlock().getState()).getInventory().getContents();
    for (ItemStack itemStack : contents) {
      if (itemStack == null || itemStack.getType() == Material.REDSTONE_BLOCK) {
        continue;
      }
      ItemMeta meta = itemStack.getItemMeta();
      //seek for wave limit
      if (meta != null && meta.hasLore()) {
        for (String line : ComplementAccessor.getComplement().getLore(meta)) {
          if (line.contains(defaultWaveLockName)) {
            newOffersAtWaves.add(parseNumberSafely(line));
          }
        }
      }
    }
  }

  public void openShop(Player player, Villager source) {
    User user = plugin.getUserManager().getUser(player);
    if (user == null || user.getArena() == null) {
      return;
    }
    if (!shopValidated) {
      new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_SHOP_NOT_DEFINED").asKey().player(player).sendPlayer();
      return;
    }
    boolean hasSpecialOffer = source.hasMetadata(SHOP_OFFER_METADATA);
    NormalFastInv gui = doGenerateShop(user, hasSpecialOffer);
    gui.open(player);
  }

  private NormalFastInv doGenerateShop(User user, boolean hasSpecialOffer) {
    ItemStack[] contents = ((Chest) LocationSerializer.getLocation(config.getString("instances." + arena.getId() + ".shop"))
      .getBlock().getState()).getInventory().getContents();
    NormalFastInv gui = new NormalFastInv(plugin.getBukkitHelper().serializeInt(contents.length), new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_SHOP_GUI").asKey().build());
    gui.getInventory().setMaxStackSize(plugin.getConfig().getInt("Limit.Wave.Game-End", 127));
    gui.addClickHandler(inventoryClickEvent -> inventoryClickEvent.setCancelled(true));
    for(int slot = 0; slot < contents.length; slot++) {
      ItemStack itemStack = contents[slot];
      if(itemStack == null || itemStack.getType() == Material.REDSTONE_BLOCK) {
        continue;
      }
      //we do not want to override our real chest contents
      itemStack = itemStack.clone();

      int orbsRequired = 0;
      int waveRequired = 0;

      ItemMeta meta = itemStack.getItemMeta();
      //seek for price or wave limit
      if(meta != null && meta.hasLore()) {
        for (String line : ComplementAccessor.getComplement().getLore(meta)) {
          if (line.contains(defaultOrbsName) || line.contains("orbs")) {
            orbsRequired = parseNumberSafely(line);
          } else if (line.contains(defaultWaveLockName)) {
            waveRequired = parseNumberSafely(line);
          }
        }
      }
      if (orbsRequired == -1 || waveRequired == -1) {
        plugin.getDebugger().debug(Level.WARNING, "Invalid or no price/wave unlock value set for shop item in arena {0} skipping item!", arena.getId());
        continue;
      }
      int currentWave = ((Arena) user.getArena()).getWave();
      if (hasSpecialOffer) {
        orbsRequired = (int) (orbsRequired - (orbsRequired * 0.1));
      }
      final int finalOrbs = orbsRequired;
      final int finalWaveLimit = waveRequired;
      if (meta != null && meta.hasLore()) {
        List<String> newLore = meta.getLore()
          .stream()
          .map(lore -> {
            if (lore.contains(defaultWaveLockName)) {
              lore = new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_SHOP_WAVE_LOCK").asKey().integer(finalWaveLimit).build();
            }
            if (lore.contains(defaultOrbsName) || lore.contains("orbs")) {
              lore = ChatColor.GOLD + "" + finalOrbs + " " + defaultOrbsName;
            }
            return lore;
          })
          .collect(Collectors.toList());
        if (hasSpecialOffer) {
          newLore.add(specialOfferMessage);
        }
        ComplementAccessor.getComplement().setLore(meta, newLore);
        if (currentWave < waveRequired) {
          itemStack.setType(XMaterial.BARRIER.parseMaterial());
          itemStack.setAmount(finalWaveLimit);
          meta.setDisplayName(itemLockedMessage);
        }
        itemStack.setItemMeta(meta);
      }

      final ItemStack finalStack = itemStack;
      gui.setItem(slot, itemStack, event -> {
        Player player = user.getPlayer();
        int orbs = user.getStatistic("ORBS");
        if (currentWave < finalWaveLimit) {
          new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_SHOP_WAVE_STILL_LOCKED").asKey().integer(finalWaveLimit).player(player).sendPlayer();
          XSound.ENTITY_VILLAGER_NO.play(player);
          return;
        }
        if (orbs < finalOrbs) {
          new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_SHOP_NOT_ENOUGH_CURRENCY").asKey().player(player).sendPlayer();
          XSound.ENTITY_VILLAGER_NO.play(player);
          return;
        }
        if (ItemUtils.isItemStackNamed(finalStack)) {
          String name = ComplementAccessor.getComplement().getDisplayName(finalStack.getItemMeta());
          if(name.contains(new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_SHOP_GOLEM_ITEM", false).asKey().build())
            || name.contains(defaultGolemItemName)) {
            if(!arena.canSpawnMobForPlayer(player, EntityType.IRON_GOLEM)) {
              XSound.ENTITY_VILLAGER_NO.play(player);
              return;
            }
            arena.spawnGolem(arena.getStartLocation(), player);
            adjustOrbs(user, finalOrbs);
            return;
          }
          if(name.contains(new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_SHOP_WOLF_ITEM", false).asKey().build())
            || name.contains(defaultWolfItemName)) {
            if(!arena.canSpawnMobForPlayer(player, EntityType.WOLF)) {
              XSound.ENTITY_VILLAGER_NO.play(player);
              return;
            }
            arena.spawnWolf(arena.getStartLocation(), player);
            adjustOrbs(user, finalOrbs);
            return;
          }
        }

        ItemStack stack = finalStack.clone();
        ItemMeta itemMeta = stack.getItemMeta();

        if (itemMeta != null && itemMeta.hasLore()) {
          List<String> updatedLore = ComplementAccessor.getComplement()
            .getLore(itemMeta)
            .stream()
            .filter(lore -> !lore.contains(new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_SHOP_CURRENCY").asKey().build()))
            .filter(lore -> !lore.contains(new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_SHOP_WAVE_LOCK").asKey().integer(finalWaveLimit).build()))
            .filter(lore -> !lore.contains(specialOfferMessage))
            .collect(Collectors.toList());
          ComplementAccessor.getComplement().setLore(itemMeta, updatedLore);
          stack.setItemMeta(itemMeta);
        }

        applyOrGiveItem(player, stack);
        adjustOrbs(user, finalOrbs);
      });
    }
    return gui;
  }

  private int parseNumberSafely(String text) {
    String stripped = ChatColor.stripColor(text).replaceAll("&[0-9a-zA-Z]", "").replaceAll("[^0-9]", "");
    try {
      return Integer.parseInt(stripped);
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  private void applyOrGiveItem(Player player, ItemStack itemStack) {
    Material type = itemStack.getType();
    if (type == Material.LEATHER_HELMET || type == Material.CHAINMAIL_HELMET || type == Material.IRON_HELMET || type == Material.GOLDEN_HELMET
      || type == Material.DIAMOND_HELMET || type == Material.NETHERITE_HELMET || type == Material.TURTLE_HELMET) {
      player.getInventory().setHelmet(itemStack);
      new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_SHOP_AUTO_ARMOR_EQUIPPED").asKey().player(player).sendPlayer();
      return;
    }
    if (type == Material.LEATHER_CHESTPLATE || type == Material.CHAINMAIL_CHESTPLATE || type == Material.IRON_CHESTPLATE || type == Material.GOLDEN_CHESTPLATE
      || type == Material.DIAMOND_CHESTPLATE || type == Material.NETHERITE_CHESTPLATE) {
      player.getInventory().setChestplate(itemStack);
      new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_SHOP_AUTO_ARMOR_EQUIPPED").asKey().player(player).sendPlayer();
      return;
    }
    if (type == Material.LEATHER_LEGGINGS || type == Material.CHAINMAIL_LEGGINGS || type == Material.IRON_LEGGINGS || type == Material.GOLDEN_LEGGINGS
      || type == Material.DIAMOND_LEGGINGS || type == Material.NETHERITE_LEGGINGS) {
      player.getInventory().setLeggings(itemStack);
      new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_SHOP_AUTO_ARMOR_EQUIPPED").asKey().player(player).sendPlayer();
      return;
    }
    if (type == Material.LEATHER_BOOTS || type == Material.CHAINMAIL_BOOTS || type == Material.IRON_BOOTS || type == Material.GOLDEN_BOOTS
      || type == Material.DIAMOND_BOOTS || type == Material.NETHERITE_BOOTS) {
      player.getInventory().setBoots(itemStack);
      new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_SHOP_AUTO_ARMOR_EQUIPPED").asKey().player(player).sendPlayer();
      return;
    }
    player.getInventory().addItem(itemStack);
  }

  private void adjustOrbs(User user, int cost) {
    user.adjustStatistic("ORBS", -cost);
    arena.changeArenaOptionBy("TOTAL_ORBS_SPENT", cost);
    XSound.ENTITY_VILLAGER_YES.play(user.getPlayer());
  }

  private void validateShop() {
    String shop = config.getString("instances." + arena.getId() + ".shop", "");
    if(!shop.contains(",")) {
      plugin.getDebugger().debug(Level.WARNING, "There is no shop for arena {0}! Aborting registering shop!", arena.getId());
      this.shopValidated = false;
      return;
    }
    Location location = LocationSerializer.getLocation(shop);
    if(location.getWorld() == null || !(location.getBlock().getState() instanceof Chest)) {
      plugin.getDebugger().debug(Level.WARNING, "Shop failed to load, invalid location for location {0}", LocationSerializer.locationToString(location));
      this.shopValidated = false;
      return;
    }
    this.shopValidated = true;
  }

}
