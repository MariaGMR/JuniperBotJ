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
import net.dv8tion.jda.core.entities.Member;
import ru.caramel.juniperbot.core.persistence.entity.LocalMember;

import java.util.List;

public interface MemberService {

    LocalMember getOrCreate(Member member);

    LocalMember save(LocalMember member);

    LocalMember updateIfRequired(Member member, LocalMember localMember);

    List<LocalMember> syncMembers(Guild guild);

    boolean isApplicable(Member member);
}
