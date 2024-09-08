package dev.shadowsoffire.gateways;

import dev.shadowsoffire.gateways.command.GatewayCommand;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent.AllowDespawn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;

public class GatewayEvents {

    @SubscribeEvent
    public void commands(RegisterCommandsEvent e) {
        GatewayCommand.register(e.getDispatcher());
    }

    @SubscribeEvent
    public void teleport(EntityTeleportEvent e) {
        GatewayEntity gate = GatewayEntity.getOwner(e.getEntity());
        if (gate != null && gate.getGateway().rules().failOnOutOfBounds()) {
            if (gate.distanceToSqr(e.getTargetX(), e.getTargetY(), e.getTargetZ()) >= gate.getGateway().getLeashRangeSq()) {
                e.setTargetX(gate.getX() + 0.5 * gate.getBbWidth());
                e.setTargetY(gate.getY() + 0.5 * gate.getBbHeight());
                e.setTargetZ(gate.getZ() + 0.5 * gate.getBbWidth());
            }
        }
    }

    @SubscribeEvent
    public void convert(LivingConversionEvent.Post e) {
        Entity entity = e.getEntity();
        GatewayEntity gate = GatewayEntity.getOwner(entity);
        if (gate != null) {
            gate.handleConversion(entity, e.getOutcome());
        }
    }

    @SubscribeEvent
    public void hurt(LivingHurtEvent e) {
        GatewayEntity gate = GatewayEntity.getOwner(e.getEntity());
        if (gate != null) {
            boolean isPlayerDamage = e.getSource().getEntity() instanceof Player p && !(p instanceof FakePlayer);
            if (!isPlayerDamage && gate.getGateway().rules().playerDamageOnly()) e.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void despawn(AllowDespawn e) {
        if (GatewayEntity.getOwner(e.getEntity()) != null) e.setResult(Result.DENY);
    }

}
