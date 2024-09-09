package dev.shadowsoffire.gateways.compat;

import dev.shadowsoffire.gateways.GatewayObjects;
import dev.shadowsoffire.gateways.client.GatewaysClient;
import net.minecraft.client.renderer.Rect2i;
import snownee.jade.Jade;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IWailaConfig.BossBarOverlapMode;
import snownee.jade.impl.ui.BoxElement;

@WailaPlugin
public class GatewayJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration reg) {
        reg.hideTarget(GatewayObjects.NORMAL_GATEWAY.get());
        reg.hideTarget(GatewayObjects.ENDLESS_GATEWAY.get());
        reg.addBeforeRenderCallback((root, rect, gfx, accessor) -> {
            // Fix positioning of Jade when Gateway boss bars are present.
            // Jade doesn't handle CustomizeGuiOverlayEvent.BossEventProgress correctly (they do not account for the increment when cancelled), so I have to do it manually.
            BossBarOverlapMode mode = Jade.CONFIG.get().getGeneral().getBossBarOverlapMode();
            if (mode == BossBarOverlapMode.PUSH_DOWN && GatewaysClient.bossBarRect != null) {
                Rect2i intersection = rect.expectedRect.intersect(GatewaysClient.bossBarRect);
                if (intersection.getHeight() > 0 || intersection.getWidth() > 0) {
                    rect.expectedRect.setY(GatewaysClient.bossBarRect.getHeight() - 8);
                    rect.expectedRect.setHeight(rect.rect.getHeight());
                    rect.rect.setWidth(0);
                    ((BoxElement) root).updateRect(rect);
                }
            }

            return false;
        });
    }

}
