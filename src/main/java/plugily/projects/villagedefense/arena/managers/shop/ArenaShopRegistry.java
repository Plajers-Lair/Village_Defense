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

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.villagedefense.Main;

import java.util.ArrayList;
import java.util.List;

public class ArenaShopRegistry {

  private final @Getter List<ShopItem> shopItems = new ArrayList<>();

  public void registerItems() {
    registerSwords();
    registerBows();
    registerCrossbows();
    registerHelmets();
    registerChestplate();
    registerLeggings();
    registerBoots();
    registerConsumables();
  }

  private void registerSwords() {
    shopItems.add(new ShopItem("WeaponT1",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.STONE_SWORD)
          .name(color("&7Stone Sword &e&l(Tier I)"))
          .enchantment(Enchantment.DAMAGE_ALL, 1)
          .build()), "WeaponT1"),
        ShopItem.PurchasableItem.Position.INVENTORY
      ),
      150, 3, 1, null, true, false
    ));
    shopItems.add(new ShopItem("WeaponT2",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.IRON_SWORD)
          .name(color("&7Iron Sword &e&l(Tier II)"))
          .build()), "WeaponT2"),
        ShopItem.PurchasableItem.Position.INVENTORY
      ),
      250, 3, 1, "WeaponT1", true, false
    ));
    shopItems.add(new ShopItem("WeaponT3",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.IRON_SWORD)
          .name(color("&7Iron Sword &e&l(Tier III)"))
          .enchantment(Enchantment.DAMAGE_ALL, 1)
          .build()), "WeaponT3"),
        ShopItem.PurchasableItem.Position.INVENTORY
      ),
      450, 3, 1, "WeaponT2", true, false
    ));
    shopItems.add(new ShopItem("WeaponT4",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.IRON_SWORD)
          .name(color("&7Iron Sword &e&l(Tier IV)"))
          .enchantment(Enchantment.DAMAGE_ALL, 2)
          .enchantment(Enchantment.KNOCKBACK, 1)
          .build()), "WeaponT4"),
        ShopItem.PurchasableItem.Position.INVENTORY
      ),
      650, 3, 1, "WeaponT3", true, false
    ));
    shopItems.add(new ShopItem("WeaponT5",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.DIAMOND_SWORD)
          .name(color("&dDiamond Sword &e&l(Tier V)"))
          .enchantment(Enchantment.DAMAGE_ALL, 2)
          .enchantment(Enchantment.KNOCKBACK, 1)
          .build()), "WeaponT5"),
        ShopItem.PurchasableItem.Position.INVENTORY
      ),
      850, 3, 1, "WeaponT4", true, false
    ));
    shopItems.add(new ShopItem("WeaponT6",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.DIAMOND_SWORD)
          .name(color("&dDiamond Sword &e&l(Tier VI)"))
          .enchantment(Enchantment.DAMAGE_ALL, 3)
          .enchantment(Enchantment.KNOCKBACK, 1)
          .build()), "WeaponT6"),
        ShopItem.PurchasableItem.Position.INVENTORY
      ),
      1050, 3, 1, "WeaponT5", true, false
    ));
    shopItems.add(new ShopItem("WeaponT7",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.DIAMOND_SWORD)
          .name(color("&dDiamond Sword &e&l(Tier VII)"))
          .enchantment(Enchantment.DAMAGE_ALL, 3)
          .enchantment(Enchantment.FIRE_ASPECT, 1)
          .enchantment(Enchantment.KNOCKBACK, 1)
          .build()), "WeaponT7"),
        ShopItem.PurchasableItem.Position.INVENTORY
      ),
      1250, 3, 1, "WeaponT6", true, false
    ));
    shopItems.add(new ShopItem("WeaponT8",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.NETHERITE_SWORD)
          .name(color("&8Netherite Sword &e&l(Tier VIII)"))
          .enchantment(Enchantment.DAMAGE_ALL, 3)
          .enchantment(Enchantment.FIRE_ASPECT, 1)
          .enchantment(Enchantment.KNOCKBACK, 1)
          .build()), "WeaponT8"),
        ShopItem.PurchasableItem.Position.INVENTORY
      ),
      1450, 3, 1, "WeaponT7", true, false
    ));
    shopItems.add(new ShopItem("WeaponT9",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.NETHERITE_SWORD)
          .name(color("&8Netherite Sword &e&l(Tier IX)"))
          .enchantment(Enchantment.DAMAGE_ALL, 4)
          .enchantment(Enchantment.FIRE_ASPECT, 1)
          .enchantment(Enchantment.KNOCKBACK, 1)
          .build()), "WeaponT9"),
        ShopItem.PurchasableItem.Position.INVENTORY
      ),
      1650, 3, 1, "WeaponT8", true, true
    ));
  }

  private void registerBows() {
    shopItems.add(new ShopItem("BowT1",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.BOW)
          .name(color("&7Bow &e&l(Tier I)"))
          .enchantment(Enchantment.ARROW_INFINITE, 1)
          .enchantment(Enchantment.ARROW_DAMAGE, 1)
          .build()), "BowT1"),
        ShopItem.PurchasableItem.Position.INVENTORY
      ), 100, 4, 1, null, true, false
    ));
    shopItems.add(new ShopItem("BowT2",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.BOW)
          .name(color("&7Bow &e&l(Tier II)"))
          .enchantment(Enchantment.ARROW_INFINITE, 1)
          .enchantment(Enchantment.ARROW_DAMAGE, 2)
          .build()), "BowT2"),
        ShopItem.PurchasableItem.Position.INVENTORY
      ), 100, 4, 1, "BowT1", true, false
    ));
    shopItems.add(new ShopItem("BowT3",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.BOW)
          .name(color("&7Bow &e&l(Tier III)"))
          .enchantment(Enchantment.ARROW_INFINITE, 1)
          .enchantment(Enchantment.ARROW_DAMAGE, 3)
          .build()), "BowT3"),
        ShopItem.PurchasableItem.Position.INVENTORY
      ), 100, 4, 1, "BowT2", true, false
    ));
    shopItems.add(new ShopItem("BowT4",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.BOW)
          .name(color("&7Bow &e&l(Tier IV)"))
          .enchantment(Enchantment.ARROW_INFINITE, 1)
          .enchantment(Enchantment.ARROW_DAMAGE, 3)
          .enchantment(Enchantment.ARROW_KNOCKBACK, 1)
          .build()), "BowT4"),
        ShopItem.PurchasableItem.Position.INVENTORY
      ), 100, 4, 1, "BowT3", true, false
    ));
    shopItems.add(new ShopItem("BowT5",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.BOW)
          .name(color("&7Bow &e&l(Tier V)"))
          .enchantment(Enchantment.ARROW_INFINITE, 1)
          .enchantment(Enchantment.ARROW_DAMAGE, 3)
          .enchantment(Enchantment.ARROW_KNOCKBACK, 1)
          .enchantment(Enchantment.ARROW_FIRE, 1)
          .build()), "BowT5"),
        ShopItem.PurchasableItem.Position.INVENTORY
      ), 100, 4, 1, "BowT4", true, true
    ));
  }

  private void registerCrossbows() {
    shopItems.add(new ShopItem("CrossbowT1",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.CROSSBOW)
          .name(color("&7Crossbow &e&l(Tier I)"))
          .enchantment(Enchantment.ARROW_INFINITE, 1)
          .enchantment(Enchantment.ARROW_DAMAGE, 1)
          .build()), "CrossbowT1"),
        ShopItem.PurchasableItem.Position.INVENTORY
      ), 100, 5, 1, null, true, false
    ));
    shopItems.add(new ShopItem("CrossbowT2",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.CROSSBOW)
          .name(color("&7Crossbow &e&l(Tier II)"))
          .enchantment(Enchantment.ARROW_INFINITE, 1)
          .enchantment(Enchantment.ARROW_DAMAGE, 2)
          .build()), "CrossbowT2"),
        ShopItem.PurchasableItem.Position.INVENTORY
      ), 100, 5, 1, "CrossbowT1", true, false
    ));
    shopItems.add(new ShopItem("CrossbowT3",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.CROSSBOW)
          .name(color("&7Crossbow &e&l(Tier III)"))
          .enchantment(Enchantment.ARROW_INFINITE, 1)
          .enchantment(Enchantment.ARROW_DAMAGE, 2)
          .enchantment(Enchantment.QUICK_CHARGE, 1)
          .build()), "CrossbowT3"),
        ShopItem.PurchasableItem.Position.INVENTORY
      ), 100, 5, 1, "CrossbowT2", true, false
    ));
    shopItems.add(new ShopItem("CrossbowT4",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.CROSSBOW)
          .name(color("&7Crossbow &e&l(Tier IV)"))
          .enchantment(Enchantment.ARROW_INFINITE, 1)
          .enchantment(Enchantment.ARROW_DAMAGE, 2)
          .enchantment(Enchantment.QUICK_CHARGE, 1)
          .enchantment(Enchantment.PIERCING, 1)
          .build()), "CrossbowT4"),
        ShopItem.PurchasableItem.Position.INVENTORY
      ), 100, 5, 1, "CrossbowT3", true, false
    ));
    shopItems.add(new ShopItem("CrossbowT5",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.CROSSBOW)
          .name(color("&7Crossbow &e&l(Tier V)"))
          .enchantment(Enchantment.ARROW_INFINITE, 1)
          .enchantment(Enchantment.ARROW_DAMAGE, 3)
          .enchantment(Enchantment.QUICK_CHARGE, 1)
          .enchantment(Enchantment.PIERCING, 1)
          .build()), "CrossbowT5"),
        ShopItem.PurchasableItem.Position.INVENTORY
      ), 100, 5, 1, "CrossbowT4", true, false
    ));
    shopItems.add(new ShopItem("CrossbowT6",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.CROSSBOW)
          .name(color("&7Crossbow &e&l(Tier VI)"))
          .enchantment(Enchantment.ARROW_INFINITE, 1)
          .enchantment(Enchantment.ARROW_DAMAGE, 3)
          .enchantment(Enchantment.QUICK_CHARGE, 2)
          .enchantment(Enchantment.PIERCING, 1)
          .build()), "CrossbowT6"),
        ShopItem.PurchasableItem.Position.INVENTORY
      ), 100, 5, 1, "CrossbowT5", true, false
    ));
    shopItems.add(new ShopItem("CrossbowT7",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.CROSSBOW)
          .name(color("&7Crossbow &e&l(Tier VII)"))
          .enchantment(Enchantment.ARROW_INFINITE, 1)
          .enchantment(Enchantment.ARROW_DAMAGE, 3)
          .enchantment(Enchantment.QUICK_CHARGE, 2)
          .enchantment(Enchantment.PIERCING, 2)
          .build()), "CrossbowT7"),
        ShopItem.PurchasableItem.Position.INVENTORY
      ), 100, 5, 1, "CrossbowT6", true, false
    ));
    shopItems.add(new ShopItem("CrossbowT8",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.CROSSBOW)
          .name(color("&7Crossbow &e&l(Tier VIII)"))
          .enchantment(Enchantment.ARROW_INFINITE, 1)
          .enchantment(Enchantment.ARROW_DAMAGE, 3)
          .enchantment(Enchantment.QUICK_CHARGE, 2)
          .enchantment(Enchantment.PIERCING, 2)
          .enchantment(Enchantment.ARROW_FIRE, 1)
          .build()), "CrossbowT8"),
        ShopItem.PurchasableItem.Position.INVENTORY
      ), 100, 5, 1, "CrossbowT7", true, false
    ));
    shopItems.add(new ShopItem("CrossbowT9",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.CROSSBOW)
          .name(color("&7Crossbow &e&l(Tier IX)"))
          .enchantment(Enchantment.ARROW_INFINITE, 1)
          .enchantment(Enchantment.ARROW_DAMAGE, 3)
          .enchantment(Enchantment.QUICK_CHARGE, 2)
          .enchantment(Enchantment.PIERCING, 2)
          .enchantment(Enchantment.ARROW_FIRE, 1)
          .enchantment(Enchantment.MULTISHOT, 1)
          .build()), "CrossbowT9"),
        ShopItem.PurchasableItem.Position.INVENTORY
      ), 100, 5, 1, "CrossbowT8", true, true
    ));
  }

  private void registerHelmets() {
    shopItems.add(new ShopItem("HelmetT1",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.CHAINMAIL_HELMET)
          .name(color("&7Chainmail Helmet &e&l(Tier I)"))
          .build()), "HelmetT1"),
        ShopItem.PurchasableItem.Position.ARMOR_HELMET
      ), 100, 2, 2, null, true, false
    ));
    shopItems.add(new ShopItem("HelmetT2",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.IRON_HELMET)
          .name(color("&7Iron Helmet &e&l(Tier II)"))
          .build()), "HelmetT2"),
        ShopItem.PurchasableItem.Position.ARMOR_HELMET
      ), 100, 2, 2, "HelmetT1", true, false
    ));
    shopItems.add(new ShopItem("HelmetT3",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.IRON_HELMET)
          .name(color("&7Iron Helmet &e&l(Tier III)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1)
          .build()), "HelmetT3"),
        ShopItem.PurchasableItem.Position.ARMOR_HELMET
      ), 100, 2, 2, "HelmetT2", true, false
    ));
    shopItems.add(new ShopItem("HelmetT4",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.IRON_HELMET)
          .name(color("&7Iron Helmet &e&l(Tier IV)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
          .build()), "HelmetT4"),
        ShopItem.PurchasableItem.Position.ARMOR_HELMET
      ), 100, 2, 2, "HelmetT3", true, false
    ));
    shopItems.add(new ShopItem("HelmetT5",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.IRON_HELMET)
          .name(color("&7Iron Helmet &e&l(Tier V)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
          .enchantment(Enchantment.THORNS, 1)
          .build()), "HelmetT5"),
        ShopItem.PurchasableItem.Position.ARMOR_HELMET
      ), 100, 2, 2, "HelmetT4", true, false
    ));
    shopItems.add(new ShopItem("HelmetT6",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.DIAMOND_HELMET)
          .name(color("&dDiamond Helmet &e&l(Tier VI)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
          .enchantment(Enchantment.THORNS, 1)
          .build()), "HelmetT6"),
        ShopItem.PurchasableItem.Position.ARMOR_HELMET
      ), 100, 2, 2, "HelmetT5", true, false
    ));
    shopItems.add(new ShopItem("HelmetT7",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.DIAMOND_HELMET)
          .name(color("&dDiamond Helmet &e&l(Tier VII)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
          .enchantment(Enchantment.PROTECTION_EXPLOSIONS, 1)
          .enchantment(Enchantment.THORNS, 1)
          .build()), "HelmetT7"),
        ShopItem.PurchasableItem.Position.ARMOR_HELMET
      ), 100, 2, 2, "HelmetT6", true, false
    ));
    shopItems.add(new ShopItem("HelmetT8",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.DIAMOND_HELMET)
          .name(color("&dDiamond Helmet &e&l(Tier VIII)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3)
          .enchantment(Enchantment.PROTECTION_EXPLOSIONS, 1)
          .enchantment(Enchantment.THORNS, 1)
          .build()), "HelmetT8"),
        ShopItem.PurchasableItem.Position.ARMOR_HELMET
      ), 100, 2, 2, "HelmetT7", true, false
    ));
    shopItems.add(new ShopItem("HelmetT9",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.NETHERITE_HELMET)
          .name(color("&8Netherite Helmet &e&l(Tier IX)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3)
          .enchantment(Enchantment.PROTECTION_EXPLOSIONS, 1)
          .enchantment(Enchantment.THORNS, 1)
          .build()), "HelmetT9"),
        ShopItem.PurchasableItem.Position.ARMOR_HELMET
      ), 100, 2, 2, "HelmetT8", true, false
    ));
    shopItems.add(new ShopItem("HelmetT10",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.NETHERITE_HELMET)
          .name(color("&8Netherite Helmet &e&l(Tier X)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
          .enchantment(Enchantment.PROTECTION_EXPLOSIONS, 1)
          .enchantment(Enchantment.THORNS, 1)
          .build()), "HelmetT10"),
        ShopItem.PurchasableItem.Position.ARMOR_HELMET
      ), 100, 2, 2, "HelmetT9", true, true
    ));
  }

  private void registerChestplate() {
    shopItems.add(new ShopItem("ChestplateT1",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.CHAINMAIL_CHESTPLATE)
          .name(color("&7Chainmail Chestplate &e&l(Tier I)"))
          .build()), "ChestplateT1"),
        ShopItem.PurchasableItem.Position.ARMOR_CHESTPLATE
      ), 100, 3, 2, null, true, false
    ));
    shopItems.add(new ShopItem("ChestplateT2",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.IRON_CHESTPLATE)
          .name(color("&7Iron Chestplate &e&l(Tier II)"))
          .build()), "ChestplateT2"),
        ShopItem.PurchasableItem.Position.ARMOR_CHESTPLATE
      ), 100, 3, 2, "ChestplateT1", true, false
    ));
    shopItems.add(new ShopItem("ChestplateT3",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.IRON_CHESTPLATE)
          .name(color("&7Iron Chestplate &e&l(Tier III)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1)
          .build()), "ChestplateT3"),
        ShopItem.PurchasableItem.Position.ARMOR_CHESTPLATE
      ), 100, 3, 2, "ChestplateT2", true, false
    ));
    shopItems.add(new ShopItem("ChestplateT4",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.IRON_CHESTPLATE)
          .name(color("&7Iron Chestplate &e&l(Tier IV)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
          .build()), "ChestplateT4"),
        ShopItem.PurchasableItem.Position.ARMOR_CHESTPLATE
      ), 100, 3, 2, "ChestplateT3", true, false
    ));
    shopItems.add(new ShopItem("ChestplateT5",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.IRON_CHESTPLATE)
          .name(color("&7Iron Chestplate &e&l(Tier V)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
          .enchantment(Enchantment.THORNS, 1)
          .build()), "ChestplateT5"),
        ShopItem.PurchasableItem.Position.ARMOR_CHESTPLATE
      ), 100, 3, 2, "ChestplateT4", true, false
    ));
    shopItems.add(new ShopItem("ChestplateT6",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.DIAMOND_CHESTPLATE)
          .name(color("&dDiamond Chestplate &e&l(Tier VI)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
          .enchantment(Enchantment.THORNS, 1)
          .build()), "ChestplateT6"),
        ShopItem.PurchasableItem.Position.ARMOR_CHESTPLATE
      ), 100, 3, 2, "ChestplateT5", true, false
    ));
    shopItems.add(new ShopItem("ChestplateT7",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.DIAMOND_CHESTPLATE)
          .name(color("&dDiamond Chestplate &e&l(Tier VII)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
          .enchantment(Enchantment.PROTECTION_EXPLOSIONS, 1)
          .enchantment(Enchantment.THORNS, 1)
          .build()), "ChestplateT7"),
        ShopItem.PurchasableItem.Position.ARMOR_CHESTPLATE
      ), 100, 3, 2, "ChestplateT6", true, false
    ));
    shopItems.add(new ShopItem("ChestplateT8",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.DIAMOND_CHESTPLATE)
          .name(color("&dDiamond Chestplate &e&l(Tier VIII)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3)
          .enchantment(Enchantment.PROTECTION_EXPLOSIONS, 1)
          .enchantment(Enchantment.THORNS, 1)
          .build()), "ChestplateT8"),
        ShopItem.PurchasableItem.Position.ARMOR_CHESTPLATE
      ), 100, 3, 2, "ChestplateT7", true, false
    ));
    shopItems.add(new ShopItem("ChestplateT9",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.NETHERITE_CHESTPLATE)
          .name(color("&8Netherite Chestplate &e&l(Tier IX)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3)
          .enchantment(Enchantment.PROTECTION_EXPLOSIONS, 1)
          .enchantment(Enchantment.THORNS, 1)
          .build()), "ChestplateT9"),
        ShopItem.PurchasableItem.Position.ARMOR_CHESTPLATE
      ), 100, 3, 2, "ChestplateT8", true, false
    ));
    shopItems.add(new ShopItem("ChestplateT10",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.NETHERITE_CHESTPLATE)
          .name(color("&8Netherite Chestplate &e&l(Tier X)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
          .enchantment(Enchantment.PROTECTION_EXPLOSIONS, 1)
          .enchantment(Enchantment.THORNS, 1)
          .build()), "ChestplateT10"),
        ShopItem.PurchasableItem.Position.ARMOR_CHESTPLATE
      ), 100, 3, 2, "ChestplateT9", true, true
    ));
  }

  private void registerLeggings() {
    shopItems.add(new ShopItem("LeggingsT1",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.CHAINMAIL_LEGGINGS)
          .name(color("&7Chainmail Leggings &e&l(Tier I)"))
          .build()), "LeggingsT1"),
        ShopItem.PurchasableItem.Position.ARMOR_LEGGINGS
      ), 100, 5, 2, null, true, false
    ));
    shopItems.add(new ShopItem("LeggingsT2",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.IRON_LEGGINGS)
          .name(color("&7Iron Leggings &e&l(Tier II)"))
          .build()), "LeggingsT2"),
        ShopItem.PurchasableItem.Position.ARMOR_LEGGINGS
      ), 100, 5, 2, "LeggingsT1", true, false
    ));
    shopItems.add(new ShopItem("LeggingsT3",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.IRON_LEGGINGS)
          .name(color("&7Iron Leggings &e&l(Tier III)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1)
          .build()), "LeggingsT3"),
        ShopItem.PurchasableItem.Position.ARMOR_LEGGINGS
      ), 100, 5, 2, "LeggingsT2", true, false
    ));
    shopItems.add(new ShopItem("LeggingsT4",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.IRON_LEGGINGS)
          .name(color("&7Iron Leggings &e&l(Tier IV)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
          .build()), "LeggingsT4"),
        ShopItem.PurchasableItem.Position.ARMOR_LEGGINGS
      ), 100, 5, 2, "LeggingsT3", true, false
    ));
    shopItems.add(new ShopItem("LeggingsT5",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.IRON_LEGGINGS)
          .name(color("&7Iron Leggings &e&l(Tier V)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
          .enchantment(Enchantment.THORNS, 1)
          .build()), "LeggingsT5"),
        ShopItem.PurchasableItem.Position.ARMOR_LEGGINGS
      ), 100, 5, 2, "LeggingsT4", true, false
    ));
    shopItems.add(new ShopItem("LeggingsT6",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.DIAMOND_LEGGINGS)
          .name(color("&dDiamond Leggings &e&l(Tier VI)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
          .enchantment(Enchantment.THORNS, 1)
          .build()), "LeggingsT6"),
        ShopItem.PurchasableItem.Position.ARMOR_LEGGINGS
      ), 100, 5, 2, "LeggingsT5", true, false
    ));
    shopItems.add(new ShopItem("LeggingsT7",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.DIAMOND_LEGGINGS)
          .name(color("&dDiamond Leggings &e&l(Tier VII)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
          .enchantment(Enchantment.PROTECTION_EXPLOSIONS, 1)
          .enchantment(Enchantment.THORNS, 1)
          .build()), "LeggingsT7"),
        ShopItem.PurchasableItem.Position.ARMOR_LEGGINGS
      ), 100, 5, 2, "LeggingsT6", true, false
    ));
    shopItems.add(new ShopItem("LeggingsT8",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.DIAMOND_LEGGINGS)
          .name(color("&dDiamond Leggings &e&l(Tier VIII)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3)
          .enchantment(Enchantment.PROTECTION_EXPLOSIONS, 1)
          .enchantment(Enchantment.THORNS, 1)
          .build()), "LeggingsT8"),
        ShopItem.PurchasableItem.Position.ARMOR_LEGGINGS
      ), 100, 5, 2, "LeggingsT7", true, false
    ));
    shopItems.add(new ShopItem("LeggingsT9",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.NETHERITE_LEGGINGS)
          .name(color("&8Netherite Leggings &e&l(Tier IX)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3)
          .enchantment(Enchantment.PROTECTION_EXPLOSIONS, 1)
          .enchantment(Enchantment.THORNS, 1)
          .build()), "LeggingsT9"),
        ShopItem.PurchasableItem.Position.ARMOR_LEGGINGS
      ), 100, 5, 2, "LeggingsT8", true, false
    ));
    shopItems.add(new ShopItem("LeggingsT10",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.NETHERITE_LEGGINGS)
          .name(color("&8Netherite Leggings &e&l(Tier X)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
          .enchantment(Enchantment.PROTECTION_EXPLOSIONS, 1)
          .enchantment(Enchantment.THORNS, 1)
          .build()), "LeggingsT10"),
        ShopItem.PurchasableItem.Position.ARMOR_LEGGINGS
      ), 100, 5, 2, "LeggingsT9", true, true
    ));
  }

  private void registerBoots() {
    shopItems.add(new ShopItem("BootsT1",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.CHAINMAIL_BOOTS)
          .name(color("&7Chainmail Boots &e&l(Tier I)"))
          .build()), "BootsT1"),
        ShopItem.PurchasableItem.Position.ARMOR_BOOTS
      ), 100, 6, 2, null, true, false
    ));
    shopItems.add(new ShopItem("BootsT2",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.IRON_BOOTS)
          .name(color("&7Iron Boots &e&l(Tier II)"))
          .build()), "BootsT2"),
        ShopItem.PurchasableItem.Position.ARMOR_BOOTS
      ), 100, 6, 2, "BootsT1", true, false
    ));
    shopItems.add(new ShopItem("BootsT3",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.IRON_BOOTS)
          .name(color("&7Iron Boots &e&l(Tier III)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1)
          .build()), "BootsT3"),
        ShopItem.PurchasableItem.Position.ARMOR_BOOTS
      ), 100, 6, 2, "BootsT2", true, false
    ));
    shopItems.add(new ShopItem("BootsT4",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.IRON_BOOTS)
          .name(color("&7Iron Boots &e&l(Tier IV)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
          .build()), "BootsT4"),
        ShopItem.PurchasableItem.Position.ARMOR_BOOTS
      ), 100, 6, 2, "BootsT3", true, false
    ));
    shopItems.add(new ShopItem("BootsT5",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.IRON_BOOTS)
          .name(color("&7Iron Boots &e&l(Tier V)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
          .enchantment(Enchantment.THORNS, 1)
          .build()), "BootsT5"),
        ShopItem.PurchasableItem.Position.ARMOR_BOOTS
      ), 100, 6, 2, "BootsT4", true, false
    ));
    shopItems.add(new ShopItem("BootsT6",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.DIAMOND_BOOTS)
          .name(color("&dDiamond Boots &e&l(Tier VI)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
          .enchantment(Enchantment.THORNS, 1)
          .build()), "BootsT6"),
        ShopItem.PurchasableItem.Position.ARMOR_BOOTS
      ), 100, 6, 2, "BootsT5", true, false
    ));
    shopItems.add(new ShopItem("BootsT7",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.DIAMOND_BOOTS)
          .name(color("&dDiamond Boots &e&l(Tier VII)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
          .enchantment(Enchantment.PROTECTION_EXPLOSIONS, 1)
          .enchantment(Enchantment.THORNS, 1)
          .build()), "BootsT7"),
        ShopItem.PurchasableItem.Position.ARMOR_BOOTS
      ), 100, 6, 2, "BootsT6", true, false
    ));
    shopItems.add(new ShopItem("BootsT8",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.DIAMOND_BOOTS)
          .name(color("&dDiamond Boots &e&l(Tier VIII)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3)
          .enchantment(Enchantment.PROTECTION_EXPLOSIONS, 1)
          .enchantment(Enchantment.THORNS, 1)
          .build()), "BootsT8"),
        ShopItem.PurchasableItem.Position.ARMOR_BOOTS
      ), 100, 6, 2, "BootsT7", true, false
    ));
    shopItems.add(new ShopItem("BootsT9",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.NETHERITE_BOOTS)
          .name(color("&8Netherite Boots &e&l(Tier IX)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3)
          .enchantment(Enchantment.PROTECTION_EXPLOSIONS, 1)
          .enchantment(Enchantment.THORNS, 1)
          .build()), "BootsT9"),
        ShopItem.PurchasableItem.Position.ARMOR_BOOTS
      ), 100, 6, 2, "BootsT8", true, false
    ));
    shopItems.add(new ShopItem("BootsT10",
      new ShopItem.PurchasableItem(
        metadata(unbreakable(new ItemBuilder(Material.NETHERITE_BOOTS)
          .name(color("&8Netherite Boots &e&l(Tier X)"))
          .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
          .enchantment(Enchantment.PROTECTION_EXPLOSIONS, 1)
          .enchantment(Enchantment.THORNS, 1)
          .build()), "BootsT10"),
        ShopItem.PurchasableItem.Position.ARMOR_BOOTS
      ), 100, 6, 2, "BootsT9", true, true
    ));
  }

  private void registerConsumables() {
    shopItems.add(new ShopItem("Food",
      new ShopItem.PurchasableItem(
        new ItemBuilder(Material.COOKED_PORKCHOP)
          .name(color("&6Cooked Porkchop"))
          .amount(3)
          .build(),
        ShopItem.PurchasableItem.Position.INVENTORY
      ), 100, 3, 3, null, false, true
    ));
    ItemStack splashPotion1 = new ItemBuilder(Material.SPLASH_POTION)
      .name(color("&6Splash Potion of Slowness"))
      .amount(1)
      .build();
    PotionMeta meta1 = (PotionMeta) splashPotion1.getItemMeta();
    meta1.addCustomEffect(PotionEffectType.SLOW.createEffect(20 * 10, 0), true);
    splashPotion1.setItemMeta(meta1);
    shopItems.add(new ShopItem("SplashPotion1",
      new ShopItem.PurchasableItem(
        splashPotion1,
        ShopItem.PurchasableItem.Position.INVENTORY
      ), 100, 4, 3, null, false, true
    ));
    ItemStack splashPotion2 = new ItemBuilder(Material.SPLASH_POTION)
      .name(color("&6Splash Potion of Healing"))
      .amount(1)
      .build();
    PotionMeta meta2 = (PotionMeta) splashPotion2.getItemMeta();
    meta2.addCustomEffect(PotionEffectType.HEAL.createEffect(1, 0), true);
    splashPotion2.setItemMeta(meta2);
    shopItems.add(new ShopItem("SplashPotion2",
      new ShopItem.PurchasableItem(
        splashPotion2,
        ShopItem.PurchasableItem.Position.INVENTORY
      ), 100, 5, 3, null, false, true
    ));
  }

  private ItemStack unbreakable(ItemStack stack) {
    ItemMeta meta = stack.getItemMeta();
    meta.setUnbreakable(true);
    stack.setItemMeta(meta);
    return stack;
  }

  private ItemStack metadata(ItemStack stack, String key) {
    ItemMeta meta = stack.getItemMeta();
    PersistentDataContainer container = meta.getPersistentDataContainer();
    container.set(new NamespacedKey(JavaPlugin.getProvidingPlugin(Main.class), key), PersistentDataType.STRING, "true");
    stack.setItemMeta(meta);
    return stack;
  }

  private String color(String msg) {
    return ChatColor.translateAlternateColorCodes('&', msg);
  }

}
