/*
 * Village Defense - Protect villagers from hordes of zombies
 * Copyright (c) 2023  Plugily Projects - maintained by Tigerpanzer_02 and contributors
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

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plugily.projects.minigamesbox.classic.kits.basekits.Kit;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.minigamesbox.classic.utils.helper.ItemUtils;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.villagedefense.Main;

/**
 * @author Plajer
 * <p>
 * Created at 01.08.2023
 */
public class KitHelper {

  private static Main plugin;

  private KitHelper() {
  }

  public static void init(Main plugin) {
    KitHelper.plugin = plugin;
  }

  public static boolean isInGameWithKitAndItemInHand(Player player, Class<? extends Kit> instance) {
    if(plugin.getArenaRegistry().getArena(player) == null) {
      return false;
    }

    User user = plugin.getUserManager().getUser(player);
    if(user.isSpectator() || !instance.isInstance(user.getKit())) {
      return false;
    }
    ItemStack stack = VersionUtils.getItemInHand(player);
    return ItemUtils.isItemStackNamed(stack);
  }

  public static void scheduleAbilityCooldown(ItemStack item, Player player, int castTime, int cooldown) {
    ItemMeta meta = item.getItemMeta();
    meta.addEnchant(Enchantment.DURABILITY, 1, true);
    item.setItemMeta(meta);
    Bukkit.getScheduler().runTaskLater(KitHelper.plugin, () -> {
      ItemMeta newMeta = item.getItemMeta();
      newMeta.removeEnchant(Enchantment.DURABILITY);
      item.setItemMeta(newMeta);

      VersionUtils.setMaterialCooldown(player, item.getType(), (cooldown - castTime) * 20);
    }, castTime * 20L);
  }

  public static void healPlayer(Player player, double healAmount) {
    player.setHealth(Math.min(player.getHealth() + healAmount, VersionUtils.getMaxHealth(player)));
    VersionUtils.sendParticles("HEART", player, player.getLocation(), 3);
  }

  public static boolean executeEnemy(LivingEntity entity, Player damager) {
    //todo implement execution immunity here (for bosses)
    entity.damage(KitSpecifications.LETHAL_DAMAGE, damager);
    return true;
  }

  public static double maxHealthPercentDamage(LivingEntity entity, Player damager, double percent) {
    //todo implement max health percentage immunity here (for bosses)
    entity.damage(0, damager);
    double damageDone = (VersionUtils.getMaxHealth(entity) / 100.0) * percent;
    entity.setHealth(Math.max(0, entity.getHealth() - damageDone));
    return damageDone;
  }

}
