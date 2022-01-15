package dev.imanity.discordbot.example.command.arg;

import io.fairyproject.command.CommandContext;
import io.fairyproject.command.exception.ArgTransformException;
import io.fairyproject.command.parameter.ArgTransformer;
import io.fairyproject.container.Component;

import java.time.ZoneId;

@Component
public class ZoneIdArgTransformer implements ArgTransformer<ZoneId> {
    @Override
    public Class[] type() {
        return new Class[] {ZoneId.class};
    }

    @Override
    public ZoneId transform(CommandContext commandContext, String s) throws ArgTransformException {
        switch (s.toLowerCase()) {
            case "hk":
                return ZoneId.of("Asia/Hong_Kong");
            case "fr":
                return ZoneId.of("Europe/Paris");
        }
        return null;
    }
}
