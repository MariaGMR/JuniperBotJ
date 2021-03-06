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
package ru.caramel.juniperbot.core.persistence.entity.base;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.core.entities.MessageEmbed;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Size;

@MappedSuperclass
@Getter
@Setter
public abstract class MemberMessageEntity extends MemberEntity {

    @Size(max = MessageEmbed.TEXT_MAX_LENGTH)
    @Basic
    protected String message;

    @Column(name = "channel_id")
    protected String channelId;

}
