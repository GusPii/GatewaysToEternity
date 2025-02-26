package dev.shadowsoffire.gateways.gate;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Gate Rules are all of the various metadata rules for a Gateway.
 * 
 * @param spawnRange        The spawn range as a radius in blocks in which mobs may spawn around the gateway, from the edges of the gateway.
 * @param leashRange        The distance that a wave entity may be from the center of the Gateway before out-of-bounds rules are triggered.
 * @param allowDiscarding   If entities marked as discarded are counted as valid kills.
 * @param allowDimChange    If entities marked as changed dimension are counted as valid kills.
 * @param playerDamageOnly  If wave entities may only be hurt by damage that is sourced to a player.
 * @param removeOnFailure   If the wave entities will be removed if the Gateway is failed.
 * @param failOnOutOfBounds If true, when out-of-bounds rules are triggered, the Gateway will fail. If false, the entity will be re-placed using the spawn
 *                          algorithm.
 * @param spacing           The distance that this gateway must be from another Gateway.
 * @param followRangeBoost  Bonus value added to the follow range of all wave entities. Invisible to users.
 * @param defaultDropChance Default drop chance set for all slots on all wave entities. Invisible to users.
 */
public record GateRules(double spawnRange, double leashRange, boolean allowDiscarding,
    boolean allowDimChange, boolean playerDamageOnly, boolean removeOnFailure,
    boolean failOnOutOfBounds, double spacing, double followRangeBoost, float defaultDropChance) {

    public static final DecimalFormat FORMAT = Util.make(new DecimalFormat("#.#"), fmt -> fmt.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT)));

    public static final Codec<GateRules> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Codec.DOUBLE.optionalFieldOf("spawn_range", 8D).forGetter(GateRules::spawnRange),
            Codec.DOUBLE.optionalFieldOf("leash_range", 32D).forGetter(GateRules::leashRange),
            Codec.BOOL.optionalFieldOf("allow_discarding", false).forGetter(GateRules::allowDiscarding),
            Codec.BOOL.optionalFieldOf("allow_dim_change", false).forGetter(GateRules::allowDimChange),
            Codec.BOOL.optionalFieldOf("player_damage_only", false).forGetter(GateRules::playerDamageOnly),
            Codec.BOOL.optionalFieldOf("remove_mobs_on_failure", true).forGetter(GateRules::removeOnFailure),
            Codec.BOOL.optionalFieldOf("fail_on_out_of_bounds", false).forGetter(GateRules::failOnOutOfBounds),
            Codec.DOUBLE.optionalFieldOf("spacing", 0D).forGetter(GateRules::spacing),
            Codec.DOUBLE.optionalFieldOf("follow_range_boost", 32D).forGetter(GateRules::followRangeBoost),
            Codec.FLOAT.optionalFieldOf("default_drop_chance", 0F).forGetter(GateRules::defaultDropChance))
        .apply(inst, GateRules::new));

    public static final GateRules DEFAULT = CODEC.decode(JsonOps.INSTANCE, new JsonObject()).getOrThrow().getFirst();

    /**
     * Builds a list of tooltips showing the deviations from {@link #DEFAULT}.
     */
    public List<MutableComponent> buildDeviations() {
        if (DEFAULT.equals(this)) return Collections.emptyList();
        List<MutableComponent> list = new ArrayList<>();
        append("spawn_range", list, this.spawnRange, DEFAULT.spawnRange);
        append("leash_range", list, this.leashRange, DEFAULT.leashRange);
        append("allow_discarding", list, this.allowDiscarding, DEFAULT.allowDiscarding);
        append("allow_dim_change", list, this.allowDimChange, DEFAULT.allowDimChange);
        append("player_damage_only", list, this.playerDamageOnly, DEFAULT.playerDamageOnly);
        append("remove_mobs_on_failure", list, this.removeOnFailure, DEFAULT.removeOnFailure);
        append("fail_on_out_of_bounds", list, this.failOnOutOfBounds, DEFAULT.failOnOutOfBounds);
        append("spacing", list, this.spacing, DEFAULT.spacing);
        return list;
    }

    private static <T> void append(String name, List<MutableComponent> list, T val, T def) {
        if (!val.equals(def)) {
            var comp = Component.translatable("rule.gateways." + name, fmt(val).withStyle(ChatFormatting.GREEN));
            comp.append(CommonComponents.SPACE);
            comp.append(Component.translatable("rule.gateways.default", fmt(def)).withStyle(ChatFormatting.DARK_GRAY));
            list.add(comp);
        }
    }

    private static MutableComponent fmt(Object val) {
        if (val instanceof Number n) return Component.literal(FORMAT.format(n));
        else if (val instanceof Boolean b) return Component.translatable("tooltip.gateways." + (b ? "true" : "false"));
        return Component.literal("Unknown: " + val);
    }

}
