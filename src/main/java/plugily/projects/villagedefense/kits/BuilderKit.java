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

package plugily.projects.villagedefense.kits;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import plugily.projects.minigamesbox.classic.handlers.language.Message;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.handlers.language.MessageManager;
import plugily.projects.minigamesbox.classic.kits.basekits.PremiumKit;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.minigamesbox.classic.utils.helper.ArmorHelper;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.classic.utils.helper.MaterialUtils;
import plugily.projects.minigamesbox.classic.utils.helper.WeaponHelper;
import plugily.projects.minigamesbox.classic.utils.misc.complement.ComplementAccessor;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.minigamesbox.classic.utils.version.events.api.PlugilyPlayerInteractEvent;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XParticle;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XSound;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.kits.ability.AbilitySource;
import plugily.projects.villagedefense.kits.utils.KitHelper;
import plugily.projects.villagedefense.kits.utils.KitSpecifications;
import plugily.projects.villagedefense.utils.ProtocolUtils;
import plugily.projects.villagedefense.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Plajer
 * <p>
 * Created at 11.08.2023
 */
public class BuilderKit extends PremiumKit implements AbilitySource, Listener, ChatDisplayable {

  private static final String LANGUAGE_ACCESSOR = "KIT_CONTENT_BUILDER_";
  private final Map<Arena, Player> knockbackResistantArenas = new HashMap<>();

