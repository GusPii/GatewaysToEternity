package dev.shadowsoffire.gateways;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.shadowsoffire.gateways.gate.Failure;
import dev.shadowsoffire.gateways.gate.GatewayRegistry;
import dev.shadowsoffire.gateways.gate.Reward;
import dev.shadowsoffire.gateways.gate.WaveEntity;
import dev.shadowsoffire.gateways.gate.WaveModifier;
import dev.shadowsoffire.gateways.gate.endless.ApplicationMode;
import dev.shadowsoffire.gateways.net.ParticleMessage;
import dev.shadowsoffire.placebo.network.MessageHelper;
import dev.shadowsoffire.placebo.tabs.TabFillingRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Gateways.MODID)
public class Gateways {

    public static final String MODID = "gateways";
    public static final Logger LOGGER = LogManager.getLogger("Gateways to Eternity");

    public Gateways(IEventBus bus) {
        bus.register(this);
        MessageHelper.registerMessage(CHANNEL, 0, new ParticleMessage.Provider());
        NeoForge.EVENT_BUS.register(new GatewayEvents());
        GatewayObjects.bootstrap();
    }

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent e) {
        GatewayRegistry.INSTANCE.registerToBus();
        e.enqueueWork(() -> {
            WaveModifier.initSerializers();
            Reward.initSerializers();
            WaveEntity.initSerializers();
            Failure.initSerializers();
            ApplicationMode.initSerializers();
            TabFillingRegistry.register(GatewayObjects.TAB_KEY, GatewayObjects.GATE_PEARL);
            Stats.CUSTOM.get(GatewayObjects.GATES_DEFEATED.get(), StatFormatter.DEFAULT);
        });
    }

    public static ResourceLocation loc(String s) {
        return new ResourceLocation(MODID, s);
    }

}
