package com.rizhik.visuals.client.mixin;

import com.rizhik.visuals.client.module.SkyColor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public class SkyColorMixin {
    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true, require = 0)
    private void rizhik$sky(CallbackInfoReturnable<Object> cir) {
        SkyColor m = SkyColor.INSTANCE;
        if (m == null || !m.isEnabled()) return;
        SkyColor.hooked = true;
        int rgb = m.rgb();
        Object old = cir.getReturnValue();
        if (old instanceof Integer) {
            cir.setReturnValue(0xFF000000 | rgb);
        } else if (old instanceof Vec3) {
            cir.setReturnValue(new Vec3(((rgb >> 16) & 255) / 255.0, ((rgb >> 8) & 255) / 255.0, (rgb & 255) / 255.0));
        }
    }
}
