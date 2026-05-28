package jackiecrazy.wardance.capability.quiver;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
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
                    updateAvailableSlots();
                }
            };
            visibleSlots[i] = 1; // Start with 1 visible slot
        }
    }

    public static QuiverData getData(Player p) {
        return p.getCapability(QuiverData.QUIVER_CAP).orElseThrow(() -> new IllegalStateException("player has no quiver!"));
    }

    /**
     * Updates visible slots per quiver:
     * - Always shows enough slots to display every item currently in the quiver.
     * - Shows at least 1 extra empty slot (for adding new weapons), unless the quiver is completely full.
     * - Correctly handles gaps (e.g. item in slot 3 but slots 0-1 empty).
     */
    public void updateAvailableSlots() {
        for (int quiver = 0; quiver < NUM_QUIVERS; quiver++) {
            ItemStackHandler handler = quivers[quiver];
            int lastOccupied = -1;

            // Find the highest index that contains an item
            for (int s = 0; s < SLOTS_PER_QUIVER; s++) {
                if (!handler.getStackInSlot(s).isEmpty()) {
                    lastOccupied = s;
                }
            }

            if (lastOccupied == -1) {
                // No items at all
                visibleSlots[quiver] = 1;
            } else {
                int needed = lastOccupied + 1; // Show all slots up to and including the last item

                // If there is at least one empty slot anywhere, show one extra slot
                boolean hasEmptySlot = false;
                for (int s = 0; s < needed; s++) {
                    if (handler.getStackInSlot(s).isEmpty()) {
                        hasEmptySlot = true;
                        break;
                    }
                }

                if (!hasEmptySlot) {
                    needed = Math.min(SLOTS_PER_QUIVER, needed + 1);
                }
                // If completely full, needed stays at SLOTS_PER_QUIVER (no extra)

                visibleSlots[quiver] = Math.max(1, needed);
            }
        }
    }

    public int getVisibleSlots(int quiverIndex) {
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
    public boolean swapWithHand(Player player, InteractionHand h, int selectedSlot) {
        ItemStack hand = player.getItemInHand(h).copy();
        ItemStackHandler quiver = quivers[selectedQuiver];
        if (selectedSlot >= quiver.getSlots()) return false;
        if (selectedSlot < 0) {
            if (sheathe(hand, true)) {
                player.setItemInHand(h, ItemStack.EMPTY);
                return true;
            } else return false;
        }
        ItemStack inQuiver = quiver.extractItem(selectedSlot, 999, false);
        boolean success = false;

        try {
            boolean isWeapon = isWeapon(hand); // Implement your weapon check
            int preferredColor = getPreferredColor(hand); // -1 if none
            int preferredSlot = getPreferredSlot(hand); // -1 if none

            if (hand.isEmpty()) {
                //no thoughts, head empty. Just pull the weapon out.
                player.setItemInHand(h, inQuiver);
                markDirty(player);
                success = true;
            } else if (!isWeapon) {
                // Try overflow
                if (tryInsertOverflow(hand)) {
                    player.setItemInHand(h, ItemStack.EMPTY);
                    if (!inQuiver.isEmpty()) {
                        player.setItemInHand(h, inQuiver);
                        markDirty(player);
                        success = true;
                    }
                }
            } else {
                // Weapon handling
                if (preferredColor >= 0 && preferredSlot >= 0) {
                    // Move to preferred if different
                    if (quivers[preferredColor].insertItem(preferredSlot, hand, false).isEmpty()) {
                        player.setItemInHand(h, inQuiver);
                        markDirty(player);
                        success = true;
                    }
                }

                // no preferred colors? Try to get another slot in the same quiver, otherwise fail
                else {
                    int emptySlot = -1;
                    for (int fuckmylife = 0; fuckmylife < quivers[preferredColor].getSlots(); fuckmylife++)
                        if (quivers[preferredColor].getStackInSlot(fuckmylife).isEmpty()) {
                            emptySlot = fuckmylife;
                            break;
                        }
                    if (quiver.insertItem(emptySlot, hand, false).isEmpty()) { // insertItem returns remainder
                        player.setItemInHand(h, inQuiver);
                        markDirty(player);
                        success = true;
                    }
                }
            }
        } catch (Exception e) {
            WarDance.LOGGER.error("failed to swap weapons! ", e.fillInStackTrace());
        } finally {
            // failed, replace and return
            if (!success)
                quiver.setStackInSlot(selectedSlot, inQuiver);
        }
        return success;
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
        if (stack.hasTag()) {
            if (stack.getOrCreateTag().contains("quiverColorIndex"))
                return stack.getOrCreateTag().getInt("quiverColorIndex");
        }
        return -1;
    }

    private int getPreferredSlot(ItemStack stack) {
        // Return quiver index 0-7 or -1
        if (stack.hasTag()) {
            if (stack.getOrCreateTag().contains("quiverSlotIndex"))
                return stack.getOrCreateTag().getInt("quiverSlotIndex");
        }
        return -1;
    }

    // On GUI open / insert: assign weapon to clicked quiver or random
    public boolean sheathe(ItemStack stack, boolean assignNew) {
        if (!isWeapon(stack)) {
            return tryInsertOverflow(stack);
        }

        int quiver = getPreferredColor(stack);
        int slot = getPreferredSlot(stack);

        // find a good quiver for it
        if (quiver == -1) {
            if (!assignNew) return false;
            List<Integer> available = new ArrayList<>();
            for (int i = 0; i < NUM_QUIVERS; i++) {
                if (hasEmptySlot(i)) available.add(i);
            }
            if (!available.isEmpty()) {
                if (available.contains(selectedQuiver))
                    quiver = selectedQuiver;
                else {
                    int numOfWeapons = 100;
                    for (Integer i : available) {
                        if (getVisibleSlots(i) < numOfWeapons) {
                            numOfWeapons = getVisibleSlots(i);
                            quiver = i;
                        }
                    }
                }
            }
        }
        if (quiver >= 0) {
            for (int fuckmylife = 0; fuckmylife < quivers[quiver].getSlots(); fuckmylife++) {
                if (quivers[quiver].getStackInSlot(fuckmylife).isEmpty()) {
                    slot = fuckmylife;
                    break;
                }
            }
        }
        if (slot < 0 || quiver < 0)//couldn't find a good quiver or slot
            return false;
        if (!quivers[quiver].getStackInSlot(slot).isEmpty())//preferred spot taken
            return false;

        if (stack.getMaxStackSize() == 1) {
            stack.getOrCreateTag().putInt("quiverColorIndex", quiver);
            stack.getOrCreateTag().putInt("quiverSlotIndex", slot);
        }
        quivers[quiver].setStackInSlot(slot, stack);
        return true;
    }

    private boolean hasEmptySlot(int quiverIdx) {
        for (int i = 0; i < SLOTS_PER_QUIVER; i++) {
            if (quivers[quiverIdx].getStackInSlot(i).isEmpty()) return true;
        }
        return false;
    }

    void markDirty(Player player) {
        // Trigger sync
        for (int j = 0; j < quivers.length; j++) {
            ItemStackHandler is = quivers[j];
            for (int i = 0; i < is.getSlots(); i++) {
                if (!is.getStackInSlot(i).isEmpty()) {
                    ItemStack stack = is.getStackInSlot(i);
                    stack.getOrCreateTag().putInt("quiverColorIndex", j);
                    stack.getOrCreateTag().putInt("quiverSlotIndex", i);
                }
            }
        }
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