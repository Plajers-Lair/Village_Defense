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

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
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
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author Plajer
 * <p>
 * Created at 01.03.2018
 * <p>
 */
public class WizardKit extends PremiumKit implements Listener, AbilitySource, ChatDisplayable, ScoreboardModifiable {

  private static final String LANGUAGE_ACCESSOR = "KIT_CONTENT_WIZARD_";
  private final Random random = new Random();
  private final List<Player> abilityUsers = new ArrayList<>();

  public WizardKit() {
    registerMessages();
    setName(new MessageBuilder(LANGUAGE_ACCESSOR + "NAME").asKey().build());
    setKey("Wizard");
    List<String> description = getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "DESCRIPTION");
    setDescription(description);
    getPlugin().getKitRegistry().registerKit(this);
    getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
    ((Main) getPlugin()).getKitsMenu().registerKit(KitsMenu.KitCategory.DAMAGE_DEALER, this);
  }

  @Override
  @SuppressWarnings("UnnecessaryUnicodeEscape")
  public String getChatPrefix() {
    return "\u0050";
  }

  private void registerMessages() {
    MessageManager manager = getPlugin().getMessageManager();
    manager.registerMessage(LANGUAGE_ACCESSOR + "NAME", new Message("Kit.Content.Wizard.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "DESCRIPTION", new Message("Kit.Content.Wizard.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_WAND_NAME", new Message("Kit.Content.Wizard.Game-Item.Wand.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_WAND_NAME_OVERCHARGE_READY", new Message("Kit.Content.Wizard.Game-Item.Wand.Name-Overcharge-Ready", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_WAND_NAME_OVERCHARGED", new Message("Kit.Content.Wizard.Game-Item.Wand.Name-Overcharged", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_WAND_NAME_OVERCHARGED_2", new Message("Kit.Content.Wizard.Game-Item.Wand.Name-Overcharged-2", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_WAND_DESCRIPTION", new Message("Kit.Content.Wizard.Game-Item.Wand.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_WAND_OVERCHARGE_NOT_READY", new Message("Kit.Content.Wizard.Game-Item.Wand.Overcharge-Not-Ready", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_ASCENSION_NAME", new Message("Kit.Content.Wizard.Game-Item.Ascension.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_ASCENSION_DESCRIPTION", new Message("Kit.Content.Wizard.Game-Item.Ascension.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_ASCENSION_ACTIVATE", new Message("Kit.Content.Wizard.Game-Item.Ascension.Activate", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_ASCENSION_ACTIVE_ACTION_BAR", new Message("Kit.Content.Wizard.Game-Item.Ascension.Active-Action-Bar", ""));
  }

  @Override
  public boolean isUnlockedByPlayer(Player player) {
    return getPlugin().getPermissionsManager().hasPermissionString("KIT_PREMIUM_UNLOCK", player) || player.hasPermission("villagedefense.kit.wizard");
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
        renderOvercharge(user),
        ScoreboardModifiable.renderAbilityCooldown(user, "wizard_ascension", "Ascension", wave, KitSpecifications.GameTimeState.MID),
        ""
      )
    );
    if (!arena.isFighting()) {
      lines.add(1, "&fNext Wave in &a" + arena.getTimer() + "s");
    }
    return lines;
  }

  private String renderOvercharge(User user) {
    if (abilityUsers.contains(user.getPlayer())) {
      return "&fOvercharge: &aActive";
    }
    for (ItemStack itemStack : user.getPlayer().getInventory()) {
      if (itemStack == null || !itemStack.hasItemMeta() || itemStack.getType() != Material.GOLDEN_HOE) {
        continue;
      }
      //dig speed is used as a flag for overcharge
      if (itemStack.getItemMeta().hasEnchant(Enchantment.DIG_SPEED)) {
        return "&fOvercharge: &a✔";
      } else {
        Damageable damageable = (Damageable) itemStack.getItemMeta();
        int max = itemStack.getType().getMaxDurability() - 1;
        int percent = (int) ((1 - ((double) damageable.getDamage() / (double) max)) * 100);
        return "&fOvercharge: &6" + percent + "%";
      }
    }
    return "&fOvercharge: &60%";
  }

  @Override
  public void giveKitItems(Player player) {
    ItemStack wandItem = new ItemBuilder(getMaterial())
      .flags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
      .name(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_WAND_NAME").asKey().build())
      .lore(getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_WAND_DESCRIPTION"))
      .build();
    Damageable damageable = (Damageable) wandItem.getItemMeta();
    damageable.setDamage(31);
    wandItem.setItemMeta(damageable);
    player.getInventory().setItem(3, wandItem);
    player.getInventory().setItem(4, new ItemBuilder(new ItemStack(XMaterial.SPIDER_EYE.parseMaterial(), 1))
      .name(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_ASCENSION_NAME").asKey().build())
      .lore(getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_ASCENSION_DESCRIPTION"))
      .build());
    player.getInventory().setItem(5, new ItemStack(Material.SADDLE));

    ArmorHelper.setColouredArmor(Color.fromRGB(100, 149, 237), player);
    player.getInventory().setItem(8, new ItemStack(Material.SADDLE));
  }

  @Override
  public Material getMaterial() {
    return Material.GOLDEN_HOE;
  }

  @Override
  public void reStock(Player player) {
    Arena arena = (Arena) getPlugin().getArenaRegistry().getArena(player);
    if (arena.getWave() == KitSpecifications.GameTimeState.MID.getStartWave()) {
      new MessageBuilder("KIT_ABILITY_UNLOCKED").asKey().value(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_ASCENSION_NAME").asKey().build()).send(player);
      new MessageBuilder("KIT_ABILITY_POWER_INCREASED").asKey().value(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_WAND_NAME").asKey().build()).send(player);
    } else if (arena.getWave() == KitSpecifications.GameTimeState.LATE.getStartWave()) {
      new MessageBuilder("KIT_ABILITY_POWER_INCREASED").asKey().value(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_WAND_NAME").asKey().build()).send(player);
      new MessageBuilder("KIT_ABILITY_POWER_INCREASED").asKey().value(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_ASCENSION_NAME").asKey().build()).send(player);
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
    if (user.isSpectator() || !(user.getKit() instanceof WizardKit) || !abilityUsers.contains(damager) || !NewCreatureUtils.isEnemy(event.getEntity())) {
      return;
    }
    LivingEntity entity = (LivingEntity) event.getEntity();
    if (!entity.hasMetadata("VD_UNSTUNNABLE")) {
      entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 3, 3, false, true));
    }
    entity.setFireTicks(20 * 3);
    ((Arena) user.getArena()).getAssistHandler().doRegisterDebuffOnEnemy(user.getPlayer(), (Creature) entity);
  }

  @Override
  @EventHandler
  public void onAbilityCast(PlugilyPlayerInteractEvent event) {
    if (!KitHelper.isInGameWithKitAndItemInHand(event.getPlayer(), WizardKit.class)) {
      return;
    }
    ItemStack stack = VersionUtils.getItemInHand(event.getPlayer());
    User user = getPlugin().getUserManager().getUser(event.getPlayer());
    String displayName = ChatColor.translateAlternateColorCodes('&', ComplementAccessor.getComplement().getDisplayName(stack.getItemMeta()));
    if (displayName.equals(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_ASCENSION_NAME").asKey().build())) {
      event.setCancelled(true);
      onAscensionPreCast(stack, user);
    } else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
      String wandDefault = new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_WAND_NAME").asKey().build();
      String wandOverchargeReady = new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_WAND_NAME_OVERCHARGE_READY").asKey().build();
      String wandOvercharge = new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_WAND_NAME_OVERCHARGED").asKey().build();
      String wandOvercharge2 = new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_WAND_NAME_OVERCHARGED_2").asKey().build();
      if (displayName.equals(wandDefault) || displayName.equals(wandOvercharge)
        || displayName.equals(wandOvercharge2) || displayName.equals(wandOverchargeReady)) {
        event.setCancelled(true);
        onWandPreCast(stack, user);
      }
    } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
      String wandDefault = new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_WAND_NAME").asKey().build();
      if (displayName.equals(wandDefault)) {
        event.setCancelled(true);
        new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_WAND_OVERCHARGE_NOT_READY").asKey().player(user.getPlayer()).sendPlayer();
        return;
      }
      String wandOverchargeReady = new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_WAND_NAME_OVERCHARGE_READY").asKey().build();
      if (displayName.equals(wandOverchargeReady)) {
        event.setCancelled(true);
        onWandOverchargePreCast(stack, user);
      }
    }
  }

  private void onAscensionPreCast(ItemStack stack, User user) {
    if (!user.checkCanCastCooldownAndMessage("wizard_ascension")) {
      return;
    }
    if (KitSpecifications.getTimeState((Arena) user.getArena()) == KitSpecifications.GameTimeState.EARLY) {
      new MessageBuilder("KIT_LOCKED_TILL").asKey().integer(16).send(user.getPlayer());
      return;
    }
    int cooldown = 70;
    user.setCooldown("wizard_ascension", cooldown);
    int castTime = 12;
    user.setCooldown("wizard_ascension_running", castTime);
    new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_ASCENSION_ACTIVATE").asKey().send(user.getPlayer());

    KitHelper.scheduleAbilityCooldown(stack, user.getPlayer(), castTime, cooldown);
    onAscensionCast(stack, user);
  }

  @SneakyThrows
  private void onAscensionCast(ItemStack stack, User user) {
    Player player = user.getPlayer();
    double yDefault = player.getLocation().getY();
    player.getWorld().strikeLightningEffect(player.getLocation());
    Location particleLocation = player.getLocation().clone();
    particleLocation.setY(yDefault + 0.3);
    KitHelper.damageNearbyPercent(player, 10.0, 1.5);
    XParticle.circle(0.25, 7, ParticleDisplay.simple(particleLocation, XParticle.getParticle("SMOKE_NORMAL")));
    player.playSound(player, Sound.BLOCK_BEACON_POWER_SELECT, 1, 0.25f);
    List<Location> locations = new ArrayList<>();
    locations.add(player.getLocation().clone().add(3.25, 0, 0));
    locations.add(player.getLocation().clone().add(0, 0, 3.25));
    locations.add(player.getLocation().clone().add(-3.25, 0, 0));
    locations.add(player.getLocation().clone().add(0, 0, -3.25));
    for (int i = 0; i < 4; i++) {
      final int finalI = i;
      Bukkit.getScheduler().runTaskLater(getPlugin(), () -> spawnAscensionProjectile(locations.get(finalI), user, finalI), 10 * (i + 1));
    }
    List<String> messages = getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_ASCENSION_ACTIVE_ACTION_BAR");
    new BukkitRunnable() {
      int tick = 0;
      double radius = 0.25;
      int particlesAmount = 7;
      int messageIndex = 0;

      @Override
      public void run() {
        for (double t = 0; t < Math.PI * 2; t += Math.PI / 10) {
          double x = Math.sin(t) * 0.5 * 2.0;
          double y = Math.sin(t * 2) * 0.5 * 2.0;

          double rotatedX = x;
          double rotatedY = y * Math.cos(Math.PI / 4);

          Location pLoc = player.getLocation();
          Location location = new Location(pLoc.getWorld(), pLoc.getX() + rotatedX, yDefault + 2.25 + rotatedY, pLoc.getZ());
          location = location.setDirection(location.getDirection().normalize().multiply(-1));

          // Spawn particle
          player.getLocation().getWorld().spawnParticle(Particle.SMOKE_NORMAL, location, 1, 0, 0, 0, 0);
        }
        XParticle.circle(radius, particlesAmount, ParticleDisplay.simple(particleLocation, XParticle.getParticle("SMOKE_NORMAL")));
        for (Entity entity : player.getLocation().getNearbyEntities(2.5, 2.5, 2.5)) {
          if (!NewCreatureUtils.isEnemy(entity)) {
            continue;
          }
          if (!entity.hasMetadata("VD_UNSTUNNABLE")) {
            Vector vector = entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
            vector.add(new Vector(0, 0.1, 0));
            entity.setVelocity(vector.multiply(1.2));
          }
        }
        if (player.getY() - yDefault < 1.5) {
          double value = 0.1;
          if (tick <= 10) {
            value = 0.25;
          } else if (tick <= 20) {
            value = 0.15;
          }
          player.setVelocity(new Vector(0, value, 0));
        }
        if (radius <= 3.5) {
          radius += 0.15;
          particlesAmount += 1;
        }
        particleLocation.setX(player.getLocation().getX());
        particleLocation.setZ(player.getLocation().getZ());
        if (tick % 10 == 0) {
          VersionUtils.sendActionBar(player, messages.get(messageIndex)
            .replace("%number%", String.valueOf(user.getCooldown("wizard_ascension_running"))));
          messageIndex++;
          if (messageIndex > messages.size() - 1) {
            messageIndex = 0;
          }
        }

        if (tick >= 20 * 12 || !getPlugin().getArenaRegistry().isInArena(user.getPlayer()) || user.isSpectator()) {
          //reset action bar
          VersionUtils.sendActionBar(player, "");
          this.cancel();
          for (Entity entity : player.getNearbyEntities(4, 4, 4)) {
            if (!NewCreatureUtils.isEnemy(entity)) {
              continue;
            }
            ((LivingEntity) entity).damage(1.5, player);
            entity.setFireTicks(20 * 3);
          }
          return;
        }
        tick++;
      }
    }.runTaskTimer(getPlugin(), 0, 1);
  }

  private void spawnAscensionProjectile(Location targetLocation, User user, int i) {
    ArmorStand projectile = spawnProjectile(targetLocation);
    projectile.getEquipment().setHelmet(new ItemStack(Material.WITHER_SKELETON_SKULL));
    projectile.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, projectile.getLocation().add(0, 0.5, 0), 1, 0, 0, 0, 0);
    projectile.getWorld().playSound(projectile.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.75f, .5f);
    XSound.BLOCK_END_PORTAL_FRAME_FILL.play(user.getPlayer(), 0.25f, 0.25f);
    LivingEntity targetEntity = rollNearbyEntity(user.getPlayer().getLocation());
    if (targetEntity != null) {
      targetEntity.setMetadata("VD_WIZARD_ULTIMATE_TARGET", new FixedMetadataValue(getPlugin(), projectile.getUniqueId()));
    }
    final LivingEntity finalEntity = targetEntity;
    new BukkitRunnable() {
      final Location location = targetLocation;
      final double speed = 0.25;
      double positionModifier = 0;
      int rotation = 0;
      int aliveTicks = 0;
      LivingEntity target = finalEntity;

      @Override
      public void run() {
        Location newPosition = location.clone();
        if (positionModifier <= 1.5) {
          positionModifier += 0.1;
          newPosition = newPosition.add(0, 0.1, 0);
        } else {
          if (target == null || target.isDead()) {
            target = rollNearbyEntity(user.getPlayer().getLocation());
          }
          if (target != null) {
            target.setMetadata("VD_WIZARD_ULTIMATE_TARGET", new FixedMetadataValue(getPlugin(), projectile.getUniqueId()));
            Location currentLocation = projectile.getLocation().clone();
            double distance = currentLocation.distance(target.getLocation());
            if (distance > 0.1) {
              double dx = target.getX() - currentLocation.getX();
              double dy = target.getY() - currentLocation.getY();
              double dz = target.getZ() - currentLocation.getZ();

              double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
              dx /= length;
              dy /= length;
              dz /= length;

              dx *= speed;
              dy *= speed;
              dz *= speed;
              newPosition = newPosition.add(dx, dy, dz);
            } else {
              //damage only every second tick
              if (aliveTicks % 2 == 0) {
                KitHelper.maxHealthPercentDamage(target, user.getPlayer(), Settings.WAND_PERCENT_DAMAGE.getForArenaState((Arena) user.getArena()));
              }
              if (target.isDead()) {
                for (Entity entity : user.getPlayer().getLocation().getNearbyEntities(6, 6, 6)) {
                  if (!NewCreatureUtils.isEnemy(entity) || entity.hasMetadata("VD_WIZARD_ULTIMATE_TARGET")) {
                    continue;
                  }
                  entity.setMetadata("VD_WIZARD_ULTIMATE_TARGET", new FixedMetadataValue(getPlugin(), projectile.getUniqueId()));
                  target = (LivingEntity) entity;
                  break;
                }
              }
            }
          }
        }

        rotation += 10;
        if (rotation >= 360) {
          rotation -= 360;
        }
        //spawn before location update
        location.getWorld().spawnParticle(Particle.DRAGON_BREATH, projectile.getEyeLocation(), 1, 0, 0, 0, 0);
        location.set(newPosition.getX(), newPosition.getY(), newPosition.getZ());
        projectile.teleport(location);
        projectile.setHeadPose(new EulerAngle(Math.toRadians(rotation), Math.toRadians(rotation), 0));

        if (aliveTicks >= (20 * 10) + (10 * (4 - i))) {
          if (target != null) {
            target.removeMetadata("VD_WIZARD_ULTIMATE_TARGET", getPlugin());
          }
          projectile.getWorld().spawnParticle(Particle.SOUL, projectile.getLocation(), 5, 0.15, 0.15, 0.15, 0);
          projectile.remove();
          cancel();
        }
        aliveTicks++;
      }
    }.runTaskTimer(getPlugin(), 0, 1);
  }

  private LivingEntity rollNearbyEntity(Location location) {
    List<LivingEntity> entities = location.getNearbyLivingEntities(6)
      .stream()
      .filter(NewCreatureUtils::isEnemy)
      .filter(e -> !e.hasMetadata("VD_WIZARD_ULTIMATE_TARGET"))
      .collect(Collectors.toList());
    if (entities.isEmpty()) {
      return null;
    }
    return entities.get(random.nextInt(entities.size()));
  }

  private void onWandOverchargePreCast(ItemStack stack, User user) {
    Damageable meta = (Damageable) stack.getItemMeta();
    meta.setDisplayName(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_WAND_NAME").asKey().build());
    user.getPlayer().playSound(user.getPlayer(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.5f);
    abilityUsers.add(user.getPlayer());
    new BukkitRunnable() {
      int ticks = 0;
      boolean toggle = false;

      @Override
      public void run() {
        if (toggle) {
          ComplementAccessor.getComplement().setDisplayName(meta, new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_WAND_NAME_OVERCHARGED_2").asKey().build());
        } else {
          ComplementAccessor.getComplement().setDisplayName(meta, new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_WAND_NAME_OVERCHARGED").asKey().build());
        }
        toggle = !toggle;
        meta.setDamage(Math.min(stack.getType().getMaxDurability() - 1, meta.getDamage() + 2));
        if (ticks >= 210 || meta.getDamage() >= stack.getType().getMaxDurability() - 1) {
          meta.removeEnchantments();
          ComplementAccessor.getComplement().setDisplayName(meta, new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_WAND_NAME").asKey().build());
          abilityUsers.remove(user.getPlayer());
          cancel();
        }
        stack.setItemMeta(meta);
        ticks += 20;
      }
    }.runTaskTimer(getPlugin(), 20, 20);
  }

  private void onWandPreCast(ItemStack stack, User user) {
    //no cooldown message, this ability is spammy no need for such message
    if (user.getCooldown("wizard_staff") > 0) {
      return;
    }
    double cooldown = Settings.WAND_COOLDOWN.getForArenaState((Arena) user.getArena());
    user.setCooldown("wizard_staff", cooldown);

    VersionUtils.setMaterialCooldown(user.getPlayer(), stack.getType(), (int) (cooldown * 20));
    onWandCast(stack, user);
  }

  private void onWandCast(ItemStack stack, User user) {
    final int finalPierce = (int) Settings.DEFAULT_PIERCE.getForArenaState((Arena) user.getArena());
    Location startLocation = user.getPlayer().getLocation().add(0, 0.75, 0);
    startLocation.setDirection(startLocation.getDirection().normalize());
    ArmorStand projectile = spawnProjectile(startLocation);
    Material skull = Material.WITHER_SKELETON_SKULL;
    if (abilityUsers.contains(user.getPlayer())) {
      skull = Material.CREEPER_HEAD;
    }
    projectile.getEquipment().setHelmet(new ItemStack(skull));
    new BukkitRunnable() {
      final Location location = startLocation;
      Vector direction = location.getDirection().normalize();
      double positionModifier = 0;
      int rotation = 0;
      int aliveTicks = 0;
      int pierce = finalPierce;
      boolean anyHit = false;
      boolean anyKill = false;

      @Override
      public void run() {
        positionModifier += 0.15;
        double x = direction.getX() * positionModifier;
        double y = direction.getY() * positionModifier;
        double z = direction.getZ() * positionModifier;
        Location newPosition = location.clone().add(x, y, z);

        boolean localHit = false;
        boolean localKill = false;
        for (Entity entity : newPosition.getNearbyEntities(0.5, 0.5, 0.5)) {
          if (pierce <= 0 || !NewCreatureUtils.isEnemy(entity) || entity.equals(user.getPlayer())) {
            continue;
          }
          LivingEntity livingEntity = (LivingEntity) entity;
          double maxHealthPercent = Settings.WAND_PERCENT_DAMAGE.getForArenaState((Arena) user.getArena());
          KitHelper.maxHealthPercentDamage(livingEntity, user.getPlayer(), maxHealthPercent);
          if (!entity.hasMetadata("VD_UNSTUNNABLE")) {
            Vector pushDirection = entity.getLocation().toVector().subtract(projectile.getLocation().add(0, 1.5, 0).toVector()).normalize();
            entity.setVelocity(pushDirection.multiply(1.5));
          }
          localHit = true;
          if (entity.isDead() || ((LivingEntity) entity).getHealth() <= 0) {
            localKill = true;
          }
          VersionUtils.sendParticles("DAMAGE_INDICATOR", null, entity.getLocation(), 1, 0, 0, 0);
          pierce--;
        }
        if (localKill && !anyKill) {
          XSound.ENTITY_GENERIC_HURT.play(user.getPlayer());
          anyKill = true;
          anyHit = true;
          location.getWorld().playSound(location, Sound.ENTITY_GENERIC_HURT, 1, 1);
          if (!stack.getItemMeta().hasEnchants()) {
            Damageable damageable = (Damageable) stack.getItemMeta();
            damageable.setDamage(Math.max(1, damageable.getDamage() - 1));
            if (damageable.getDamage() <= (short) 1) {
              damageable.addEnchant(Enchantment.DIG_SPEED, 1, true);
              ComplementAccessor.getComplement().setDisplayName(damageable, new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_WAND_NAME_OVERCHARGE_READY").asKey().build());
              user.getPlayer().playSound(user.getPlayer(), Sound.ENTITY_BLAZE_SHOOT, 1, 0.25f);
            }
            stack.setItemMeta(damageable);
          }
        }
        if (localHit && !anyHit) {
          XSound.ENTITY_GENERIC_DEATH.play(user.getPlayer());
          location.getWorld().playSound(location, Sound.ENTITY_GENERIC_DEATH, 1, 1);
          anyHit = true;
        }

        if (newPosition.getBlock().getType().isSolid()) {
          Vector normal = getSurfaceNormal(newPosition.getBlock());
          direction = direction.subtract(normal.multiply(2 * direction.dot(normal)));
          positionModifier -= 0.1;
          newPosition = location.clone().add(direction.normalize().multiply(0.12));
          newPosition.getWorld().spawnParticle(Particle.WHITE_SMOKE, newPosition, 3, 0.1, 0.1, 0.1, 0);
        }
        rotation += 10;
        if (rotation >= 360) {
          rotation -= 360;
        }
        //spawn before location update
        location.getWorld().spawnParticle(Particle.DRAGON_BREATH, projectile.getEyeLocation(), 1, 0, 0, 0, 0);
        location.set(newPosition.getX(), newPosition.getY(), newPosition.getZ());
        projectile.teleport(location);
        projectile.setHeadPose(new EulerAngle(Math.toRadians(rotation), Math.toRadians(rotation), 0));

        if (aliveTicks >= 60) {
          projectile.getWorld().spawnParticle(Particle.SOUL, projectile.getLocation(), 5, 0.15, 0.15, 0.15, 0);
          projectile.remove();
          cancel();
        }
        aliveTicks++;
      }

      private Vector getSurfaceNormal(Block block) {
        double x = 0, y = 0, z = 0;
        if (block.getX() < location.getBlockX()) {
          x = 1;
        } else if (block.getX() > location.getBlockX()) {
          x = -1;
        } else if (block.getY() < location.getBlockY()) {
          y = 1;
        } else if (block.getY() > location.getBlockY()) {
          y = -1;
        } else if (block.getZ() < location.getBlockZ()) {
          z = 1;
        } else if (block.getZ() > location.getBlockZ()) {
          z = -1;
        }
        return new Vector(x, y, z);
      }
    }.runTaskTimer(getPlugin(), 0, 1);
  }

  private ArmorStand spawnProjectile(Location location) {
    ArmorStand projectile = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
    projectile.setVisible(false);
    projectile.setGravity(false);
    projectile.setSmall(true);
    projectile.setInvulnerable(true);
    projectile.setBasePlate(false);
    return projectile;
  }

  private enum Settings {
    DEFAULT_PIERCE(3, 4, 5), WAND_COOLDOWN(1.25, 1, 0.75), WAND_PERCENT_DAMAGE(20.0, 25.0, 30.0);

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
