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

package plugily.projects.villagedefense.arena.midwave;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.utils.hologram.ArmorStandHologram;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;

import java.util.concurrent.ThreadLocalRandom;

public class PinataZombieEvent implements MidWaveEvent, Listener {

  public static final String PINATA_METADATA = "VD_PINATA_EVENT";
  private final Main plugin;

  public PinataZombieEvent(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @Override
  public boolean canTrigger(Arena arena) {
    return arena.getWave() % 11 == 0;
  }

  @Override
  public void initiate(Arena arena) {
    LivingEntity target = (LivingEntity) arena.getStartLocation().getWorld().spawnEntity(arena.getStartLocation(), EntityType.ZOMBIE_VILLAGER);
    target.setGlowing(true);
    target.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1);
    target.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100.0);
    ((Ageable) target).setAdult();
    target.setMetadata(PINATA_METADATA, new FixedMetadataValue(plugin, true));

    ArmorStandHologram hologram = new ArmorStandHologram(target.getLocation().add(0, 0.25, 0),
      ChatColor.translateAlternateColorCodes('&', "&e&lPUNCH ME"));
    new BukkitRunnable() {
      boolean toggle = false;
      int ticks = 0;
      int color = 0;

      @Override
      public void run() {
        if (!target.hasMetadata(PINATA_METADATA) || target.isDead()) {
          hologram.delete();
          target.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getLocation().clone().add(0, 0.5, 0), 1);
          target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 1, 0.5f);
          for (int i = 0; i < 8; i++) {
            dropNaturalItem(hologram.getLocation(), XMaterial.ROTTEN_FLESH.parseItem());
          }
          target.remove();
          cancel();
          return;
        }
        ArmorStand stand = hologram.getArmorStands().get(0);
        stand.teleport(target.getLocation().add(0, 0.25, 0));
        if (ticks % 5 == 0) {
          for (Player player : arena.getPlayers()) {
            try {
              plugin.getGlowingEntities().setGlowing(target, player, getNextColor(color));
            } catch (Exception ignored) {
            }
            color = (color + 1) % 7;
          }
        }
        if (ticks % 10 == 0) {
          String message;
          if (toggle) {
            message = ChatColor.translateAlternateColorCodes('&', "&c&lPUNCH ME");
          } else {
            message = ChatColor.translateAlternateColorCodes('&', "&e&lPUNCH ME");
          }
          toggle = !toggle;
          stand.setCustomName(message);
        }
        ticks++;
      }
    }.runTaskTimer(plugin, 0, 1);
    arena.getSpecialEntities().add(target);
    new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_VILLAGER_PINATA_EVENT")
      .asKey()
      .arena(arena)
      .sendArena();
    for (Player player : arena.getPlayers()) {
      player.playSound(player, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 0.5f, 2f);
    }
  }

  @Override
  public void cleanup(Arena arena) {
    for (LivingEntity livingEntity : arena.getSpecialEntities()) {
      if (livingEntity.hasMetadata(PINATA_METADATA)) {
        livingEntity.removeMetadata(PINATA_METADATA, plugin);
      }
    }
  }

  @EventHandler
  public void onPinataFocus(EntityTargetLivingEntityEvent event) {
    if (!(event.getTarget() instanceof ZombieVillager)) {
      return;
    }
    for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if (!arena.getSpecialEntities().contains(event.getTarget())) {
        continue;
      }
      if (!event.getTarget().hasMetadata(PINATA_METADATA)) {
        continue;
      }
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPinataDamage(EntityDamageByEntityEvent event) {
    //cancel all pinata damages
    if (event.getDamager().hasMetadata(PINATA_METADATA)) {
      event.setCancelled(true);
    }
    //drop rewards on pinata punch
    if (event.getDamager() instanceof Player player && event.getEntity().hasMetadata(PINATA_METADATA)) {
      if (!plugin.getArenaRegistry().isInArena(player)) {
        return;
      }
      Vector velocity = event.getEntity().getLocation().getDirection().multiply(-1).normalize().multiply(2.0);
      event.getEntity().setVelocity(velocity);
      //60% chance of drop
      if (ThreadLocalRandom.current().nextInt(0, 100) >= 40) {
        doCustomDrop(player, event.getDamager().getLocation());
      }
      event.setDamage(1.0);
    }
  }

  private void dropNaturalItem(Location location, ItemStack itemStack) {
    Item item = location.getWorld().dropItemNaturally(location.clone().add(0, 0.5, 0), itemStack);
    item.setInvulnerable(true);
    item.setPickupDelay(Integer.MAX_VALUE);
    item.setGravity(true);
    item.setCustomNameVisible(false);
    Bukkit.getScheduler().runTaskLater(plugin, () -> item.remove(), 15);
  }

  private ChatColor getNextColor(int color) {
    return switch (color) {
      case 0 -> ChatColor.WHITE;
      case 1 -> ChatColor.RED;
      case 2 -> ChatColor.GOLD;
      case 3 -> ChatColor.DARK_RED;
      case 4 -> ChatColor.BLUE;
      case 5 -> ChatColor.GREEN;
      case 6 -> ChatColor.DARK_PURPLE;
      default -> ChatColor.BLACK;
    };
  }

  private void doCustomDrop(Player player, Location location) {
    if (ThreadLocalRandom.current().nextBoolean()) {
      int rand = ThreadLocalRandom.current().nextInt(4, 12);
      plugin.getUserManager().getUser(player).adjustStatistic(plugin.getStatsStorage().getStatisticType("ORBS"), rand);
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a+" + rand + " &7orbs"));
    } else {
      int amount = ThreadLocalRandom.current().nextInt(1, 3);
      for (int i = 0; i < amount; i++) {
        dropNaturalItem(location, XMaterial.ROTTEN_FLESH.parseItem());
      }
      player.getInventory().addItem(new ItemStack(Material.ROTTEN_FLESH, amount));
    }
  }

}
