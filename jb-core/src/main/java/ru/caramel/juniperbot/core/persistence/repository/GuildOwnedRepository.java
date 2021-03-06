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
package ru.caramel.juniperbot.core.persistence.repository;

import net.dv8tion.jda.core.entities.Guild;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import ru.caramel.juniperbot.core.persistence.entity.GuildConfig;
import ru.caramel.juniperbot.core.persistence.entity.GuildOwnedEntity;

import java.util.List;

@NoRepositoryBean
public interface GuildOwnedRepository<T extends GuildOwnedEntity> extends JpaRepository<T, Long> {

    @Query("SELECT e FROM #{#entityName} e WHERE e.guildConfig.guildId = :guildId")
    T findByGuildId(@Param("guildId") long guildId);

    T findByGuildConfig(GuildConfig config);

    @Query("SELECT e FROM #{#entityName} e WHERE e.guildConfig.guildId = :guildId")
    List<T> findAllByGuildId(@Param("guildId") long guildId);

    default T findByGuild(Guild guild) {
        return findByGuildId(guild.getIdLong());
    }

    default List<T> findAllByGuild(Guild guild) {
        return findAllByGuildId(guild.getIdLong());
    }
}
