package com.github.karmadeb.closedblocks.api.item;

/**
 * Item types
 */
public final class ItemType {

    public static final ItemType DIFFUSER = new ItemType("diffuser");

    private static final ItemType[] TYPES = new ItemType[]{DIFFUSER};

    private final String name;

    /**
     * Initialize the item type
     *
     * @param name the item name
     */
    private ItemType(final String name) {
        this.name = name;
    }

    /**
     * Get the block type name
     *
     * @return the block type name
     */
    public String name() {
        return this.name;
    }

    /**
     * Get all the block types
     *
     * @return the block types
     */
    public static ItemType[] values() {
        return TYPES.clone();
    }
}
