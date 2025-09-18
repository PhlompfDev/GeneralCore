package com.phlompf.generalcore.commands;

import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class TargetInventoryContainer implements Container {
    public static final int SIZE = 54;
    private final ServerPlayer target;

    public TargetInventoryContainer(ServerPlayer target) {
        this.target = target;
    }

    // ----- Mapping helpers -----
    private boolean isMain(int slot) { return slot >= 0 && slot < 36; }
    private boolean isArmor(int slot) { return slot >= 36 && slot < 40; }
    private boolean isOffhand(int slot) { return slot == 40; }
    private boolean isDisabled(int slot) { return slot < 0 || slot >= SIZE || slot > 40; }

    private ItemStack ref(int slot) {
        if (isMain(slot)) return target.getInventory().items.get(slot);
        if (isArmor(slot)) return target.getInventory().armor.get(slot - 36);
        if (isOffhand(slot)) return target.getInventory().offhand.get(0);
        return ItemStack.EMPTY;
    }

    private void setRef(int slot, ItemStack stack) {
        if (isMain(slot)) {
            target.getInventory().items.set(slot, stack);
        } else if (isArmor(slot)) {
            target.getInventory().armor.set(slot - 36, stack);
        } else if (isOffhand(slot)) {
            target.getInventory().offhand.set(0, stack);
        }
    }

    // ----- Container interface -----
    @Override public int getContainerSize() { return SIZE; }

    @Override public boolean isEmpty() {
        for (int i = 0; i <= 40; i++) {
            ItemStack s = ref(i);
            if (!s.isEmpty()) return false;
        }
        return true;
    }

    @Override public ItemStack getItem(int slot) {
        if (isDisabled(slot)) return ItemStack.EMPTY;
        return ref(slot);
    }

    @Override public ItemStack removeItem(int slot, int amount) {
        if (isDisabled(slot)) return ItemStack.EMPTY;
        ItemStack stack = ref(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack split = stack.split(amount);
        if (stack.isEmpty()) setRef(slot, ItemStack.EMPTY);
        return split;
    }

    @Override public ItemStack removeItemNoUpdate(int slot) {
        if (isDisabled(slot)) return ItemStack.EMPTY;
        ItemStack stack = ref(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        setRef(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override public void setItem(int slot, ItemStack stack) {
        if (isDisabled(slot)) return;
        setRef(slot, stack);
    }

    @Override public void setChanged() {
        // We’re directly editing the target’s inventory; nothing else required here.
        // If you want, you could force a container sync on the target:
        // if (target.containerMenu != null) target.containerMenu.broadcastChanges();
    }

    @Override public boolean stillValid(Player player) {
        // Always valid; we allow remote access regardless of distance.
        return target.isAlive();
    }

    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        // Allow placing into mapped slots, disallow padding.
        return !isDisabled(slot);
    }

    @Override public void clearContent() {
        for (int i = 0; i <= 40; i++) setRef(i, ItemStack.EMPTY);
    }

    @Override public void startOpen(Player player) {}
    @Override public void stopOpen(Player player) {}
}
