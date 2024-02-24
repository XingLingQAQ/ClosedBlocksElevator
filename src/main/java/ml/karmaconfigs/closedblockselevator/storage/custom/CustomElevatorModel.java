package ml.karmaconfigs.closedblockselevator.storage.custom;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Mushroom;
import org.bukkit.material.types.MushroomBlockTexture;

public class CustomElevatorModel {

    private final String name;
    private final boolean isCustom;
    private final Material material;

    public CustomElevatorModel(final String n) {
        name = n;
        Material tmp = Material.getMaterial(n);
        if (tmp == null) {
            tmp = Material.QUARTZ_BLOCK;
            isCustom = true;
        } else {
            isCustom = false;
            if (!tmp.isBlock())
                tmp = Material.QUARTZ_BLOCK;
        }

        material = tmp;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public Material find() {
        return material;
    }

    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return name;
    }
}
