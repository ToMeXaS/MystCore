package lt.tomexas.mystcore.submodules.worldguard.flags;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import lt.tomexas.mystcore.data.MystPlayer;
import lt.tomexas.mystcore.submodules.worldguard.data.EntryCheckResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class DenyEntryFlag {

    public static StringFlag DENY_ENTRY_SKILL = new StringFlag("deny-entry-skill");
    public static IntegerFlag DENY_ENTRY_SKILL_LEVEL = new IntegerFlag("deny-entry-skill-level");
    public static StringFlag DENY_ENTRY_SKILL_MSG = new StringFlag("deny-entry-skill-msg");

    public static void register() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            registry.register(DENY_ENTRY_SKILL);
            registry.register(DENY_ENTRY_SKILL_LEVEL);
            registry.register(DENY_ENTRY_SKILL_MSG);
        } catch (FlagConflictException e) {
            Flag<?> existingSkill = registry.get("deny-entry-skill");
            if (existingSkill instanceof StringFlag) {
                DENY_ENTRY_SKILL = (StringFlag) existingSkill;
            } else if (existingSkill != null) {
                System.err.println("[MystCore] 'deny-entry-skill' flag conflict with a different type: " + existingSkill.getClass().getSimpleName());
            }

            Flag<?> existingSkillMsg = registry.get("deny-entry-skill-msg");
            if (existingSkillMsg instanceof StringFlag) {
                DENY_ENTRY_SKILL_MSG = (StringFlag) existingSkillMsg;
            } else if (existingSkillMsg != null) {
                System.err.println("[MystCore] 'deny-entry-skill-msg' flag conflict with a different type: " + existingSkillMsg.getClass().getSimpleName());
            }

            Flag<?> existingSkillLevel = registry.get("deny-entry-skill-level");
            if (existingSkillLevel instanceof IntegerFlag) {
                DENY_ENTRY_SKILL_LEVEL = (IntegerFlag) existingSkillLevel;
            } else if (existingSkillLevel != null) {
                System.err.println("[MystCore] 'deny-entry-skill-level' flag conflict with a different type: " + existingSkillLevel.getClass().getSimpleName());
            }
        }
    }

    public static EntryCheckResult canEnter(Player player, Location destination) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(destination));
        Integer requiredLevel = set.queryValue(localPlayer, DENY_ENTRY_SKILL_LEVEL);
        String requiredSkill = set.queryValue(localPlayer, DENY_ENTRY_SKILL);
        String requiredMessage = set.queryValue(localPlayer, DENY_ENTRY_SKILL_MSG);

        if (requiredSkill == null || requiredLevel == null || requiredMessage == null) {
            return new EntryCheckResult(true, null);
        }

        requiredMessage = requiredMessage.replace("%skill%", requiredSkill)
                .replace("%level%", requiredLevel.toString());

        requiredMessage = LegacyComponentSerializer.legacyAmpersand().serialize(Component.text(requiredMessage));

        MystPlayer mystPlayer = MystPlayer.getMystPlayer(player);
        if (mystPlayer == null) {
            return new EntryCheckResult(true, null);
        }

        int playerLevel = mystPlayer.getPlayerData().getCollectionSkills().getLevel(requiredSkill);

        boolean canEnter = playerLevel >= requiredLevel;
        return new EntryCheckResult(canEnter, requiredMessage);
    }

}
