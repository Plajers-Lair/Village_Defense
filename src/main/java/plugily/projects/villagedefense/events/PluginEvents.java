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

package plugily.projects.villagedefense.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import plugily.projects.minigamesbox.classic.api.event.player.PlugilyPlayerChooseKitEvent;
import plugily.projects.minigamesbox.classic.arena.ArenaState;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.kits.basekits.Kit;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.minigamesbox.classic.utils.version.events.api.PlugilyPlayerInteractEntityEvent;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XSound;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.api.event.game.VillageGameSecretWellEvent;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.creatures.CreatureUtils;
import plugily.projects.villagedefense.kits.BuilderKit;
import plugily.projects.villagedefense.kits.CleanerKit;
import plugily.projects.villagedefense.kits.CrusaderKit;
import plugily.projects.villagedefense.kits.MedicKit;
import plugily.projects.villagedefense.kits.PetsFriend;
import plugily.projects.villagedefense.kits.ShotBowKit;
import plugily.projects.villagedefense.kits.TerminatorKit;
import plugily.projects.villagedefense.kits.TornadoKit;
import plugily.projects.villagedefense.kits.WizardKit;
import plugily.projects.villagedefense.utils.Utils;

import java.util.UUID;

/**
 * Created by Tom on 16/08/2014.
 */
public class PluginEvents implements Listener {

  private final Main plugin;

