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

package plugily.projects.villagedefense.kits.petsfriend;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import plugily.projects.minigamesbox.classic.handlers.language.Message;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.handlers.language.MessageManager;
import plugily.projects.minigamesbox.classic.kits.basekits.PremiumKit;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.minigamesbox.classic.utils.helper.ArmorHelper;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.classic.utils.misc.complement.ComplementAccessor;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.minigamesbox.classic.utils.version.events.api.PlugilyPlayerInteractEvent;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.managers.spawner.gold.NewCreatureUtils;
import plugily.projects.villagedefense.arena.managers.spawner.gold.NewEnemySpawnerManager;
import plugily.projects.villagedefense.arena.villager.CompletionCallback;
import plugily.projects.villagedefense.handlers.hologram.ArmorStandHologram;
import plugily.projects.villagedefense.handlers.upgrade.EntityUpgrade;
import plugily.projects.villagedefense.handlers.upgrade.NewEntityUpgradeManager;
import plugily.projects.villagedefense.kits.ChatDisplayable;
import plugily.projects.villagedefense.kits.KitsMenu;
import plugily.projects.villagedefense.kits.ScoreboardModifiable;
import plugily.projects.villagedefense.kits.ability.AbilitySource;
import plugily.projects.villagedefense.kits.utils.KitHelper;
import plugily.projects.villagedefense.kits.utils.KitSpecifications;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author Plajer
 * <p>
 * Created at 11.08.2023
 */
public class PetsFriendKit extends PremiumKit implements AbilitySource, Listener, ChatDisplayable, ScoreboardModifiable {

  private static final String LANGUAGE_ACCESSOR = "KIT_CONTENT_PETS_FRIEND_";
  private final List<String> petNames = Arrays.asList("Rex", "Buddy", "Max", "Daisy", "Bailey", "Molly", "Lucy", "Sadie", "Bella", "Maggie", "Chloe", "Sophie", "Lola", "Zoe", "Abby", "Ginger", "Roxy", "Gracie", "Coco", "Sasha", "Angel", "Lily", "Emma", "Annie", "Rosie", "Ruby", "Lady", "Missy", "Katie", "Dixie", "Penny", "Heidi", "Sandy", "Misty", "Holly", "Shelby", "Jasmine", "Sugar", "Honey", "Dakota", "Josie", "Samantha", "Brandy", "Mocha", "Pebbles", "Cinnamon", "Bonnie", "Kelsey", "Casey", "Chelsea", "Muffin", "Sheba", "Fiona", "Duchess", "Pumpkin", "Precious", "Baby", "Sassy", "Minnie", "Mimi", "Lulu", "Cleo", "Jewel", "Princess", "Amber");

