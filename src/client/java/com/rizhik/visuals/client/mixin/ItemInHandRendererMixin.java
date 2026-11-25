package com.rizhik.visuals.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.rizhik.visuals.client.module.CustomHand;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Inject(method = "renderArmWithItem", at = @At("HEAD"), require = 0)
    private void rizhik$begin(AbstractClientPlayer player, float partialTicks, float pitch,
                              InteractionHand hand, float swing, ItemStack stack, float equip,
                              PoseStack poseStack, SubmitNodeCollector collector, int light,
                              CallbackInfo ci) {
        poseStack.pushPose();
        CustomHand.transform(poseStack, stack);
    }

    @Inject(method = "renderArmWithItem", at = @At("RETURN"), require = 0)
    private void rizhik$end(AbstractClientPlayer player, float partialTicks, float pitch,
                            InteractionHand hand, float swing, ItemStack stack, float equip,
                            PoseStack poseStack, SubmitNodeCollector collector, int light,
                            CallbackInfo ci) {
        poseStack.popPose();
    }
}
