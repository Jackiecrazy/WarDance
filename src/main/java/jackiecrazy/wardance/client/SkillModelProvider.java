package jackiecrazy.wardance.client;

import com.google.gson.JsonObject;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

public class SkillModelProvider  extends ModelProvider<SkillModelProvider.SkillModelBuilder> {

    public SkillModelProvider(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), modid, "skill", SkillModelBuilder::new, existingFileHelper);
    }

    private SkillModelBuilder build(Skill s){
        return getBuilder(s.getRegistryName().getPath())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", new ResourceLocation(s.icon().toString().replace("textures/", "").replace(".png", "")));
    }

    @NotNull
    @Override
    public String getName() {
        return "Item Models: " + modid;
    }

    @Override
    protected void registerModels() {
        final Set<Skill> set = WarSkills.SKILLS.getEntries().stream().map(RegistryObject::get).collect(Collectors.toSet());
        for (Skill s : set)
            build(s);
    }

    public static class SkillModelBuilder extends ModelBuilder<SkillModelBuilder> {


        public SkillModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper) {
            super(outputLocation, existingFileHelper);
        }

        @Override
        public JsonObject toJson() {
            JsonObject root = super.toJson();
            return root;
        }

    }
}