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

package plugily.projects.villagedefense.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
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
import plugily.projects.minigamesbox.classic.utils.misc.complement.ComplementAccessor;
import plugily.projects.minigamesbox.classic.utils.version.ServerVersion;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.minigamesbox.classic.utils.version.events.api.PlugilyPlayerInteractEvent;
import plugily.projects.minigamesbox.classic.utils.version.xseries.ParticleDisplay;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XParticle;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.creatures.CreatureUtils;
import plugily.projects.villagedefense.kits.ability.AbilitySource;
import plugily.projects.villagedefense.kits.utils.KitHelper;
import plugily.projects.villagedefense.kits.utils.KitSpecifications;
import plugily.projects.villagedefense.utils.NumberUtils;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by Tom on 18/07/2015.
 */
public class TerminatorKit extends PremiumKit implements Listener, AbilitySource {

  private static final String LANGUAGE_ACCESSOR = "KIT_CONTENT_TERMINATOR_";
  private final Random random = new Random();

  public TerminatorKit() {
    registerMessages();
    setName(new MessageBuilder(LANGUAGE_ACCESSOR + "NAME").asKey().build());
    setKey("Terminator");
    List<String> description = getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "DESCRIPTION");
    setDescription(description);
    getPlugin().getKitRegistry().registerKit(this);
    getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
  }

  private void registerMessages() {
    MessageManager manager = getPlugin().getMessageManager();
    manager.registerMessage(LANGUAGE_ACCESSOR + "NAME", new Message("Kit.Content.Terminator.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "DESCRIPTION", new Message("Kit.Content.Terminator.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_TERMINUS_NAME", new Message("Kit.Content.Terminator.Game-Item.Terminus.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_TERMINUS_DESCRIPTION", new Message("Kit.Content.Terminator.Game-Item.Terminus.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_NEVERDEATH_NAME", new Message("Kit.Content.Terminator.Game-Item.Neverdeath.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_NEVERDEATH_DESCRIPTION", new Message("Kit.Content.Terminator.Game-Item.Neverdeath.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_NEVERDEATH_ACTIVATE", new Message("Kit.Content.Terminator.Game-Item.Neverdeath.Activate", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_MOTORS_OVERCHARGE_NAME", new Message("Kit.Content.Terminator.Game-Item.Motors-Overcharge.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_MOTORS_OVERCHARGE_DESCRIPTION", new Message("Kit.Content.Terminator.Game-Item.Motors-Overcharge.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_MOTORS_OVERCHARGE_ACTIVATE", new Message("Kit.Content.Terminator.Game-Item.Motors-Overcharge.Activate", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_MOTORS_OVERCHARGE_ACTIVE_ACTION_BAR", new Message("Kit.Content.Terminator.Game-Item.Motors-Overcharge.Active-Action-Bar", ""));
  }

  @Override
  public boolean isUnlockedByPlayer(Player player) {
    return player.hasPermission("villagedefense.kit.terminator") || getPlugin().getPermissionsManager().hasPermissionString("KIT_PREMIUM_UNLOCK", player);
  }

  @Override
  public void giveKitItems(Player player) {
    player.getInventory().setItem(3, new ItemBuilder(XMaterial.DIAMOND_SWORD.parseItem())
      .enchantment(Enchantment.DURABILITY, 100)
      .name(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_TERMINUS_NAME").asKey().build())
      .lore(getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_TERMINUS_DESCRIPTION"))
      .build());
    player.getInventory().setItem(4, new ItemBuilder(new ItemStack(XMaterial.CHARCOAL.parseMaterial(), 1))
      .name(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_NEVERDEATH_NAME").asKey().build())
      .lore(getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_NEVERDEATH_DESCRIPTION"))
      .build());
    player.getInventory().setItem(5, new ItemBuilder(new ItemStack(XMaterial.TRIPWIRE_HOOK.parseMaterial(), 1))
      .name(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_MOTORS_OVERCHARGE_NAME").asKey().build())
      .lore(getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_MOTORS_OVERCHARGE_DESCRIPTION"))
      .build());

    player.getInventory().setItem(8, new ItemStack(XMaterial.COOKED_PORKCHOP.parseMaterial(), 8));
    ArmorHelper.setArmor(player, ArmorHelper.ArmorType.IRON);
  }

  @Override
  public Material getMaterial() {
    return Material.ANVIL;
  }

  @Override
  public void reStock(Player player) {
    Arena arena = (Arena) getPlugin().getArenaRegistry().getArena(player);
    if(arena.getWave() > 1 && arena.getWave() <= 30 && arena.getWave() % 5 == 0) {
      new MessageBuilder("KIT_ABILITY_POWER_INCREASED").asKey().value(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_TERMINUS_NAME").asKey().build()).send(player);
    }
    if(arena.getWave() == KitSpecifications.GameTimeState.MID.getStartWave()) {
      new MessageBuilder("KIT_ABILITY_UNLOCKED").asKey().value(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_NEVERDEATH_NAME").asKey().build()).send(player);
      new MessageBuilder("KIT_ABILITY_UNLOCKED").asKey().value(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_MOTORS_OVERCHARGE_NAME").asKey().build()).send(player);
    }
  }

  @EventHandler
  public void onPreSwordDamage(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player) || !CreatureUtils.isEnemy(event.getEntity())) {
      return;
    }
    if (event.isCancelled()) {
      return;
    }
    Player damager = (Player) event.getDamager();
    if(!KitHelper.isInGameWithKitAndItemInHand(damager, TerminatorKit.class)) {
      return;
    }
    ItemStack stack = VersionUtils.getItemInHand(damager);
    User user = getPlugin().getUserManager().getUser(damager);
    String displayName = ComplementAccessor.getComplement().getDisplayName(stack.getItemMeta());
    if(displayName.equals(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_TERMINUS_NAME").asKey().build())) {
      onSwordDamage(event, user);
    }
  }

  private void onSwordDamage(EntityDamageByEntityEvent event, User user) {
    Arena arena = (Arena) user.getArena();
    double onHitBurnChance = getOnHitBurnChance(arena.getWave());
    double onHitInstakillChance = arena.getWave() >= 10 ? 0.05 : 0;
    double onHitSharedCombustChance = arena.getWave() >= 20 ? 0.2 : 0;
    double onHitLargeKnockbackChance = arena.getWave() >= 25 ? 0.35 : 0;
    double onHitLifestealPercent = arena.getWave() >= 15 ? 0.06 : 0;
    double maxHealthBonusDamage = getMaxHealthBonusDamage(arena.getWave());
    double chance = random.nextDouble();
    LivingEntity entity = (LivingEntity) event.getEntity();
    if(chance <= onHitBurnChance) {
      entity.setFireTicks(20 * 3);
    }
    double steal = event.getDamage() * onHitLifestealPercent;
    //filter out large lifesteals from insta kills or such
    if(steal > 0 && steal < 100) {
      KitHelper.healPlayer(user.getPlayer(), steal);
    }
    if(chance <= onHitInstakillChance) {
      KitHelper.executeEnemy(entity, user.getPlayer());
      return;
    }
    if(chance <= onHitSharedCombustChance) {
      List<LivingEntity> enemies = entity.getNearbyEntities(1.5, 1.5, 1.5)
        .stream()
        .filter(e -> !entity.equals(e))
        .filter(CreatureUtils::isEnemy)
        .map(e -> (LivingEntity) e)
        .collect(Collectors.toList());
      for(LivingEntity enemy : enemies) {
        enemy.setFireTicks(20 * 3);
        enemy.damage(0, user.getPlayer());
      }
    }
    if(chance <= onHitLargeKnockbackChance) {
      entity.setVelocity(user.getPlayer().getLocation().getDirection().multiply(2.5));
    }
    double bonusDamage = VersionUtils.getMaxHealth(entity) * maxHealthBonusDamage;
    event.setDamage(event.getDamage() + bonusDamage);
  }

  private double getOnHitBurnChance(int wave) {
    if(wave >= 15) {
      return 0.4;
    } else if(wave >= 5) {
      return 0.2;
    }
    return 0;
  }

  private double getMaxHealthBonusDamage(int wave) {
    if(wave < 30) {
      return 0;
    }
    int val = wave - 29;
    return NumberUtils.clamp(val, 1, 40) / 100.0;
  }

  @EventHandler
  public void onEntityDamage(EntityDamageByEntityEvent event) {
    if (!event.getDamager().hasMetadata("VD_TERMINATOR_NO_DAMAGE")) {
      return;
    }
    event.setCancelled(true);
  }

  @Override
  @EventHandler
  public void onAbilityCast(PlugilyPlayerInteractEvent event) {
    if(!KitHelper.isInGameWithKitAndItemInHand(event.getPlayer(), TerminatorKit.class)) {
      return;
    }
    ItemStack stack = VersionUtils.getItemInHand(event.getPlayer());
    User user = getPlugin().getUserManager().getUser(event.getPlayer());
    String displayName = ComplementAccessor.getComplement().getDisplayName(stack.getItemMeta());
    if(displayName.equals(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_NEVERDEATH_NAME").asKey().build())) {
      onNeverdeathCast(stack, user);
    } else if(displayName.equals(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_MOTORS_OVERCHARGE_NAME").asKey().build())) {
      onMotorsOverchargePreCast(stack, user);
    }
  }

  private void onNeverdeathCast(ItemStack stack, User user) {
    if(!user.checkCanCastCooldownAndMessage("terminator_neverdeath")) {
      return;
    }
    if(KitSpecifications.getTimeState((Arena) user.getArena()) == KitSpecifications.GameTimeState.EARLY) {
      new MessageBuilder("KIT_LOCKED_TILL").asKey().integer(16).send(user.getPlayer());
      return;
    }
    int cooldown = getKitsConfig().getInt("Kit-Cooldown.Terminator.Neverdeath", 20);
    user.setCooldown("terminator_neverdeath", cooldown);
    new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_NEVERDEATH_ACTIVATE").asKey().send(user.getPlayer());

    VersionUtils.setMaterialCooldown(user.getPlayer(), stack.getType(), cooldown * 20);

    double missingHealthPercent = 1.0 - (user.getPlayer().getHealth() / VersionUtils.getMaxHealth(user.getPlayer()));
    user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 15, (int) Math.floor(missingHealthPercent * 10.0)));
  }

  private void onMotorsOverchargePreCast(ItemStack stack, User user) {
    if(!user.checkCanCastCooldownAndMessage("terminator_overcharge")) {
      return;
    }
    if(KitSpecifications.getTimeState((Arena) user.getArena()) == KitSpecifications.GameTimeState.EARLY) {
      new MessageBuilder("KIT_LOCKED_TILL").asKey().integer(16).send(user.getPlayer());
      return;
    }
    int cooldown = getKitsConfig().getInt("Kit-Cooldown.Terminator.Motors-Overcharge", 60);
    user.setCooldown("terminator_overcharge", cooldown);
    int castTime = 10;
    user.setCooldown("terminator_overcharge_running", castTime);
    new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_MOTORS_OVERCHARGE_ACTIVATE").asKey().send(user.getPlayer());

    KitHelper.scheduleAbilityCooldown(stack, user.getPlayer(), castTime, cooldown);
    onMotorsOverchargeCast(user);
  }

  private void onMotorsOverchargeCast(User user) {
    int castTime = 10;
    Player player = user.getPlayer();
    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * castTime, 3));

    List<String> messages = getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_MOTORS_OVERCHARGE_ACTIVE_ACTION_BAR");
    new BukkitRunnable() {
      int tick = 0;
      int messageIndex = 0;

      @Override
      public void run() {
        if(ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_9_R1)) {
          XParticle.circle(3, 26, ParticleDisplay.simple(player.getLocation().add(0, 0.35, 0), XParticle.getParticle("SMOKE_NORMAL")));
        }
        List<Entity> enemies = player.getNearbyEntities(2.5, 2.5, 2.5)
          .stream()
          .filter(CreatureUtils::isEnemy)
          .collect(Collectors.toList());
        for(Entity enemy : enemies) {
          LivingEntity livingEntity = (LivingEntity) enemy;
          if(tick >= 20 * castTime) {
            Vector oppositeDirection = enemy.getLocation().subtract(player.getLocation()).toVector();
            enemy.setVelocity(oppositeDirection.normalize().multiply(2.0));
            KitHelper.executeEnemy(livingEntity, player);
            continue;
          }
          Vector direction = player.getLocation().subtract(enemy.getLocation()).toVector();
          if (enemy instanceof Ageable ageable) {
            ageable.setBaby();
          }
          if (!enemy.hasMetadata("VD_TERMINATOR_NO_DAMAGE")) {
            ((LivingEntity) enemy).setCollidable(false);
            enemy.setMetadata("VD_TERMINATOR_NO_DAMAGE", new FixedMetadataValue(getPlugin(), true));
          }
          enemy.setVelocity(direction.normalize().multiply(1.0));
          if(tick % 20 == 0) {
            KitHelper.maxHealthPercentDamage(livingEntity, player, 35.0);
          }
        }
        if(tick % 10 == 0) {
          VersionUtils.sendActionBar(player, messages.get(messageIndex)
            .replace("%number%", String.valueOf(user.getCooldown("terminator_overcharge_running"))));
          messageIndex++;
          if(messageIndex > messages.size() - 1) {
            messageIndex = 0;
          }
        }
        if(user.isSpectator() || user.getArena() == null || !player.isOnline()) {
          cancel();
          return;
        }
        if(tick >= 20 * castTime) {
          VersionUtils.sendActionBar(player, "");
          player.getWorld().spawnParticle(XParticle.getParticle("EXPLOSION_LARGE"), player.getLocation(), 3);
          cancel();
          return;
        }
        tick++;
      }
    }.runTaskTimer(getPlugin(), 0, 1);
  }

}
