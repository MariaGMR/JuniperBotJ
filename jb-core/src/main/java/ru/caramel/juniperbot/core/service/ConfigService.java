/*
 * This file is part of JuniperBotJ.
 *
 * JuniperBotJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * JuniperBotJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with JuniperBotJ. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.caramel.juniperbot.core.service;

import net.dv8tion.jda.core.entities.Guild;
import ru.caramel.juniperbot.core.persistence.entity.GuildConfig;

public interface ConfigService {

    String getDefaultPrefix();

    boolean exists(long serverId);

    void save(GuildConfig config);

    GuildConfig getById(long serverId);

    GuildConfig getOrCreate(long serverId);

    GuildConfig getOrCreate(Guild guild);

    String getPrefix(long serverId);

    String getLocale(Guild guild);

    String getLocale(long serverId);
}
