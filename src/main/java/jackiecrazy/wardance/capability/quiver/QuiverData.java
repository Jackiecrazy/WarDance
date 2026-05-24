package jackiecrazy.wardance.capability.quiver;

import jackiecrazy.wardance.config.weapon.WeaponStats;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class QuiverData implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static final Capability<QuiverData> QUIVER_CAP = CapabilityManager.get(new CapabilityToken<>() {
    });

    // 8 colors: white, gold, purple, red, green, cyan, blue, gray
    public static final int NUM_QUIVERS = 8;
    public static final int SLOTS_PER_QUIVER = 9;
    public static final int OVERFLOW_SIZE = 5;

    private final ItemStackHandler[] quivers = new ItemStackHandler[NUM_QUIVERS];
    private final ItemStackHandler overflow = new ItemStackHandler(OVERFLOW_SIZE);

    // Visual "unlocked" slots per quiver (starts at 1, increases as filled)
    private final int[] visibleSlots = new int[NUM_QUIVERS];
    private final LazyOptional<QuiverData> holder = LazyOptional.of(() -> this);
    private int selectedQuiver = 0; // Current active quiver index

    public QuiverData() {
        for (int i = 0; i < NUM_QUIVERS; i++) {
            quivers[i] = new ItemStackHandler(SLOTS_PER_QUIVER) {
                @Override
                protected void onContentsChanged(int slot) {
                    super.onContentsChanged(slot);
                    updateFilledSlots();
                }
            };
            visibleSlots[i] = 1; // Start with 1 visible slot
        }
    }

    public static QuiverData getData(Player p) {
        return p.getCapability(QuiverData.QUIVER_CAP).orElseThrow(() -> new IllegalStateException("player has no quiver!"));
    }

    void updateFilledSlots() {
        for (int i = 0; i < NUM_QUIVERS; i++) {
            int filled = 0;
            for (int s = 0; s < SLOTS_PER_QUIVER; s++) {
                if (!quivers[i].getStackInSlot(s).isEmpty()) filled++;
            }
            visibleSlots[i] = Math.max(1, Math.min(SLOTS_PER_QUIVER, filled + 1));
        }
    }

    public int getFilledSlots(int quiverIndex) {
        return visibleSlots[quiverIndex];
    }

    public ItemStackHandler getSelectedQuiverInventory() {
        return quivers[getSelectedQuiver()];
    }

    public ItemStackHandler getQuiver(int index) {
        return quivers[index];
    }

    public ItemStackHandler getOverflow() {
        return overflow;
    }

    public int getSelectedQuiver() {
        return selectedQuiver;
    }

    public void setSelectedQuiver(int idx) {
        if (idx >= 0 && idx < NUM_QUIVERS) selectedQuiver = idx;
    }

    public void cycleQuiver(boolean forward) {
        selectedQuiver = (selectedQuiver + (forward ? 1 : -1) + NUM_QUIVERS) % NUM_QUIVERS;
    }

    // Main swap logic
    public boolean swapWithHand(Player player, int selectedSlot) {
        ItemStack hand = player.getMainHandItem().copy();
        ItemStackHandler quiver = quivers[selectedQuiver];
        ItemStack inQuiver = quiver.getStackInSlot(selectedSlot).copy();

        boolean isWeapon = isWeapon(hand); // Implement your weapon check
        int preferredColor = getPreferredColor(hand); // -1 if none

        if (!hand.isEmpty() && !isWeapon) {
            // Try overflow
            if (tryInsertOverflow(hand)) {
                player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                if (!inQuiver.isEmpty()) {
                    player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, inQuiver);
                }
            } else {
                // Overflow full -> block swap
                return false;
            }
        } else {
            // Weapon handling
            if (preferredColor >= 0 && preferredColor != selectedQuiver) {
                // Move to preferred if different
                if (quivers[preferredColor].insertItem(selectedSlot, hand, false).isEmpty()) {
                    player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, inQuiver);
                }
                return true;
            }

            // Normal swap
            if (quiver.insertItem(selectedSlot, hand, false).isEmpty()) { // insertItem returns remainder
                player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, inQuiver);
            }
        }

        // Guard against overwrite: already handled by insertItem logic + copies
        markDirty(player);
        return true;
    }

    private boolean tryInsertOverflow(ItemStack stack) {
        for (int i = 0; i < OVERFLOW_SIZE; i++) {
            if (overflow.getStackInSlot(i).isEmpty()) {
                overflow.setStackInSlot(i, stack);
                return true;
            }
        }
        return false;
    }

    // Implement these based on your mod's weapon definition
    private boolean isWeapon(ItemStack stack) {
        return WeaponStats.isWeapon(null, stack);
    }

    private int getPreferredColor(ItemStack stack) {
        // Return quiver index 0-7 or -1
        // You can use NBT on the weapon item or registry lookup
        return -1;
    }

    // On GUI open / insert: assign weapon to clicked quiver or random
    public void assignOnInsert(ItemStack stack, int targetQuiver, int targetSlot) {
        if (!isWeapon(stack)) {
            tryInsertOverflow(stack);
            return;
        }

        int preferred = getPreferredColor(stack);
        int useQuiver = (preferred >= 0) ? preferred : targetQuiver;

        // Random if needed
        if (useQuiver == -1) {
            List<Integer> available = new ArrayList<>();
            for (int i = 0; i < NUM_QUIVERS; i++) {
                if (hasEmptySlot(i)) available.add(i);
            }
            if (!available.isEmpty()) {
                useQuiver = available.get(new Random().nextInt(available.size()));
            } else {
                useQuiver = targetQuiver;
            }
        }

        quivers[useQuiver].setStackInSlot(targetSlot, stack);
    }

    private boolean hasEmptySlot(int quiverIdx) {
        for (int i = 0; i < SLOTS_PER_QUIVER; i++) {
            if (quivers[quiverIdx].getStackInSlot(i).isEmpty()) return true;
        }
        return false;
    }

    void markDirty(Player player) {
        // Trigger sync
        player.inventoryMenu.broadcastChanges();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == QUIVER_CAP ? holder.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag quiversTag = new ListTag();
        for (ItemStackHandler handler : quivers) {
            quiversTag.add(handler.serializeNBT());
        }
        tag.put("quivers", quiversTag);
        tag.put("overflow", overflow.serializeNBT());
        tag.putInt("selectedQuiver", selectedQuiver);

        CompoundTag visibleTag = new CompoundTag();
        for (int i = 0; i < NUM_QUIVERS; i++) {
            visibleTag.putInt("v" + i, visibleSlots[i]);
        }
        tag.put("visible", visibleTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ListTag quiversTag = nbt.getList("quivers", 10); // Compound tag type
        for (int i = 0; i < Math.min(quiversTag.size(), NUM_QUIVERS); i++) {
            quivers[i].deserializeNBT(quiversTag.getCompound(i));
        }
        overflow.deserializeNBT(nbt.getCompound("overflow"));
        selectedQuiver = nbt.getInt("selectedQuiver");

        CompoundTag visibleTag = nbt.getCompound("visible");
        for (int i = 0; i < NUM_QUIVERS; i++) {
            visibleSlots[i] = visibleTag.getInt("v" + i);
            if (visibleSlots[i] == 0) visibleSlots[i] = 1;
        }
    }
}