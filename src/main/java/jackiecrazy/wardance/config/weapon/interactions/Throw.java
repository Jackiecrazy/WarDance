package jackiecrazy.wardance.config.weapon.interactions;

import jackiecrazy.footwork.move.argument.Argument;
import jackiecrazy.footwork.move.motionframe.HitInfo;
import jackiecrazy.footwork.move.motionframe.MotionFrame;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.footwork.move.motionframe.MotionManagers;
import jackiecrazy.footwork.move.utils.ArgumentContext;
import jackiecrazy.wardance.entity.ThrownWeaponEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class Throw extends WeaponInteractions.WeaponInteraction {
    public static final WeaponInteractions.InteractionGroup DEFAULT = new Throw().setBounce(5).asGroup()
            .addOverride(
                    new WeaponInteractions.InteractionOverride(WeaponInteractions.BREACH_CONDITION, new Throw().setHit(HitInfo.BREACH).asGroup()));
    private HitInfo attack_info = HitInfo.BREACH;
    private int pierce = 0;
    private int bounce = 0;
    private MotionManager flying_pose = new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 0)), 5);
    private Vec3 direction = new Vec3(0, 0, 1);
    private Vec3 offset = new Vec3(0, 0, 1);
    private double gravity = 0;
    private boolean lodge_block = true;
    private boolean lodge_entity = true;
    private int auto_recall_cooldown = -1;
    private double throw_speed = 2;
    private boolean consume_item = true;
    private Argument<ItemStack> display_stack;

    public Throw() {
    }

    public Throw setBounce(int bounce) {
        this.bounce = bounce;
        return this;
    }

    public Throw setHit(HitInfo attack_info) {
        this.attack_info = attack_info;
        return this;
    }

    public double getThrowSpeed() {
        return throw_speed;
    }
            /*
            pierce bounce embed (wall/entity) -> hit wall/entity behavior?
            auto retrieve/homing return, pull player? impact aoe/shatter
            homing throw, hitstop on mobs?
             */

    public boolean consume() {
        return consume_item;
    }

    @Override
    public InteractionType getInteractionType() {
        return InteractionType.THROW;
    }

    @Override
    public WeaponInteractions.WeaponInteraction clone() {
        return new Throw();
    }

    @Override
    public void write(FriendlyByteBuf f) {
        super.write(f);
        f.writeInt(pierce);
        f.writeInt(bounce);
        f.writeDouble(gravity);
    }

    public void applyCosmeticStack(ThrownWeaponEntity twe, ArgumentContext ctx){
        if(display_stack==null)return;
        ItemStack is= display_stack.resolve(ctx);
        twe.setCosmeticItem(is);
    }

    @Override
    public WeaponInteractions.WeaponInteraction read(FriendlyByteBuf f) {
        super.read(f);
        pierce = f.readInt();
        bounce = f.readInt();
        gravity = f.readDouble();
        return this;
    }

    public void transformThrown(ThrownWeaponEntity e) {
        e.setAutoRecall(auto_recall_cooldown);
        e.setLodgeEntity(lodge_entity);
        e.setLodgeBlock(lodge_block);
        e.setPierce(pierce).setBounce(bounce);
        e.setIdlePose(flying_pose);
        e.setGravity(gravity);
        e.setFake(!consume_item);
        e.setHitInfo(attack_info);
        applyCosmeticStack(e, new ArgumentContext(e.getOwner(), null));
    }
}
