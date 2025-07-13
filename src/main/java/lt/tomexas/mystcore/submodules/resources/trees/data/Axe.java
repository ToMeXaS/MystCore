package lt.tomexas.mystcore.submodules.resources.trees.data;

import lt.tomexas.mystcore.managers.ItemManager;
import org.bukkit.inventory.ItemStack;

public record Axe(String item, double damage, double criticalHit) {

    public ItemStack getItemStack() {
        return ItemManager.getItemStack(item, 1);
    }

    public String serialize() {
        return item + "/" + damage + "/" + criticalHit;
    }

    public static Axe deserialize(String serializedAxe) {
        if (serializedAxe == null || serializedAxe.isEmpty()) {
            throw new IllegalArgumentException("Invalid serialized axe format");
        }

        String[] parts = serializedAxe.split("/");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Serialized axe must have 3 parts");
        }

        String item = parts[0];
        double damage = Double.parseDouble(parts[1]);
        double criticalHit = Double.parseDouble(parts[2]);

        return new Axe(item, damage, criticalHit);
    }
}
