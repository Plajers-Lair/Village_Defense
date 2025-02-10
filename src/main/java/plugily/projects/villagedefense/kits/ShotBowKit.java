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
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
 * Created at 30.08.2023
 */
public class ShotBowKit extends PremiumKit implements AbilitySource, Listener, ChatDisplayable, ScoreboardModifiable {

  private static final String LANGUAGE_ACCESSOR = "KIT_CONTENT_SHOT_BOW_";
  private static final String ARROW_RAIN_KNOCK_METADATA = "VD_SHOTBOW_KNOCKBACK";
  private final Random random = new Random();

  public ShotBowKit() {
    registerMessages();
    setName(new MessageBuilder(LANGUAGE_ACCESSOR + "NAME").asKey().build());
    setKey("ShotBow");
    List<String> description = getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "DESCRIPTION");
    setDescription(description);
    getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
    getPlugin().getKitRegistry().registerKit(this);
    ((Main) getPlugin()).getKitsMenu().registerKit(KitsMenu.KitCategory.DAMAGE_DEALER, this);
  }

  @Override
  @SuppressWarnings("UnnecessaryUnicodeEscape")
  public String getChatPrefix() {
    return "\u0046";
  }

  private void registerMessages() {
    MessageManager manager = getPlugin().getMessageManager();
    manager.registerMessage(LANGUAGE_ACCESSOR + "NAME", new Message("Kit.Content.Shot-Bow.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "DESCRIPTION", new Message("Kit.Content.Shot-Bow.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_ARROW_RAIN_NAME", new Message("Kit.Content.Shot-Bow.Game-Item.Arrow-Rain.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_ARROW_RAIN_DESCRIPTION", new Message("Kit.Content.Shot-Bow.Game-Item.Arrow-Rain.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_ARROW_RAIN_ACTIVATE", new Message("Kit.Content.Shot-Bow.Game-Item.Arrow-Rain.Activate", ""));
  }

  @Override
  public boolean isUnlockedByPlayer(Player player) {
    return getPlugin().getPermissionsManager().hasPermissionString("KIT_PREMIUM_UNLOCK", player) || player.hasPermission("villagedefense.kit.shotbow");
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
        ScoreboardModifiable.renderAbilityCooldown(user, "shotbow_arrowrain", "Arrow Rain", wave, KitSpecifications.GameTimeState.MID),
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
    ItemStack bow = new ItemStack(Material.BOW);
    ItemMeta meta = bow.getItemMeta();
    meta.setUnbreakable(true);
    meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
    bow.setItemMeta(meta);
    player.getInventory().addItem(bow);
    player.getInventory().setItem(9, new ItemStack(getMaterial(), 1));
    ArmorHelper.setColouredArmor(Color.YELLOW, player);
    player.getInventory().setItem(4, new ItemBuilder(new ItemStack(XMaterial.STICK.parseMaterial()))
      .name(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_ARROW_RAIN_NAME").asKey().build())
      .lore(getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_ARROW_RAIN_DESCRIPTION"))
      .build());

    player.getInventory().setItem(5, new ItemStack(Material.SADDLE));
    player.getInventory().setItem(8, new ItemStack(XMaterial.COOKED_BEEF.parseMaterial(), 8));
  }

  @Override
  public Material getMaterial() {
    return Material.ARROW;
  }

  @Override
  public void reStock(Player player) {
    //no restock for this kit
  }

  @Override
  @EventHandler
  public void onAbilityCast(PlugilyPlayerInteractEvent event) {
    if(!KitHelper.isInGameWithKitAndItemInHand(event.getPlayer(), ShotBowKit.class)) {
      return;
    }
    ItemStack stack = VersionUtils.getItemInHand(event.getPlayer());
    User user = getPlugin().getUserManager().getUser(event.getPlayer());
    String displayName = ComplementAccessor.getComplement().getDisplayName(stack.getItemMeta());
    if(displayName.equals(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_ARROW_RAIN_NAME").asKey().build())) {
      onArrowRainCast(stack, user);
    }
  }

  private void onArrowRainCast(ItemStack stack, User user) {
    String abilityId = "shotbow_arrowrain";
    if(!user.checkCanCastCooldownAndMessage(abilityId)) {
      return;
    }
    if (KitSpecifications.getTimeState((Arena) user.getArena()) == KitSpecifications.GameTimeState.EARLY) {
      new MessageBuilder("KIT_LOCKED_TILL").asKey().integer(16).send(user.getPlayer());
      return;
    }
    int cooldown = 20;
    user.setCooldown(abilityId, cooldown);
    VersionUtils.setMaterialCooldown(user.getPlayer(), stack.getType(), 20 * cooldown);

    for(int i = 0; i < Settings.ABILITY_ARROW_RAIN_COUNT.getForArenaState((Arena) user.getArena()); i++) {
      Player player = user.getPlayer();
      Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
        Arrow pr = player.launchProjectile(Arrow.class);
        pr.setMetadata(ARROW_RAIN_KNOCK_METADATA, new FixedMetadataValue(getPlugin(), true));
        pr.setVelocity(player.getLocation().getDirection().multiply(4));
        pr.setBounce(false);
        pr.setShooter(player);
        pr.setCritical(true);
      }, 4L * (2L * i));
    }
  }

  @EventHandler
  public void onArrowDamage(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Arrow) || !NewCreatureUtils.isEnemy(event.getEntity())) {
      return;
    }
    Arrow arrow = (Arrow) event.getDamager();
    if(!(arrow.getShooter() instanceof Player)) {
      return;
    }
    Player shooter = (Player) arrow.getShooter();
    User user = getPlugin().getUserManager().getUser(shooter);
    if(user == null || user.getArena() == null || !(user.getKit() instanceof ShotBowKit)) {
      return;
    }
    //large knockback for arrow rain ability
    if(arrow.hasMetadata(ARROW_RAIN_KNOCK_METADATA)) {
      event.getEntity().setVelocity(user.getPlayer().getLocation().getDirection().multiply(5));
      KitHelper.executeEnemy((LivingEntity) event.getEntity(), shooter);
      return;
    }
    Arena arena = (Arena) user.getArena();
    double oneshotChance = Settings.PASSIVE_INSTAKILL_CHANCE.getForArenaState(arena);
    double chance = random.nextDouble();
    if(chance <= oneshotChance) {
      KitHelper.executeEnemy((LivingEntity) event.getEntity(), shooter);
      XSound.BLOCK_NOTE_BLOCK_HARP.play(user.getPlayer());
      return;
    }
    //apply effective arrows passive from mid game
    if(KitSpecifications.getTimeState(arena) == KitSpecifications.GameTimeState.EARLY) {
      return;
    }
    doApplyEffectiveArrows((LivingEntity) event.getEntity(), shooter);
  }

  private void doApplyEffectiveArrows(LivingEntity target, Player source) {
    int choice = random.nextInt(4) + 1;
    switch(choice) {
      case 1: {
        //lightning aoe
        target.getWorld().strikeLightningEffect(target.getLocation());
        KitHelper.maxHealthPercentDamage(target, source, 10.0);
        List<Entity> enemies = target.getNearbyEntities(1.5, 1.5, 1.5)
          .stream()
          .filter(e -> !target.equals(e))
          .filter(NewCreatureUtils::isEnemy)
          .collect(Collectors.toList());
        for(Entity entity : enemies) {
          KitHelper.maxHealthPercentDamage((LivingEntity) entity, source, 10.0);
        }
        break;
      }
      case 2:
        //slowness
        if (!target.hasMetadata("VD_UNSTUNNABLE")) {
          target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 10, 1));
        }
        break;
      case 3: {
        //shared combust
        List<LivingEntity> enemies = target.getNearbyEntities(1.5, 1.5, 1.5)
          .stream()
          .filter(e -> !target.equals(e))
          .filter(NewCreatureUtils::isEnemy)
          .map(e -> (LivingEntity) e)
          .collect(Collectors.toList());
        for(LivingEntity enemy : enemies) {
          enemy.setFireTicks(20 * 10);
          enemy.damage(0, source);
        }
        break;
      }
      case 4:
      default:
        //weakness
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20 * 10, 1));
        break;
    }
  }

  private enum Settings {
    PASSIVE_INSTAKILL_CHANCE(0.1, 0.2, 0.3), ABILITY_ARROW_RAIN_COUNT(0, 8, 12);

    private final double earlyValue;
    private final double midValue;
    private final double lateValue;

    Settings(double earlyValue, double midValue, double lateValue) {
      this.earlyValue = earlyValue;
      this.midValue = midValue;
      this.lateValue = lateValue;
    }

    public double getForArenaState(Arena arena) {
      switch(KitSpecifications.getTimeState(arena)) {
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