  public PetsFriendKit() {
    registerMessages();
    setName(new MessageBuilder(LANGUAGE_ACCESSOR + "NAME").asKey().build());
    setKey("PetsFriend");
    List<String> description = getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "DESCRIPTION");
    setDescription(description);
    getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
    getPlugin().getKitRegistry().registerKit(this);
    ((Main) getPlugin()).getKitsMenu().registerKit(KitsMenu.KitCategory.SUPPORT, this);
  }

  @Override
  @SuppressWarnings("UnnecessaryUnicodeEscape")
  public String getChatPrefix() {
    return "\u0045";
  }

  private void registerMessages() {
    MessageManager manager = getPlugin().getMessageManager();
    manager.registerMessage(LANGUAGE_ACCESSOR + "NAME", new Message("Kit.Content.Pets-Friend.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "DESCRIPTION", new Message("Kit.Content.Pets-Friend.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "PASSIVE_WOLF_SPAWNED", new Message("Kit.Content.Pets-Friend.Game-Item.Passive.Wolf-Spawned", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "PASSIVE_WOLF_AGED", new Message("Kit.Content.Pets-Friend.Game-Item.Passive.Wolf-Aged", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "PASSIVE_GOLEM_SPAWNED", new Message("Kit.Content.Pets-Friend.Game-Item.Passive.Golem-Spawned", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "PASSIVE_WOLF_DIED", new Message("Kit.Content.Pets-Friend.Game-Item.Passive.Wolf-Died", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "PASSIVE_GOLEM_DIED", new Message("Kit.Content.Pets-Friend.Game-Item.Passive.Golem-Died", ""));

    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_RECALL_NAME", new Message("Kit.Content.Pets-Friend.Game-Item.Recall.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_RECALL_DESCRIPTION", new Message("Kit.Content.Pets-Friend.Game-Item.Recall.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_RECALL_ACTIVATE", new Message("Kit.Content.Pets-Friend.Game-Item.Recall.Activate", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_RECALL_NO_ALIVE_PETS", new Message("Kit.Content.Pets-Friend.Game-Item.Recall.No-Alive-Pets", ""));

    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_GRAVE_DANGER_NAME", new Message("Kit.Content.Pets-Friend.Game-Item.Grave-Danger.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_GRAVE_DANGER_DESCRIPTION", new Message("Kit.Content.Pets-Friend.Game-Item.Grave-Danger.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_GRAVE_DANGER_ACTIVATE", new Message("Kit.Content.Pets-Friend.Game-Item.Grave-Danger.Activate", ""));
  }

  @Override
  public boolean isUnlockedByPlayer(Player player) {
    return getPlugin().getPermissionsManager().hasPermissionString("KIT_PREMIUM_UNLOCK", player) || player.hasPermission("villagedefense.kit.petsfriend");
  }

  @Override
  public List<String> getScoreboardLines(User user) {
    Arena arena = (Arena) user.getArena();
    int wave = arena.getWave();
    List<String> lines = new ArrayList<>(
      Arrays.asList(
        "",
        "&fVillagers: &a" + arena.getVillagers().size(),
        "&fZombies: &a" + arena.getEnemies().size(),
        "&fOrbs: &a" + user.getStatistic("orbs"),
        "",
        "&e&lABILITIES:",
        ScoreboardModifiable.renderAbilityCooldown(user, "petsfriend_recall", "Recall", wave, KitSpecifications.GameTimeState.EARLY),
        ScoreboardModifiable.renderAbilityCooldown(user, "petsfriend_gravedanger", "Grave Danger", wave, KitSpecifications.GameTimeState.MID),
        "",
        "&e&lPETS:",
        renderPet(user, arena, 1),
        renderPet(user, arena, 2),
        renderPet(user, arena, 3),
        ""
      )
    );
    if (!arena.isFighting()) {
      lines.add(1, "&fNext Wave in &a" + arena.getTimer() + "s");
    }
    return lines;
  }

  private String renderPet(User user, Arena arena, int petNumber) {
    if (arena.getWolves().isEmpty()) {
      return "&a" + petNumber + ". &7-------------";
    }
    int i = 1;
    for (Wolf wolf : arena.getWolves()) {
      if (wolf.getMetadata("VD_OWNER_UUID").get(0).asString().equals(user.getPlayer().getUniqueId().toString())) {
        if (i != petNumber) {
          i++;
          continue;
        }
        if (wolf.getMetadata("VD_PET_DEAD").isEmpty()) {
          return "&a" + petNumber + ". &f" + NewCreatureUtils.getHealthNameTag(wolf);
        } else {
          String customName = wolf.getMetadata(NewEnemySpawnerManager.CREATURE_CUSTOM_NAME_METADATA).get(0).asString();
          return "&a" + petNumber + ". &7&m" + customName + "&r &7&lDead";
        }
      }
    }
    return "&a" + petNumber + ". &7-------------";
  }

  @Override
  public void giveKitItems(Player player) {
    ItemStack itemStack = XMaterial.STONE_SWORD.parseItem();
    ItemMeta meta = itemStack.getItemMeta();
    meta.setUnbreakable(true);
    itemStack.setItemMeta(meta);
    player.getInventory().addItem(itemStack);
    ArmorHelper.setArmor(player, ArmorHelper.ArmorType.LEATHER);

    player.getInventory().setItem(3, new ItemBuilder(new ItemStack(XMaterial.BONE.parseMaterial()))
      .name(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_RECALL_NAME").asKey().build())
      .lore(getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_RECALL_DESCRIPTION"))
      .build());
    player.getInventory().setItem(4, new ItemBuilder(new ItemStack(XMaterial.SHEARS.parseMaterial()))
      .name(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_GRAVE_DANGER_NAME").asKey().build())
      .lore(getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_GRAVE_DANGER_DESCRIPTION"))
      .build());

    player.getInventory().setItem(5, new ItemStack(Material.SADDLE));
    player.getInventory().setItem(8, new ItemStack(XMaterial.COOKED_PORKCHOP.parseMaterial(), 8));
    Arena arena = (Arena) getPlugin().getArenaRegistry().getArena(player);
    if (arena == null) {
      return;
    }
    spawnWolf(arena, arena.getStartLocation(), player);
  }

  private void doGrowWolf(Wolf wolf, Player player) {
    for (int i = 0; i < 12; i++) {
      final int finalI = i;
      Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
        if (finalI % 2 == 0) {
          wolf.setBaby();
        } else {
          wolf.setAdult();
        }
        try {
          ((Main) getPlugin()).getGlowingEntities().setGlowing(wolf, player, finalI % 2 == 0 ? ChatColor.BLUE : ChatColor.GOLD);
          if (finalI == 11) {
            ((Main) getPlugin()).getGlowingEntities().unsetGlowing(wolf, player);
            wolf.getWorld().spawnParticle(Particle.HEART, wolf.getLocation().clone().add(0, 1, 0), 5, 0.5, 0.25, 0.5, 0);
            wolf.getWorld().playSound(wolf.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

            wolf.setAdult();
            wolf.setCustomName(NewCreatureUtils.getHealthNameTag(wolf));
          }
        } catch (Exception ignored) {
        }
      }, i * 5);
    }
  }

  @Override
  public Material getMaterial() {
    return Material.BONE;
  }

  @Override
  public void reStock(Player player) {
    Arena arena = (Arena) getPlugin().getArenaRegistry().getArena(player);
    KitSpecifications.GameTimeState timeState = KitSpecifications.getTimeState(arena);
    int wolfCount = (int) arena.getWolves()
      .stream()
      .map(wolf -> UUID.fromString(wolf.getMetadata("VD_OWNER_UUID").get(0).asString()))
      .filter(uuid -> player.getUniqueId().equals(uuid))
      .count();
    int wave = arena.getWave();
    if (wave == KitSpecifications.GameTimeState.EARLY.getStartWave() + 5
      || wave == KitSpecifications.GameTimeState.MID.getStartWave() + 4
      || wave == KitSpecifications.GameTimeState.LATE.getStartWave() + 3) {
      for (Wolf wolf : arena.getWolves()) {
        if (wolf.isAdult()) {
          continue;
        }
        if (wolf.getMetadata("VD_OWNER_UUID").get(0).asString().equals(player.getUniqueId().toString())) {
          createPathfinderToPlayer(wolf, player, () -> {
            doGrowWolf(wolf, player);
          });
        }
        new MessageBuilder(LANGUAGE_ACCESSOR + "PASSIVE_WOLF_AGED").asKey().player(player).sendPlayer();
        break;
      }
    }
    if (timeState == KitSpecifications.GameTimeState.MID) {
      if (wolfCount == 1) {
        new MessageBuilder(LANGUAGE_ACCESSOR + "PASSIVE_WOLF_SPAWNED").player(player).prefix().sendPlayer();
        spawnWolf(arena, player.getLocation(), player);
      }
      int golemCount = (int) arena.getIronGolems()
        .stream()
        .map(golem -> UUID.fromString(golem.getMetadata("VD_OWNER_UUID").get(0).asString()))
        .filter(uuid -> player.getUniqueId().equals(uuid))
        .count();
      if (golemCount == 0) {
        new MessageBuilder(LANGUAGE_ACCESSOR + "PASSIVE_GOLEM_SPAWNED").player(player).prefix().sendPlayer();
        arena.spawnGolemForce(player.getLocation(), player);
      }
    } else if (timeState == KitSpecifications.GameTimeState.LATE) {
      if (wolfCount == 2) {
        new MessageBuilder(LANGUAGE_ACCESSOR + "PASSIVE_WOLF_SPAWNED").player(player).prefix().sendPlayer();
        spawnWolf(arena, player.getLocation(), player);
      }
    }
  }

  private Wolf spawnWolf(Arena arena, Location location, Player player) {
    Wolf wolf = NewCreatureUtils.spawnWolf(location);
    wolf.setMetadata("VD_OWNER_UUID", new FixedMetadataValue(getPlugin(), player.getUniqueId().toString()));
    wolf.setMetadata("VD_ALIVE_SINCE_WAVE", new FixedMetadataValue(getPlugin(), arena.getWave()));
    String name = petNames.get(ThreadLocalRandom.current().nextInt(petNames.size()));
    wolf.setMetadata(NewEnemySpawnerManager.CREATURE_CUSTOM_NAME_METADATA, new FixedMetadataValue(getPlugin(), name));
    wolf.setOwner(player);
    wolf.setCustomNameVisible(true);
    wolf.setBaby();
    wolf.setAgeLock(true);
    wolf.setCollarColor(DyeColor.values()[ThreadLocalRandom.current().nextInt(DyeColor.values().length)]);
    wolf.setCustomName(NewCreatureUtils.getHealthNameTag(wolf));
    arena.addWolf(wolf);
    return wolf;
  }

  @EventHandler
  public void onPetDeath(EntityDamageEvent event) {
    if (event.getEntityType() != EntityType.IRON_GOLEM && event.getEntityType() != EntityType.WOLF) {
      return;
    }
    if (!(event.getEntity() instanceof Creature creature)) {
      return;
    }
    if (creature.getHealth() - event.getDamage() > 0) {
      return;
    }

    for (Arena arena : ((Main) getPlugin()).getArenaRegistry().getPluginArenas()) {
      boolean contains = false;
      if (arena.getIronGolems().contains(creature)) {
        Player player = Bukkit.getPlayer(UUID.fromString(creature.getMetadata("VD_OWNER_UUID").get(0).asString()));
        new MessageBuilder(LANGUAGE_ACCESSOR + "PASSIVE_GOLEM_DIED").asKey().player(player).sendPlayer();
        contains = true;
      } else if (arena.getWolves().contains(creature)) {
        Player owner = Bukkit.getPlayer(UUID.fromString(creature.getMetadata("VD_OWNER_UUID").get(0).asString()));
        new MessageBuilder(LANGUAGE_ACCESSOR + "PASSIVE_WOLF_DIED").asKey().player(owner).sendPlayer();
        contains = true;
      }
      if (contains) {
        NewCreatureUtils.doMarkPetDead(creature);
        for (Creature enemy : arena.getEnemies()) {
          if (creature.equals(enemy.getTarget())) {
            enemy.setTarget(null);
          }
        }
        event.setCancelled(true);
        event.setDamage(0);
        return;
      }
    }
  }

  @Override
  @EventHandler
  public void onAbilityCast(PlugilyPlayerInteractEvent event) {
    if (!KitHelper.isInGameWithKitAndItemInHand(event.getPlayer(), PetsFriendKit.class)) {
      return;
    }
    ItemStack stack = VersionUtils.getItemInHand(event.getPlayer());
    User user = getPlugin().getUserManager().getUser(event.getPlayer());
    String displayName = ComplementAccessor.getComplement().getDisplayName(stack.getItemMeta());
    if (displayName.equals(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_RECALL_NAME").asKey().build())) {
      onRecall(stack, user);
    } else if (displayName.equals(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_GRAVE_DANGER_NAME").asKey().build())) {
      onGraveDangerCast(stack, user);
    }
  }

  private void onRecall(ItemStack stack, User user) {
    Arena arena = ((Arena) user.getArena());
    List<Creature> pets = getAllAlivePets(arena, user);
    if (pets.isEmpty()) {
      new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_RECALL_NO_ALIVE_PETS").asKey().send(user.getPlayer());
      return;
    }
    String abilityId = "petsfriend_recall";
    if (!user.checkCanCastCooldownAndMessage(abilityId)) {
      return;
    }
    int cooldown = 10;
    user.setCooldown(abilityId, cooldown);

    Player player = user.getPlayer();
    KitHelper.scheduleAbilityCooldown(stack, player, 5, cooldown);
    new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_RECALL_ACTIVATE").asKey().send(user.getPlayer());
    for (Creature pet : pets) {
      pet.setTarget(null);
      createPathfinderToPlayer(pet, player, () -> {
      });
    }
  }

  private void createPathfinderToPlayer(Creature pet, Player player, CompletionCallback completionCallback) {
    new BukkitRunnable() {
      @Override
      public void run() {
        if (pet.isDead()) {
          this.cancel();
          return;
        }
        if (pet.getLocation().distanceSquared(player.getLocation()) <= 2) {
          completionCallback.onComplete();
          this.cancel();
          return;
        }
        pet.getPathfinder().moveTo(player, 1.75);
      }
    }.runTaskTimer(getPlugin(), 0, 5);
  }

  private void onGraveDangerCast(ItemStack stack, User user) {
    String abilityId = "petsfriend_gravedanger";
    if (!user.checkCanCastCooldownAndMessage(abilityId)) {
      return;
    }
    if (KitSpecifications.getTimeState((Arena) user.getArena()) == KitSpecifications.GameTimeState.EARLY) {
      new MessageBuilder("KIT_LOCKED_TILL").asKey().integer(16).send(user.getPlayer());
      return;
    }
    int cooldown = 140;
    user.setCooldown(abilityId, cooldown);
    VersionUtils.setMaterialCooldown(user.getPlayer(), stack.getType(), cooldown * 20);

    new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_GRAVE_DANGER_ACTIVATE").prefix().player(user.getPlayer()).sendPlayer();
    Arena arena = (Arena) user.getArena();
    List<Creature> pets = getAllAlivePets(arena, user);
    for (Creature pet : pets) {
      if (pet instanceof Wolf) {
        Wolf newWolf = spawnWolf(arena, pet.getLocation(), user.getPlayer());
        copyAllUpgrades(pet, newWolf, user.getPlayer());
        schedulePetRemoval(newWolf, arena);
        continue;
      }
      Creature newGolem = arena.spawnGolemForce(pet.getLocation(), user.getPlayer());
      copyAllUpgrades(pet, newGolem, user.getPlayer());
      schedulePetRemoval(newGolem, arena);
    }
  }

  private List<Creature> getAllAlivePets(Arena arena, User user) {
    List<Creature> pets = arena.getWolves()
      .stream()
      .filter(w -> w.getMetadata("VD_OWNER_UUID").get(0).asString().equals(user.getPlayer().getUniqueId().toString()))
      .collect(Collectors.toList());
    pets.addAll(
      arena.getIronGolems()
        .stream()
        .filter(w -> w.getMetadata("VD_OWNER_UUID").get(0).asString().equals(user.getPlayer().getUniqueId().toString()))
        .collect(Collectors.toList())
    );
    return pets.stream()
      .filter(pet -> !pet.hasMetadata("VD_PET_DEAD"))
      .collect(Collectors.toList());
  }

  private void copyAllUpgrades(Creature origin, Creature target, Player owner) {
    NewEntityUpgradeManager upgradeManager = ((Main) getPlugin()).getEntityUpgradeManager();
    for (EntityUpgrade upgrade : upgradeManager.getRegisteredUpgrades()) {
      if (!origin.hasMetadata(upgrade.getMetadataKey())) {
        continue;
      }
      upgradeManager.applyUpgradeSilent(target, owner, upgrade);
    }
  }

  private void schedulePetRemoval(Creature pet, Arena arena) {
    ArmorStandHologram petHologram = new ArmorStandHologram(pet.getLocation())
      .appendLine(ChatColor.translateAlternateColorCodes('&', "&e&lCLONED PET"));
    pet.getWorld().playSound(pet.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 0.75f);

    new BukkitRunnable() {
      int tick = 0;
      int castTime = (int) Settings.ULTIMATE_GRAVE_DANGER_CAST_TIME.getForArenaState(arena);
      boolean toggle = false;

      @Override
      public void run() {
        if (tick == castTime * 20 || petHologram.isDeleted() || petHologram.getArmorStands().isEmpty()) {
          World world = pet.getLocation().getWorld();
          world.spawnParticle(Particle.EXPLOSION_LARGE, pet.getLocation().clone().add(0, 0.5, 0), 1);
          world.playSound(pet.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 0.5f);
          if (pet instanceof Wolf wolf) {
            arena.removeWolf(wolf);
          } else {
            arena.removeIronGolem((IronGolem) pet);
          }
          petHologram.delete();
          cancel();
          return;
        }
        ArmorStand stand = petHologram.getArmorStands().get(0);
        if (pet instanceof Wolf) {
          stand.teleport(pet.getLocation().clone().add(0, 0.5, 0));
        } else {
          stand.teleport(pet.getLocation().clone().add(0, 1.1, 0));
        }
        //postpone countdown until wave starts but still teleport moving holograms
        if (!arena.isFighting()) {
          return;
        }
        //todo improve modulo here?
        int modulo = tick < 100 ? 10 : tick > 350 ? 3 : 5;
        if (tick % modulo == 0) {
          String message;
          if (toggle) {
            message = ChatColor.translateAlternateColorCodes('&', "&c&lCLONED PET (" + ((castTime - tick) / 20) + "s)");
          } else {
            message = ChatColor.translateAlternateColorCodes('&', "&e&lCLONED PET (" + ((castTime - tick) / 20) + "s)");
          }
          toggle = !toggle;
          stand.setCustomName(message);
        }
        tick++;
      }
    }.runTaskTimer(getPlugin(), 1, 1);
    Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
      if (!petHologram.isDeleted()) {
        petHologram.delete();
      }
    }, /* remove after 20 seconds to prevent staying even if arena is finished */ 20 * 20);
  }

  private enum Settings {
    ULTIMATE_GRAVE_DANGER_CAST_TIME(0, 10, 15);

    private final double earlyValue;
    private final double midValue;
    private final double lateValue;

    Settings(double earlyValue, double midValue, double lateValue) {
      this.earlyValue = earlyValue;
      this.midValue = midValue;
      this.lateValue = lateValue;
    }

    public double getForArenaState(Arena arena) {
      switch (KitSpecifications.getTimeState(arena)) {
        case LATE:
          return lateValue;
        case MID:
          return midValue;
        case EARLY:
        default:
          return earlyValue;
      }
    }
  }

}