  public PluginEvents(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onConsumeClearBottle(PlayerItemConsumeEvent event) {
    if (!plugin.getArenaRegistry().isInArena(event.getPlayer())) {
      return;
    }
    //Quality of Life - remove empty glass bottles on potion consume
    event.getPlayer().getInventory().remove(Material.GLASS_BOTTLE);
  }

  @EventHandler
  public void onKitSelectSound(PlugilyPlayerChooseKitEvent event) {
    XSound.UI_BUTTON_CLICK.play(event.getPlayer());
    playKitSound(event.getKit(), event.getPlayer());
  }

  private void playKitSound(Kit kit, Player player) {
    //knight kit omitted
    if (kit instanceof BuilderKit) {
      player.playSound(player, Sound.BLOCK_WOODEN_DOOR_OPEN, 1, 0.75f);
    } else if (kit instanceof CleanerKit) {
      player.playSound(player, Sound.BLOCK_LAVA_EXTINGUISH, 1, 1.5f);
    } else if (kit instanceof CrusaderKit) {
      player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_0, 1, 1);
    } else if (kit instanceof MedicKit) {
      player.playSound(player, Sound.ENTITY_VILLAGER_CELEBRATE, 1, 1.25f);
    } else if (kit instanceof PetsFriend) {
      player.playSound(player, Sound.ENTITY_WOLF_WHINE, 1, 1.15f);
    } else if (kit instanceof ShotBowKit) {
      player.playSound(player, Sound.ENTITY_ARROW_HIT, 1, 0.75f);
    } else if (kit instanceof TerminatorKit) {
      player.playSound(player, Sound.ENTITY_WITHER_DEATH, 1, 2.0f);
    } else if (kit instanceof TornadoKit) {
      player.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 0.25f);
    } else if (kit instanceof WizardKit) {
      player.playSound(player, Sound.AMBIENT_CAVE, 1, 2.0f);
    }
  }

  @EventHandler
  public void onEntityInteractEntity(PlugilyPlayerInteractEntityEvent event) {
    if(VersionUtils.checkOffHand(event.getHand())) {
      return;
    }

    Arena arena = plugin.getArenaRegistry().getArena(event.getPlayer());
    if(arena == null) {
      return;
    }

    if(plugin.getUserManager().getUser(event.getPlayer()).isSpectator()) {
      event.setCancelled(true);
      return;
    }
    if(VersionUtils.getItemInHand(event.getPlayer()).getType() == Material.SADDLE) {
      if(event.getRightClicked().getType() == EntityType.IRON_GOLEM || event.getRightClicked().getType() == EntityType.VILLAGER || event.getRightClicked().getType() == EntityType.WOLF) {
        VersionUtils.setPassenger(event.getRightClicked(), event.getPlayer());
        event.setCancelled(true);
        return;
      }
    }
    Entity target = event.getRightClicked();
    if(target.getType() == EntityType.VILLAGER) {
      event.setCancelled(true);
      arena.getShopManager().openShop(event.getPlayer(), (Villager) target);
    } else if(target.getType() == EntityType.IRON_GOLEM || target.getType() == EntityType.WOLF) {
      if(target.getType() == EntityType.WOLF) {
        Wolf wolf = (Wolf) event.getRightClicked();
        Bukkit.getScheduler().runTask(plugin, () -> wolf.setSitting(false));
      }
      if(event.getPlayer().isSneaking()) {
        return;
      }
      if(!target.hasMetadata("VD_OWNER_UUID")
        || event.getPlayer().getUniqueId().equals(UUID.fromString(target.getMetadata("VD_OWNER_UUID").get(0).asString()))) {
        VersionUtils.setPassenger(event.getRightClicked(), event.getPlayer());
        return;
      }
      new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_WAVE_ENTITIES_CANT_RIDE_OTHER").asKey().player(event.getPlayer()).sendPlayer();
    }
  }

  @EventHandler
  public void onDoorDrop(ItemSpawnEvent event) {
    if(event.getEntity().getItemStack().getType() == Utils.getCachedDoor(event.getLocation().getBlock())) {
      for(Entity entity : plugin.getBukkitHelper().getNearbyEntities(event.getLocation(), 20)) {
        if(entity instanceof Player && plugin.getArenaRegistry().getArena((Player) entity) != null) {
          event.getEntity().remove();
        }
      }
    }
  }

  @EventHandler
  public void onDrop(PlayerDropItemEvent event) {
    if(plugin.getArenaRegistry().isInArena(event.getPlayer()) && event.getItemDrop().getItemStack().getType() == Material.SADDLE) {
      event.setCancelled(true);
    }
  }


  @EventHandler
  public void onItemMove(InventoryClickEvent event) {
    if(event.getWhoClicked() instanceof Player && plugin.getArenaRegistry().isInArena((Player) event.getWhoClicked())) {
      if(plugin.getArenaRegistry().getArena(((Player) event.getWhoClicked())).getArenaState() != ArenaState.IN_GAME) {
        if(event.getClickedInventory() == event.getWhoClicked().getInventory()) {
          if(event.getView().getType() == InventoryType.CRAFTING || event.getView().getType() == InventoryType.PLAYER) {
            event.setResult(Event.Result.DENY);
          }
        }
      }
    }
  }


  @EventHandler
  public void onEntityCombust(EntityCombustByEntityEvent event) {
    if(!(event.getCombuster() instanceof Projectile)) {
      return;
    }
    Projectile projectile = (Projectile) event.getCombuster();
    if(!(projectile.getShooter() instanceof Player)) {
      return;
    }
    if(event.getEntity() instanceof Player) {
      Arena arena = plugin.getArenaRegistry().getArena((Player) projectile.getShooter());
      if(arena != null && arena.equals(plugin.getArenaRegistry().getArena((Player) event.getEntity()))) {
        event.setCancelled(true);
      }
    } else if(event.getEntity() instanceof IronGolem || event.getEntity() instanceof Villager || event.getEntity() instanceof Wolf) {
      for(Arena a : plugin.getArenaRegistry().getPluginArenas()) {
        if(a.getWolves().contains(event.getEntity()) || a.getVillagers().contains(event.getEntity()) || a.getIronGolems().contains(event.getEntity())) {
          event.setCancelled(true);
          return;
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onFriendHurt(EntityDamageByEntityEvent event) {
    if(!(event.getDamager() instanceof Player) || plugin.getArenaRegistry().getArena((Player) event.getDamager()) == null) {
      return;
    }
    if(plugin.getUserManager().getUser((Player) event.getDamager()).isSpectator()) {
      event.setCancelled(true);
      return;
    }
    if(!(event.getEntity() instanceof Player || event.getEntity() instanceof Wolf || event.getEntity() instanceof IronGolem || event.getEntity() instanceof Villager)) {
      return;
    }
    event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onCreatureHurt(EntityDamageEvent event) {
    if(!(event.getEntity() instanceof Creature)) {
      return;
    }
    if (event.isCancelled()) {
      return;
    }
    for(Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if(!arena.getEnemies().contains(event.getEntity())) {
        continue;
      }
      event.setCancelled(false);
      Creature creature = (Creature) event.getEntity();
      creature.setCustomName(CreatureUtils.getHealthNameTagPreDamage(creature, event.getFinalDamage()));
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onAssistApply(EntityDamageByEntityEvent event) {
    if(!(event.getEntity() instanceof Creature) || !(event.getDamager() instanceof LivingEntity)) {
      return;
    }
    for(Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if(!arena.getEnemies().contains(event.getEntity())) {
        continue;
      }
      arena.getAssistHandler().doRegisterDamageOnEnemy((LivingEntity) event.getDamager(), (Creature) event.getEntity(), event.getFinalDamage());
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onSecond(EntityDamageByEntityEvent event) {
    if(!(event.getDamager() instanceof Projectile)) {
      return;
    }
    Projectile projectile = (Projectile) event.getDamager();
    if(!(projectile.getShooter() instanceof Player)) {
      return;
    }
    if(plugin.getArenaRegistry().getArena((Player) projectile.getShooter()) == null || !(event.getEntity() instanceof Player || event.getEntity() instanceof Wolf
      || event.getEntity() instanceof IronGolem || event.getEntity() instanceof Villager)) {
      return;
    }
    event.setCancelled(true);
  }

  @EventHandler
  public void onEntityLeash(PlayerLeashEntityEvent event) {
    if(event.getEntity() instanceof Villager) {
      ((Villager) event.getEntity()).setLeashHolder(event.getPlayer());
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onBlockBreakEvent(BlockBreakEvent event) {
    if(plugin.getArenaRegistry().isInArena(event.getPlayer())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onBuild(BlockPlaceEvent event) {
    if(plugin.getArenaRegistry().isInArena(event.getPlayer()) && event.getBlock().getType() != Utils.getCachedDoor(event.getBlock())) {
      event.setCancelled(true);
    }
  }


  @EventHandler
  public void onSecretWellDrop(InventoryPickupItemEvent event) {
    if(event.getInventory().getType() != InventoryType.HOPPER) {
      return;
    }

    Item item = event.getItem();
    Location location = item.getLocation();

    Arena currentArena = null;
    for(Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if(location.getWorld() == arena.getStartLocation().getWorld()) {
        currentArena = arena;
        item.remove();
        event.setCancelled(true);
        event.getInventory().clear();
        break;
      }
    }

    if(currentArena == null) {
      return;
    }

    ItemStack itemStack = item.getItemStack();

    VillageGameSecretWellEvent villageGameSecretWellEvent = new VillageGameSecretWellEvent(currentArena, itemStack, location);
    Bukkit.getPluginManager().callEvent(villageGameSecretWellEvent);
    if(villageGameSecretWellEvent.isCancelled()) {
      return;
    }

    if(itemStack.getType() == Material.ROTTEN_FLESH) {
      for(Entity entity : plugin.getBukkitHelper().getNearbyEntities(location, 20)) {
        if(!(entity instanceof Player)) {
          continue;
        }
        Arena arena = plugin.getArenaRegistry().getArena((Player) entity);
        if(arena == null) {
          continue;
        }
        arena.changeArenaOptionBy("ROTTEN_FLESH_AMOUNT", itemStack.getAmount());
        VersionUtils.sendParticles("CLOUD", arena.getPlayers(), location, 50, 2, 2, 2);
        if(!arena.checkLevelUpRottenFlesh() || arena.getArenaOption("ROTTEN_FLESH_LEVEL") >= 30) {
          return;
        }
        for(Player player : arena.getPlayers()) {
          player.setHealth(VersionUtils.getMaxHealth(player));
          VersionUtils.setMaxHealth(player, VersionUtils.getMaxHealth(player) + 2.0);
          new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_ROTTEN_FLESH_LEVEL_UP").asKey().player(player).sendPlayer();
        }
      }
    }
  }

  /**
   * Triggers when something combusts in the world.
   * Thanks to @HomieDion for part of this class!
   */
  @EventHandler(ignoreCancelled = true)
  public void onCombust(EntityCombustEvent event) {
    // Ignore if this is caused by an event lower down the chain.
    if(event instanceof EntityCombustByEntityEvent || event instanceof EntityCombustByBlockEvent
      || !(event.getEntity() instanceof Creature)
      || event.getEntity().getWorld().getEnvironment() != World.Environment.NORMAL) {
      return;
    }

    for(Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if (arena.getEnemies().contains(event.getEntity()) || arena.getSpecialEntities().contains(event.getEntity())) {
        event.setCancelled(true);
        break;
      }
    }
  }


}
