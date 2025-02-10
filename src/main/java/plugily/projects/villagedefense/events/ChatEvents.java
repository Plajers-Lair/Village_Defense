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

package plugily.projects.villagedefense.events;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.kits.ChatDisplayable;

//Village Defense Gold - backported into the main plugin
public class ChatEvents implements Listener, ChatRenderer {

  private final Main plugin;

  public ChatEvents(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }


  @EventHandler
  public void onChatIngame(AsyncChatEvent event) {
    event.renderer(this);
  }

  @Override
  public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
    Arena arena = plugin.getArenaRegistry().getArena(source);
    if (!(viewer instanceof Player viewerPlayer)) {
      return ChatRenderer.defaultRenderer().render(source, sourceDisplayName, message, viewer);
    }
    Arena viewerArena = plugin.getArenaRegistry().getArena(viewerPlayer);
    //if target is in arena - ignore messages outside their arena unless spychat is enabled
    if (viewerArena != null) {
      if (viewerArena.equals(arena) || plugin.getArgumentsRegistry().getSpyChat().isSpyChatEnabled(viewerPlayer)) {
        return formatChatPlaceholders(source, arena, sourceDisplayName, message);
      }
      return Component.empty();
    }
    //return non formatted message outside arena
    return ChatRenderer.defaultRenderer().render(source, sourceDisplayName, message, viewer);
  }

  private Component formatChatPlaceholders(Player player, Arena arena, Component displayName, Component message) {
    Component formatted = Component.text(new MessageBuilder("IN_GAME_GAME_CHAT_FORMAT").asKey().arena(arena).build());
    User user = plugin.getUserManager().getUser(player);
    if (user.getKit() != null) {
      formatted = formatted.replaceText(
        TextReplacementConfig
          .builder()
          .match("%kit%")
          .replacement(Component.text(((ChatDisplayable) user.getKit()).getChatPrefix()).font(Key.key("villagedefense")))
          .build()
      );
    } else {
      formatted = formatted.replaceText(
        TextReplacementConfig
          .builder()
          .match("%kit%")
          .replacement(Component.text("-"))
          .build()
      );
    }
    formatted = formatted
      .replaceText(TextReplacementConfig.builder().match("%player%").replacement(displayName).build())
      .replaceText(TextReplacementConfig.builder().match("%user_statistic_level%").replacement(Component.text(user.getStatistic(plugin.getStatsStorage().getStatisticType("LEVEL")))).build())
      .replaceText(TextReplacementConfig.builder().match("%message%").replacement(message).build());
    return formatted;
  }
}
