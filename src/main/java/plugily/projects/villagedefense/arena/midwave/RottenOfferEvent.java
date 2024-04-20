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

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.utils.hologram.ArmorStandHologram;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.creatures.CreatureUtils;
import plugily.projects.villagedefense.creatures.v1_9_UP.CustomCreature;

import java.util.Map;

public class RottenOfferEvent implements MidWaveEvent, Listener {

  public static final String ROTTEN_SALE_METADATA = "VD_TRADER_ROTTEN_SALE";
  private final Main plugin;

  public RottenOfferEvent(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @Override
  public boolean canTrigger(Arena arena) {
    return arena.getWave() % 7 == 0;
  }

  @Override
  public void initiate(Arena arena) {
    LivingEntity target = (LivingEntity) arena.getStartLocation().getWorld().spawnEntity(arena.getStartLocation(), EntityType.WANDERING_TRADER);
    String name = CreatureUtils.getRandomVillagerName();
    target.setCustomName(name);
    target.setCustomNameVisible(true);
    target.setMetadata(CustomCreature.CREATURE_CUSTOM_NAME_METADATA, new FixedMetadataValue(plugin, name));
    target.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(1.0);
    target.setGlowing(true);

    String offerMessage = new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_VILLAGER_ROTTEN_OFFER").asKey().build();
    ArmorStandHologram hologram = new ArmorStandHologram(target.getLocation().add(0, 0.25, 0), offerMessage);
    new BukkitRunnable() {
      @Override
      public void run() {
        if (!target.hasMetadata(ROTTEN_SALE_METADATA)) {
          hologram.delete();
          target.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getLocation().clone().add(0, 0.5, 0), 1);
          target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, .5f);
          target.remove();
          cancel();
          return;
        }
        ArmorStand stand = hologram.getArmorStands().get(0);
        stand.teleport(target.getLocation().add(0, 0.25, 0));
      }
    }.runTaskTimer(plugin, 0, 1);
    target.setMetadata(ROTTEN_SALE_METADATA, new FixedMetadataValue(plugin, true));
    arena.addSpecialEntity(target);
    new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_VILLAGER_ROTTEN_SALE")
      .asKey()
      .value(ChatColor.translateAlternateColorCodes('&', "&c&lROTTEN FLESH"))
      .arena(arena)
      .sendArena();
    for (Player player : arena.getPlayers()) {
      player.playSound(player, Sound.ENTITY_WANDERING_TRADER_TRADE, 1, 1.15f);
    }
  }

  @Override
  public void cleanup(Arena arena) {
    for (LivingEntity livingEntity : arena.getSpecialEntities()) {
      if (livingEntity.hasMetadata(ROTTEN_SALE_METADATA)) {
        livingEntity.removeMetadata(ROTTEN_SALE_METADATA, plugin);
      }
    }
  }

  @EventHandler
  public void onTraderDamage(EntityDamageByEntityEvent event) {
    if (!event.getEntity().hasMetadata(ROTTEN_SALE_METADATA)) {
      return;
    }
    //cancel no matter what
    event.setCancelled(true);
  }

  @EventHandler
  public void onInteract(PlayerInteractEntityEvent event) {
    if (!event.getRightClicked().hasMetadata(ROTTEN_SALE_METADATA)) {
      return;
    }
    Player player = event.getPlayer();
    if (!plugin.getArenaRegistry().isInArena(player)) {
      return;
    }
    event.setCancelled(true);
    if (!player.getInventory().contains(Material.ROTTEN_FLESH)) {
      player.playSound(player, Sound.ENTITY_WANDERING_TRADER_NO, 1, 1);
      return;
    }
    int count = 0;
    for (Map.Entry<Integer, ? extends ItemStack> entry : player.getInventory().all(Material.ROTTEN_FLESH).entrySet()) {
      count += entry.getValue().getAmount();
    }
    player.getInventory().remove(Material.ROTTEN_FLESH);
    Arena arena = plugin.getArenaRegistry().getArena(player);
    arena.changeArenaOptionBy("ROTTEN_FLESH_AMOUNT", count);
    int orbs = (int) (count * 1.45);
    plugin.getUserManager().getUser(player).adjustStatistic(plugin.getStatsStorage().getStatisticType("ORBS"), orbs);
    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a+" + orbs + " &7orbs"));
    player.playSound(player, Sound.ENTITY_WANDERING_TRADER_TRADE, 1, 1);
    if (!arena.checkLevelUpRottenFlesh() || arena.getArenaOption("ROTTEN_FLESH_LEVEL") >= 30) {
      return;
    }
    for (Player arenaPlayer : arena.getPlayers()) {
      arenaPlayer.setHealth(VersionUtils.getMaxHealth(player));
      VersionUtils.setMaxHealth(player, VersionUtils.getMaxHealth(player) + 2.0);
      new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_ROTTEN_FLESH_LEVEL_UP").asKey().player(arenaPlayer).sendPlayer();
    }
  }

}
