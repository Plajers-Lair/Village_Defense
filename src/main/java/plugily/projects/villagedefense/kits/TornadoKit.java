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
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import plugily.projects.minigamesbox.classic.handlers.language.Message;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.handlers.language.MessageManager;
import plugily.projects.minigamesbox.classic.kits.basekits.PremiumKit;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.minigamesbox.classic.utils.helper.ArmorHelper;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.classic.utils.helper.ItemUtils;
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
import plugily.projects.villagedefense.kits.utils.KitHelper;
import plugily.projects.villagedefense.kits.utils.KitSpecifications;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Tom on 30/12/2015.
 */
public class TornadoKit extends PremiumKit implements Listener, AbilitySource, ChatDisplayable, ScoreboardModifiable {

  private static final String LANGUAGE_ACCESSOR = "KIT_CONTENT_TORNADO_";
  private static final int TORNADO_MAX_HEIGHT = 5;
  private static final double TORNADO_MAX_RADIUS = 4;
  private static final double TORNADO_RADIUS_INCREMENT = TORNADO_MAX_RADIUS / TORNADO_MAX_HEIGHT;
  private final List<Player> ultimateUsers = new ArrayList<>();

  public TornadoKit() {
    registerMessages();
    setName(new MessageBuilder(LANGUAGE_ACCESSOR + "NAME").asKey().build());
    setKey("Tornado");
    List<String> description = getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "DESCRIPTION");
    setDescription(description);
    getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
    getPlugin().getKitRegistry().registerKit(this);
    ((Main) getPlugin()).getKitsMenu().registerKit(KitsMenu.KitCategory.SUPPORT, this);
  }

  @Override
  @SuppressWarnings("UnnecessaryUnicodeEscape")
  public String getChatPrefix() {
    return "\u0049";
  }

  private void registerMessages() {
    MessageManager manager = getPlugin().getMessageManager();
    manager.registerMessage(LANGUAGE_ACCESSOR + "NAME", new Message("Kit.Content.Tornado.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "DESCRIPTION", new Message("Kit.Content.Tornado.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_TORNADO_NAME", new Message("Kit.Content.Tornado.Game-Item.Tornado.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_TORNADO_DESCRIPTION", new Message("Kit.Content.Tornado.Game-Item.Tornado.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_MONSOON_NAME", new Message("Kit.Content.Tornado.Game-Item.Monsoon.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_MONSOON_DESCRIPTION", new Message("Kit.Content.Tornado.Game-Item.Monsoon.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_MONSOON_ACTIVE_ACTION_BAR", new Message("Kit.Content.Tornado.Game-Item.Monsoon.Active-Action-Bar", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_FINAL_FLIGHT_NAME", new Message("Kit.Content.Tornado.Game-Item.Final-Flight.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_FINAL_FLIGHT_DESCRIPTION", new Message("Kit.Content.Tornado.Game-Item.Final-Flight.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_FINAL_FLIGHT_ACTIVE_ACTION_BAR", new Message("Kit.Content.Tornado.Game-Item.Final-Flight.Active-Action-Bar", ""));
  }

  @Override
  public boolean isUnlockedByPlayer(Player player) {
    return player.hasPermission("villagedefense.kit.tornado") || getPlugin().getPermissionsManager().hasPermissionString("KIT_PREMIUM_UNLOCK", player);
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
        ScoreboardModifiable.renderAbilityCooldown(user, "tornado_monsoon", "Monsoon", wave, KitSpecifications.GameTimeState.EARLY),
        ScoreboardModifiable.renderAbilityCooldown(user, "tornado_final_flight", "Final Flight", wave, KitSpecifications.GameTimeState.MID),
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
    ArmorHelper.setArmor(player, ArmorHelper.ArmorType.GOLD);
    ItemStack itemStack = XMaterial.STONE_SWORD.parseItem();
    ItemMeta meta = itemStack.getItemMeta();
    meta.setUnbreakable(true);
    itemStack.setItemMeta(meta);
    player.getInventory().addItem(itemStack);

    player.getInventory().addItem(new ItemStack(Material.SADDLE));
    player.getInventory().addItem(new ItemBuilder(new ItemStack(getMaterial(), 5))
      .name(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_TORNADO_NAME").asKey().build())
      .lore(getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_TORNADO_DESCRIPTION"))
      .build());
    player.getInventory().addItem(new ItemBuilder(new ItemStack(XMaterial.OXEYE_DAISY.parseMaterial(), 1))
      .name(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_MONSOON_NAME").asKey().build())
      .lore(getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_MONSOON_DESCRIPTION"))
      .build());
    player.getInventory().addItem(new ItemBuilder(new ItemStack(XMaterial.FEATHER.parseMaterial(), 1))
      .name(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_FINAL_FLIGHT_NAME").asKey().build())
      .lore(getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_FINAL_FLIGHT_DESCRIPTION"))
      .build());
  }

  @Override
  public Material getMaterial() {
    return XMaterial.COBWEB.parseMaterial();
  }

  @Override
  public void reStock(Player player) {
    int amount = (int) Settings.RESTOCK_AMOUNT.getForArenaState((Arena) getPlugin().getArenaRegistry().getArena(player));
    player.getInventory().addItem(new ItemBuilder(new ItemStack(getMaterial(), amount))
      .name(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_TORNADO_NAME").asKey().build())
      .lore(getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_TORNADO_DESCRIPTION"))
      .build());

    Arena arena = (Arena) getPlugin().getUserManager().getUser(player).getArena();
    if (arena.getWave() == KitSpecifications.GameTimeState.MID.getStartWave()) {
      new MessageBuilder("KIT_ABILITY_UNLOCKED").asKey().value(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_FINAL_FLIGHT_NAME").asKey().build()).send(player);
      new MessageBuilder("KIT_ABILITY_POWER_INCREASED").asKey().value(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_MONSOON_DESCRIPTION").asKey().build()).send(player);
    } else if (arena.getWave() == KitSpecifications.GameTimeState.LATE.getStartWave()) {
      new MessageBuilder("KIT_ABILITY_POWER_INCREASED").asKey().value(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_MONSOON_DESCRIPTION").asKey().build()).send(player);
    }
  }

  @Override
  @EventHandler
  public void onAbilityCast(PlugilyPlayerInteractEvent event) {
    User user = getPlugin().getUserManager().getUser(event.getPlayer());
    if (user.isSpectator() || !(user.getKit() instanceof TornadoKit)) {
      return;
    }
    Player player = event.getPlayer();
    if (!getPlugin().getArenaRegistry().isInArena(player)) {
      return;
    }

    ItemStack stack = VersionUtils.getItemInHand(player);
    if (!ItemUtils.isItemStackNamed(stack)) {
      return;
    }
    String displayName = ComplementAccessor.getComplement().getDisplayName(stack.getItemMeta());
    if (displayName.equalsIgnoreCase(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_TORNADO_NAME").asKey().build())) {
      onTornadoCast(stack, user);
    } else if (displayName.equalsIgnoreCase(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_MONSOON_NAME").asKey().build())) {
      onMonsoonPreCast(stack, user);
    } else if (displayName.equalsIgnoreCase(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_FINAL_FLIGHT_NAME").asKey().build())) {
      onFinalFlightPreCast(stack, user);
    }
  }

  @EventHandler
  public void onDamage(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player)) {
      return;
    }
    Player damager = (Player) event.getDamager();
    if (getPlugin().getArenaRegistry().getArena(damager) == null) {
      return;
    }
    User user = getPlugin().getUserManager().getUser(damager);
    if (user.isSpectator() || !(user.getKit() instanceof TornadoKit)
      || !ultimateUsers.contains(damager) || !NewCreatureUtils.isEnemy(event.getEntity())) {
      return;
    }
    LivingEntity livingEntity = (LivingEntity) event.getEntity();
    new BukkitRunnable() {
      int tick = 0;

      @Override
      public void run() {
        if (tick % 5 == 0) {
          livingEntity.setVelocity(new Vector(0, 0.5, 0));
        }

        if (tick % 20 == 0) {
          KitHelper.maxHealthPercentDamage(livingEntity, user.getPlayer(), 25.0);
        }
        if (tick >= 20 * 4 || livingEntity.isDead()) {
          this.cancel();
          return;
        }
        tick++;
      }
    }.runTaskTimer(getPlugin(), 0, 1);
  }

  private void onTornadoCast(ItemStack stack, User user) {
    getPlugin().getBukkitHelper().takeOneItem(user.getPlayer(), stack);
    XSound.ENTITY_FIREWORK_ROCKET_LAUNCH.play(user.getPlayer(), 1f, 0f);
    new BukkitRunnable() {
      final Vector vector = user.getPlayer().getLocation().getDirection();
      Location location = user.getPlayer().getLocation();
      int angle;
      int times = 0;
      int pierce = 0;

      @Override
      public void run() {
        int lines = 3;
        for (int l = 0; l < lines; l++) {
          for (double y = 0; y < TORNADO_MAX_HEIGHT; y += 0.5) {
            double radius = y * TORNADO_RADIUS_INCREMENT,
              radians = Math.toRadians(360.0 / lines * l + y * 25 - angle),
              x = Math.cos(radians) * radius,
              z = Math.sin(radians) * radius;
            VersionUtils.sendParticles("CLOUD", null, location.clone().add(x, y, z), 1, 0, 0, 0);
          }
        }
        pierce += pushAndDamageNearbyEnemies(location, vector);
        location = location.add(vector.getX() / (3 + Math.random() / 2), 0, vector.getZ() / (3 + Math.random() / 2));
        angle += 50;
        times++;

        if (pierce >= 20 || times > 55) {
          cancel();
        }
      }
    }.runTaskTimer(getPlugin(), 1, 1);
  }

  private int pushAndDamageNearbyEnemies(Location location, Vector vector) {
    int pierce = 0;
    for (LivingEntity entity : getNearbyEnemies(location, 2)) {
      pierce++;

      if (!entity.hasMetadata("VD_UNSTUNNABLE")) {
        Vector velocity = vector.multiply(1.5).setY(0).add(new Vector(0, 1, 0));
        if (VersionUtils.isPaper() && (vector.getX() > 4.0 || vector.getZ() > 4.0)) {
          velocity = vector.setX(2.0).setZ(1.0); // Paper's sh*t
        }
        entity.setVelocity(velocity);
      }
      entity.damage(5.0);
    }
    return pierce;
  }

  private void onMonsoonPreCast(ItemStack stack, User user) {
    if (!user.checkCanCastCooldownAndMessage("tornado_monsoon")) {
      return;
    }
    final int castTime = (int) Settings.MONSOON_CAST_TIME.getForArenaState((Arena) user.getArena());
    int cooldown = getKitsConfig().getInt("Kit-Cooldown.Tornado.Monsoon", 20);
    user.setCooldown("tornado_monsoon", cooldown);
    user.setCooldown("tornado_monsoon_running", castTime);

    KitHelper.scheduleAbilityCooldown(stack, user.getPlayer(), castTime, cooldown);
    onMonsoonCast(user);
  }

  private void onMonsoonCast(User user) {
    Player player = user.getPlayer();
    final int spellTime = (int) Settings.MONSOON_CAST_TIME.getForArenaState((Arena) user.getArena());

    List<String> messages = getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_MONSOON_ACTIVE_ACTION_BAR");
    new BukkitRunnable() {
      int spellTick = 0;
      int messageIndex = 0;

      @Override
      public void run() {
        if (ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_9_R1)) {
          XParticle.circle(4, 15, ParticleDisplay.simple(player.getLocation().add(0, 0.5, 0), XParticle.getParticle("CLOUD")));
        }
        if (spellTick % 5 == 0) {
          for (LivingEntity entity : getNearbyEnemies(player.getLocation(), 4)) {
            Vector vector = entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
            vector.add(new Vector(0, 0.1, 0));
            if (!entity.hasMetadata("VD_UNSTUNNABLE")) {
              entity.setVelocity(vector.multiply(1.5));
              entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 9999, 2, false, true));
            }
            entity.playEffect(EntityEffect.HURT);
            ((Arena) user.getArena()).getAssistHandler().doRegisterDebuffOnEnemy(player, (Creature) entity);
          }
          XSound.BLOCK_SNOW_STEP.play(player, 1f, 0f);
        }
        if (spellTick % 10 == 0) {
          VersionUtils.sendActionBar(player, messages.get(messageIndex)
            .replace("%number%", String.valueOf(user.getCooldown("tornado_monsoon_running"))));
          messageIndex++;
          if (messageIndex > messages.size() - 1) {
            messageIndex = 0;
          }
        }
        if (spellTick >= 20 * spellTime || !getPlugin().getArenaRegistry().isInArena(user.getPlayer()) || user.isSpectator()) {
          //reset action bar
          VersionUtils.sendActionBar(player, "");
          cancel();
          return;
        }
        spellTick++;
      }
    }.runTaskTimer(getPlugin(), 0, 1);
  }

  private List<LivingEntity> getNearbyEnemies(Location location, int radius) {
    return location.getWorld()
      .getNearbyEntities(location, radius, radius, radius)
      .stream()
      .filter(NewCreatureUtils::isEnemy)
      .map(e -> (LivingEntity) e)
      .collect(Collectors.toList());
  }

  private void onFinalFlightPreCast(ItemStack stack, User user) {
    if (!user.checkCanCastCooldownAndMessage("tornado_final_flight")) {
      return;
    }
    if (KitSpecifications.getTimeState((Arena) user.getArena()) == KitSpecifications.GameTimeState.EARLY) {
      new MessageBuilder("KIT_LOCKED_TILL").asKey().integer(16).send(user.getPlayer());
      return;
    }
    int cooldown = getKitsConfig().getInt("Kit-Cooldown.Tornado.Final-Flight", 45);
    user.setCooldown("tornado_final_flight", cooldown);
    int castTime = 10;
    user.setCooldown("tornado_final_flight_running", castTime);

    KitHelper.scheduleAbilityCooldown(stack, user.getPlayer(), castTime, cooldown);
    onFinalFlightCast(user);
  }

  private void onFinalFlightCast(User user) {
    Player player = user.getPlayer();
    final int spellTime = 10;
    ultimateUsers.add(user.getPlayer());

    List<String> messages = getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_FINAL_FLIGHT_ACTIVE_ACTION_BAR");
    new BukkitRunnable() {
      int spellTick = 0;
      int messageIndex = 0;

      @Override
      public void run() {
        if (spellTick % 10 == 0) {
          VersionUtils.sendActionBar(player, messages.get(messageIndex)
            .replace("%number%", String.valueOf(user.getCooldown("tornado_final_flight_running"))));
          messageIndex++;
          if (messageIndex > messages.size() - 1) {
            messageIndex = 0;
          }
        }
        if (spellTick >= 20 * spellTime || !getPlugin().getArenaRegistry().isInArena(user.getPlayer()) || user.isSpectator()) {
          //reset action bar
          VersionUtils.sendActionBar(player, "");
          ultimateUsers.remove(user.getPlayer());
          cancel();
          return;
        }
        spellTick++;
      }
    }.runTaskTimer(getPlugin(), 0, 1);
    Bukkit.getScheduler().runTaskLater(getPlugin(), () -> ultimateUsers.remove(user.getPlayer()), spellTime * 20L);
  }

  private enum Settings {
    RESTOCK_AMOUNT(2, 3, 5), MONSOON_CAST_TIME(6, 9, 12);

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
          return earlyValue;
        case MID:
          return midValue;
        case EARLY:
        default:
          return lateValue;
      }
    }
  }

}
