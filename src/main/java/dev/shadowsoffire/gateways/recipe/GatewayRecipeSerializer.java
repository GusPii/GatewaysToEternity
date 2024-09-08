package dev.shadowsoffire.gateways.recipe;

import java.util.function.Function;

import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.gateways.gate.Gateway;
import dev.shadowsoffire.gateways.gate.GatewayRegistry;
import dev.shadowsoffire.gateways.item.GatePearlItem;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;

/**
 * Extension of the {@link ShapedRecipe.Serializer} which adds a "gateway" field and sets the gateway as a component on the result item.
 */
public class GatewayRecipeSerializer extends ShapedRecipe.Serializer {

    public static final MapCodec<ShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        ShapedRecipe.Serializer.CODEC.forGetter(Function.identity()),
        GatewayRegistry.INSTANCE.holderCodec().fieldOf("gateway").forGetter(GatewayRecipeSerializer::resolveGatewayHolder))
        .apply(inst, GatewayRecipeSerializer::buildRecipe));

    public static ShapedRecipe buildRecipe(ShapedRecipe recipe, DynamicHolder<Gateway> holder) {
        ItemStack gateway = recipe.getResultItem(null);
        if (!(gateway.getItem() instanceof GatePearlItem)) {
            throw new JsonSyntaxException("Gateway Recipe output must be a gate opener item.  Provided: " + BuiltInRegistries.ITEM.getKey(gateway.getItem()));
        }
        GatePearlItem.setGate(gateway, holder.get());
        return recipe;
    }

    public static DynamicHolder<Gateway> resolveGatewayHolder(ShapedRecipe recipe) {
        ItemStack gateway = recipe.getResultItem(null);
        return GatePearlItem.getGate(gateway);
    }
}
