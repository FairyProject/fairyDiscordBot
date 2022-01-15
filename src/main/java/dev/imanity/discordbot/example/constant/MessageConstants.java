package dev.imanity.discordbot.example.constant;

import dev.imanity.discordbot.example.giveaway.Giveaway;
import io.fairyproject.discord.DCBot;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.time.Instant;

@UtilityClass
public class MessageConstants {

    public MessageEmbed fromUnknownChannel(Giveaway giveaway) {
        return new EmbedBuilder()
                .setTitle("Unknown Channel.")
                .setDescription("ex: #giveaway")
                .setColor(Color.RED)
                .build();
    }

    public MessageEmbed fromSelectChannel(Giveaway giveaway) {
        return new EmbedBuilder()
                .setTitle("Please select a channel.")
                .setDescription("ex: #giveaway")
                .build();
    }

    public MessageEmbed fromIncorrectDeadlineFormat(Giveaway giveaway) {
        return new EmbedBuilder()
                .setTitle("Incorrect deadline format.")
                .setDescription("Format: ``dd-M-yyyy hh:mm:ss <AM/PM>``")
                .setColor(Color.RED)
                .build();
    }

    public MessageEmbed fromSelectDeadline(Giveaway giveaway) {
        return new EmbedBuilder()
                .setTitle("Please select a deadline.")
                .setDescription("Format: ``dd-M-yyyy hh:mm:ss <AM/PM>``")
                .build();
    }

    public MessageEmbed fromWritingMessage(Giveaway giveaway) {
        return new EmbedBuilder()
                .setTitle("Write message(s) for the giveaway (optional)")
                .setDescription("You can write 0 to multiple lines, just click push giveaway whenever you are ready.")
                .build();
    }

    public MessageEmbed fromGiveaway(Giveaway giveaway, User author) {
        StringBuilder message = new StringBuilder("---");
        if (!giveaway.getMessages().isEmpty()) {
            message = new StringBuilder();
            for (String msg : giveaway.getMessages()) {
                message.append(msg).append("\n");
            }
        }

        return new EmbedBuilder()
                .setTitle(":tada: Giveaway :tada:")
                .setDescription("Click the button below to **join** the giveaway!")
                .setColor(Color.CYAN)
                .addField("Deadline", "<t:" + giveaway.getDeadline() + ":R>", false)
                .addField("Messages", message.toString(), false)
                .setTimestamp(Instant.now())
                .setAuthor(author.getAsTag(), author.getAvatarUrl(), author.getAvatarUrl())
                .build();
    }

    public MessageEmbed fromGiveawayJoining(Giveaway giveaway, User user) {
        return new EmbedBuilder()
                .setTitle(":tada: Giveaway :tada:")
                .setDescription("Thanks for joining! we will be picking the winner at the deadline!")
                .setColor(Color.CYAN)
                .addField("Deadline", "<t:" + giveaway.getDeadline() + ":R>", false)
                .setTimestamp(Instant.now())
                .setAuthor(user.getAsTag(), user.getAvatarUrl(), user.getAvatarUrl())
                .build();
    }

    public MessageEmbed fromGiveawayBuilder(Giveaway giveaway, User author, DCBot bot) {
        String deadline = "---";
        if (giveaway.getDeadline() != -1) {
            deadline = "<t:" + giveaway.getDeadline() + ":R>";
        }

        StringBuilder message = new StringBuilder("    ---");
        if (!giveaway.getMessages().isEmpty()) {
            message = new StringBuilder();
            for (String msg : giveaway.getMessages()) {
                message.append("    ").append(msg).append("\n");
            }
        }

        String channel = "---";
        if (giveaway.getChannel() != -1) {
            channel = bot.getTextChannelById(giveaway.getChannel()).getAsMention();
        }

        return new EmbedBuilder()
                .setTitle(":tada: Giveaway Builder :tada:")
                .setDescription("**Creator:** " + author.getAsTag() + "\n" +
                        "**Deadline:** " + deadline + "\n" +
                        "**Channel:** " + channel + "\n" +
                        "**Messages:**\n" +
                        message)
                .setColor(Color.CYAN)
                .setAuthor(author.getAsTag(), author.getAvatarUrl(), author.getAvatarUrl())
                .setTimestamp(Instant.now())
                .build();
    }

    public MessageEmbed fromBuilderNotReady(User author) {
        return new EmbedBuilder()
                .setTitle(":no_entry_sign: Failed to push Giveaway. :no_entry_sign:")
                .setDescription("Deadline and Channel must be set for a Giveaway.")
                .setTimestamp(Instant.now())
                .setColor(Color.RED)
                .setAuthor(author.getAsTag(), author.getAvatarUrl(), author.getAvatarUrl())
                .build();
    }

}
