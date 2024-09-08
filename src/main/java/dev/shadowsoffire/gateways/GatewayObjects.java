package dev.shadowsoffire.gateways;

import com.mojang.serialization.Codec;

import dev.shadowsoffire.gateways.client.GatewayParticleData;
import dev.shadowsoffire.gateways.client.GatewayTickableSound;
import dev.shadowsoffire.gateways.entity.EndlessGatewayEntity;
import dev.shadowsoffire.gateways.entity.NormalGatewayEntity;
import dev.shadowsoffire.gateways.item.GatePearlItem;
import dev.shadowsoffire.placebo.registry.DeferredHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class GatewayObjects {

    private static final DeferredHelper R = DeferredHelper.create(Gateways.MODID);

    public static final Holder<EntityType<NormalGatewayEntity>> NORMAL_GATEWAY = R.entity("normal_gateway", () -> EntityType.Builder
        .<NormalGatewayEntity>of(NormalGatewayEntity::new, MobCategory.MISC)
        .setTrackingRange(5)
        .setUpdateInterval(20)
        .sized(2F, 3F)
        .setCustomClientFactory((packet, level) -> {
            NormalGatewayEntity ent = GatewayObjects.NORMAL_GATEWAY.get().create(level);
            GatewayTickableSound.startGatewaySound(ent);
            return ent;
        })
        .build("gateway"));

    public static final Holder<EntityType<EndlessGatewayEntity>> ENDLESS_GATEWAY = R.entity("endless_gateway", () -> EntityType.Builder
        .<EndlessGatewayEntity>of(EndlessGatewayEntity::new, MobCategory.MISC)
        .setTrackingRange(5)
        .setUpdateInterval(20)
        .sized(2F, 3F)
        .setCustomClientFactory((packet, level) -> {
            EndlessGatewayEntity ent = GatewayObjects.ENDLESS_GATEWAY.get().create(level);
            GatewayTickableSound.startGatewaySound(ent);
            return ent;
        })
        .build("gateway"));

    public static final Holder<Item> GATE_PEARL = R.item("gate_pearl", () -> new GatePearlItem(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final Holder<SoundEvent> GATE_AMBIENT = sound("gate_ambient");
    public static final Holder<SoundEvent> GATE_WARP = sound("gate_warp");
    public static final Holder<SoundEvent> GATE_START = sound("gate_start");
    public static final Holder<SoundEvent> GATE_END = sound("gate_end");

    public static final Holder<ParticleType<?>> GLOW = R.particle("glow", () -> new ParticleType<>(false, GatewayParticleData.DESERIALIZER){
        @Override
        public Codec<GatewayParticleData> codec() {
            return GatewayParticleData.CODEC;
        }
    });

    public static final Holder<CreativeModeTab> TAB = R.creativeTab("tab", b -> b.title(Component.translatable("itemGroup.gateways")).icon(() -> GATE_PEARL.value().getDefaultInstance()));

    public static final Holder<ResourceLocation> GATES_DEFEATED = R.custom("gates_defeated", Registries.CUSTOM_STAT, () -> Gateways.loc("gates_defeated"));

    private static Holder<SoundEvent> sound(String name) {
        return R.sound(name, () -> SoundEvent.createVariableRangeEvent(Gateways.loc(name)));
    }

    static void bootstrap() {}
}
