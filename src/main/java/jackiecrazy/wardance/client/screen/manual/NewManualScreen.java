package jackiecrazy.wardance.client.screen.manual;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import jackiecrazy.wardance.client.screen.skill.PassiveButton;
import jackiecrazy.wardance.client.screen.skill.SkillSelectionScreen;
import jackiecrazy.wardance.client.screen.skill.SkillSliceButton;
import jackiecrazy.wardance.client.screen.skill.SkillStyleButton;
import jackiecrazy.wardance.items.ManualItem;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.LearnManualPacket;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class NewManualScreen extends SkillSelectionScreen {
    /*
    this is a skill selection screen with the following caveats:
    there is no filter sidebar to select skills.
    info panel space is split for two panels
    panel on the left displays manual data
    panel on the right displays skill descriptions
     */

    private ItemStack manual;
    private Button study, cancel;
    private InfoPanel manualDesc;


    public NewManualScreen(ItemStack from) {
        manual = from;
        int skillCircleWidth = 150;
        style = new SkillStyleButton(this, width - skillCircleWidth / 2 - 12, PADDING + skillCircleWidth / 2 - 12, 23);
        style.setSkill(ManualItem.getStyle(from));
        skillCache = ManualItem.getSkills(from);
    }

    @Override
    protected void renderSearchText(PoseStack mStack) {

    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
    }

    @Override
    public void init() {
        int y = this.height - 20 - PADDING;
        study = new Button(this.width / 2 + 20, y, 80, 20, Component.translatable("wardance.manual.gui.study"), (p_98299_) -> {
            //send study request to server
            if (ManualItem.learn(getMinecraft().player, manual).getResult() != InteractionResult.FAIL) {
                CombatChannel.INSTANCE.sendToServer(new LearnManualPacket(this.minecraft.player.getMainHandItem()!=this.manual));
            }
            this.minecraft.setScreen(null);
        });
        cancel = new Button(this.width / 2 - 100, y, 80, 20, CommonComponents.GUI_CANCEL, (p_98299_) -> {
            this.minecraft.setScreen(null);
        });
        int split = (this.height - (PADDING) * 3 - 20);
        int descWidth = (width - (PADDING * 4) - SKILL_CIRCLE_WIDTH)/2;
        this.manualDesc = new InfoPanel(this.minecraft, descWidth, split, PADDING, PADDING);
        super.init();
        this.skillInfo = new InfoPanel(this.minecraft, descWidth, split, PADDING * 2 + descWidth, PADDING);
        clearWidgets();
        ArrayList<String> add=new ArrayList<>();
        BookViewScreen.WrittenBookAccess wba =new BookViewScreen.WrittenBookAccess(manual);
        for(int x=0;x<wba.getPageCount();x++){
            add.add(wba.getPageRaw(x).getString());
        }
        manualDesc.setInfo(add, null);
        addWidgets();
        updateCache();
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if(button==1)return false;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * copied verbatim from BookViewScreen
     */
    private static List<String> readPages(ItemStack stack) {
        CompoundTag compoundtag = stack.getTag();
        if(compoundtag==null)return ImmutableList.of();
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        BookViewScreen.loadPages(compoundtag, builder::add);
        return builder.build();
    }

    protected void updateCache() {
        if (style.getStyle() == null && selectedSkill == null) {
            this.skillInfo.clearInfo();
            List<String> lines = new ArrayList<>();
            lines.add(Component.translatable("wardance.manual.gui.default").getString() + "\n");
            skillInfo.setInfo(lines, null);
            return;
        }
        if (selectedSkill == null) {
            this.skillInfo.clearInfo();
            List<String> lines = new ArrayList<>();
            lines.add(Component.translatable("wardance.manual.gui.default").getString() + "\n");
            skillInfo.setInfo(lines, null);
            return;
        }
        Skill selectedSkill = this.selectedSkill.getSkill();
        displaySkillInfo(selectedSkill);
    }

    @Override
    protected void addWidgets() {
        //use a builder in 1.19.3
        addRenderableWidget(study);
        addRenderableWidget(cancel);
        addRenderableWidget(manualDesc);
        for (SkillSliceButton skillSliceButton : skillPie) {
            addRenderableWidget(skillSliceButton);
        }
        for (PassiveButton passive : passives) {
            addRenderableWidget(passive);
        }
        addRenderableWidget(skillInfo);
        addRenderableWidget(style);
    }
}
