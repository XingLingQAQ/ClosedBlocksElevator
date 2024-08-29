package com.github.karmadeb.closedblocks.plugin.provider.block.type.mine;

import com.github.karmadeb.closedblocks.api.block.type.Mine;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import com.github.karmadeb.closedblocks.plugin.provider.block.BlockSaveData;

public final class MineSaveData extends BlockSaveData<Mine> {

    public MineSaveData(final ClosedBlocksPlugin plugin, final Mine block) {
        super(plugin, block);
    }

    /**
     * Tries to save the block
     *
     * @return if the block was saved
     */
    @Override
    public boolean saveBlockData() {
        return this.save((object) -> {
            object.set("power", this.block.getPower());
            object.set("fire", this.block.causesFire());
            object.set("defused", this.block.isDefused());
        });
    }
}
