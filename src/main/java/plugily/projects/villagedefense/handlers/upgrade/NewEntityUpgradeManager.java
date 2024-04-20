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

package plugily.projects.villagedefense.handlers.upgrade;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Golem;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.Nullable;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.minigamesbox.inventory.normal.NormalFastInv;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.ArenaManager;
import plugily.projects.villagedefense.creatures.CreatureUtils;
import plugily.projects.villagedefense.kits.PetsFriend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NewEntityUpgradeManager {

  public static final String UPGRADES_DISABLED_METADATA = "VD_UPGRADES_DISABLED";

  private final List<EntityUpgrade> registeredUpgrades = new ArrayList<>();
  private final Main plugin;

  public NewEntityUpgradeManager(Main plugin) {
    this.plugin = plugin;
    registerUpgrades();
  }

  private void registerUpgrades() {
    registerGolemUpgrades();
    registerWolfUpgrades();
  }

  private void registerGolemUpgrades() {
    for (int i = 0; i < 3; i++) {
      EntityUpgrade.Builder builder = new EntityUpgrade.Builder()
        .withId("GOLEM_HEALTH_AND_REGEN_" + (i + 1))
        .withName(color("&a&lHEALTH AND REGEN " + toRoman(i + 1)))
        .withDescription(listFromStrings(
          color("&aUpgrade Max Health to &e&l" + (200 * (i + 1)) + " HP"),
          color("&aand Per Wave Regeneration to &e&l" + (2 * (i + 1)) + " HP"),
          color("&e&lCOST:&a %cost_value% orbs"),
          "",
          color("&8Click to purchase")
        ))
        .withApplicableEntity(EntityType.IRON_GOLEM)
        .atSlot(10 + i)
        .putUpgradeData("max_hp", 200 * (i + 1))
        .putUpgradeData("regen", 2 * (i + 1))
        .withCost(150 * (i + 1));
      if (i > 0) {
        builder = builder.andDependsOn("GOLEM_HEALTH_AND_REGEN_" + i);
      }
      registeredUpgrades.add(builder.build());
    }
    for (int i = 0; i < 3; i++) {
      EntityUpgrade.Builder builder = new EntityUpgrade.Builder()
        .withId("GOLEM_DAMAGE_AND_SPEED_" + (i + 1))
        .withName(color("&a&lDAMAGE AND SPEED " + toRoman(i + 1)))
        .withDescription(listFromStrings(
          color("&aIncrease Damage by &e&l" + (3 + (i * 3)) + " DMG"),
          color("&aand Movement Speed by &e&l" + (0.25 + (i * 0.05)) + " MS"),
          color("&e&lCOST:&a %cost_value% orbs"),
          "",
          color("&8Click to purchase")
        ))
        .withApplicableEntity(EntityType.IRON_GOLEM)
        .atSlot(14 + i)
        .putUpgradeData("damage", 3 + (i * 3))
        .putUpgradeData("movement_speed", 0.25 + (i * 0.05))
        .withCost(150 * (i + 1));
      if (i > 0) {
        builder = builder.andDependsOn("GOLEM_DAMAGE_AND_SPEED_" + i);
      }
      registeredUpgrades.add(builder.build());
    }
    for (int i = 0; i < 2; i++) {
      EntityUpgrade.Builder builder = new EntityUpgrade.Builder()
        .withId("GOLEM_UNSTOPPABLE_STREAK_" + (i + 1))
        .withName(color("&6&lUNSTOPPABLE STREAK " + toRoman(i + 1)))
        .withDescription(listFromStrings(
          color("&aEvery kill increases damage"),
          color("&aby &e&l" + (5 + (i * 5)) + "%&a for five seconds"),
          color("&aand consecutive kills reset the timer"),
          color("&e&lCOST:&a %cost_value% orbs"),
          "",
          color("&8Click to purchase")
        ))
        .withApplicableEntity(EntityType.IRON_GOLEM)
        .atSlot(28 + i)
        .putUpgradeData("increase", 5 + (i * 5))
        .withCost(200 * (i + 1));
      if (i > 0) {
        builder = builder.andDependsOn("GOLEM_UNSTOPPABLE_STREAK_" + i);
      }
      registeredUpgrades.add(builder.build());
    }
    registeredUpgrades.add(new EntityUpgrade.Builder()
      .withId("GOLEM_FINAL_DEFENSE")
      .withName(color("&6&lFINAL DEFENSE"))
      .withDescription(listFromStrings(
        color("&aOn death &e&lLETHALLY DAMAGE"),
        color("&aenemies within six blocks and &e&lPERMA"),
        color("&e&lSLOW&a all enemies within nine blocks"),
        color("&e&lCOST:&a %cost_value% orbs"),
        "",
        color("&8Click to purchase")
      ))
      .withApplicableEntity(EntityType.IRON_GOLEM)
      .atSlot(32)
      .withCost(250)
      .build());
    registeredUpgrades.add(new EntityUpgrade.Builder()
      .withId("GOLEM_TOUGHENING")
      .withName(color("&6&lTOUGHENING"))
      .withDescription(listFromStrings(
        color("&aReceive &e&l25% LESS DMG&a while"),
        color("&abelow 40% HP and receive &e&l30% LESS DMG"),
        color("&afrom explosion if recently damaged"),
        color("&aby an explosion within eight seconds"),
        color("&e&lCOST:&a %cost_value% orbs"),
        "",
        color("&8Click to purchase")
      ))
      .withApplicableEntity(EntityType.IRON_GOLEM)
      .atSlot(33)
      .withCost(300)
      .build());
    registeredUpgrades.add(new EntityUpgrade.Builder()
      .withId("GOLEM_IRON_WILL")
      .withName(color("&e&l★ &6&lIRON WILL"))
      .withDescription(listFromStrings(
        color("&aIncrease &e&lDAMAGE and ARMOR"),
        color("&aby &e&l3%&a for every one"),
        color("&avillager killed in game"),
        color("&e&lCOST:&a %cost_value% orbs"),
        "",
        color("&8(★ Special upgrade)"),
        color("&8Click to purchase")
      ))
      .withApplicableEntity(EntityType.IRON_GOLEM)
      .atSlot(34)
      .withCost(300)
      .isSpecial()
      .build());
    registeredUpgrades.add(new EntityUpgrade.Builder()
      .withId("GOLEM_SURVIVOR")
      .withName(color("&b&lWILL TO SURVIVE"))
      .withDescription(listFromStrings(
        color("&aReceive &e&l5% LIFESTEAL&a and"),
        color("&e&l10% MAX HEALTH REGEN&a every wave"),
        color("&aUnlocked for surviving three"),
        color("&aand more waves in game"),
        "",
        color("&8(Survivor upgrade)")
      ))
      .withApplicableEntity(EntityType.IRON_GOLEM)
      .atSlot(48)
      .isHidden(3)
      .build());
    registeredUpgrades.add(new EntityUpgrade.Builder()
      .withId("GOLEM_BANNER_OF_COMMAND")
      .withName(color("&b&lBANNER OF COMMAND"))
      .withDescription(listFromStrings(
        color("&aInspire nearby Iron Golems"),
        color("&awith &e&l10% BONUS DMG"),
        color("&awithin six blocks radius"),
        color("&aUnlocked for surviving five"),
        color("&aand more waves in game"),
        "",
        color("&8(Survivor upgrade)")
      ))
      .withApplicableEntity(EntityType.IRON_GOLEM)
      .atSlot(49)
      .isHidden(5)
      .build());
    registeredUpgrades.add(new EntityUpgrade.Builder()
      .withId("GOLEM_LAMENT")
      .withName(color("&b&lREMEMBRANCE LAMENT"))
      .withDescription(listFromStrings(
        color("&aOn death &e&lKILL EVERY"),
        color("&aalive enemy and &e&lHEAL&a every"),
        color("&aalive ally (villager, pet & player)"),
        color("&ato 100% HP as a final gift"),
        color("&aUnlocked for surviving seven"),
        color("&aand more waves in game"),
        "",
        color("&8(Survivor upgrade)")
      ))
      .withApplicableEntity(EntityType.IRON_GOLEM)
      .atSlot(50)
      .isHidden(7)
      .build());
  }

  private void registerWolfUpgrades() {
    for (int i = 0; i < 3; i++) {
      EntityUpgrade.Builder builder = new EntityUpgrade.Builder()
        .withId("WOLF_HEALTH_AND_REGEN_" + (i + 1))
        .withName(color("&a&lHEALTH AND REGEN " + toRoman(i + 1)))
        .withDescription(listFromStrings(
          color("&aUpgrade Max Health to &e&l" + (125 * (i + 1)) + " HP"),
          color("&aand Per Wave Regeneration to &e&l" + (1.5 * (i + 1)) + " HP"),
          color("&e&lCOST:&a %cost_value% orbs"),
          "",
          color("&8Click to purchase")
        ))
        .withApplicableEntity(EntityType.WOLF)
        .atSlot(10 + i)
        .putUpgradeData("max_hp", 125 * (i + 1))
        .putUpgradeData("regen", 1.5 * (i + 1))
        .withCost(150 * (i + 1));
      if (i > 0) {
        builder = builder.andDependsOn("WOLF_HEALTH_AND_REGEN_" + i);
      }
      registeredUpgrades.add(builder.build());
    }
    for (int i = 0; i < 3; i++) {
      EntityUpgrade.Builder builder = new EntityUpgrade.Builder()
        .withId("WOLF_DAMAGE_AND_SPEED_" + (i + 1))
        .withName(color("&a&lDAMAGE AND SPEED " + toRoman(i + 1)))
        .withDescription(listFromStrings(
          color("&aIncrease Damage by &e&l" + (2 + (i * 2)) + " DMG"),
          color("&aand Movement Speed by &e&l" + (0.25 + (i * 0.05)) + " MS"),
          color("&e&lCOST:&a %cost_value% orbs"),
          "",
          color("&8Click to purchase")
        ))
        .withApplicableEntity(EntityType.WOLF)
        .atSlot(14 + i)
        .putUpgradeData("damage", 2 + (i * 2))
        .putUpgradeData("movement_speed", 0.25 + (i * 0.05))
        .withCost(150 * (i + 1));
      if (i > 0) {
        builder = builder.andDependsOn("WOLF_DAMAGE_AND_SPEED_" + i);
      }
      registeredUpgrades.add(builder.build());
    }
    for (int i = 0; i < 2; i++) {
      EntityUpgrade.Builder builder = new EntityUpgrade.Builder()
        .withId("WOLF_SWARM_AWARENESS_" + (i + 1))
        .withName(color("&6&lSWARM AWARENESS " + toRoman(i + 1)))
        .withDescription(listFromStrings(
          color("&aWolf deals &e&l" + (3 + (i * 2)) + "% BONUS DMG&a per"),
          color("&awolf nearby within three blocks"),
          color("&aradius capped at 30% BONUS DMG"),
          color("&e&lCOST:&a %cost_value% orbs"),
          "",
          color("&8Click to purchase")
        ))
        .withApplicableEntity(EntityType.WOLF)
        .atSlot(28 + i)
        .putUpgradeData("increase", 3 + (i * 2))
        .withCost(200 * (i + 1));
      if (i > 0) {
        builder = builder.andDependsOn("WOLF_SWARM_AWARENESS_" + i);
      }
      registeredUpgrades.add(builder.build());
    }
    registeredUpgrades.add(new EntityUpgrade.Builder()
      .withId("WOLF_BLOODY_REVENGE")
      .withName(color("&6&lBLOODY REVENGE"))
      .withDescription(listFromStrings(
        color("&aOn ally wolf death &e&lENRAGE"),
        color("&athe wolf to receive &e&l5% LIFESTEAL"),
        color("&e&l10% BONUS DAMAGE&a and &e&lKNOCKBACK"),
        color("&afor five seconds"),
        color("&e&lCOST:&a %cost_value% orbs"),
        "",
        color("&8Click to purchase")
      ))
      .withApplicableEntity(EntityType.WOLF)
      .atSlot(32)
      .withCost(250)
      .build());
    registeredUpgrades.add(new EntityUpgrade.Builder()
      .withId("WOLF_WOLF_PACK")
      .withName(color("&6&lWOLF PACK"))
      .withDescription(listFromStrings(
        color("&aAttacking an enemy &e&lINCREASES"),
        color("&e&lDAMAGE&a dealt by other wolves"),
        color("&aby &e&l10% DMG&a for five seconds"),
        color("&e&lCOST:&a %cost_value% orbs"),
        "",
        color("&8Click to purchase")
      ))
      .withApplicableEntity(EntityType.WOLF)
      .atSlot(33)
      .withCost(300)
      .build());
    registeredUpgrades.add(new EntityUpgrade.Builder()
      .withId("WOLF_DEEP_WOUNDS")
      .withName(color("&e&l★ &6&lDEEP WOUNDS"))
      .withDescription(listFromStrings(
        color("&aReceive &e&l25% CHANCE&a when"),
        color("&aattacking an enemy to apply"),
        color("&e&lBLEEDING&a for three seconds"),
        color("&e&lCOST:&a %cost_value% orbs"),
        "",
        color("&8(★ Special upgrade)"),
        color("&8Click to purchase")
      ))
      .withApplicableEntity(EntityType.WOLF)
      .atSlot(34)
      .withCost(300)
      .isSpecial()
      .build());
    registeredUpgrades.add(new EntityUpgrade.Builder()
      .withId("WOLF_ROBBER")
      .withName(color("&b&lROBBER"))
      .withDescription(listFromStrings(
        color("&aGenerate &e&l4-12 BONUS ORBS"),
        color("&ato owner for every enemy killed"),
        color("&aUnlocked for surviving three"),
        color("&aand more waves in game"),
        "",
        color("&8(Survivor upgrade)")
      ))
      .withApplicableEntity(EntityType.WOLF)
      .atSlot(48)
      .isHidden(3)
      .build());
    registeredUpgrades.add(new EntityUpgrade.Builder()
      .withId("WOLF_ALPHA")
      .withName(color("&b&lALPHA WOLF"))
      .withDescription(listFromStrings(
        color("&aInspire nearby Wolves"),
        color("&awith &e&l10% BONUS DMG"),
        color("&awithin six blocks radius"),
        color("&aUnlocked for surviving five"),
        color("&aand more waves in game"),
        "",
        color("&8(Survivor upgrade)")
      ))
      .withApplicableEntity(EntityType.WOLF)
      .atSlot(49)
      .isHidden(5)
      .build());
    registeredUpgrades.add(new EntityUpgrade.Builder()
      .withId("WOLF_MORE_THAN_DEATH")
      .withName(color("&b&lMORE THAN DEATH"))
      .withDescription(listFromStrings(
        color("&aUpon receiving fatal damage"),
        color("&abe &e&lUNKILLABLE&a and receive"),
        color("&e&l75% BONUS DMG&a for five seconds"),
        color("&athen perish into the shadows"),
        color("&aUnlocked for surviving seven"),
        color("&aand more waves in game"),
        "",
        color("&8(Survivor upgrade)")
      ))
      .withApplicableEntity(EntityType.WOLF)
      .atSlot(50)
      .isHidden(7)
      .build());
  }

  private List<String> listFromStrings(String... lines) {
    return new ArrayList<>(Arrays.asList(lines));
  }

  public List<EntityUpgrade> getRegisteredUpgrades() {
    return registeredUpgrades;
  }

  @Nullable
  public EntityUpgrade getUpgrade(String id) {
    for (EntityUpgrade upgrade : registeredUpgrades) {
      if (upgrade.getId().equals(id)) {
        return upgrade;
      }
    }
    return null;
  }

  /**
   * Opens menu with upgrades for wolf or golem
   *
   * @param livingEntity entity to check upgrades for
   * @param player       player who will see inventory
   */
  public void openUpgradeMenu(LivingEntity livingEntity, Player player) {
    NormalFastInv gui = new NormalFastInv(6 * 9, color("&e&lENTITY UPGRADES"));
    gui.addClickHandler(inventoryClickEvent -> inventoryClickEvent.setCancelled(true));
    User user = plugin.getUserManager().getUser(player);

    for (EntityUpgrade upgrade : registeredUpgrades) {
      if (!upgrade.getApplicableEntity().equals(livingEntity.getType())) {
        continue;
      }
      if (upgrade.isSpecial() && !(user.getKit() instanceof PetsFriend)) {
        continue;
      }
      gui.setItem(upgrade.getSlot(), upgradeAsItem(livingEntity, upgrade), event -> {
        event.setCancelled(true);
        if (upgrade.isHidden()) {
          player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1, 1);
          return;
        }
        if (livingEntity.hasMetadata(upgrade.getMetadataKey())) {
          player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1, 1);
          return;
        }
        if (upgrade.getDependsOn() != null && !livingEntity.hasMetadata(upgrade.getDependencyMetadataKey())) {
          player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1, 1);
          return;
        }
        int orbs = user.getStatistic("ORBS");
        if (orbs < upgrade.getCost()) {
          new MessageBuilder("UPGRADE_MENU_CANNOT_AFFORD").asKey().player(player).sendPlayer();
          player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1, 1);
          return;
        }
        user.setStatistic("ORBS", orbs - upgrade.getCost());
        player.sendMessage(color("&aSuccessfully upgraded entity!"));
        user.getArena().changeArenaOptionBy("TOTAL_ORBS_SPENT", orbs);
        applyUpgradeWithVisuals(livingEntity, player, upgrade);

        openUpgradeMenu(livingEntity, player);
      });
      applyStatisticsBookOfEntityToGui(gui, livingEntity);
    }
    gui.open(player);
  }

  private void applyStatisticsBookOfEntityToGui(NormalFastInv gui, LivingEntity entity) {
    List<String> lore = new ArrayList<>();
    double healPower = ArenaManager.DEFAULT_PET_HEAL_POWER;
    double defaultHeal = entity instanceof IronGolem ? 2.0 : 1.0;
    String prefix = entity instanceof IronGolem ? "GOLEM_" : "WOLF_";
    for (int i = 3; i > 0; i--) {
      EntityUpgrade heal = plugin.getEntityUpgradeManager().getUpgrade(prefix + "HEALTH_AND_REGEN_" + i);
      if (entity.hasMetadata(heal.getMetadataKey())) {
        defaultHeal += heal.getUpgradeData().getOrDefault("regen", 0.0);
        break;
      }
    }
    healPower = healPower * defaultHeal;
    lore.add("&a&lHEALTH: &7" + entity.getHealth() + "/" + VersionUtils.getMaxHealth(entity) + " (&a&lREGEN &7" + healPower + " HP/wave)");
    lore.add("&a&lDAMAGE: &7" + entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue() + " DMG");
    lore.add("&a&lMOVEMENT SPEED: &7" + entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue() + " MS");
    int totalKills = 0;
    if (entity.hasMetadata("VD_ENTITY_KILLS")) {
      totalKills = entity.getMetadata("VD_ENTITY_KILLS").get(0).asInt();
    }
    lore.add("&a&lKILLS: &7" + totalKills);
    gui.setItem(4, new ItemBuilder(new ItemStack(Material.BOOK))
      .name(color("&e&lCURRENT STATISTICS"))
      .lore(
        lore.stream()
          .map(this::color)
          .collect(Collectors.toList())
      )
      .build());
  }

  private ItemStack upgradeAsItem(LivingEntity entity, EntityUpgrade upgrade) {
    Material icon;
    String name = upgrade.getName();
    List<String> lore = upgrade.getDescription();
    if (entity.hasMetadata(upgrade.getMetadataKey())) {
      icon = Material.LIME_DYE;
      name = color("&a&l✓ ") + name;
    } else {
      if (upgrade.isHidden()) {
        icon = Material.MAGENTA_DYE;
        name = color("&c&lSECRET UPGRADE");
        lore = listFromStrings(
          color("&aThis upgrade will be"),
          color("&aautomatically unlocked when"),
          color("&aentity survives " + upgrade.getSurviveWaves() + " waves")
        );
      } else {
        icon = Material.GRAY_DYE;
        name = color("&c&l✘ ") + name;
      }
    }
    lore = lore.stream()
      .map(line -> line.replace("%cost_value%", String.valueOf(upgrade.getCost())))
      .collect(Collectors.toList());
    return new ItemBuilder(icon)
      .name(name)
      .lore(lore)
      .build();
  }

  private String toRoman(int number) {
    return switch (number) {
      case 1 -> "I";
      case 2 -> "II";
      case 3 -> "III";
      default -> "?";
    };
  }

  private String color(String text) {
    return ChatColor.translateAlternateColorCodes('&', text);
  }

  public boolean applyUpgradeSilent(Entity entity, Player player, EntityUpgrade upgrade) {
    List<MetadataValue> meta = entity.getMetadata(upgrade.getMetadataKey());

    if (!meta.isEmpty()) {
      return false;
    }
    entity.setMetadata(upgrade.getMetadataKey(), new FixedMetadataValue(plugin, true));
    applyUpgradeEffect(entity, player, upgrade, true);
    applyUpgradeOnEntity(entity, upgrade);
    return true;
  }

  /**
   * Applies upgrade for target entity
   *
   * @param entity  target entity
   * @param player  player which upgraded target
   * @param upgrade upgrade to apply
   * @return true if applied successfully, false if cannot be applied
   */
  public boolean applyUpgradeWithVisuals(Entity entity, Player player, EntityUpgrade upgrade) {
    List<MetadataValue> meta = entity.getMetadata(upgrade.getMetadataKey());

    if (!meta.isEmpty()) {
      return false;
    }
    entity.setMetadata(upgrade.getMetadataKey(), new FixedMetadataValue(plugin, true));
    applyUpgradeEffect(entity, player, upgrade, false);
    applyUpgradeOnEntity(entity, upgrade);
    return true;
  }

  private void applyUpgradeEffect(Entity entity, Player player, EntityUpgrade upgrade, boolean silent) {
    if (!silent) {
      applyParticlesAndSounds(entity, upgrade);
    }
    int totalLevel = 0;
    for (EntityUpgrade entityUpgrade : registeredUpgrades) {
      if (entityUpgrade.getApplicableEntity() == entity.getType() && entity.hasMetadata(entityUpgrade.getMetadataKey())) {
        totalLevel++;
      }
    }
    if (entity instanceof Golem) {
      entity.setCustomName(new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_WAVE_ENTITIES_GOLEM_NAME").asKey().integer(totalLevel).player(player).build());
    } else if (entity instanceof Wolf) {
      entity.setCustomName(new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_WAVE_ENTITIES_WOLF_NAME").asKey().integer(totalLevel).player(player).build());
    }
    User user = plugin.getUserManager().getUser(player);
    ChatColor targetColor = null;
    if (totalLevel >= 13) {
      targetColor = ChatColor.RED;
    } else if (totalLevel == 12) {
      targetColor = ChatColor.GOLD;
    } else if (totalLevel == 11) {
      targetColor = ChatColor.YELLOW;
    } else if (totalLevel == 10) {
      targetColor = ChatColor.WHITE;
    }
    if (targetColor != null) {
      try {
        for (Player arenaPlayer : user.getArena().getPlayers()) {
          plugin.getGlowingEntities().setGlowing(entity, arenaPlayer, ChatColor.GOLD);
        }
      } catch (Exception ignored) {
      }
    }
  }

  private void applyUpgradeOnEntity(Entity entity, EntityUpgrade upgrade) {
    LivingEntity living = (LivingEntity) entity;
    if (upgrade.getId().contains("HEALTH_AND_REGEN")) {
      VersionUtils.setMaxHealth(living, upgrade.getUpgradeData().get("max_hp"));
      living.setHealth(VersionUtils.getMaxHealth(living));
    } else if (upgrade.getId().contains("DAMAGE_AND_SPEED")) {
      double baseDamage = living.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue();
      CreatureUtils.getCreatureInitializer().applyDamageModifier((LivingEntity) entity, baseDamage + upgrade.getUpgradeData().get("damage"));
      CreatureUtils.getCreatureInitializer().applySpeedModifier((LivingEntity) entity, upgrade.getUpgradeData().get("movement_speed"));
    }
  }

  private void applyParticlesAndSounds(Entity entity, EntityUpgrade upgrade) {
    Location targetLocation = entity.getLocation().clone();
    if (entity instanceof IronGolem) {
      targetLocation = targetLocation.add(0, 1.6, 0);
    } else {
      targetLocation = targetLocation.add(0, 0.75, 0);
    }
    if (upgrade.getId().contains("HEALTH_AND_REGEN")) {
      VersionUtils.sendParticles("HEART", null, targetLocation, 15, 0.7, 0.7, 0.7);
    } else if (upgrade.getId().contains("DAMAGE_AND_SPEED")) {
      VersionUtils.sendParticles("CRIT", null, targetLocation, 15, 0.7, 0.7, 0.7);
    } else if (upgrade.getId().contains("UNSTOPPABLE_STREAK")) {
      VersionUtils.sendParticles("EXPLOSION_LARGE", null, targetLocation, 15, 0.7, 0.7, 0.7);
    } else if (upgrade.getId().contains("FINAL_DEFENSE") || upgrade.getId().contains("LAMENT")) {
      VersionUtils.sendParticles("DAMAGE_INDICATOR", null, targetLocation, 15, 0.7, 0.7, 0.7);
    } else if (upgrade.getId().contains("SURVIVOR") || upgrade.getId().contains("TOUGHENING")) {
      VersionUtils.sendParticles("DRIP_LAVA", null, targetLocation, 15, 0.7, 0.7, 0.7);
    } else if (upgrade.getId().contains("BANNER_OF_COMMAND") || upgrade.getId().contains("ALPHA")) {
      VersionUtils.sendParticles("ENCHANTMENT_TABLE", null, targetLocation, 15, 0.7, 0.7, 0.7);
    } else if (upgrade.getId().contains("SWARM_AWARENESS")) {
      VersionUtils.sendParticles("SOUL", null, targetLocation, 15, 0.7, 0.7, 0.7);
    } else if (upgrade.getId().contains("BLOODY_REVENGE") || upgrade.getId().contains("PACK")) {
      VersionUtils.sendParticles("VILLAGER_ANGRY", null, targetLocation, 15, 0.7, 0.7, 0.7);
    } else if (upgrade.getId().contains("DEEP_WOUNDS") || upgrade.getId().contains("MORE_THAN_DEATH")) {
      VersionUtils.sendParticles("GLOW", null, targetLocation, 15, 0.7, 0.7, 0.7);
    } else if (upgrade.getId().contains("ROBBER")) {
      VersionUtils.sendParticles("SNEEZE", null, targetLocation, 15, 0.7, 0.7, 0.7);
    }
    if (upgrade.isHidden()) {
      targetLocation.getWorld().playSound(targetLocation, Sound.ENTITY_PLAYER_LEVELUP, 1, 0.25f);
    } else if (upgrade.isSpecial()) {
      targetLocation.getWorld().playSound(targetLocation, Sound.ENTITY_PLAYER_LEVELUP, 1, 0.5f);
    } else {
      targetLocation.getWorld().playSound(targetLocation, Sound.ENTITY_PLAYER_LEVELUP, 1, 0.5f);
    }
  }

}