  public BuilderKit() {
    registerMessages();
    setName(new MessageBuilder(LANGUAGE_ACCESSOR + "NAME").asKey().build());
    setKey("Builder");
    List<String> description = getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "DESCRIPTION");
    setDescription(description);
    getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
    getPlugin().getKitRegistry().registerKit(this);
    ((Main) getPlugin()).getKitsMenu().registerKit(KitsMenu.KitCategory.SUPPORT, this);
  }

  @Override
  @SuppressWarnings("UnnecessaryUnicodeEscape")
  public String getChatPrefix() {
    return "\u0041";
  }

  private void registerMessages() {
    MessageManager manager = getPlugin().getMessageManager();
    manager.registerMessage(LANGUAGE_ACCESSOR + "NAME", new Message("Kit.Content.Builder.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "DESCRIPTION", new Message("Kit.Content.Builder.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_FENCE_NAME", new Message("Kit.Content.Builder.Game-Item.Fence.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_FENCE_DESCRIPTION", new Message("Kit.Content.Builder.Game-Item.Fence.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_DOOR_NAME", new Message("Kit.Content.Builder.Game-Item.Door.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_DOOR_DESCRIPTION", new Message("Kit.Content.Builder.Game-Item.Door.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_EARTHED_NAME", new Message("Kit.Content.Builder.Game-Item.Earthed.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_EARTHED_DESCRIPTION", new Message("Kit.Content.Builder.Game-Item.Earthed.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_EARTHED_ACTIVATE", new Message("Kit.Content.Builder.Game-Item.Earthed.Activate", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_EARTHED_ACTIVE_ACTION_BAR", new Message("Kit.Content.Builder.Game-Item.Earthed.Active-Action-Bar", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_EARTHED_PROTECTED_BY_ACTION_BAR", new Message("Kit.Content.Builder.Game-Item.Earthed.Knock-Protected-By-Action-Bar", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_BLOCKAGE_NAME", new Message("Kit.Content.Builder.Game-Item.Blockage.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_BLOCKAGE_DESCRIPTION", new Message("Kit.Content.Builder.Game-Item.Blockage.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_BLOCKAGE_ACTIVATE", new Message("Kit.Content.Builder.Game-Item.Blockage.Activate", ""));
  }

  @Override
  public boolean isUnlockedByPlayer(Player player) {
    return getPlugin().getPermissionsManager().hasPermissionString("KIT_PREMIUM_UNLOCK", player) || player.hasPermission("villagedefense.kit.builder");
  }

  @Override
  public void giveKitItems(Player player) {
    ArmorHelper.setColouredArmor(Color.RED, player);
    player.getInventory().addItem(WeaponHelper.getEnchanted(new ItemStack(Material.STONE_SWORD), new org.bukkit.enchantments.Enchantment[]{org.bukkit.enchantments.Enchantment.DURABILITY}, new int[]{10}));

    player.getInventory().setItem(2, new ItemBuilder(new ItemStack(XMaterial.OAK_FENCE.parseMaterial(), 3))
      .name(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_FENCE_NAME").asKey().build())
      .lore(getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_FENCE_DESCRIPTION"))
      .build());
    player.getInventory().setItem(3, new ItemBuilder(new ItemStack(getMaterial(), 2))
      .name(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_DOOR_NAME").asKey().build())
      .lore(getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_DOOR_DESCRIPTION"))
      .build());
    player.getInventory().setItem(4, new ItemBuilder(XMaterial.CLAY_BALL.parseMaterial())
      .name(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_EARTHED_NAME").asKey().build())
      .lore(getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_EARTHED_DESCRIPTION"))
      .build());
    player.getInventory().setItem(5, new ItemBuilder(XMaterial.IRON_BARS.parseMaterial())
      .name(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_BLOCKAGE_NAME").asKey().build())
      .lore(getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_BLOCKAGE_DESCRIPTION"))
      .build());
    player.getInventory().setItem(6, new ItemStack(Material.SADDLE));
    player.getInventory().setItem(8, new ItemStack(Material.COOKED_BEEF, 10));
  }

  @Override
  public Material getMaterial() {
    return Utils.getCachedDoor(null);
  }

  @Override
  public void reStock(Player player) {
    Arena arena = (Arena) getPlugin().getUserManager().getUser(player).getArena();
    int fences = (int) Settings.PASSIVE_FENCE_COUNT.getForArenaState(arena);
    player.getInventory().addItem(new ItemBuilder(new ItemStack(XMaterial.OAK_FENCE.parseMaterial(), fences))
      .name(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_FENCE_NAME").asKey().build())
      .lore(getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_FENCE_DESCRIPTION"))
      .build());
    if (arena.getWave() % (int) Settings.PASSIVE_DOOR_MODULO.getForArenaState(arena) == 0) {
      player.getInventory().addItem(new ItemBuilder(new ItemStack(getMaterial(), 1))
        .name(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_DOOR_NAME").asKey().build())
        .lore(getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_DOOR_DESCRIPTION"))
        .build());
    }
    if (arena.getWave() == KitSpecifications.GameTimeState.MID.getStartWave()) {
      new MessageBuilder("KIT_ABILITY_UNLOCKED").asKey().value(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_BLOCKAGE_NAME").asKey().build()).send(player);
      new MessageBuilder("KIT_PASSIVE_POWER_INCREASED").asKey().send(player);
    } else if (arena.getWave() == KitSpecifications.GameTimeState.LATE.getStartWave()) {
      new MessageBuilder("KIT_ABILITY_POWER_INCREASED").asKey().value(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_BLOCKAGE_NAME").asKey().build()).send(player);
      new MessageBuilder("KIT_PASSIVE_POWER_INCREASED").asKey().send(player);
    }
  }

  @Override
  @EventHandler
  public void onAbilityCast(PlugilyPlayerInteractEvent event) {
    if (!KitHelper.isInGameWithKitAndItemInHand(event.getPlayer(), BuilderKit.class)) {
      return;
    }
    ItemStack stack = VersionUtils.getItemInHand(event.getPlayer());
    User user = getPlugin().getUserManager().getUser(event.getPlayer());
    String displayName = ComplementAccessor.getComplement().getDisplayName(stack.getItemMeta());
    if (displayName.equals(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_EARTHED_NAME").asKey().build())) {
      onEarthedCast(stack, user);
    } else if (displayName.equals(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_BLOCKAGE_NAME").asKey().build())) {
      onBlockageCast(stack, user);
    } else if (displayName.equals(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_FENCE_NAME").asKey().build())) {
      onBarrierPlace(stack, user);
    }
  }

  private void onEarthedCast(ItemStack stack, User user) {
    if (!user.checkCanCastCooldownAndMessage("builder_earthed")) {
      return;
    }
    int cooldown = 20;
    user.setCooldown("builder_earthed", cooldown);
    int castTime = 10;
    user.setCooldown("builder_earthed_running", castTime);
    KitHelper.scheduleAbilityCooldown(stack, user.getPlayer(), castTime, cooldown);

    new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_EARTHED_ACTIVATE").asKey().send(user.getPlayer());
    List<String> messages = getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_EARTHED_ACTIVE_ACTION_BAR");
    List<String> buffMessages = getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_EARTHED_PROTECTED_BY_ACTION_BAR");
    Arena arena = (Arena) user.getArena();
    knockbackResistantArenas.put(arena, user.getPlayer());
    new BukkitRunnable() {
      int tick = 0;
      int messageIndex = 0;
      int buffMessageIndex = 0;

      @Override
      public void run() {
        if (tick % 10 == 0) {
          for (Player player : arena.getPlayersLeft()) {
            VersionUtils.sendParticles("CRIT_MAGIC", null, player.getLocation(), 5, 0, 0, 0);
            VersionUtils.sendActionBar(player, buffMessages.get(buffMessageIndex)
              .replace("%player%", user.getPlayer().getName()));
          }
          VersionUtils.sendActionBar(user.getPlayer(), messages.get(messageIndex)
            .replace("%number%", String.valueOf(user.getCooldown("builder_earthed_running"))));
          messageIndex++;
          buffMessageIndex++;
          if (messageIndex > messages.size() - 1) {
            messageIndex = 0;
          }
          if (buffMessageIndex > buffMessages.size() - 1) {
            buffMessageIndex = 0;
          }
        }
        if (tick >= 20 * castTime || !getPlugin().getArenaRegistry().isInArena(user.getPlayer()) || user.isSpectator()) {
          //reset action bar
          VersionUtils.sendActionBar(user.getPlayer(), "");
          cancel();
          knockbackResistantArenas.remove(arena);
          return;
        }
        tick++;
      }
    }.runTaskTimer(getPlugin(), 0, 1);
  }

  private void onBlockageCast(ItemStack stack, User user) {
    if (!user.checkCanCastCooldownAndMessage("builder_blockage")) {
      return;
    }
    if (KitSpecifications.getTimeState((Arena) user.getArena()) == KitSpecifications.GameTimeState.EARLY) {
      new MessageBuilder("KIT_LOCKED_TILL").asKey().integer(16).send(user.getPlayer());
      return;
    }
    int cooldown = (int) Settings.ABILITY_BLOCKAGE_COOLDOWN.getForArenaState((Arena) user.getArena());
    user.setCooldown("builder_blockage", cooldown);
    int castTime = 15;
    user.setCooldown("builder_blockage_running", cooldown);
    KitHelper.scheduleAbilityCooldown(stack, user.getPlayer(), castTime, cooldown);
    new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_BLOCKAGE_ACTIVATE").asKey().send(user.getPlayer());
    for (Creature enemy : ((Arena) user.getArena()).getEnemies()) {
      enemy.setMetadata("VD_DOOR_BLOCK_BAN", new FixedMetadataValue(getPlugin(), true));
      enemy.setMetadata("VD_DOOR_BLOCK_BAN_SOURCE", new FixedMetadataValue(getPlugin(), user.getPlayer().getUniqueId()));
    }
    Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
      for (Creature enemy : ((Arena) user.getArena()).getEnemies()) {
        enemy.removeMetadata("VD_DOOR_BLOCK_BAN", getPlugin());
        enemy.removeMetadata("VD_DOOR_BLOCK_BAN_SOURCE", getPlugin());
      }
    }, 20L * castTime);
  }

  private void onBarrierPlace(ItemStack stack, User user) {
    Player player = user.getPlayer();
    Block block = null;
    for (Block blocks : player.getLastTwoTargetBlocks(null, 5)) {
      if (blocks.getType() == Material.AIR) {
        block = blocks;
      }
    }
    if (block == null) {
      XSound.ENTITY_VILLAGER_NO.play(player);
      return;
    }
    getPlugin().getBukkitHelper().takeOneItem(player, stack);

    XSound.ENTITY_VILLAGER_YES.play(player);
    ZombieBarrier zombieBarrier = new ZombieBarrier();
    zombieBarrier.setLocation(block.getLocation());

    VersionUtils.sendParticles("FIREWORKS_SPARK", user.getArena().getPlayers(), block.getLocation(), 20);
    int castTime = (int) Settings.PASSIVE_FENCE_CAST_TIME.getForArenaState((Arena) user.getArena());
    final Block finalBlock = block;
    new BukkitRunnable() {
      int time = castTime;

      @Override
      public void run() {
        time -= 1;
        if (time <= 9) {
          int stage = 9 - time;
          ProtocolUtils.sendBlockBreakAnimation(finalBlock, stage);
        }

        if (time <= 0) {
          ProtocolUtils.removeBlockBreakAnimation(finalBlock);
          finalBlock.setType(Material.AIR);
          XSound.BLOCK_WOOD_BREAK.play(finalBlock.getLocation());
          Location loc = finalBlock.getLocation().clone();
          //centered location, more or less accurate
          loc.add(loc.getX() > 0 ? 0.5 : -0.5, 0.0, loc.getZ() > 0 ? 0.5 : -0.5);
          finalBlock.getWorld().spawnParticle(XParticle.getParticle("EXPLOSION_LARGE"), loc, 1);
          cancel();
        }
      }
    }.runTaskTimer(getPlugin(), 20, 20);
    block.setType(XMaterial.OAK_FENCE.parseMaterial());
  }

  @EventHandler
  public void onEarthedDamage(EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof Player)) {
      return;
    }
    Arena arena = (Arena) getPlugin().getArenaRegistry().getArena((Player) event.getEntity());
    if (arena == null || !knockbackResistantArenas.containsKey(arena)) {
      return;
    }
    arena.getAssistHandler().doRegisterBuffOnAlly(knockbackResistantArenas.get(arena), (Player) event.getEntity());
    Vector vector = new Vector();
    event.getEntity().setVelocity(vector);
    Bukkit.getScheduler().runTaskLater(getPlugin(), () -> event.getEntity().setVelocity(vector), 1L);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onDoorPlace(BlockPlaceEvent event) {
    Arena arena = (Arena) getPlugin().getArenaRegistry().getArena(event.getPlayer());
    if (arena == null) {
      return;
    }
    if (getPlugin().getUserManager().getUser(event.getPlayer()).isSpectator()) {
      return;
    }
    ItemStack stack = VersionUtils.getItemInHand(event.getPlayer());
    String displayName = ComplementAccessor.getComplement().getDisplayName(stack.getItemMeta());
    if (!displayName.equals(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_DOOR_NAME").asKey().build())) {
      event.setCancelled(true);
      return;
    }
    List<Location> locations = arena.getMapRestorerManager().getGameDoorLocations();
    Location above = event.getBlock().getLocation().add(0, 1, 0);
    if ((!locations.contains(event.getBlock().getLocation()) && !locations.contains(above))
      || !MaterialUtils.isDoor(stack.getType())) {
      XSound.ENTITY_VILLAGER_NO.play(event.getPlayer());
      event.setCancelled(true);
      return;
    }
    XSound.ENTITY_VILLAGER_YES.play(event.getPlayer());
    //to override world guard protection
    event.setCancelled(false);
  }

  private enum Settings {
    PASSIVE_FENCE_COUNT(1, 2, 3), PASSIVE_FENCE_CAST_TIME(10, 12, 14), PASSIVE_DOOR_MODULO(5, 3, 2),
    ABILITY_BLOCKAGE_COOLDOWN(0, 40, 30);

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

  private static class ZombieBarrier {
    private Location location;
    private int seconds = 10;

    void setLocation(Location location) {
      this.location = location;
    }

    void decrementSeconds() {
      seconds--;
    }

  }

}
