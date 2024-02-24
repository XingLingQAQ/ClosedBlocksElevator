package ml.karmaconfigs.closedblockselevator.storage.custom;

import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.ItemsAdder;
import ml.karmaconfigs.api.common.karma.file.KarmaMain;
import ml.karmaconfigs.api.common.karma.file.element.types.Element;
import ml.karmaconfigs.api.common.karma.file.element.types.ElementPrimitive;
import ml.karmaconfigs.api.common.utils.enums.Level;
import ml.karmaconfigs.closedblockselevator.Main;
import ml.karmaconfigs.closedblockselevator.storage.Config;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import static ml.karmaconfigs.closedblockselevator.ClosedBlocksElevator.plugin;

public class IAdder {

    public static ItemStack getItem() {
        Config config = new Config();
        String model_name = config.literalElevatorMaterialName();

        CustomStack stack = CustomStack.getInstance(model_name);
        if (stack != null && stack.isBlock()) {
            return stack.getItemStack();
        }

        return null;
    }

    public static boolean isCustomBlock(final Block block) {
        if (Main.hasItemAdder()) {
            CustomBlock cb = CustomBlock.byAlreadyPlaced(block);
            return cb != null;
        }

        return false;
    }

    public static boolean isCustomItem(final ItemStack hand) {
        if (Main.hasItemAdder()) {
            CustomStack st = CustomStack.byItemStack(hand);
            return st != null;
        }

        return false;
    }

    public static String getNameSpace(final ItemStack item) {
        CustomStack stack = CustomStack.byItemStack(item);
        if (stack != null && stack.isBlock()) {
            return stack.getNamespacedID();
        }

        return null;
    }

    public static String getNameSpace(final Block block) {
        CustomBlock custom = CustomBlock.byAlreadyPlaced(block);
        if (custom != null) {
            return custom.getNamespacedID();
        }

        return null;
    }

    public static ItemStack getItem(final ItemStack item) {
        CustomStack stack = CustomStack.byItemStack(item);
        if (stack != null && stack.isBlock()) {
            return stack.getItemStack();
        }

        return null;
    }

    public static boolean isBlock(final ItemStack item) {
        CustomStack c_stack = CustomStack.byItemStack(item);
        return c_stack != null && c_stack.isBlock();
    }

    public static ItemStack getItem(final String name) {
        CustomStack stack = CustomStack.getInstance(name);
        if (stack != null && stack.isBlock()) {
            return stack.getItemStack();
        }

        return null;
    }
    public static void tryBlock(final String name_space, final Location location) {
        Config config = new Config();
        CustomStack stack = CustomStack.getInstance(name_space);
        if (stack != null && stack.isBlock()) {
            CustomBlock block = CustomBlock.getInstance(stack.getNamespacedID());
            if (block != null) {
                block.place(location);
            }
        }
    }

    public static void tryBlock(final Location location) {
        Config config = new Config();
        String model_name = config.literalElevatorMaterialName();
        CustomStack stack = CustomStack.getInstance(model_name);
        if (stack != null && stack.isBlock()) {
            CustomBlock block = CustomBlock.getInstance(stack.getNamespacedID());
            if (block != null) {
                block.place(location);
            }
        }
    }

    public static int getOffset(final Block block) {
        KarmaMain offsets = new KarmaMain(plugin, "offsets.kf").internal(Main.class.getResourceAsStream("/offsets.kf"));
        if (!offsets.exists()) offsets.exportDefaults();

        if (isCustomBlock(block)) {
            String nameSpace = getNameSpace(block);
            Element<?> element = offsets.get(nameSpace);

            if (element.isPrimitive()) {
                ElementPrimitive primitive = element.getAsPrimitive();
                if (primitive.isString()) {
                    String value = element.getAsString();
                    if (value.startsWith("+") || value.startsWith("-")) {
                        return Integer.parseInt(value);
                    }
                }
            }
        }

        return 0;
    }
}
