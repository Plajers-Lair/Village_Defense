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

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.kits.CleanerKit;
import plugily.projects.villagedefense.kits.CrusaderKit;
import plugily.projects.villagedefense.kits.MedicKit;
import plugily.projects.villagedefense.kits.utils.KitSpecifications;

import java.util.ArrayList;
import java.util.List;

public class TerminatorAugmentRegistry {

  private static final String LANGUAGE_ACCESSOR = "KIT_CONTENT_TERMINATOR_";
  private final static @Getter List<TerminatorAugment> augments = new ArrayList<>();
  private static Main plugin;

  public static void init(Main plugin) {
    TerminatorAugmentRegistry.plugin = plugin;
    registerAugments();
  }

  private static void registerAugments() {
    augments.add(new TerminatorAugment(
      "SHARPNESS",
      new ItemBuilder(XMaterial.IRON_SWORD.parseItem()).build(),
      ChatColor.translateAlternateColorCodes('&', "&6&lBLADE AUGMENT: &e&lSHARPNESS"),
      colorLore("&6&lBLADE OF TERMINUS&7 receives", "&7Sharpness I enchantment"),
      TerminatorAugment.AugmentType.BLADE,
      KitSpecifications.GameTimeState.EARLY,
      TerminatorAugment.CONSTANT_SYNERGY,
      user -> {
        user.getPlayer().setMetadata(TerminatorAugment.METADATA_KEY + "SHARPNESS", new FixedMetadataValue(plugin, true));
        for (ItemStack itemStack : user.getPlayer().getInventory()) {
          if (itemStack == null || !itemStack.hasItemMeta()) {
            continue;
          }
          if (!itemStack.getItemMeta().getDisplayName().equals(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_TERMINUS_NAME").asKey().build())) {
            continue;
          }
          ItemMeta meta = itemStack.getItemMeta();
          meta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
          itemStack.setItemMeta(meta);
        }
      }
    ));
    augments.add(new TerminatorAugment(
      "VITALITY",
      new ItemBuilder(XMaterial.LEATHER_BOOTS.parseItem()).build(),
      ChatColor.translateAlternateColorCodes('&', "&c&lSYSTEM AUGMENT: &e&lVITALITY"),
      colorLore("&7Earn brief burst of speed", "&7every 5/4/3 zombies killed"),
      TerminatorAugment.AugmentType.SYSTEM,
      KitSpecifications.GameTimeState.EARLY,
      TerminatorAugment.CONSTANT_SYNERGY,
      user -> user.getPlayer().setMetadata(TerminatorAugment.METADATA_KEY + "VITALITY", new FixedMetadataValue(plugin, 0))
    ));
    augments.add(new TerminatorAugment(
      "ADAPTIVE_SHIELDS",
      new ItemBuilder(XMaterial.TNT_MINECART.parseItem()).build(),
      ChatColor.translateAlternateColorCodes('&', "&c&lSYSTEM AUGMENT: &e&lADAPTIVE SHIELDS"),
      colorLore("&7Receive permanent 25/30/35% blast resistance"),
      TerminatorAugment.AugmentType.SYSTEM,
      KitSpecifications.GameTimeState.EARLY,
      TerminatorAugment.CONSTANT_SYNERGY,
      user -> user.getPlayer().setMetadata(TerminatorAugment.METADATA_KEY + "ADAPTIVE_SHIELDS", new FixedMetadataValue(plugin, true))
    ));
    augments.add(new TerminatorAugment(
      "STEADY_SCALING",
      new ItemBuilder(XMaterial.GOLDEN_SWORD.parseItem()).build(),
      ChatColor.translateAlternateColorCodes('&', "&6&lBLADE AUGMENT: &e&lSTEADY SCALING"),
      colorLore("&7Increase &6&lBLADE OF TERMINUS&7 damage", "&7 by 1% every 50 enemies killed", "&7capped at 30%"),
      TerminatorAugment.AugmentType.BLADE,
      KitSpecifications.GameTimeState.EARLY,
      TerminatorAugment.CONSTANT_SYNERGY,
      user -> {
        user.getPlayer().setMetadata(TerminatorAugment.METADATA_KEY + "STEADY_SCALING", new FixedMetadataValue(plugin, 0));
        user.getPlayer().setMetadata(TerminatorAugment.METADATA_KEY + "STEADY_SCALING_COUNT", new FixedMetadataValue(plugin, 0));
      }
    ));
    augments.add(new TerminatorAugment(
      "EXPANDED_AID",
      new ItemBuilder(XMaterial.HONEY_BOTTLE.parseItem()).build(),
      ChatColor.translateAlternateColorCodes('&', "&b&lSYNERGY AUGMENT: &e&lEXPANDED AID"),
      colorLore("&7Redirect healing effects from", "&e&lMEDIC&7 additionally to 1/2/3 nearby", "&7wounded allies"),
      TerminatorAugment.AugmentType.BLADE,
      KitSpecifications.GameTimeState.EARLY,
      (arena) -> arena.getPlayers()
        .stream()
        .anyMatch(player -> plugin.getUserManager().getUser(player).getKit() instanceof MedicKit),
      user -> {
        user.getPlayer().setMetadata(TerminatorAugment.METADATA_KEY + "EXPANDED_AID", new FixedMetadataValue(plugin, true));
      }
    ));

    augments.add(new TerminatorAugment(
      "DECAPITATION",
      new ItemBuilder(XMaterial.ZOMBIE_HEAD.parseItem()).build(),
      ChatColor.translateAlternateColorCodes('&', "&6&lBLADE AUGMENT: &e&lDECAPITATION"),
      colorLore("&7Have -/5/8% chance of enemy dropping", "&7their head on death, picking", "&7the head reduces all ability", "&7cooldowns by 10%"),
      TerminatorAugment.AugmentType.BLADE,
      KitSpecifications.GameTimeState.MID,
      TerminatorAugment.CONSTANT_SYNERGY,
      user -> {
        user.getPlayer().setMetadata(TerminatorAugment.METADATA_KEY + "DECAPITATION", new FixedMetadataValue(plugin, true));
      }
    ));
    augments.add(new TerminatorAugment(
      "SAFETY_PROTOCOLS",
      new ItemBuilder(XMaterial.IRON_DOOR.parseItem()).build(),
      ChatColor.translateAlternateColorCodes('&', "&c&lSYSTEM AUGMENT: &e&lSAFETY PROTOCOLS DISABLED"),
      colorLore("&7Receive -/20/25% increased damage", "&7while being low on health"),
      TerminatorAugment.AugmentType.SYSTEM,
      KitSpecifications.GameTimeState.MID,
      TerminatorAugment.CONSTANT_SYNERGY,
      user -> {
        user.getPlayer().setMetadata(TerminatorAugment.METADATA_KEY + "SAFETY_PROTOCOLS", new FixedMetadataValue(plugin, true));
      }
    ));
    augments.add(new TerminatorAugment(
      "REINFORCED_LEARNING",
      new ItemBuilder(XMaterial.PLAYER_HEAD.parseItem()).build(),
      ChatColor.translateAlternateColorCodes('&', "&c&lSYSTEM AUGMENT: &e&lREINFORCED LEARNING"),
      colorLore("&7Earn permanent +2% &8(&730% cap&8) damage", "&7increase every time you die"),
      TerminatorAugment.AugmentType.SYSTEM,
      KitSpecifications.GameTimeState.MID,
      TerminatorAugment.CONSTANT_SYNERGY,
      user -> {
        user.getPlayer().setMetadata(TerminatorAugment.METADATA_KEY + "REINFORCED_LEARNING", new FixedMetadataValue(plugin, 0));
      }
    ));
    augments.add(new TerminatorAugment(
      "BLEEDING_VITALS",
      new ItemBuilder(XMaterial.NETHERITE_SWORD.parseItem()).build(),
      ChatColor.translateAlternateColorCodes('&', "&6&lBLADE AUGMENT: &e&lBLEEDING VITALS"),
      colorLore("&7Critical hits deal", "&7-/15/20% increased damage"),
      TerminatorAugment.AugmentType.BLADE,
      KitSpecifications.GameTimeState.MID,
      TerminatorAugment.CONSTANT_SYNERGY,
      user -> {
        user.getPlayer().setMetadata(TerminatorAugment.METADATA_KEY + "BLEEDING_VITALS", new FixedMetadataValue(plugin, true));
      }
    ));
    augments.add(new TerminatorAugment(
      "POPLUST_COPYCAT",
      new ItemBuilder(XMaterial.MAGMA_CREAM.parseItem()).build(),
      ChatColor.translateAlternateColorCodes('&', "&b&lSYNERGY AUGMENT: &e&lPOPLUST COPYCAT"),
      colorLore("&7You can oneshot enemies", "&7as well when &e&lCleaner's POPLUST", "&7ultimate is active"),
      TerminatorAugment.AugmentType.BLADE,
      KitSpecifications.GameTimeState.MID,
      (arena) -> arena.getPlayers()
        .stream()
        .anyMatch(player -> plugin.getUserManager().getUser(player).getKit() instanceof CleanerKit),
      user -> {
        user.getPlayer().setMetadata(TerminatorAugment.METADATA_KEY + "POPLUST_COPYCAT", new FixedMetadataValue(plugin, true));
      }
    ));
    augments.add(new TerminatorAugment(
      "WILL_OF_STEEL",
      new ItemBuilder(XMaterial.SHIELD.parseItem()).build(),
      ChatColor.translateAlternateColorCodes('&', "&b&lSYNERGY AUGMENT: &e&lWILL OF STEEL"),
      colorLore("&7You deal -/20/25% increased damage", "&7to stunned enemies"),
      TerminatorAugment.AugmentType.BLADE,
      KitSpecifications.GameTimeState.MID,
      (arena) -> arena.getPlayers()
        .stream()
        .anyMatch(player -> plugin.getUserManager().getUser(player).getKit() instanceof CrusaderKit),
      user -> {
        user.getPlayer().setMetadata(TerminatorAugment.METADATA_KEY + "WILL_OF_STEEL", new FixedMetadataValue(plugin, true));
      }
    ));

    augments.add(new TerminatorAugment(
      "NEURONAL_REPURPOSE",
      new ItemBuilder(XMaterial.MUSIC_DISC_11.parseItem()).build(),
      ChatColor.translateAlternateColorCodes('&', "&6&lBLADE AUGMENT: &e&lNEURONAL REPURPOSE"),
      colorLore("&7Have 5% chance to force weak", "&7enemies to enter &b&lFRENZY&7 and", "&7attack their allies for 5 seconds"),
      TerminatorAugment.AugmentType.BLADE,
      KitSpecifications.GameTimeState.LATE,
      TerminatorAugment.CONSTANT_SYNERGY,
      user -> {
        user.getPlayer().setMetadata(TerminatorAugment.METADATA_KEY + "NEURONAL_REPURPOSE", new FixedMetadataValue(plugin, true));
      }
    ));
    augments.add(new TerminatorAugment(
      "PSIONIC_BLOW",
      new ItemBuilder(XMaterial.FEATHER.parseItem()).build(),
      ChatColor.translateAlternateColorCodes('&', "&c&lSYSTEM AUGMENT: &e&lPSIONIC BLOW"),
      colorLore("&7If you are to receive", "&c&lLETHAL DAMAGE&7 you explode and", "&7push all enemies away", "&8(&720 seconds cooldown&8)"),
      TerminatorAugment.AugmentType.SYSTEM,
      KitSpecifications.GameTimeState.LATE,
      TerminatorAugment.CONSTANT_SYNERGY,
      user -> {
        user.getPlayer().setMetadata(TerminatorAugment.METADATA_KEY + "PSIONIC_BLOW", new FixedMetadataValue(plugin, true));
      }
    ));
    augments.add(new TerminatorAugment(
      "ULTRAVISION",
      new ItemBuilder(XMaterial.IRON_BARS.parseItem()).build(),
      ChatColor.translateAlternateColorCodes('&', "&c&lSYSTEM AUGMENT: &e&lULTRAVISION"),
      colorLore("&7Make every &3&lINVISIBLE", "&7enemy visible for yourself"),
      TerminatorAugment.AugmentType.SYSTEM,
      KitSpecifications.GameTimeState.LATE,
      TerminatorAugment.CONSTANT_SYNERGY,
      user -> {
        user.getPlayer().setMetadata(TerminatorAugment.METADATA_KEY + "ULTRAVISION", new FixedMetadataValue(plugin, true));
      }
    ));
  }

  private static List<String> colorLore(String... strings) {
    List<String> list = new ArrayList<>();
    for (String string : strings) {
      list.add(ChatColor.translateAlternateColorCodes('&', string));
    }
    return list;
  }

}
