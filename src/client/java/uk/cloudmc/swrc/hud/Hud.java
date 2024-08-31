package uk.cloudmc.swrc.hud;

import net.minecraft.client.gui.DrawContext;
import uk.cloudmc.swrc.SWRC;

public interface Hud {
    public boolean shouldRender();
    public void render(DrawContext context, float tickDelta);
}
