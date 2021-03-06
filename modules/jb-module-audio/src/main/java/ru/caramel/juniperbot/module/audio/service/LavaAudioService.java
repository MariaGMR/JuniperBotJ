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
package ru.caramel.juniperbot.module.audio.service;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import lavalink.client.io.Lavalink;
import lavalink.client.player.IPlayer;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import ru.caramel.juniperbot.core.service.AudioService;

public interface LavaAudioService extends AudioService {

    AudioPlayerManager getPlayerManager();

    IPlayer createPlayer(String guildId);

    void openConnection(IPlayer player, VoiceChannel channel);

    void closeConnection(Guild guild);

    VoiceChannel getConnectedChannel(Guild guild);

    VoiceChannel getConnectedChannel(long guildId);

    Lavalink getLavaLink();

    void shutdown();

    boolean isConnected(Guild guild);
}
