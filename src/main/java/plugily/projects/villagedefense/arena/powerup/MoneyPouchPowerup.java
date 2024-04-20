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

package plugily.projects.villagedefense.arena.powerup;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XSound;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.handlers.hologram.ArmorStandHologram;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class MoneyPouchPowerup implements Powerup {

  private final List<ItemStack> goldBlocks = Arrays.asList(XMaterial.GOLD_NUGGET.parseItem(), XMaterial.GOLD_INGOT.parseItem());
  private final Random random = new Random();
  private final Main plugin;

  public MoneyPouchPowerup(Main plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean canSpawn(Arena arena) {
    return true;
  }

  @Override
  public void spawn(Arena arena, Location location) {
    ArmorStandHologram hologram = new ArmorStandHologram(location.clone().add(0, -1.35, 0))
      .appendItem(XMaterial.GOLD_BLOCK.parseItem())
      .appendLines(ChatColor.translateAlternateColorCodes('&', "&e&lMONEY POUCH"))
      .appendLines(ChatColor.translateAlternateColorCodes('&', "&7Click me!"));

    AtomicInteger pouchHits = new AtomicInteger(10);
    hologram.setTouchHandler(player -> {
      if (!plugin.getArenaRegistry().getArena(player).equals(arena)) {
        return;
      }
      XSound.BLOCK_LAVA_POP.play(player);
      Item item = hologram.getLocation().getWorld().dropItemNaturally(hologram.getLocation(), goldBlocks.get(random.nextInt(goldBlocks.size())));
      item.setInvulnerable(true);
      item.setPickupDelay(Integer.MAX_VALUE);
      item.setGravity(true);
      item.setCustomNameVisible(false);
      Bukkit.getScheduler().runTaskLater(plugin, () -> item.remove(), 15);
      int rand = random.nextInt(3, 15);
      plugin.getUserManager().getUser(player).adjustStatistic(plugin.getStatsStorage().getStatisticType("ORBS"), rand);
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a+" + rand + " &7orbs"));
      int hits = pouchHits.decrementAndGet();
      if (hits <= 0) {
        hologram.delete();
      }
    });

    new BukkitRunnable() {
      boolean toggle = true;
      boolean grounded = false;
      boolean flyReverse = false;
      boolean soundPlayed = false;
      int tick = 0;

      @Override
      public void run() {
        Item entityItem = hologram.getEntityItem();
        if (tick == 190) {
          XSound.BLOCK_ANVIL_DESTROY.play(hologram.getLocation(), 1, 1.25f);
          soundPlayed = true;
        }
        if (tick == 200 || hologram.isDeleted() || hologram.getArmorStands().isEmpty()) {
          if (!soundPlayed) {
            XSound.BLOCK_ANVIL_DESTROY.play(hologram.getLocation(), 1, 1.25f);
          }
          for (int i = 0; i < 12; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
              Item item = hologram.getLocation().getWorld().dropItemNaturally(entityItem.getLocation(), goldBlocks.get(random.nextInt(goldBlocks.size())));
              item.setVelocity(item.getVelocity().multiply(random.nextDouble(1.0, 1.5)));
              item.setInvulnerable(true);
              item.setPickupDelay(Integer.MAX_VALUE);
              item.setGravity(true);
              item.setCustomNameVisible(false);
              Bukkit.getScheduler().runTaskLater(plugin, () -> item.remove(), 15);
            }, random.nextInt(1, 10));
          }
          hologram.delete();
          cancel();
          return;
        }
        if (hologram.getEntityItem().isOnGround() && !grounded) {
          grounded = true;
          entityItem.setGravity(false);
          entityItem.teleport(hologram.getEntityItem().getLocation().add(0, 0.75, 0));
        }
        if (grounded) {
          entityItem.setGravity(true);
          Location newLocation = hologram.getEntityItem().getLocation().clone().add(0, 0.015 * (flyReverse ? -1 : 1), 0);
          entityItem.teleport(newLocation);
          entityItem.setGravity(false);
          if (tick % 60 == 0) {
            flyReverse = !flyReverse;
          }
        }
        ArmorStand stand = hologram.getArmorStands().get(0);
        int modulo = tick < 100 ? 10 : tick > 150 ? 3 : 5;
        if (tick % modulo == 0) {
          String message;
          if (toggle) {
            message = ChatColor.translateAlternateColorCodes('&', "&c&lMONEY POUCH (" + ((200 - tick) / 20) + "s)");
          } else {
            message = ChatColor.translateAlternateColorCodes('&', "&e&lMONEY POUCH (" + ((200 - tick) / 20) + "s)");
          }
          toggle = !toggle;
          stand.setCustomName(message);
        }
        if (tick % 10 == 0) {
          for (int i = 0; i < 2; i++) {
            Item item = hologram.getLocation().getWorld().dropItemNaturally(stand.getLocation().clone().add(0, 0.5, 0), goldBlocks.get(random.nextInt(goldBlocks.size())));
            item.setInvulnerable(true);
            item.setPickupDelay(Integer.MAX_VALUE);
            item.setGravity(true);
            item.setCustomNameVisible(false);
            Bukkit.getScheduler().runTaskLater(plugin, () -> item.remove(), 15);
          }
        }
        stand.teleport(entityItem.getLocation().clone().add(0, -1.25 + 0.27, 0));
        hologram.getArmorStands().get(1).teleport(entityItem.getLocation().clone().add(0, -1.25, 0));
        tick++;
      }
    }.runTaskTimer(plugin, 1, 1);
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      if (!hologram.isDeleted()) {
        hologram.delete();
      }
    }, /* remove after 40 seconds to prevent staying even if arena is finished */ 20 * 40);
  }

}
