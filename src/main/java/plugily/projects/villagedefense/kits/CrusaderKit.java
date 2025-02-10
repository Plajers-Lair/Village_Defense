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

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import plugily.projects.minigamesbox.classic.handlers.language.Message;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.handlers.language.MessageManager;
import plugily.projects.minigamesbox.classic.kits.basekits.PremiumKit;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.minigamesbox.classic.utils.actionbar.ActionBar;
import plugily.projects.minigamesbox.classic.utils.helper.ArmorHelper;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.classic.utils.misc.complement.ComplementAccessor;
import plugily.projects.minigamesbox.classic.utils.version.ServerVersion;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.minigamesbox.classic.utils.version.events.api.PlugilyPlayerInteractEvent;
import plugily.projects.minigamesbox.classic.utils.version.xseries.ParticleDisplay;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XParticle;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XSound;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.managers.spawner.gold.NewCreatureUtils;
import plugily.projects.villagedefense.kits.ability.AbilitySource;
import plugily.projects.villagedefense.kits.utils.ActionBarPriority;
import plugily.projects.villagedefense.kits.utils.KitHelper;
import plugily.projects.villagedefense.kits.utils.KitSpecifications;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Plajer
 * <p>
 * Created at 30.08.2023
 */
public class CrusaderKit extends PremiumKit implements AbilitySource, Listener, ChatDisplayable, ScoreboardModifiable {

  private static final String COURAGE_METADATA = "VD_CRUSADER_COURAGE";
  private static final String UNKILLABLE_METADATA = "VD_CRUSADER_UNKILLABLE";
  private static final String LANGUAGE_ACCESSOR = "KIT_CONTENT_CRUSADER_";

