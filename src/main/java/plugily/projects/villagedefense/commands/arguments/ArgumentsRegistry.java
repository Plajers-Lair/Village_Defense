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

package plugily.projects.villagedefense.commands.arguments;

import plugily.projects.minigamesbox.classic.commands.arguments.PluginArgumentsRegistry;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.commands.arguments.admin.AddOrbsArgument;
import plugily.projects.villagedefense.commands.arguments.admin.ClearEntitiesArgument;
import plugily.projects.villagedefense.commands.arguments.admin.RespawnArgument;
import plugily.projects.villagedefense.commands.arguments.admin.ScriptEngineDebugArgument;
import plugily.projects.villagedefense.commands.arguments.admin.SetPriceArgument;
import plugily.projects.villagedefense.commands.arguments.admin.SetWaveUnlockArgument;
import plugily.projects.villagedefense.commands.arguments.admin.arena.SetWaveArgument;

/**
 * @author Plajer
 * <p>
 * Created at 24.11.2018
 */
public class ArgumentsRegistry extends PluginArgumentsRegistry {

  public ArgumentsRegistry(Main plugin) {
    super(plugin);

    //register Village Defense admin arguments
    //arena related arguments
    new SetWaveArgument(this);


    //other admin related arguments
    new AddOrbsArgument(this);
    new ClearEntitiesArgument(this);
    new RespawnArgument(this);
    new SetPriceArgument(this);
    new SetWaveUnlockArgument(this);
    new ScriptEngineDebugArgument(this);
  }
}
