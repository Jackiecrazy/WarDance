package jackiecrazy.wardance.client;

import jackiecrazy.footwork.client.screen.dashboard.PonderingOrb;
import jackiecrazy.footwork.event.DashboardEvent;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.action.PermissionData;
import jackiecrazy.wardance.client.screen.skill.SkillSelectionScreen;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.meta.ManualizePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WarDance.MODID)
public class DashboardEvents {
    private static final ResourceLocation SKILL = new ResourceLocation(WarDance.MODID, "textures/gui/skill.png");
    private static final ResourceLocation MANUALIZE = new ResourceLocation(WarDance.MODID, "textures/gui/manualize.png");

    @SubscribeEvent
    public static void add(DashboardEvent e) {
        Player player = e.getPlayer();
        if (player.getMainHandItem().getItem() == Items.WRITTEN_BOOK)
            e.addThought(new PonderingOrb(e.getScreen(), MANUALIZE, a -> {
                if (player.getMainHandItem().getItem() == Items.WRITTEN_BOOK) {
                    CombatChannel.INSTANCE.sendToServer(new ManualizePacket());
                    player.displayClientMessage(Component.translatable("wardance.manualized"), true);
                    Minecraft.getInstance().setScreen(null);
                }
            }, Component.translatable("wardance.dashboard.manualize")));
        if (PermissionData.getCap(player).canSelectSkills()) {
            e.addThought(new PonderingOrb(e.getScreen(), SKILL, a -> e.getScreen().getMinecraft().setScreen(new SkillSelectionScreen()), Component.translatable("wardance.dashboard.skills")));
        }
    }
}
