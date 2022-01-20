package dev.imanity.discordbot.example.bot;

import io.fairyproject.container.Service;
import io.fairyproject.discord.DCBot;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

@Service
public class FairyDCBot extends DCBot {
    @Override
    protected String createToken() {
        return "TOKEN";
    }

    @Override
    public @NotNull EnumSet<GatewayIntent> createGatewayIntents() {
        return EnumSet.allOf(GatewayIntent.class);
    }
}
