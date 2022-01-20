package dev.imanity.discordbot.example.command;

import dev.imanity.discordbot.example.constant.MessageConstants;
import dev.imanity.discordbot.example.giveaway.Giveaway;
import io.fairyproject.command.BaseCommand;
import io.fairyproject.command.annotation.Arg;
import io.fairyproject.command.annotation.Command;
import io.fairyproject.container.Component;
import io.fairyproject.discord.DCBot;
import io.fairyproject.discord.button.DCButton;
import io.fairyproject.discord.channel.DCMessageChannel;
import io.fairyproject.discord.command.DCCommandContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Command("giveaway")
public class GiveawayCommand extends BaseCommand {

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    @Command("create")
    public void create(DCCommandContext commandContext, @Arg("id") String id, @Arg("timezone") ZoneId timezone) {
        final User author = commandContext.getAuthor();
        final Giveaway giveaway = new Giveaway(id);
        final DCBot bot = commandContext.getBot();
        final DCMessageChannel channel = commandContext.getChannel();
        final GiveawayBuilder giveawayBuilder = new GiveawayBuilder(channel, giveaway, author, bot, timezone);

        channel
                .sendMessage(new MessageBuilder()
                        .setEmbeds(MessageConstants.fromGiveawayBuilder(giveaway, author, bot))
                        .setActionRows(DCButton.success("send")
                                        .label("Push Giveaway")
                                        .emojiUnicode("\uD83D\uDCCC")
                                        .action((ignored, buttonInteraction) -> {
                                            if (!giveaway.isReady()) {
                                                buttonInteraction.replyEmbeds(MessageConstants.fromBuilderNotReady(author))
                                                        .setEphemeral(true)
                                                        .queue();
                                                return;
                                            }

                                            buttonInteraction.getMessage().delete().queue();
                                            giveaway.getTextChannel().sendMessage(new MessageBuilder()
                                                    .setEmbeds(MessageConstants.fromGiveaway(giveaway, author))
                                                    .setActionRows(DCButton.primary("join")
                                                            .label("Join Giveaway.")
                                                            .emojiUnicode("✅")
                                                            .action((user, interaction) -> interaction.replyEmbeds(MessageConstants.fromGiveawayJoining(giveaway, author))
                                                                    .setEphemeral(true)
                                                                    .queue())
                                                            .build()
                                                            .toActionRows(bot)
                                                    )
                                                    .build()
                                            ).queue();
                                        })
                                        .build().toActionRow(bot),
                                DCButton.danger("giveup")
                                        .label("Drop Giveaway")
                                        .emojiUnicode("⛔")
                                        .action((ignored, buttonInteraction) -> {
                                            giveawayBuilder.setActive(false);
                                            buttonInteraction.getMessage().delete().queue();
                                        }).build().toActionRow(bot)
                        )
                        .build()
                ).queue(message -> {
                    giveawayBuilder.setMessage(message);
                    askChannel(giveawayBuilder);
                });
    }

    private void askChannel(GiveawayBuilder giveawayBuilder) {
        giveawayBuilder.getCommandChannel().sendMessageEmbeds(MessageConstants.fromSelectChannel(giveawayBuilder.getGiveaway()))
                .queue(ignored -> giveawayBuilder.getCommandChannel().readNextMessage(giveawayBuilder.getAuthor())
                        .whenComplete((message, throwable) -> {
                            if (!giveawayBuilder.isActive()) {
                                return;
                            }
                            if (message == null) {
                                return;
                            }

                            final List<TextChannel> mentionedChannels = message.getMentionedChannels();
                            if (mentionedChannels.isEmpty()) {
                                giveawayBuilder.getCommandChannel().sendMessageEmbeds(MessageConstants.fromUnknownChannel(giveawayBuilder.getGiveaway())).queue(i -> this.askChannel(giveawayBuilder));
                                return;
                            }

                            giveawayBuilder.getGiveaway().setChannel(mentionedChannels.get(0).getIdLong());
                            giveawayBuilder.getMessage().editMessageEmbeds(MessageConstants.fromGiveawayBuilder(giveawayBuilder.getGiveaway(), giveawayBuilder.getAuthor(), giveawayBuilder.getBot())).queue();
                            askDeadline(giveawayBuilder);
                        }));
    }

    private void askDeadline(GiveawayBuilder giveawayBuilder) {
        giveawayBuilder.getCommandChannel().sendMessageEmbeds(MessageConstants.fromSelectDeadline(giveawayBuilder.getGiveaway()))
                .queue(ignored -> giveawayBuilder.getCommandChannel().readNextMessage(giveawayBuilder.getAuthor())
                        .whenComplete((message, throwable) -> {
                            if (!giveawayBuilder.isActive()) {
                                return;
                            }
                            if (message == null) {
                                return;
                            }

                            ZonedDateTime ldt;
                            try {
                                ldt = LocalDateTime.parse(message.getContentRaw(), DateTimeFormatter.ofPattern(DATE_FORMAT)).atZone(giveawayBuilder.getZoneId());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                giveawayBuilder.getCommandChannel().sendMessageEmbeds(MessageConstants.fromIncorrectDeadlineFormat(giveawayBuilder.getGiveaway())).queue();
                                askDeadline(giveawayBuilder);
                                return;
                            }

                            giveawayBuilder.getGiveaway().setDeadline(ldt.toInstant().toEpochMilli());
                            giveawayBuilder.getMessage().editMessageEmbeds(MessageConstants.fromGiveawayBuilder(giveawayBuilder.getGiveaway(), giveawayBuilder.getAuthor(), giveawayBuilder.getBot())).queue();

                            askMessage(giveawayBuilder, true);
                        }));
    }

    private void askMessage(GiveawayBuilder giveawayBuilder, boolean sendMessage) {
        Runnable runnable = () -> {
            giveawayBuilder.getBot().getNextMessageReader().read(giveawayBuilder.getCommandChannel(), giveawayBuilder.getAuthor())
                    .whenComplete((message, throwable) -> {
                        if (!giveawayBuilder.isActive()) {
                            return;
                        }
                        if (message == null) {
                            return;
                        }

                        giveawayBuilder.getGiveaway().getMessages().add(message.getContentRaw());
                        giveawayBuilder.getMessage().editMessageEmbeds(MessageConstants.fromGiveawayBuilder(giveawayBuilder.getGiveaway(), giveawayBuilder.getAuthor(), giveawayBuilder.getBot())).queue();

                        askMessage(giveawayBuilder, false);
                    });
        };

        if (sendMessage) {
            giveawayBuilder.getCommandChannel()
                    .sendMessageEmbeds(MessageConstants.fromWritingMessage(giveawayBuilder.getGiveaway()))
                    .queue(ignored -> runnable.run());
        } else {
            runnable.run();
        }
    }

    @RequiredArgsConstructor
    @Getter
    private static class GiveawayBuilder {

        private final DCMessageChannel commandChannel;
        private final Giveaway giveaway;
        private final User author;
        private final DCBot bot;
        private final ZoneId zoneId;

        @Setter
        private Message message;

        @Setter
        private boolean active = true;

    }

}
