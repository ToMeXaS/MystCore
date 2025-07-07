package lt.tomexas.mystcore.submodules.worldguard.flags;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import lt.tomexas.mystcore.Main;
import lt.tomexas.mystcore.MystPlayer;
import lt.tomexas.mystcore.listeners.PlayerAreaEnterListener;
import org.bukkit.entity.Player;

public class DenyEntryFlag {

    public static IntegerFlag DENY_ENTRY_CODE_FLAG = new IntegerFlag("deny-entry-combat");

    private static final Main plugin = Main.getInstance();

    // Call this from your plugin's onEnable()
    public static void register() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            registry.register(DENY_ENTRY_CODE_FLAG);
            plugin.getServer().getPluginManager().registerEvents(new PlayerAreaEnterListener(), plugin);
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("deny-entry-combat");
            if (existing instanceof IntegerFlag) {
                DENY_ENTRY_CODE_FLAG = (IntegerFlag) existing;
            } else {
                System.err.println("[MystCore] 'deny-entry-combat' flag conflict with a different type!");
            }
        }
    }

    // Call this in your move event, passing the player and their code
    public static boolean canEnter(Player player) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));
        Integer requiredLevel = set.queryValue(localPlayer, DENY_ENTRY_CODE_FLAG);

        MystPlayer mystPlayer = MystPlayer.getMystPlayer(player);
        if (mystPlayer == null) {
            // If MystPlayer is not found, deny entry
            return true;
        }

        int playerLevel = mystPlayer.getPlayerData().getCollectionSkills().getLevel("combat");

        if (requiredLevel == null) {
            // If the flag is not set, allow entry
            return true;
        }

        // Allow entry only if the codes match
        return requiredLevel <= playerLevel;
    }

}