  public CrusaderKit() {
    registerMessages();
    setName(new MessageBuilder(LANGUAGE_ACCESSOR + "NAME").asKey().build());
    setKey("Crusader");
    List<String> description = getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "DESCRIPTION");
    setDescription(description);
    getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
    getPlugin().getKitRegistry().registerKit(this);
    ((Main) getPlugin()).getKitsMenu().registerKit(KitsMenu.KitCategory.TANK, this);
  }

  @Override
  @SuppressWarnings("UnnecessaryUnicodeEscape")
  public String getChatPrefix() {
    return "\u0043";
  }

  private void registerMessages() {
    MessageManager manager = getPlugin().getMessageManager();
    manager.registerMessage(LANGUAGE_ACCESSOR + "NAME", new Message("Kit.Content.Crusader.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "DESCRIPTION", new Message("Kit.Content.Crusader.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_COURAGEOUS_NAME", new Message("Kit.Content.Crusader.Game-Item.Courageous.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_COURAGEOUS_DESCRIPTION", new Message("Kit.Content.Crusader.Game-Item.Courageous.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_COURAGEOUS_ACTIVATE", new Message("Kit.Content.Crusader.Game-Item.Courageous.Activate", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_COURAGEOUS_AURA_ACTION_BAR", new Message("Kit.Content.Crusader.Game-Item.Crusader.Aura-Action-Bar", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_COURAGEOUS_STACKS", new Message("Kit.Content.Crusader.Game-Item.Courageous.Stacks.Base", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_COURAGEOUS_STACKS_MAX", new Message("Kit.Content.Crusader.Game-Item.Courageous.Stacks.Max", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_GLORY_TO_THE_KING_NAME", new Message("Kit.Content.Crusader.Game-Item.Glory-To-The-King.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_GLORY_TO_THE_KING_DESCRIPTION", new Message("Kit.Content.Crusader.Game-Item.Glory-To-The-King.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_GLORY_TO_THE_KING_ACTIVATE", new Message("Kit.Content.Crusader.Game-Item.Glory-To-The-King.Activate", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_GLORY_TO_THE_KING_ACTIVE_ACTION_BAR", new Message("Kit.Content.Crusader.Game-Item.Glory-To-The-King.Active-Action-Bar", ""));
  }

  @Override
  public boolean isUnlockedByPlayer(Player player) {
    return player.hasPermission("villagedefense.kit.crusader") || getPlugin().getPermissionsManager().hasPermissionString("KIT_PREMIUM_UNLOCK", player);
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
        ScoreboardModifiable.renderAbilityCooldown(user, "crusader_courageous", "Courageous", wave, KitSpecifications.GameTimeState.EARLY),
        ScoreboardModifiable.renderAbilityCooldown(user, "crusader_glorytotheking", "Glory to the King", wave, KitSpecifications.GameTimeState.MID),
        ""
      )
    );
    if (!arena.isFighting()) {
      lines.add(1, "&fNext Wave in &a" + arena.getTimer() + "s");
    }
    return lines;
  }

  @Override
  public void giveKitItems(Player player) {
    ArmorHelper.setArmor(player, ArmorHelper.ArmorType.LEATHER);
    ItemStack itemStack = XMaterial.STONE_SWORD.parseItem();
    ItemMeta meta = itemStack.getItemMeta();
    meta.setUnbreakable(true);
    itemStack.setItemMeta(meta);
    player.getInventory().addItem(itemStack);

    player.getInventory().setItem(3, new ItemBuilder(new ItemStack(XMaterial.GOAT_HORN.parseMaterial()))
      .name(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_COURAGEOUS_NAME").asKey().build())
      .lore(getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_COURAGEOUS_DESCRIPTION"))
      .build());

    player.getInventory().setItem(4, new ItemBuilder(new ItemStack(XMaterial.NETHER_STAR.parseMaterial()))
      .name(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_GLORY_TO_THE_KING_NAME").asKey().build())
      .lore(getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_GLORY_TO_THE_KING_DESCRIPTION"))
      .build());

    player.getInventory().setItem(5, new ItemStack(Material.SADDLE));
    player.getInventory().setItem(8, new ItemStack(XMaterial.COOKED_PORKCHOP.parseMaterial(), 6));
    User user = getPlugin().getUserManager().getUser(player);
    doDisplayCourageBar(user);
  }

  @Override
  public Material getMaterial() {
    return Material.MAGMA_CREAM;
  }

  @Override
  public void reStock(Player player) {
    User user = getPlugin().getUserManager().getUser(player);
    doDisplayCourageBar(user);
    Arena arena = (Arena) getPlugin().getArenaRegistry().getArena(player);
    if (arena.getWave() == KitSpecifications.GameTimeState.MID.getStartWave()) {
      new MessageBuilder("KIT_ABILITY_UNLOCKED").asKey().value(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_GLORY_TO_THE_KING_NAME").asKey().build()).send(player);
      new MessageBuilder("KIT_PASSIVE_POWER_INCREASED").asKey().send(player);
    } else if (arena.getWave() == KitSpecifications.GameTimeState.LATE.getStartWave()) {
      new MessageBuilder("KIT_ABILITY_POWER_INCREASED").asKey().value(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_GLORY_TO_THE_KING_NAME").asKey().build()).send(player);
      new MessageBuilder("KIT_PASSIVE_POWER_INCREASED").asKey().send(player);
    }
  }

  private void doDisplayCourageBar(User user) {
    getPlugin().getActionBarManager().clearActionBarsFromPlayer(user.getPlayer());
    new BukkitRunnable() {
      @Override
      public void run() {
        int stacks = 0;
        if (user.getPlayer().hasMetadata(COURAGE_METADATA)) {
          stacks = user.getPlayer().getMetadata(COURAGE_METADATA).get(0).asInt();
        }
        MessageBuilder message;
        if (stacks >= 75) {
          message = new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_COURAGEOUS_STACKS_MAX").asKey();
        } else {
          message = new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_COURAGEOUS_STACKS").asKey().integer(stacks);
        }
        getPlugin().getActionBarManager().addActionBar(user.getPlayer(), new ActionBar(message, ActionBar.ActionBarType.DISPLAY, ActionBarPriority.LOW_PRIORITY));
        if (!getPlugin().getArenaRegistry().isInArena(user.getPlayer()) || user.isSpectator()) {
          cancel();
          getPlugin().getActionBarManager().clearActionBarsFromPlayer(user.getPlayer());
        }
      }
    }.runTaskTimer(getPlugin(), 0, 5);
  }

  @EventHandler
  public void onStunAura(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player player) || !NewCreatureUtils.isEnemy(event.getEntity())) {
      return;
    }
    User user = getPlugin().getUserManager().getUser(player);
    if (player.hasMetadata("VD_CRUSADER_STUN_AURA")) {
      NewCreatureUtils.doStunEnemy((Creature) event.getEntity(), (int) Settings.PASSIVE_STUN_DURATION.getForArenaState((Arena) user.getArena()));
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onUnkillableDamage(EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof Player)) {
      return;
    }
    User user = getPlugin().getUserManager().getUser((Player) event.getEntity());
    if (user == null || user.getArena() == null || user.isSpectator() || !(user.getKit() instanceof CrusaderKit)) {
      return;
    }
    if (user.getPlayer().hasMetadata(UNKILLABLE_METADATA) && user.getPlayer().getHealth() - event.getDamage() <= 0) {
      event.setDamage(0);
      user.getPlayer().setHealth(1);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onBlastDamage(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof TNTPrimed)) {
      return;
    }
    TNTPrimed primed = (TNTPrimed) event.getDamager();
    if (!primed.hasMetadata("VD_PRIMED_TNT")) {
      return;
    }
    if (!(event.getEntity() instanceof Player)) {
      return;
    }
    User user = getPlugin().getUserManager().getUser((Player) event.getEntity());
    if (user == null || user.getArena() == null || !(user.getKit() instanceof CrusaderKit)) {
      return;
    }
    KitSpecifications.GameTimeState state = KitSpecifications.getTimeState((Arena) user.getArena());
    if (state == KitSpecifications.GameTimeState.EARLY) {
      return;
    }
    event.setDamage(0);
  }

  @EventHandler
  public void onIronWillCast(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player) || !NewCreatureUtils.isEnemy(event.getEntity())) {
      return;
    }
    Player damager = (Player) event.getDamager();
    User user = getPlugin().getUserManager().getUser(damager);
    if (user.isSpectator() || !(user.getKit() instanceof CrusaderKit)) {
      return;
    }
    if (((Arena) user.getArena()).getSpecialEntities().contains(event.getEntity())) {
      return;
    }
    String metadata = "VD_CRUSADER_HITS";
    if (damager.hasMetadata(metadata)) {
      int totalHits = damager.getMetadata(metadata).get(0).asInt();
      totalHits++;
      if (totalHits == 3) {
        damager.playSound(damager, Sound.BLOCK_ANVIL_PLACE, 0.2f, 1.5f);
        Creature enemy = ((Creature) event.getEntity());
        NewCreatureUtils.doStunEnemy(enemy, (int) Settings.PASSIVE_STUN_DURATION.getForArenaState((Arena) user.getArena()));
        damager.removeMetadata(metadata, getPlugin());
        doIncreaseCourage(damager, (int) Settings.PASSIVE_STACKS_AMOUNT.getForArenaState((Arena) user.getArena()));
        return;
      }
      damager.setMetadata(metadata, new FixedMetadataValue(getPlugin(), totalHits));
    } else {
      damager.setMetadata(metadata, new FixedMetadataValue(getPlugin(), 1));
    }
  }

  private void doIncreaseCourage(Player player, int amount) {
    if (player.hasMetadata(COURAGE_METADATA)) {
      int stacks = player.getMetadata(COURAGE_METADATA).get(0).asInt();
      stacks += amount;
      stacks = Math.min(stacks, 75);
      player.setMetadata(COURAGE_METADATA, new FixedMetadataValue(getPlugin(), stacks));
    } else {
      player.setMetadata(COURAGE_METADATA, new FixedMetadataValue(getPlugin(), amount));
    }
  }

  @Override
  @EventHandler
  public void onAbilityCast(PlugilyPlayerInteractEvent event) {
    if (!KitHelper.isInGameWithKitAndItemInHand(event.getPlayer(), CrusaderKit.class)) {
      return;
    }
    ItemStack stack = VersionUtils.getItemInHand(event.getPlayer());
    User user = getPlugin().getUserManager().getUser(event.getPlayer());
    String displayName = ComplementAccessor.getComplement().getDisplayName(stack.getItemMeta());
    if (displayName.equals(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_COURAGEOUS_NAME").asKey().build())) {
      event.setCancelled(true);
      onCourageousCast(stack, user);
    } else if (displayName.equals(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_GLORY_TO_THE_KING_NAME").asKey().build())) {
      event.setCancelled(true);
      onGloryToTheKingCast(stack, user);
    }
  }

  private void onCourageousCast(ItemStack stack, User user) {
    if (!user.checkCanCastCooldownAndMessage("crusader_courageous")) {
      return;
    }
    int cooldown = 5;
    user.setCooldown("crusader_courageous", cooldown);
    VersionUtils.setMaterialCooldown(user.getPlayer(), stack.getType(), cooldown * 20);
    new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_COURAGEOUS_ACTIVATE").asKey().send(user.getPlayer());

    int stacks = 0;
    Player player = user.getPlayer();
    if (player.hasMetadata(COURAGE_METADATA)) {
      stacks = player.getMetadata(COURAGE_METADATA).get(0).asInt();
    }
    player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 5, 1));
    if (stacks >= 10) {
      player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 5, 1));
    }
    if (stacks >= 30) {
      KitHelper.healPlayer(player, VersionUtils.getMaxHealth(player) * 0.5);
    } else if (stacks >= 20) {
      KitHelper.healPlayer(player, VersionUtils.getMaxHealth(player) * 0.25);
    }
    if (stacks >= 40) {
      player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 8, 0));
    }
    if (stacks >= 50) {
      doPrepareBurnAura(user);
    }
    //todo stun aura as well as burn aura action bar
    if (stacks >= 75) {
      player.setMetadata("VD_CRUSADER_STUN_AURA", new FixedMetadataValue(getPlugin(), true));
      Bukkit.getScheduler().runTaskLater(getPlugin(), () -> player.removeMetadata("VD_CRUSADER_STUN_AURA", getPlugin()), 20 * 8);
    }
    playRandomHorn(player);
    player.removeMetadata(COURAGE_METADATA, getPlugin());
  }

  private void playRandomHorn(Player player) {
    switch (ThreadLocalRandom.current().nextInt(0, 7)) {
      case 0 -> player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_0, .75f, 1);
      case 1 -> player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_1, .75f, 1);
      case 2 -> player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_2, .75f, 1.25f);
      case 3 -> player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_3, .75f, 1.5f);
      case 4 -> player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_4, .75f, 1.25f);
      case 5 -> player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_5, .75f, 1.75f);
      case 6 -> player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_6, .75f, 1.5f);
      case 7 -> player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_7, .75f, 1.5f);
    }
  }

  private void doPrepareBurnAura(User user) {
    int castTime = 8;
    user.setCooldown("crusader_burn_aura", 8);
    Player player = user.getPlayer();
    getPlugin().getActionBarManager().addActionBar(player, new ActionBar(LANGUAGE_ACCESSOR + "GAME_ITEM_COURAGEOUS_AURA_ACTION_BAR", castTime));
    new BukkitRunnable() {
      int tick = 0;

      @Override
      public void run() {
        //apply effects only once per second, particles every tick
        XParticle.circle(3.5, 28, ParticleDisplay.simple(player.getLocation().add(0, 0.5, 0), XParticle.getParticle("SMOKE_NORMAL")));
        if (tick % 20 == 0) {
          for (Entity entity : player.getNearbyEntities(3.5, 3.5, 3.5)) {
            if (!NewCreatureUtils.isEnemy(entity) || entity.equals(player)) {
              continue;
            }
            entity.setFireTicks(25);
          }
        }
        if (tick >= 20 * castTime || !getPlugin().getArenaRegistry().isInArena(user.getPlayer()) || user.isSpectator()) {
          //reset action bar
          VersionUtils.sendActionBar(player, "");
          cancel();
          return;
        }
        tick++;
      }
    }.runTaskTimer(getPlugin(), 0, 1);
  }

  private void onGloryToTheKingCast(ItemStack stack, User user) {
    if (!user.checkCanCastCooldownAndMessage("crusader_glorytotheking")) {
      return;
    }
    if (KitSpecifications.getTimeState((Arena) user.getArena()) == KitSpecifications.GameTimeState.EARLY) {
      new MessageBuilder("KIT_LOCKED_TILL").asKey().integer(16).send(user.getPlayer());
      return;
    }
    Player player = user.getPlayer();
    player.setMetadata(UNKILLABLE_METADATA, new FixedMetadataValue(getPlugin(), true));
    int castTime = 10;
    int cooldown = getUltimateCooldown((Arena) user.getArena());
    user.setCooldown("crusader_glorytotheking", cooldown);
    Bukkit.getScheduler().runTaskLater(getPlugin(), () -> player.removeMetadata(UNKILLABLE_METADATA, getPlugin()), 20L * castTime);
    KitHelper.scheduleAbilityCooldown(stack, player, castTime, cooldown);
    XSound.BLOCK_PORTAL_TRIGGER.play(player, 1f, 0f);
    player.removeMetadata(COURAGE_METADATA, getPlugin());
    user.setCooldown("crusader_glorytotheking_running", castTime);

    getPlugin().getActionBarManager().addActionBar(player, new ActionBar(LANGUAGE_ACCESSOR + "GAME_ITEM_GLORY_TO_THE_KING_ACTIVE_ACTION_BAR", castTime));
    setWorldBorder(player, true);
    new BukkitRunnable() {
      int tick = 0;
      boolean border = true;

      @Override
      public void run() {
        if (tick >= 20 * (castTime - 2) && tick % 5 == 0) {
          border = !border;
          setWorldBorder(player, border);
        }
        if (tick >= 20 * castTime || !getPlugin().getArenaRegistry().isInArena(user.getPlayer()) || user.isSpectator()) {
          //reset action bar
          VersionUtils.sendActionBar(player, "");
          cancel();
          setWorldBorder(player, false);
          if (getPlugin().getArenaRegistry().isInArena(user.getPlayer()) && !user.isSpectator()) {
            XSound.ITEM_SHIELD_BREAK.play(player);
            KitHelper.healPlayer(player, VersionUtils.getMaxHealth(player) * 0.5);
          }
          return;
        }
        tick++;
      }
    }.runTaskTimer(getPlugin(), 0, 1);
  }

  private void setWorldBorder(Player player, boolean warn) {
    if (ServerVersion.Version.isCurrentEqualOrLower(ServerVersion.Version.v1_16_R3)) {
      doLegacyBorder(player, warn);
    } else {
      doNewBorder(player, warn);
    }
  }

  @Deprecated
  //testme
  private void doLegacyBorder(Player player, boolean warn) {
    PacketContainer container = new PacketContainer(PacketType.Play.Server.WORLD_BORDER);
    WorldBorder border = player.getWorld().getWorldBorder();
    container.getWorldBorderActions().write(0, EnumWrappers.WorldBorderAction.INITIALIZE);
    container.getDoubles().write(0, player.getLocation().getX());
    container.getDoubles().write(1, player.getLocation().getZ());
    container.getDoubles().write(2, border.getSize());
    container.getDoubles().write(3, border.getSize());
    container.getIntegers().write(0, 29999984);
    container.getIntegers().write(1, 0);
    container.getIntegers().write(2, (int) (warn ? border.getSize() : border.getWarningDistance()));
    container.getLongs().write(0, 0L);
    ProtocolLibrary.getProtocolManager().sendServerPacket(player, container);
  }

  private void doNewBorder(Player player, boolean warn) {
    PacketContainer container = new PacketContainer(PacketType.Play.Server.INITIALIZE_BORDER);
    WorldBorder border = player.getWorld().getWorldBorder();
    container.getDoubles().write(0, player.getLocation().getX());
    container.getDoubles().write(1, player.getLocation().getZ());
    container.getDoubles().write(2, warn ? 0 : border.getSize());
    container.getDoubles().write(3, warn ? 0 : border.getSize());
    container.getIntegers().write(0, warn ? 1 : 29999984);
    container.getIntegers().write(1, 0);
    container.getIntegers().write(2, warn ? 5 : border.getWarningDistance());
    container.getLongs().write(0, 0L);
    ProtocolLibrary.getProtocolManager().sendServerPacket(player, container);
  }

  private int getUltimateCooldown(Arena arena) {
    switch (KitSpecifications.getTimeState(arena)) {
      case LATE:
        return 25;
      case MID:
        return 40;
      case EARLY:
      default:
        return 0;
    }
  }

  private enum Settings {
    PASSIVE_STUN_DURATION(2, 3, 4), PASSIVE_STACKS_AMOUNT(1, 2, 3), ULTIMATE_HEAL_POWER(0, 4, 5);

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
