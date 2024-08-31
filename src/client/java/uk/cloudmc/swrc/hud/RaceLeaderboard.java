package uk.cloudmc.swrc.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.realms.util.TextRenderingUtils;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import uk.cloudmc.swrc.Race;
import uk.cloudmc.swrc.SWRC;
import uk.cloudmc.swrc.SWRCConfig;
import uk.cloudmc.swrc.net.packets.S2CUpdatePacket;

import java.text.DecimalFormat;

public class RaceLeaderboard implements Hud {

    private static final Identifier WIDGETS_TEXTURE = new Identifier(SWRC.NAMESPACE, "textures/widgets.png");
    private static final Identifier GREY_TEXTURE = new Identifier(SWRC.NAMESPACE, "textures/grey.png");

    private int scaledWidth;
    private int scaledHeight;

    private static DecimalFormat decimalFormat = new DecimalFormat("00.000");

    public RaceLeaderboard() {}

    @Override
    public boolean shouldRender() {
        return SWRC.getRace() != null;
    }

    @Override
    public void render(DrawContext graphics, float tickDelta) {
        Race race = SWRC.getRace();

        this.scaledWidth = SWRC.instance.getWindow().getScaledWidth();
        this.scaledHeight = SWRC.instance.getWindow().getScaledHeight();

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int width = 200;
        int body_height = SWRC.getRace().raceLeaderboardPositions.size() * 9 + 2;
        int x = 10;
        int y = 10;




        renderBox(graphics, WIDGETS_TEXTURE, 0, 0, x, y, 12, body_height, width);

        graphics.drawTexture(WIDGETS_TEXTURE, x + 3, y + 3, 5, 0, 25, 10);




        renderText(graphics, String.format(SWRCConfig.getInstance().header_text, SWRC.getRaceName()), x + 30, y + 4, 0xFFFFFF);

        int offset = 0;
        for (S2CUpdatePacket.RaceLeaderboardPosition position : race.raceLeaderboardPositions) {
            renderText(graphics, String.format("%s", offset + 1), x + 4, y + 14 + offset * 9 + 4, 0xFFFFFF);
            renderText(graphics, String.format("%s", position.player_name), x + 12, y + 14 + offset * 9 + 4, 0xFFFFFF);

            int start_pos = width - widthOfText("-" + msToTimeString(position.time_delta)) - 2;

            renderText(graphics, String.format("%s%s", position.time_delta > 0 ? "+" : "" , msToTimeString(position.time_delta)), x + start_pos, y + 14 + offset * 9 + 4, position.time_delta >= 0 ? 0x00FF00 : 0xFF0000 );

            offset += 1;
        }

        RenderSystem.disableBlend();
    }

    public String msToTimeString(long ms) {
        double secconds = (double) ms / 1000;

        String prefix = "";

        if (secconds > 60) {
            int mins = (int) secconds / 60;

            prefix = String.format("%s:", mins);
        }

        return prefix + this.decimalFormat.format(secconds % 60);
    }

    public static void renderText(DrawContext graphics, String text, int x, int y, int color) {
        graphics.drawTextWithShadow(SWRC.instance.textRenderer, text, x, y, color);
    }

    public static int widthOfText(String text) {
        return SWRC.instance.textRenderer.getWidth(text);
    }

    public static void renderBox(DrawContext graphics, Identifier texture, int tx, int ty, int x, int y, int hh, int bh, int w) {
        // Top
        graphics.drawTexture(texture, x, y, tx, ty, 3, 3);
        for (int i = 0; i < w; i++) {
            graphics.drawTexture(texture, x + 3 + i, y, tx + 3, ty, 1, 3);
        }
        graphics.drawTexture(texture, x + w + 3, y, tx + 4, ty, 1, 3);

        for (int hy = 0; hy < hh; hy++) {
            graphics.drawTexture(texture, x, y + hy + 3, tx, ty + 3, 3, 1);
            graphics.drawTexture(texture, x + w + 3, y + hy + 3, tx + 4, ty + 3, 1, 1);
        }


        graphics.drawTexture(GREY_TEXTURE, x + 3, y + 3, 0, 0, w, hh);

        graphics.drawTexture(texture, x, y + hh + 3, tx, ty + 4, 3, 1);
        for (int hsx = 0; hsx < w; hsx++) {
            graphics.drawTexture(texture, x + hsx + 3, y + hh + 3, tx + 3, tx + 4, 1, 1);
        }
        graphics.drawTexture(texture, x + 3 + w, y + hh + 3, tx + 4, ty + 4, 1, 1);

        graphics.drawTexture(GREY_TEXTURE, x + 3, y + hh + 4, 0, 0, w, bh);
        for (int bby = 0; bby < bh; bby++) {
            graphics.drawTexture(texture, x, y + hh + bby + 4, tx, ty + 5, 3, 1);

            graphics.drawTexture(texture, x + w + 3, y + hh + bby + 4, tx + 4, ty + 5, 1, 1);
        }

        graphics.drawTexture(texture, x, y + hh + bh + 4, tx, ty + 6, 3, 3);
        for (int fx = 0; fx < w; fx++) {
            graphics.drawTexture(texture, x + fx + 3, y + hh + bh + 4, tx + 3, ty + 6, 1, 3);
        }
        graphics.drawTexture(texture, x + w + 3, y + hh + bh + 4, tx + 4, ty + 6, 1, 3);
    }
}
