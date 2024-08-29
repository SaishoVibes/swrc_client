package uk.cloudmc.swrc.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ChatFormatter {
    public static MutableText SWRC_PREFIX() {
        MutableText swPart = Text.literal("SW").formatted(Formatting.GOLD);
        MutableText rcPart = Text.literal("RC").formatted(Formatting.AQUA);
        return swPart.append(rcPart);
    }

    public static MutableText GENERIC_MESSAGE_PREFIX() {
        MutableText prefix = SWRC_PREFIX();
        MutableText separator = Text.literal(" | ").formatted(Formatting.WHITE);
        return prefix.append(separator);
    }

    public static MutableText GENERIC_MESSAGE(String message) {
        MutableText prefix = GENERIC_MESSAGE_PREFIX();
        MutableText content = Text.literal(message).formatted(Formatting.WHITE);
        return prefix.append(content);
    }
}
