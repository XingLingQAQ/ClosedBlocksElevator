package com.github.karmadeb.closedblocks.api.block;

import com.github.karmadeb.closedblocks.api.block.type.Elevator;
import com.github.karmadeb.closedblocks.api.block.type.Mine;

/**
 * Block types
 */
public final class BlockType<T extends ClosedBlock> {

    public static final BlockType<Elevator> ELEVATOR = new BlockType<>("elevator", "elevators", Elevator.class);
    public static final BlockType<Mine> MINE = new BlockType<>("mine", "mines", Mine.class);

    private static final BlockType<?>[] TYPES = new BlockType[]{ELEVATOR, MINE};

    private final String singular;
    private final String plural;
    private final Class<T> type;

    /**
     * Initialize the block type
     *
     * @param singular the block type name
     * @param plural the block type name
     * @param type the block type class
     */
    private BlockType(final String singular, final String plural, final Class<T> type) {
        this.singular = singular;
        this.plural = plural;
        this.type = type;
    }

    /**
     * Get the block type name
     *
     * @return the block type name
     */
    public String singular() {
        return this.singular;
    }

    /**
     * Get the block type name
     *
     * @return the block type name
     */
    public String plural() {
        return this.plural;
    }

    /**
     * Get if the specified closed block
     * can be applied from this block type
     *
     * @param block the block
     * @return if the block can be applied
     */
    public boolean isType(final ClosedBlock block) {
        if (block == null) return false;
        return type.isInstance(block) || type.isAssignableFrom(block.getClass());
    }

    /**
     * Casts the closed block to the block
     * type
     *
     * @param block the block
     * @return the cast block
     */
    public T casted(final ClosedBlock block) {
        if (isType(block))
            return type.cast(block);

        return null;
    }

    /**
     * Get all the block types
     *
     * @return the block types
     */
    public static BlockType<?>[] values() {
        return TYPES.clone();
    }
}
