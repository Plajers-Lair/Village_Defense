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

package plugily.projects.villagedefense.arena.villager;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import plugily.projects.villagedefense.Main;

public class VillagerAiAnimations {

  public static void makeEntityLookAt(Entity entity, Location target) {
    Location entityLoc = entity.getLocation();

    double dx = target.getX() - entityLoc.getX();
    double dy = target.getY() - entityLoc.getY();
    double dz = target.getZ() - entityLoc.getZ();

    double distanceXZ = Math.sqrt(dx * dx + dz * dz);

    float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
    float pitch = (float) Math.toDegrees(-Math.atan2(dy, distanceXZ));

    entityLoc.setYaw(yaw);
    entityLoc.setPitch(pitch);
    entity.teleport(entityLoc);
  }

  public static Item throwItemTowardsEntity(ItemStack itemStack, Location from, Entity target) {
    Item item = target.getWorld().dropItem(from, itemStack);
    Vector velocity = calculateVelocity(from, target.getLocation().clone().add(0, 2, 0), 0.3);
    item.setVelocity(velocity);
    return item;
  }

  private static Vector calculateVelocity(Location from, Location to, double power) {
    Vector direction = to.toVector().subtract(from.toVector());
    double distance = direction.length();

    direction.normalize();
    double speed = Math.min(power * (distance / 3), 2.0);

    return direction.multiply(speed);
  }

  public static void makeEntityEat(LivingEntity entity, ItemStack food, int eatDuration, CompletionCallback callback) {
    entity.getEquipment().setItemInMainHand(food);

    new BukkitRunnable() {
      int ticks = 0;

      @Override
      public void run() {
        if (ticks >= eatDuration) {
          this.cancel();
          entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);
          entity.getEquipment().setItemInMainHand(null);
          callback.onComplete();
          return;
        }

        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.0f, 1.0f);
        entity.getWorld().spawnParticle(Particle.ITEM_CRACK, entity.getLocation().add(0, 1, 0), 10, 0.2, 0.2, 0.2, 0, food);
        ticks += 5;
      }
    }.runTaskTimer(JavaPlugin.getProvidingPlugin(Main.class), 0L, 5);
  }

}
