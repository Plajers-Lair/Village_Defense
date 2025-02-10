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

package plugily.projects.villagedefense.arena.managers.spawner.gold.enemy;

import lombok.Data;
import lombok.experimental.Accessors;
import org.bukkit.Color;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

@Data
@Accessors(chain = true)
public class ArmorPiece {
  private int waveMinimum;
  private int priority = 1;
  private double chance = 100;
  private ItemStack itemStack;

  public ArmorPiece withEnchants(Enchant... enchants) {
    for (Enchant enchant : enchants) {
      this.itemStack.addUnsafeEnchantment(enchant.enchantment(), enchant.level());
    }
    return this;
  }

  public ArmorPiece withArmorDye(Color color) {
    ItemMeta meta = itemStack.getItemMeta();
    if (!(meta instanceof LeatherArmorMeta leatherArmorMeta)) {
      return this;
    }
    leatherArmorMeta.setColor(color);
    itemStack.setItemMeta(meta);
    return this;
  }

  public enum PiecePart {
    HELMET, CHESTPLATE, LEGGINGS, BOOTS
  }

  public record Enchant(Enchantment enchantment, int level) {
  }
}
