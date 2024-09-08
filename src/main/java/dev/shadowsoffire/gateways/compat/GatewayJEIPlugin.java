package dev.shadowsoffire.gateways.compat;

import dev.shadowsoffire.gateways.GatewayObjects;
import dev.shadowsoffire.gateways.Gateways;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.TabVisibility;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

@JeiPlugin
public class GatewayJEIPlugin implements IModPlugin {

    @Override
    public void registerItemSubtypes(ISubtypeRegistration reg) {
        reg.registerSubtypeInterpreter(GatewayObjects.GATE_PEARL.value(), new GateOpenerSubtypes());
    }

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        Dummy d = new Dummy();
        GatewayObjects.GATE_PEARL.value().fillItemCategory(CreativeModeTabs.searchTab(), d);
        reg.addIngredientInfo(d.list, VanillaTypes.ITEM_STACK, Component.translatable("info.gateways.gate_pearl"), Component.translatable("info.gateways.gate_pearl.2"));
    }

    @Override
    public ResourceLocation getPluginUid() {
        return Gateways.loc("gateways");
    }

    private static class GateOpenerSubtypes implements IIngredientSubtypeInterpreter<ItemStack> {

        @Override
        public String apply(ItemStack stack, UidContext context) {
            if (stack.hasTag() && stack.getTag().contains("gateway")) {
                return stack.getTag().getString("gateway");
            }
            return ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
        }

    }

    private static class Dummy implements CreativeModeTab.Output {

        NonNullList<ItemStack> list = NonNullList.create();

        @Override
        public void accept(ItemStack pStack, TabVisibility pTabVisibility) {
            this.list.add(pStack);
        }

    }

}
