package uk.cloudmc.swrc.hud;

import net.minecraft.client.gui.DrawContext;
import uk.cloudmc.swrc.SWRC;

public interface Hud {
    boolean shouldRender();
    void render(DrawContext context, float tickDelta);
}
