package lt.tomexas.mystcore.resources.data.trees;

import lombok.Getter;
import lt.tomexas.mystcore.resources.managers.ItemManager;
import org.bukkit.inventory.ItemStack;

public class Axe {
    private final String item;
    @Getter
    private final int damage;
    @Getter
    private final int criticalHit;

    public Axe(String item, int damage, int criticalHit) {
        this.item = item;
        this.damage = damage;
        this.criticalHit = criticalHit;
    }

    public ItemStack getItem() {
        return ItemManager.getItemStack(item);
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
