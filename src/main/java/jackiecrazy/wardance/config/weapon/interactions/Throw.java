package jackiecrazy.wardance.config.weapon.interactions;

import jackiecrazy.footwork.move.action.Action;
import jackiecrazy.footwork.move.argument.Argument;
import jackiecrazy.footwork.move.argument.misc.RenderItemArgument;
import jackiecrazy.footwork.move.argument.stack.RawItemStackArgument;
import jackiecrazy.footwork.move.motionframe.*;
import jackiecrazy.footwork.move.utils.ArgumentContext;
import jackiecrazy.footwork.utils.MovementUtils;
import jackiecrazy.wardance.entity.ThrownWeaponEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class Throw extends WeaponInteractions.WeaponInteraction {
    public static final WeaponInteractions.InteractionGroup DEFAULT = new Throw().asGroup().addOverride(new WeaponInteractions.InteractionOverride(WeaponInteractions.BREACH_CONDITION, new Throw().setBounce(5).setHit(HitInfo.BREACH).asGroup()));
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
    private boolean pickup_flourish = true;
    private RenderItemArgument display_stack;
    private List<Action> on_impact = List.of();

    public Throw() {
    }

    public Throw setPierce(int pierce) {
        this.pierce = pierce;
        return this;
    }

    public Throw setFlying_pose(MotionManager flying_pose) {
        this.flying_pose = flying_pose;
        return this;
    }

    public Throw setDirection(Vec3 direction) {
        this.direction = direction;
        return this;
    }

    public Throw setOffset(Vec3 offset) {
        this.offset = offset;
        return this;
    }

    public Throw setGravity(double gravity) {
        this.gravity = gravity;
        return this;
    }

    public Throw setLodge_block(boolean lodge_block) {
        this.lodge_block = lodge_block;
        return this;
    }

    public Throw setLodge_entity(boolean lodge_entity) {
        this.lodge_entity = lodge_entity;
        return this;
    }

    public Throw setAuto_recall_cooldown(int auto_recall_cooldown) {
        this.auto_recall_cooldown = auto_recall_cooldown;
        return this;
    }

    public Throw setThrow_speed(double throw_speed) {
        this.throw_speed = throw_speed;
        return this;
    }

    public Throw setConsumeItem(boolean consume_item) {
        this.consume_item = consume_item;
        return this;
    }

    public Throw setImpact(List<Action> on_impact) {
        this.on_impact = on_impact;
        return this;
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

    public void applyCosmeticStack(ThrownWeaponEntity twe, ArgumentContext ctx) {
        if (display_stack == null) return;
        RenderItemGroup is = display_stack.resolve(ctx);
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

    public Vec3 transformDirection(Vec3 dir) {
        return MovementUtils.resolveVelocity(dir, direction);
    }

    public void transformThrown(ThrownWeaponEntity e) {
        e.setAutoRecall(auto_recall_cooldown);
        e.setLodgeEntity(lodge_entity);
        e.setLodgeBlock(lodge_block);
        e.setPierce(pierce).setBounce(bounce);
        e.setIdlePose(flying_pose);
        e.setGravity(gravity);
        e.setHitInfo(attack_info);
        e.setPos(e.position().add(MovementUtils.resolveVelocity(e.getDeltaMovement().normalize(), offset)));
        e.setImpactActions(on_impact);
        applyCosmeticStack(e, new ArgumentContext(e.getOwner(), null));
        e.setFake(!consume_item);
        e.setFlourish(pickup_flourish);
    }
}
