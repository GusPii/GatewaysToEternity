package dev.shadowsoffire.gateways.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

@Mixin(value = SpawnerBlockEntity.class, remap = false)
public class SpawnerBlockEntityMixin {

    /**
     * Used to allow the dropped mob spawners from the Gateway of the Hellish Fortress to work properly.
     * Without this, they will lose all NBT data on placement.
     */
    @Inject(at = @At("HEAD"), method = "onlyOpCanSetNbt()Z", require = 1, cancellable = true)
    private void gateways_allowSetNbt(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

}
