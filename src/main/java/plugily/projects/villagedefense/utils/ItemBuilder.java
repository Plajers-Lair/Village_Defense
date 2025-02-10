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

package plugily.projects.villagedefense.utils;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemBuilder {

  public static final String NAMESPACE = "noheej";
  private final ItemStack itemStack;

  public ItemBuilder(ItemStack itemStack) {
    this.itemStack = itemStack.clone();
  }

  public ItemBuilder(Material material) {
    this.itemStack = new ItemStack(material);
  }

  public ItemBuilder setAmount(int amount) {
    this.itemStack.setAmount(amount);
    return this;
  }

  public ItemBuilder setDisplayName(String text) {
    ItemMeta meta = this.itemStack.getItemMeta();
    meta.setDisplayName(text);
    this.itemStack.setItemMeta(meta);
    return this;
  }

  public ItemBuilder setDisplayNamePlaceholder(String pattern, String value) {
    ItemMeta meta = this.itemStack.getItemMeta();
    meta.setDisplayName(meta.getDisplayName().replace(pattern, value));
    this.itemStack.setItemMeta(meta);
    return this;
  }

  public ItemBuilder setLore(List<String> lore) {
    ItemMeta meta = this.itemStack.getItemMeta();
    meta.setLore(lore);
    this.itemStack.setItemMeta(meta);
    return this;
  }

  public ItemBuilder appendLore(List<String> lines, int number) {
    ItemMeta meta = this.itemStack.getItemMeta();
    List<String> newLore = meta.getLore();
    if (newLore == null) {
      newLore = new ArrayList<>();
    }
    newLore.addAll(number, lines);
    meta.setLore(newLore);
    this.itemStack.setItemMeta(meta);
    return this;
  }

  public ItemBuilder appendLore(List<String> lines) {
    ItemMeta meta = this.itemStack.getItemMeta();
    List<String> newLore = meta.getLore();
    if (newLore == null) {
      newLore = new ArrayList<>();
    }
    newLore.addAll(lines);
    meta.setLore(newLore);
    this.itemStack.setItemMeta(meta);
    return this;
  }

  public ItemBuilder setLorePlaceholder(String pattern, String value) {
    ItemMeta meta = this.itemStack.getItemMeta();
    meta.setLore(
      meta.getLore()
        .stream()
        .map(lore -> lore.replace(pattern, value))
        .collect(Collectors.toList())
    );
    this.itemStack.setItemMeta(meta);
    return this;
  }

  public ItemBuilder setEnchantment(Enchantment enchantment, int level) {
    ItemMeta meta = this.itemStack.getItemMeta();
    meta.addEnchant(enchantment, level, true);
    this.itemStack.setItemMeta(meta);
    return this;
  }

  public ItemBuilder addFlag(ItemFlag flag) {
    ItemMeta meta = this.itemStack.getItemMeta();
    meta.addItemFlags(flag);
    this.itemStack.setItemMeta(meta);
    return this;
  }

  public ItemBuilder addPersistingMetadata(String key, int value) {
    ItemMeta meta = this.itemStack.getItemMeta();
    NamespacedKey namespacedKey = new NamespacedKey(NAMESPACE, key);
    meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.INTEGER, value);
    this.itemStack.setItemMeta(meta);
    return this;
  }

  public ItemBuilder addPersistingMetadata(String key, double value) {
    ItemMeta meta = this.itemStack.getItemMeta();
    NamespacedKey namespacedKey = new NamespacedKey(NAMESPACE, key);
    meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.DOUBLE, value);
    this.itemStack.setItemMeta(meta);
    return this;
  }

  public ItemBuilder addPersistingMetadata(String key, String value) {
    ItemMeta meta = this.itemStack.getItemMeta();
    NamespacedKey namespacedKey = new NamespacedKey(NAMESPACE, key);
    meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, value);
    this.itemStack.setItemMeta(meta);
    return this;
  }

  public ItemBuilder setCustomModelData(int data) {
    ItemMeta meta = this.itemStack.getItemMeta();
    meta.setCustomModelData(data);
    this.itemStack.setItemMeta(meta);
    return this;
  }

  public ItemStack build() {
    return this.itemStack;
  }

}

