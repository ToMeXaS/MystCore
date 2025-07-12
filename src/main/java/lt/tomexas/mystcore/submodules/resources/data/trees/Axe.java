package lt.tomexas.mystcore.submodules.resources.data.trees;

import lt.tomexas.mystcore.submodules.resources.managers.ItemManager;
import org.bukkit.inventory.ItemStack;

public record Axe(String item, int damage, int criticalHit) {

    public ItemStack getItemStack() {
        return ItemManager.getItemStack(item, 1);
    }

    // Serialize the Axe object to a string
    public String serialize() {
        return item + "/" + damage + "/" + criticalHit;
    }

    // Deserialize a string back into an Axe object
    public static Axe deserialize(String serializedAxe) {
        if (serializedAxe == null || serializedAxe.isEmpty()) {
            throw new IllegalArgumentException("Invalid serialized axe format");
        }

        String[] parts = serializedAxe.split("/");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Serialized axe must have 3 parts");
        }

        String item = parts[0];
        int damage = Integer.parseInt(parts[1]);
        int criticalHit = Integer.parseInt(parts[2]);

        return new Axe(item, damage, criticalHit);
    }
}
