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
import org.bukkit.Sound;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import plugily.projects.minigamesbox.classic.arena.ArenaState;
import plugily.projects.minigamesbox.classic.arena.PluginArena;
import plugily.projects.minigamesbox.classic.handlers.language.Message;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.handlers.language.MessageManager;
import plugily.projects.minigamesbox.classic.handlers.language.TitleBuilder;
import plugily.projects.minigamesbox.classic.kits.basekits.PremiumKit;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.minigamesbox.classic.utils.helper.ArmorHelper;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.classic.utils.helper.WeaponHelper;
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

/**
 * Created by Tom on 18/08/2014.
 */
public class CleanerKit extends PremiumKit implements Listener, AbilitySource, ChatDisplayable, ScoreboardModifiable {

  private static final String LANGUAGE_ACCESSOR = "KIT_CONTENT_CLEANER_";
  //this metadata must be given to every enemy killed by poplust to avoid recursive calls and stack overflow in the listener
  private static final String KILL_METADATA = "VD_POPLUST_DEATH";
  private final Random random = new Random();

  public CleanerKit() {
    registerMessages();
    setName(new MessageBuilder(LANGUAGE_ACCESSOR + "NAME").asKey().build());
    setKey("Cleaner");
    List<String> description = getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "DESCRIPTION");
    setDescription(description);
    getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
    getPlugin().getKitRegistry().registerKit(this);
    schedulePopAwe();
    ((Main) getPlugin()).getKitsMenu().registerKit(KitsMenu.KitCategory.DAMAGE_DEALER, this);
  }

  @Override
  @SuppressWarnings("UnnecessaryUnicodeEscape")
  public String getChatPrefix() {
    return "\u0042";
  }

  private void registerMessages() {
    MessageManager manager = getPlugin().getMessageManager();
    manager.registerMessage(LANGUAGE_ACCESSOR + "NAME", new Message("Kit.Content.Cleaner.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "DESCRIPTION", new Message("Kit.Content.Cleaner.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_CLEANSING_STICK_NAME", new Message("Kit.Content.Cleaner.Game-Item.Cleansing-Stick.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_CLEANSING_STICK_DESCRIPTION", new Message("Kit.Content.Cleaner.Game-Item.Cleansing-Stick.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_CLEANSING_STICK_ACTIVATE", new Message("Kit.Content.Cleaner.Game-Item.Cleansing-Stick.Activate", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_POPLUST_NAME", new Message("Kit.Content.Cleaner.Game-Item.Poplust.Name", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_POPLUST_DESCRIPTION", new Message("Kit.Content.Cleaner.Game-Item.Poplust.Description", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_POPLUST_ACTIVATE", new Message("Kit.Content.Cleaner.Game-Item.Poplust.Activate", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_POPLUST_ACTIVE_ACTION_BAR", new Message("Kit.Content.Cleaner.Game-Item.Poplust.Active-Action-Bar", ""));
    manager.registerMessage(LANGUAGE_ACCESSOR + "GAME_ITEM_POPLUST_ACTIVE_TITLE", new Message("Kit.Content.Cleaner.Game-Item.Poplust.Active-Title", ""));
  }

  private void schedulePopAwe() {
    //passive only works for alive players not dead ones
    Bukkit.getScheduler().runTaskTimer(getPlugin(), () -> {
      for(PluginArena pluginArena : getPlugin().getArenaRegistry().getArenas()) {
        Arena arena = (Arena) pluginArena;
        if(arena.getArenaState() != ArenaState.IN_GAME || arena.getEnemies().isEmpty()) {
          continue;
        }
        for(Player player : arena.getPlayersLeft()) {
          if(!(getPlugin().getUserManager().getUser(player).getKit() instanceof CleanerKit)) {
            continue;
          }
          executeRandomPops(arena, player, (int) Settings.PASSIVE_POP_COUNT.getForArenaState(arena));
        }
      }
    }, 20 * 5L, 20 * 5L);
  }

  private void executeRandomPops(Arena arena, Player player, int amount) {
    for(int i = 0; i < amount; i++) {
      Creature enemy = arena.getEnemies().get(random.nextInt(arena.getEnemies().size()));
      if (enemy.hasMetadata("VD_UNPOPPABLE")) {
        KitHelper.maxHealthPercentDamage(enemy, player, 15.0);
        VersionUtils.sendParticles("LAVA", arena.getPlayers(), enemy.getLocation(), 20);
        continue;
      }
      if(KitHelper.executeEnemy(enemy, player)) {
        VersionUtils.sendParticles("LAVA", arena.getPlayers(), enemy.getLocation(), 20);
      }
    }
    player.playSound(player, Sound.BLOCK_LAVA_POP, 1, 1);
  }

  @Override
  public boolean isUnlockedByPlayer(Player player) {
    return getPlugin().getPermissionsManager().hasPermissionString("KIT_PREMIUM_UNLOCK", player) || player.hasPermission("villagedefense.kit.cleaner");
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
        ScoreboardModifiable.renderAbilityCooldown(user, "cleaner_cleansing", "Cleansing Wand", wave, KitSpecifications.GameTimeState.EARLY),
        ScoreboardModifiable.renderAbilityCooldown(user, "cleaner_poplust", "Poplust", wave, KitSpecifications.GameTimeState.MID),
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
    ArmorHelper.setColouredArmor(Color.YELLOW, player);
    player.getInventory().addItem(WeaponHelper.getUnBreakingSword(WeaponHelper.ResourceType.WOOD, 10));

    player.getInventory().setItem(3, new ItemBuilder(new ItemStack(XMaterial.BLAZE_ROD.parseMaterial()))
      .name(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_CLEANSING_STICK_NAME").asKey().build())
      .lore(getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_CLEANSING_STICK_DESCRIPTION"))
      .build());
    player.getInventory().setItem(4, new ItemBuilder(new ItemStack(XMaterial.SUNFLOWER.parseMaterial()))
      .name(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_POPLUST_NAME").asKey().build())
      .lore(getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_POPLUST_DESCRIPTION"))
      .build());
    player.getInventory().setItem(5, new ItemStack(Material.SADDLE));
    player.getInventory().setItem(8, new ItemStack(Material.COOKED_BEEF, 10));
  }

  @Override
  public Material getMaterial() {
    return Material.BLAZE_POWDER;
  }

  @Override
  public void reStock(Player player) {
    Arena arena = (Arena) getPlugin().getUserManager().getUser(player).getArena();
    if(arena.getWave() == KitSpecifications.GameTimeState.MID.getStartWave()) {
      new MessageBuilder("KIT_ABILITY_UNLOCKED").asKey().value(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_POPLUST_NAME").asKey().build()).send(player);
      new MessageBuilder("KIT_ABILITY_POWER_INCREASED").asKey().value(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_CLEANSING_STICK_NAME").asKey().build()).send(player);
      new MessageBuilder("KIT_PASSIVE_POWER_INCREASED").asKey().send(player);
    } else if(arena.getWave() == KitSpecifications.GameTimeState.LATE.getStartWave()) {
      new MessageBuilder("KIT_ABILITY_POWER_INCREASED").asKey().value(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_POPLUST_NAME").asKey().build()).send(player);
      new MessageBuilder("KIT_ABILITY_POWER_INCREASED").asKey().value(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_CLEANSING_STICK_NAME").asKey().build()).send(player);
      new MessageBuilder("KIT_PASSIVE_POWER_INCREASED").asKey().send(player);
    }
  }

  @Override
  @EventHandler
  public void onAbilityCast(PlugilyPlayerInteractEvent event) {
    if(!KitHelper.isInGameWithKitAndItemInHand(event.getPlayer(), CleanerKit.class)) {
      return;
    }
    ItemStack stack = VersionUtils.getItemInHand(event.getPlayer());
    User user = getPlugin().getUserManager().getUser(event.getPlayer());
    String displayName = ComplementAccessor.getComplement().getDisplayName(stack.getItemMeta());
    if(displayName.equals(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_CLEANSING_STICK_NAME").asKey().build())) {
      onCleansingCast(stack, user);
    } else if(displayName.equals(new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_POPLUST_NAME").asKey().build())) {
      onPoplustPreCast(stack, user);
    }
  }

  private void onCleansingCast(ItemStack stack, User user) {
    if(!user.checkCanCastCooldownAndMessage("cleaner_cleansing")) {
      return;
    }
    int cooldown = getCleansingCooldown((Arena) user.getArena());
    user.setCooldown("cleaner_cleansing", cooldown);
    VersionUtils.setMaterialCooldown(user.getPlayer(), stack.getType(), cooldown * 20);

    new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_CLEANSING_STICK_ACTIVATE").asKey().send(user.getPlayer());
    int popsPerCycle = 5;
    int splitAmount = (int) Settings.CLEANSING_POP_COUNT.getForArenaState((Arena) user.getArena()) / popsPerCycle;
    for (int i = 1; i < splitAmount + 1; i++) {
      Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
        XSound.BLOCK_LAVA_POP.play(user.getPlayer());
        executeRandomPops((Arena) user.getArena(), user.getPlayer(), popsPerCycle);
      }, 10L * splitAmount * i);
    }
  }

  private int getCleansingCooldown(Arena arena) {
    switch(KitSpecifications.getTimeState(arena)) {
      case LATE:
        return getKitsConfig().getInt("Kit-Cooldown.Cleaner.Cleansing-Wand.III", 35);
      case MID:
        return getKitsConfig().getInt("Kit-Cooldown.Cleaner.Cleansing-Wand.II", 40);
      case EARLY:
      default:
        return getKitsConfig().getInt("Kit-Cooldown.Cleaner.Cleansing-Wand.I", 45);
    }
  }

  private void onPoplustPreCast(ItemStack stack, User user) {
    if(!user.checkCanCastCooldownAndMessage("cleaner_poplust")) {
      return;
    }
    if(KitSpecifications.getTimeState((Arena) user.getArena()) == KitSpecifications.GameTimeState.EARLY) {
      new MessageBuilder("KIT_LOCKED_TILL").asKey().integer(16).send(user.getPlayer());
      return;
    }
    onPoplustCast(stack, user);
  }

  private void onPoplustCast(ItemStack stack, User user) {
    Arena arena = (Arena) user.getArena();
    int cooldown = getKitsConfig().getInt("Kit-Cooldown.Cleaner.Poplust", 75);
    user.setCooldown("cleaner_poplust", cooldown);
    final int castTime = (int) Settings.POPLUST_CAST_TIME.getForArenaState(arena);
    user.setCooldown("cleaner_poplust_running", castTime);
    new MessageBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_POPLUST_ACTIVATE").asKey().send(user.getPlayer());

    List<String> messages = getPlugin().getLanguageManager().getLanguageListFromKey(LANGUAGE_ACCESSOR + "GAME_ITEM_POPLUST_ACTIVE_ACTION_BAR");
    Player player = user.getPlayer();
    player.setMetadata("VD_CLEANER_POPLUST", new FixedMetadataValue(getPlugin(), true));
    new BukkitRunnable() {
      int tick = 0;
      int messageIndex = 0;

      @Override
      public void run() {
        if(tick % 10 == 0) {
          VersionUtils.sendActionBar(player, messages.get(messageIndex)
            .replace("%number%", String.valueOf(user.getCooldown("cleaner_poplust_running"))));
          new TitleBuilder(LANGUAGE_ACCESSOR + "GAME_ITEM_POPLUST_ACTIVE_TITLE").asKey().send(player);
          messageIndex++;
          if(messageIndex > messages.size() - 1) {
            messageIndex = 0;
          }
        }
        if (tick >= 20 * castTime || !getPlugin().getArenaRegistry().isInArena(user.getPlayer()) || user.isSpectator()) {
          //reset action bar
          VersionUtils.sendActionBar(player, "");
          cancel();
          player.removeMetadata("VD_CLEANER_POPLUST", getPlugin());
          return;
        }
        tick++;
      }
    }.runTaskTimer(getPlugin(), 0, 1);

    KitHelper.scheduleAbilityCooldown(stack, user.getPlayer(), castTime, cooldown);
  }

  @EventHandler
  public void onPoplustDamage(EntityDamageByEntityEvent event) {
    if(!(event.getDamager() instanceof Player)) {
      return;
    }
    Player damager = (Player) event.getDamager();
    if(getPlugin().getArenaRegistry().getArena(damager) == null) {
      return;
    }
    User user = getPlugin().getUserManager().getUser(damager);
    if (!damager.hasMetadata("VD_CLEANER_POPLUST") || user.isSpectator() || !NewCreatureUtils.isEnemy(event.getEntity())
      || event.getEntity().hasMetadata(KILL_METADATA)) {
      return;
    }
    LivingEntity entity = (LivingEntity) event.getEntity();
    Arena arena = ((Arena) user.getArena());
    arena.getAssistHandler().doRegisterBuffOnAlly(damager, user.getPlayer());
    if (entity.hasMetadata("VD_UNPOPPABLE")) {
      KitHelper.maxHealthPercentDamage(entity, user.getPlayer(), 15.0);
      XSound.BLOCK_LAVA_POP.play(user.getPlayer());
      VersionUtils.sendParticles("LAVA", user.getArena().getPlayers(), entity.getLocation(), 15);
      return;
    }
    if (KitHelper.canExecuteEnemy(entity, user.getPlayer())) {
      entity.setMetadata(KILL_METADATA, new FixedMetadataValue(getPlugin(), true));
      KitHelper.executeEnemy(entity, user.getPlayer());
      XSound.BLOCK_LAVA_POP.play(user.getPlayer());
      VersionUtils.sendParticles("LAVA", user.getArena().getPlayers(), entity.getLocation(), 15);
    }
  }

  private enum Settings {
    PASSIVE_POP_COUNT(1, 2, 3), CLEANSING_POP_COUNT(8, 11, 14), POPLUST_CAST_TIME(0, 7, 11);

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
