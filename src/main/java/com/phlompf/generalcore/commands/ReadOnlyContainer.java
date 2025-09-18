package com.phlompf.generalcore.commands;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A SimpleContainer that is read-only after initialization.
 */
public class ReadOnlyContainer extends SimpleContainer {
    private boolean initializing = true;

    public ReadOnlyContainer(int size) {
        super(size);
    }

    /** Call after you've populated initial contents. */
    public void finishInit() {
        this.initializing = false;
    }

    @Override
    public boolean stillValid(Player player) { return true; }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) { return false; }

    @Override
    public ItemStack removeItem(int slot, int amount) { return ItemStack.EMPTY; }

    @Override
    public ItemStack removeItemNoUpdate(int slot) { return ItemStack.EMPTY; }

    @Override
    public void setItem(int slot, ItemStack stack) {
        // Allow writes only during initialization
        if (initializing) {
            super.setItem(slot, stack);
        }
    }
}