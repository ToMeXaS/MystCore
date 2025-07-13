package lt.tomexas.mystcore.submodules.resources.trees.managers;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.entity.Dummy;
import lt.tomexas.mystcore.Main;
import lt.tomexas.mystcore.PluginLogger;
import lt.tomexas.mystcore.data.MystPlayer;
import lt.tomexas.mystcore.data.enums.Permissions;
import lt.tomexas.mystcore.managers.EntityManager;
import lt.tomexas.mystcore.submodules.resources.trees.data.*;
import lt.tomexas.mystcore.other.Animations;
import lt.tomexas.mystcore.submodules.resources.trees.data.config.TreeConfig;
import lt.tomexas.mystcore.submodules.stats.stamina.config.StaminaConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.experience.Profession;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class TreeChopperManager {

    private final Main plugin = Main.getInstance();
    private final Map<String, TreeConfig> treeConfigs = plugin.getTreeConfigs();
    private final StaminaConfig staminaConfig = plugin.getStaminaConfig();
    private static final int INACTIVITY_TIMEOUT = 30; // seconds

    private final Map<UUID, Double> hitCounts = new HashMap<>();
    private final Map<UUID, BukkitRunnable> inactivityTimers = new HashMap<>();

    public TreeChopperManager() {
        new TreeDisplayManager();
    }

    public void handleChop(MystPlayer mystPlayer, UUID entityId) {
        Tree tree = Tree.getTreeByUuid(entityId);
        if (tree == null || mystPlayer == null || entityId == null) return;
        Skill skill = mystPlayer.getSkill(tree);
        Axe axe = mystPlayer.getAxe(tree);
        Player player = mystPlayer.getPlayer();
        if (isChoppedDown(player, tree)) return;
        if (!canHarvest(player, tree)) return;
        if (isAlreadyHarvestingDifferentTree(player, tree)) return;
        if (skill == null || axe == null) return;
        TreeConfig config = treeConfigs.get(tree.getModelId());
        double playerAttackCooldown = player.getAttackCooldown();
        if (playerAttackCooldown < 0.5) {
            player.sendMessage("§cYou're too tired to chop again so soon!");
            return;
        }
        tree.setHarvester(player);
        if (isCriticalHit(mystPlayer, tree)) return;

        double baseDamage = axe.damage();
        double scaledDamage = baseDamage * playerAttackCooldown;
        hitCounts.merge(entityId, scaledDamage, Double::sum);

        EntityManager.updateTextDisplay(entityId);
        resetInactivityTimer(player, entityId);

        EntityManager.updateHealthDisplay(mystPlayer, entityId, hitCounts.getOrDefault(entityId, 0.0));
        ChopSound chopSound = config.getChopSound();
        player.playSound(player.getLocation(), chopSound.type(), chopSound.volume(), chopSound.pitch());

        if (hitCounts.getOrDefault(entityId, 0.0) >= skill.health()) chopTree(mystPlayer, entityId);
    }

    private void chopTree(MystPlayer mystPlayer, UUID entityId) {
        Tree tree = Tree.getTreeByUuid(entityId);
        if (tree == null) return;
        Skill skill = mystPlayer.getSkill(tree);
        if (skill == null) return;

        hitCounts.remove(entityId);
        cancelInactivityTimer(entityId);
        tree.setChopped(true);
        tree.setHarvester(null);
        /*/tree.getBarrierBlocks().stream()
                .sorted(Comparator.comparingInt(block -> -block.getY()))
                .limit(2)
                .forEach(block -> block.setType(Material.AIR));*/

        IAnimationProperty animation = Animations.play(tree, Animations.ANIMATION_LIST.FALL);
        if (animation == null) return;

        //if (Boolean.TRUE.equals(glowingTrees.remove(entityId))) setTreeGlow(entityId, false);

        tree.removeHealthDisplay();

        if (tree.getDrops() != null) {
            List<Drop> drops = new ArrayList<>(tree.getDrops()); // Clone the drops list
            for (Drop drop : drops) {
                HashMap<Integer, ItemStack> remaining = mystPlayer.getPlayer().getInventory().addItem(drop.getItemStack());
                if (!remaining.isEmpty()) {
                    // If the item couldn't be fully added, drop the remaining items on the ground
                    for (ItemStack leftover : remaining.values()) {
                        mystPlayer.getPlayer().getWorld().dropItemNaturally(mystPlayer.getPlayer().getLocation(), leftover);
                    }
                    mystPlayer.getPlayer().sendMessage("§eSome items were dropped on the ground due to insufficient inventory space.");
                }
            }
        }

        if (!staminaConfig.isEnableBypass() && !mystPlayer.getPlayer().hasPermission(Permissions.BYPASS_STAMINA.asString())) {
            mystPlayer.getPlayerData().setStamina(
                    PlayerData.get(mystPlayer.getPlayer().getUniqueId()).getStamina() - skill.stamina()
            );
        }

        Profession profession = MMOCore.plugin.professionManager.get("woodcutting");
        mystPlayer.getPlayerData().getCollectionSkills().giveExperience(profession, skill.experience(), EXPSource.SOURCE);

        scheduleTreeRespawn(tree, animation);
    }

    public boolean isAlreadyHarvestingDifferentTree(Player player, Tree currentTree) {
        UUID playerUuid = player.getUniqueId();
        for (Tree tree : Tree.getAllTrees().values()) {
            if (tree.getHarvester() != null
                    && tree.getHarvester().getUniqueId().equals(playerUuid)
                    && !tree.equals(currentTree)) {
                player.sendMessage("§cYou are already harvesting a different tree!");
                return true;
            }
        }
        return false;
    }

    private boolean isCriticalHit(MystPlayer mystPlayer, Tree tree) {
        if (tree == null) return false;
        UUID entityId = tree.getUuid();
        int randomValue = new Random().nextInt(100);
        if (randomValue >= tree.getGlowChance()) return false;

        Player player = mystPlayer.getPlayer();
        player.sendMessage("§c§l*CRITICAL HIT*");
        Skill skill = mystPlayer.getSkill(tree);
        if (skill == null) return false;
        Axe axe = mystPlayer.getAxe(tree);
        if (axe == null) return false;

        TreeConfig config = treeConfigs.get(tree.getModelId());

        hitCounts.merge(entityId, axe.criticalHit(), Double::sum);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 1F);
        player.playSound(player.getLocation(), config.getChopSound().type(), config.getChopSound().volume(), config.getChopSound().pitch());
        resetInactivityTimer(player, entityId);
        EntityManager.updateTextDisplay(entityId);
        EntityManager.updateHealthDisplay(mystPlayer, entityId, hitCounts.getOrDefault(entityId, 0.0));

        return true;
    }

    private boolean isChoppedDown(Player player, Tree tree) {
        if (tree.isChopped()) {
            player.sendMessage("§cTree already chopped down!");
            return true;
        }
        return false;
    }

    private void resetInactivityTimer(Player player, UUID entityId) {
        cancelInactivityTimer(entityId);

        BukkitRunnable timer = new BukkitRunnable() {
            @Override
            public void run() {
                Tree tree = Tree.getTreeByUuid(entityId);
                if (tree == null) return;
                resetTextDisplay(entityId);
                tree.removeHealthDisplay();
                hitCounts.remove(entityId);
                player.sendMessage("§cYou took too long to chop down the tree. Progress has been reset!");
            }
        };

        timer.runTaskLater(Main.getInstance(), INACTIVITY_TIMEOUT * 20L);
        inactivityTimers.put(entityId, timer);
    }

    private void cancelInactivityTimer(UUID entityId) {
        if (inactivityTimers.containsKey(entityId)) {
            inactivityTimers.get(entityId).cancel();
            inactivityTimers.remove(entityId);
        }
    }

    private void scheduleTreeRespawn(Tree tree, IAnimationProperty animation) {
        UUID entityId = tree.getUuid();
        new BukkitRunnable() {
            private int timer = 0;

            @Override
            public void run() {
                if (timer >= tree.getRespawnTime() && animation.isFinished()) {
                    resetTextDisplay(entityId);
                    animation.stop();
                    tree.setChopped(false);
                    tree.getBarrierBlocks().stream()
                            .sorted(Comparator.comparingInt(block -> -block.getY()))
                            .limit(2)
                            .forEach(block -> block.setType(Material.BARRIER));
                    this.cancel();
                } else {
                    updateRespawnText(tree, timer);
                }
                timer++;
            }
        }.runTaskTimer(Main.getInstance(), 0L, 20L);
    }

    private void updateRespawnText(Tree tree, int timer) {
        TextDisplay textDisplay = tree.getTextDisplay();
        if (textDisplay != null) {
            textDisplay.text(Component.text("§c§l[" + (tree.getRespawnTime() - timer) + " sec]§r§8\n"
                    + tree.getEntityText().replace("§r", "§8")));
        }
    }

    private void resetTextDisplay(UUID entityId) {
        Tree tree = Tree.getTreeByUuid(entityId);
        if (tree == null) return;

        TextDisplay textDisplay = tree.getTextDisplay();
        if (textDisplay != null) {
            textDisplay.text(Component.text(tree.getEntityText()));
        }
    }

    private boolean canHarvest(Player player, Tree tree) {
        if (tree.getHarvester() != null && !player.equals(tree.getHarvester())) {
            player.sendMessage("§cSomeone else is harvesting this tree!");
            return false;
        }
        return true;
    }
}