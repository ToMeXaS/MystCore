package lt.tomexas.mystcore.submodules.resources.trees.data;

import lt.tomexas.mystcore.managers.ItemManager;
import org.bukkit.inventory.ItemStack;

public record Drop(String item, int amount) {

    public ItemStack getItemStack() {
        return ItemManager.getItemStack(item, amount);
    }

    public String serialize() {
        return item + "/" + amount;
    }

    public static Drop deserialize(String serializedDrop) {
        if (serializedDrop == null || serializedDrop.isEmpty()) {
            throw new IllegalArgumentException("Invalid serialized drop format");
        }

        String[] parts = serializedDrop.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Serialized drop must have 2 parts");
        }

        String item = parts[0];
        int amount = Integer.parseInt(parts[1]);

        return new Drop(item, amount);
    }
}
