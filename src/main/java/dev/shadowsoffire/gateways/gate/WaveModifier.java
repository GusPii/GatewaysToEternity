package dev.shadowsoffire.gateways.gate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apothic_attributes.ApothicAttributes;
import dev.shadowsoffire.apothic_attributes.api.IFormattableAttribute;
import dev.shadowsoffire.gateways.Gateways;
import dev.shadowsoffire.placebo.codec.CodecMap;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.json.ChancedEffectInstance;
import dev.shadowsoffire.placebo.json.RandomAttributeModifier;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.systems.gear.GearSet;
import dev.shadowsoffire.placebo.systems.gear.GearSetRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.alchemy.PotionContents;

public interface WaveModifier extends CodecProvider<WaveModifier> {

    public static final CodecMap<WaveModifier> CODEC = new CodecMap<>("Gateway Wave Modifier");

    /**
     * Applies this modifier to the given entity, which will have been freshly spawned by a Wave.
     * 
     * @param entity The fresh Wave Entity.
     */
    void apply(LivingEntity entity);

    /**
     * Adds this wave modifier to the gate pearl's tooltip.
     */
    public void appendHoverText(TooltipContext ctx, Consumer<MutableComponent> list);

    public static void initSerializers() {
        register("mob_effect", EffectModifier.CODEC);
        register("attribute", AttributeModifier.CODEC);
        register("gear_set", GearSetModifier.CODEC);
        CODEC.setDefaultCodec(AttributeModifier.CODEC);
    }

    private static void register(String id, Codec<? extends WaveModifier> codec) {
        CODEC.register(Gateways.loc(id), codec);
    }

    /**
     * Wave modifier that applies a mob effect to the wave entities.
     * <p>
     * The effect is applied with infinite duration, unless the entity is a creeper, in which case the duration is reduced to 5 minutes.
     */
    public static record EffectModifier(ChancedEffectInstance effect) implements WaveModifier {

        public static Codec<EffectModifier> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                ChancedEffectInstance.CONSTANT_CODEC.fieldOf("effect").forGetter(EffectModifier::effect))
            .apply(inst, EffectModifier::new));

        @Override
        public Codec<? extends WaveModifier> getCodec() {
            return CODEC;
        }

        @Override
        public void apply(LivingEntity entity) {
            int duration = entity instanceof Creeper ? 6000 : Integer.MAX_VALUE;
            entity.addEffect(effect.createDeterministic(duration));
        }

        @Override
        public void appendHoverText(TooltipContext ctx, Consumer<MutableComponent> list) {
            List<Component> output = new ArrayList<>();
            PotionContents.addPotionTooltip(Arrays.asList(this.effect.createDeterministic(1)), output::add, 1, ctx.tickRate());
            list.accept(Component.literal(output.get(0).getString()));
        }

    }

    /**
     * Wave modifier that applies an attribute modifier to the wave entities.
     * <p>
     * The modifier will be ignored if the entity does not have the attribute.
     */
    public static record AttributeModifier(RandomAttributeModifier modifier) implements WaveModifier {

        public static Codec<AttributeModifier> CODEC = RandomAttributeModifier.CONSTANT_CODEC.xmap(AttributeModifier::new, AttributeModifier::modifier);

        @Override
        public Codec<? extends WaveModifier> getCodec() {
            return CODEC;
        }

        @Override
        public void apply(LivingEntity entity) {
            AttributeInstance inst = entity.getAttribute(this.modifier.attribute());
            if (inst == null) return;
            // TODO: Figure out a better way to generate a random ID (maybe generate a full UUID?) or have users provide an identifier.
            inst.addPermanentModifier(this.modifier.createDeterministic(Gateways.loc("gateway_random_modifier_" + entity.getRandom().nextInt())));
        }

        @Override
        public void appendHoverText(TooltipContext ctx, Consumer<MutableComponent> list) {
            list.accept(IFormattableAttribute.toComponent(modifier.attribute(), modifier.createDeterministic(Gateways.loc("gateway_random_modifier")), ApothicAttributes.getTooltipFlag()));
        }

    }

    /**
     * Wave modifier that applies a gear set to the wave entities.
     * <p>
     * The applied gear set should be deterministic to a reasonable degree, since it must be translated to a single name.
     */
    public static record GearSetModifier(DynamicHolder<GearSet> set) implements WaveModifier {

        public static Codec<GearSetModifier> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                GearSetRegistry.INSTANCE.holderCodec().fieldOf("gear_set").forGetter(GearSetModifier::set))
            .apply(inst, GearSetModifier::new));

        @Override
        public Codec<? extends WaveModifier> getCodec() {
            return CODEC;
        }

        @Override
        public void apply(LivingEntity entity) {
            this.set.get().apply(entity);
        }

        @Override
        public void appendHoverText(TooltipContext ctx, Consumer<MutableComponent> list) {
            list.accept(Component.translatable("modifier.gateways.gear_set", Component.translatable(this.set.getId().toLanguageKey("gear_set"))));
        }

    }
}
