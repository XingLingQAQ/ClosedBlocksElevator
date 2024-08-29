package com.github.karmadeb.closedblocks.plugin.provider.block.type.elevator;

import com.github.karmadeb.closedblocks.api.block.type.Elevator;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import com.github.karmadeb.closedblocks.plugin.provider.block.BlockSaveData;

public final class ElevatorSaveData extends BlockSaveData<Elevator> {

    public ElevatorSaveData(final ClosedBlocksPlugin plugin, final Elevator block) {
        super(plugin, block);
    }

    /**
     * Tries to save the block
     *
     * @return if the block was saved
     */
    @Override
    public boolean saveBlockData() {
        return this.save();
    }
}
