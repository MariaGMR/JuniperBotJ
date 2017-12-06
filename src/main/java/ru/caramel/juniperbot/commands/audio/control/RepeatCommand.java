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
package ru.caramel.juniperbot.commands.audio.control;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import ru.caramel.juniperbot.audio.model.RepeatMode;
import ru.caramel.juniperbot.audio.service.PlaybackInstance;
import ru.caramel.juniperbot.commands.audio.AudioCommand;
import ru.caramel.juniperbot.commands.model.BotContext;
import ru.caramel.juniperbot.commands.model.CommandGroup;
import ru.caramel.juniperbot.commands.model.CommandSource;
import ru.caramel.juniperbot.commands.model.DiscordCommand;
import ru.caramel.juniperbot.integration.discord.model.DiscordException;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@DiscordCommand(
        key = "discord.command.repeat.key",
        description = "discord.command.repeat.desc",
        source = CommandSource.GUILD,
        group = CommandGroup.MUSIC,
        priority = 108)
public class RepeatCommand extends AudioCommand {
    @Override
    protected boolean doInternal(MessageReceivedEvent message, BotContext context, String content) throws DiscordException {
        RepeatMode mode = messageService.getEnumeration(RepeatMode.class, content);
        if (mode == null) {
            messageManager.onMessage(message.getChannel(), "discord.command.audio.repeat.help",
                    Stream.of(RepeatMode.values()).map(messageService::getEnumTitle).collect(Collectors.joining("|")));
            return false;
        }
        PlaybackInstance instance = playerService.getInstance(message.getGuild());
        if (instance.setMode(mode)) {
            messageManager.onMessage(message.getChannel(), "discord.command.audio.repeat", mode.getEmoji());
            if (instance.getCurrent() != null) {
                messageManager.updateMessage(instance.getCurrent());
            }
        } else {
            messageManager.onMessage(message.getChannel(), "discord.command.audio.notStarted");
        }
        return true;
    }
}
