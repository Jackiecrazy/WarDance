package jackiecrazy.wardance.mixin;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Gui.class)
public abstract class MixinInternalDamage {


    @Unique
    private int tempHP;

    @Shadow
    protected abstract void renderHeart(GuiGraphics p_283024_,
                                        Gui.HeartType p_281393_,
                                        int p_283636_,
                                        int p_283279_,
                                        int p_283188_,
                                        boolean p_283440_,
                                        boolean p_282496_);

    @Inject(method = "renderHearts", at = @At(value = "INVOKE", ordinal = 3, target = "Lnet/minecraft/client/gui/Gui;renderHeart(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Gui$HeartType;IIIZZ)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void wherearewe(GuiGraphics p_282497_,
                            Player p_168690_,
                            int p_168691_,
                            int p_168692_,
                            int p_168693_,
                            int p_168694_,
                            float p_168695_,
                            int p_168696_,
                            int p_168697_,
                            int p_168698_,
                            boolean p_168699_,
                            CallbackInfo ci,
                            Gui.HeartType gui$hearttype,
                            int i,
                            int j,
                            int k,
                            int l,
                            int i1,
                            int j1,
                            int k1,
                            int l1,
                            int i2,
                            int j2,
                            boolean flag2) {
        tempHP = j2;
    }

    @Redirect(method = "renderHearts",
            at = @At(value = "INVOKE", ordinal = 3, target = "Lnet/minecraft/client/gui/Gui;renderHeart(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Gui$HeartType;IIIZZ)V"))
    private void internalDamage(Gui instance,
                                GuiGraphics graphics,
                                Gui.HeartType heart,
                                int i1,
                                int i2,
                                int i3,
                                boolean b1,
                                boolean b2) {
        final LocalPlayer p = Minecraft.getInstance().player;
        //your health is greater than what you technically have
        if (tempHP > p.getHealth() - CombatData.getCap(p).getRecordedDamage())
            renderHeart(graphics, Gui.HeartType.WITHERED, i1, i2, i3, b1, b2);
        else renderHeart(graphics, heart, i1, i2, i3, b1, b2);
    }

}
