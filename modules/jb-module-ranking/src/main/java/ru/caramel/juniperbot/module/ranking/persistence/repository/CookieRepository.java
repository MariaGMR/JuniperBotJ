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
package ru.caramel.juniperbot.module.ranking.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.caramel.juniperbot.core.persistence.entity.LocalMember;
import ru.caramel.juniperbot.module.ranking.persistence.entity.Cookie;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Repository
public interface CookieRepository extends JpaRepository<Cookie, Long> {

    @Query("SELECT count(c) > 0 from Cookie c WHERE c.sender = :sender AND c.recipient = :recipient AND c.receivedAt >= :date")
    boolean isFull(
            @Param("sender") LocalMember sender,
            @Param("recipient") LocalMember recipient,
            @Param("date") Date date);

    @Query("SELECT r.id, count(c) from Cookie c, LocalMember r WHERE c.recipient.id = r.id AND r IN :recipients GROUP BY r.id")
    List<Object[]> countByRecipients(@Param("recipients") Collection<LocalMember> recipients);

    long countByRecipient(LocalMember recipient);
}
