package com.github.karmadeb.closedblocks.api.item;

/**
 * Represents a diffuser for a mine
 */
@SuppressWarnings("unused")
public interface MineDiffuser {

    /**
     * Get the diffuser usages
     *
     * @return the amount of usages
     */
    byte getUsages();

    /**
     * Set the amount of usages remaining
     * for the diffuser
     *
     * @param usages the amount of usages
     */
    void setUsages(final byte usages);

    /**
     * Use the diffuser
     */
    default void use() {
        byte currentUsages = this.getUsages();
        byte newUsages = (byte) Math.max(0, Math.min(127, currentUsages - 1));

        this.setUsages(newUsages);
    }
}
