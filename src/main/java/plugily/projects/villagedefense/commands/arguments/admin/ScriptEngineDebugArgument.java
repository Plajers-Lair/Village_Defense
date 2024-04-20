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

package plugily.projects.villagedefense.commands.arguments.admin;

import plugily.projects.villagedefense.commands.arguments.ArgumentsRegistry;

/**
 * @author Plajer
 * <p>
 * Created at 03.09.2023
 */
public class ScriptEngineDebugArgument {

  public ScriptEngineDebugArgument(ArgumentsRegistry registry) {
    /*registry.mapArgument("villagedefenseadmin", new LabeledCommandArgument("scripttest", Arrays.asList("villagedefense.admin.scripttest"),
      CommandArgument.ExecutorType.PLAYER, new LabelData("/vda scripttest &c[text]", "/vda scripttest",
      "&7Perform a rewards script engine test\n&6Permission: &7villagedefense.admin.scripttest")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if(!registry.getPlugin().getBukkitHelper().checkIsInGameInstance(player)) {
          return;
        }
        Arena arena = (Arena) registry.getPlugin().getArenaRegistry().getArena(player);
        StringBuilder combined = new StringBuilder();
        for(String argument : args) {
          if(argument.equals("scripttest")) {
            continue;
          }
          combined = combined.append(argument);
        }
        javax.script.ScriptEngine engine;
        try {
          engine = new NashornScriptEngineFactory().getScriptEngine();
        } catch(Exception ex) {
          player.sendMessage("Engine failed to initialize");
          player.sendMessage(ex.getMessage());
          return;
        }
        engine.put("player", player);
        engine.put("server", Bukkit.getServer());
        engine.put("arena", arena);
        engine.put("plugin", registry.getPlugin());
        try {
          engine.eval(combined.toString());
        } catch(ScriptException ex) {
          player.sendMessage("Evaluation failed at " + ex.getColumnNumber() + ":" + ex.getLineNumber() + ", details:");
          player.sendMessage(ex.getMessage());
        }
      }
    });*/
  }

}
