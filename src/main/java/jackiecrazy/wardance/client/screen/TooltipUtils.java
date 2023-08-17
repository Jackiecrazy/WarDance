package jackiecrazy.wardance.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TooltipUtils {
    static final Pattern TOOLTIP_PATTERN = Pattern.compile(
            "\\{[^}]*\\}", Pattern.CASE_INSENSITIVE);

    public static Component tooltipText(String string) {
        MutableComponent ichat = null;
        Matcher matcher = TOOLTIP_PATTERN.matcher(string);
        int lastEnd = 0;

        // Find all tooltips
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String literal = string.substring(lastEnd, start);
            if (literal.length() > 0) {
                if (ichat == null) ichat = Component.literal(literal);
                else ichat.append(literal);
            }
            lastEnd = end;

            //grab the full tooltip description
            String part = string.substring(start + 1, end - 1);
            String[] parted = part.split(";");
            String display = parted[0].trim();
            String[] rawFormatting = parted[1].trim().split(",");
            String tooltip = "";
            String[] additionalData = {""};
            //grab the tooltip description
            if (parted.length > 2) tooltip = parted[2].trim();
            //additional tooltip formatting
            if (parted.length > 3) additionalData = parted[3].trim().split(",");
            ArrayList<ChatFormatting> formatting = new ArrayList<>();
            for (String raw : rawFormatting) {
                ChatFormatting cf = ChatFormatting.getByName(raw.trim());
                if (cf != null) formatting.add(cf);
            }
            ChatFormatting[] fff = new ChatFormatting[formatting.size()];
            //build tooltip text
            MutableComponent tooltipText = Component.literal(display);
            Style style = tooltipText.getStyle();
            if (!formatting.isEmpty()) style = style.applyFormats(formatting.toArray(fff));
            if (!tooltip.isEmpty())
                style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, unTooltipL18N(tooltip, (Object) additionalData)));
            tooltipText.setStyle(style);
            if (ichat == null) ichat = Component.literal("");
            //literal
            ichat.append(tooltipText);
        }

        // Append the rest of the message.
        String end = string.substring(lastEnd);
        if (ichat == null) ichat = Component.literal(end);
        else if (end.length() > 0) ichat.append(Component.literal(string.substring(lastEnd)));
        return ichat;
    }

    public static Component unTooltipL18N(String resource, Object... additional) {
        String string = Component.translatable(resource, additional).getString();
        MutableComponent ichat = null;
        Matcher matcher = TOOLTIP_PATTERN.matcher(string);
        int lastEnd = 0;

        // Find all tooltips
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String literal = string.substring(lastEnd, start);
            if (literal.length() > 0) {
                if (ichat == null) ichat = Component.literal(literal);
                else ichat.append(literal);
            }
            lastEnd = end;

            //grab the full tooltip description
            String part = string.substring(start + 1, end - 1);
            String[] parted = part.split(";");
            String display = parted[0].trim();
            String[] rawFormatting = parted[1].trim().split(",");
            ArrayList<ChatFormatting> formatting = new ArrayList<>();
            for (String raw : rawFormatting) {
                ChatFormatting cf = ChatFormatting.getByName(raw.trim());
                if (cf != null) formatting.add(cf);
            }
            ChatFormatting[] fff = new ChatFormatting[formatting.size()];
            //build tooltip text
            MutableComponent tooltipText = Component.literal(display);
            Style style = tooltipText.getStyle();
            if (!formatting.isEmpty()) style = style.applyFormats(formatting.toArray(fff));
            tooltipText.setStyle(style);
            if (ichat == null) ichat = Component.literal("");
            //literal
            ichat.append(tooltipText);
        }

        // Append the rest of the message.
        String end = string.substring(lastEnd);
        if (ichat == null) ichat = Component.literal(end);
        else if (end.length() > 0) ichat.append(Component.literal(string.substring(lastEnd)));
        return ichat;
    }
}
