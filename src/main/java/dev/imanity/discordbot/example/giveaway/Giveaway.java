package dev.imanity.discordbot.example.giveaway;

import dev.imanity.discordbot.example.bot.FairyDCBot;
import io.fairyproject.container.Autowired;
import lombok.Data;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;

@Data
public class Giveaway {

    @Autowired
    private static FairyDCBot BOT;

    private final String id;
    private final List<String> messages;
    private long channel = -1;
    private long deadline = -1;

    public Giveaway(String id) {
        this.id = id;
        this.messages = new ArrayList<>();
    }

    public boolean isReady() {
        return this.deadline != -1 && this.channel != -1;
    }

    public TextChannel getTextChannel() {
        return BOT.getTextChannelById(this.channel);
    }

}
