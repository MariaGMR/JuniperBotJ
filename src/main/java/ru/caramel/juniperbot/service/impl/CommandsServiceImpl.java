package ru.caramel.juniperbot.service.impl;

import lombok.Getter;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.PropertyPlaceholderHelper;
import ru.caramel.juniperbot.commands.Command;
import ru.caramel.juniperbot.commands.model.BotContext;
import ru.caramel.juniperbot.commands.model.CommandGroup;
import ru.caramel.juniperbot.commands.model.DiscordCommand;
import ru.caramel.juniperbot.commands.model.ValidationException;
import ru.caramel.juniperbot.configuration.DiscordConfig;
import ru.caramel.juniperbot.integration.discord.model.DiscordException;
import ru.caramel.juniperbot.model.CustomCommandDto;
import ru.caramel.juniperbot.persistence.entity.CustomCommand;
import ru.caramel.juniperbot.persistence.entity.GuildConfig;
import ru.caramel.juniperbot.persistence.repository.CustomCommandRepository;
import ru.caramel.juniperbot.service.CommandsService;
import ru.caramel.juniperbot.service.ConfigService;
import ru.caramel.juniperbot.service.MapperService;
import ru.caramel.juniperbot.service.MessageService;
import ru.caramel.juniperbot.utils.MapPlaceholderResolver;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommandsServiceImpl implements CommandsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandsServiceImpl.class);

    @Autowired
    private DiscordConfig config;

    @Autowired
    private ConfigService configService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private CustomCommandRepository commandRepository;

    @Autowired
    private MapperService mapperService;

    private static PropertyPlaceholderHelper placeholderHelper = new PropertyPlaceholderHelper("{", "}");

    private Map<String, Command> localizedCommands;

    @Getter
    private Map<String, Command> commands;

    private Map<MessageChannel, BotContext> contexts = new HashMap<>();

    private Map<CommandGroup, List<DiscordCommand>> descriptors;

    @Override
    @Transactional
    public void onMessageReceived(MessageReceivedEvent event) {
        JDA jda = event.getJDA();
        if (event.getAuthor().isBot()) {
            return;
        }
        GuildConfig guildConfig = null;
        if (event.getChannelType().isGuild() && event.getGuild() != null) {
            guildConfig = configService.getOrCreate(event.getGuild().getIdLong());
        }

        String content = event.getMessage().getRawContent().trim();
        String inlinePrefix = guildConfig != null ? guildConfig.getPrefix() : config.getPrefix();
        String rawPrefix = inlinePrefix;
        if (event.getMessage().isMentioned(jda.getSelfUser())) {
            String customMention = String.format("<@!%s>", jda.getSelfUser().getId());
            rawPrefix = content.startsWith(customMention) ? customMention : jda.getSelfUser().getAsMention();
        }
        if (StringUtils.isNotEmpty(content) && content.startsWith(rawPrefix) && content.length() <= 255) {
            String input = content.substring(rawPrefix.length()).trim();
            sendCommand(event, input, inlinePrefix, guildConfig, false);
        }
    }

    @Override
    public Command getByLocale(String localizedKey) {
        return localizedCommands.get(localizedKey);
    }

    @Override
    public Command getByLocale(String localizedKey, boolean anyLocale) {
        return getByLocale(localizedKey); // TODO maybe should be implemented later
    }

    private void sendCommand(MessageReceivedEvent event, String content, String prefix, GuildConfig guildConfig, boolean alias) {
        String[] args = content.split("\\s+");
        if (args.length == 0) {
            return;
        }
        content = content.substring(args[0].length(), content.length()).trim();

        Command command = getByLocale(args[0]);
        if (command != null && !command.isApplicable(event.getChannel(), guildConfig)) {
            return;
        }
        if (command == null) {
            if (!alias && guildConfig != null) {
                sendCustomCommand(event, content, args[0], prefix, guildConfig);
            }
            return;
        }

        BotContext context = contexts.computeIfAbsent(event.getChannel(), e -> new BotContext());
        context.setPrefix(prefix);
        context.setConfig(guildConfig);
        context.setGuild(event.getGuild());
        try {
            command.doCommand(event, context, content);
        } catch (ValidationException e) {
            messageService.onError(event.getChannel(), e.getMessage(), e.getArgs());
        } catch (DiscordException e) {
            messageService.onError(event.getChannel(), "discord.global.error");
            LOGGER.error("Command {} execution error", args[0], e);
        }
    }

    private void sendCustomCommand(MessageReceivedEvent event, String content, String key, String prefix, GuildConfig config) {
        CustomCommand command = commandRepository.findByKeyAndConfig(key, config);
        if (command == null) {
            return;
        }
        String commandContent = placeholderHelper.replacePlaceholders(command.getContent(), getResolver(event, content));
        switch (command.getType()) {
            case ALIAS:
                sendCommand(event, commandContent, prefix, config, true);
                break;
            case MESSAGE:
                messageService.sendMessageSilent(event.getChannel()::sendMessage, commandContent);
                break;
        }
    }

    private MapPlaceholderResolver getResolver(MessageReceivedEvent event, String content) {
        MapPlaceholderResolver resolver = new MapPlaceholderResolver();
        resolver.put(messageService.getMessage("custom.commands.placeholder.author"), event.getAuthor().getAsMention());
        resolver.put(messageService.getMessage("custom.commands.placeholder.guild"), event.getGuild().getName());
        resolver.put(messageService.getMessage("custom.commands.placeholder.content"), content);
        return resolver;
    }

    @Transactional
    public void saveCommands(List<CustomCommandDto> commands, long serverId) {
        GuildConfig config = configService.getOrCreate(serverId);
        List<CustomCommand> customCommands = config.getCommands() != null ? config.getCommands() : new ArrayList<>();
        if (commands == null) {
            commands = Collections.emptyList();
        }

        List<CustomCommand> result = new ArrayList<>();
        // adding new commands
        result.addAll(mapperService.getCommands(commands.stream().filter(e -> e.getId() == null).collect(Collectors.toList())));
        result.forEach(e -> e.setConfig(config));

        // update existing
        commands.stream().filter(e -> e.getId() != null).forEach(e -> {
            CustomCommand command = customCommands.stream().filter(e1 -> Objects.equals(e1.getId(), e.getId())).findFirst().orElse(null);
            if (command != null) {
                mapperService.updateCommand(e, command);
                result.add(command);
            }
        });

        config.setCommands(result);
        commandRepository.save(result);

        // delete old
        commandRepository.delete(customCommands.stream().filter(e -> !result.contains(e)).collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    public List<CustomCommandDto> getCustomForView(long serverId) {
        GuildConfig config = configService.getOrCreate(serverId);
        return mapperService.getCommandsDto(commandRepository.findByConfig(config));
    }

    @Autowired
    private void registerCommands(List<Command> commands) {
        this.localizedCommands = new HashMap<>();
        this.commands = new HashMap<>();
        commands.stream().filter(e -> e.getClass().isAnnotationPresent(DiscordCommand.class)).forEach(e -> {
            String localized = messageService.getMessage(e.getClass().getAnnotation(DiscordCommand.class).key());
            this.localizedCommands.put(localized, e);
            this.commands.put(e.getClass().getAnnotation(DiscordCommand.class).key(), e);
        });
    }

    @Override
    public Map<CommandGroup, List<DiscordCommand>> getDescriptors() {
        if (descriptors == null) {
            List<DiscordCommand> discordCommands = commands.entrySet().stream()
                    .map(e -> e.getValue().getClass().getAnnotation(DiscordCommand.class))
                    .filter(e -> !e.hidden())
                    .collect(Collectors.toList());
            discordCommands.sort(Comparator.comparingInt(DiscordCommand::priority));
            descriptors = discordCommands
                    .stream().collect(Collectors.groupingBy(DiscordCommand::group, LinkedHashMap::new, Collectors.toList()));
        }
        return descriptors;
    }
}
