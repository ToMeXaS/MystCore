package lt.tomexas.mystcore.submodules.resources.managers;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.entity.Dummy;
import lt.tomexas.mystcore.Main;
import lt.tomexas.mystcore.PluginLogger;
import lt.tomexas.mystcore.data.MystPlayer;
import lt.tomexas.mystcore.submodules.resources.data.trees.Axe;
import lt.tomexas.mystcore.submodules.resources.data.trees.Drop;
import lt.tomexas.mystcore.submodules.resources.data.trees.Skill;
import lt.tomexas.mystcore.submodules.resources.data.trees.Tree;
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

    private static final int INACTIVITY_TIMEOUT = 30; // seconds

    // Maps to track player interactions with trees
    private final Map<UUID, Double> hitCounts = new HashMap<>();
    private final Map<UUID, BukkitRunnable> inactivityTimers = new HashMap<>();
    private final Map<UUID, Boolean> glowingTrees = new HashMap<>();

    public TreeChopperManager() {
        new TreeDisplayManager();
    }

    public void handleChop(Player player, UUID entityId) {
        Tree tree = Tree.getTreeByUuid(entityId);
        if (tree == null || player == null || entityId == null) return;
        MystPlayer mystPlayer = MystPlayer.getMystPlayer(player);
        if (mystPlayer == null) return;
        Skill skill = mystPlayer.getSkill(tree);
        Axe axe = mystPlayer.getAxe(tree, player.getInventory().getItemInMainHand());
        double hits = hitCounts.getOrDefault(entityId, 0.0);
        if (isChoppedDown(player, tree)) return;
        if (!canHarvestTree(player, tree)) return;
        if (isAlreadyHarvestingDifferentTree(player, tree)) return;
        if (skill == null || axe == null) return;
        double playerAttackCooldown = player.getAttackCooldown();
        int baseDamage = axe.damage();
        double scaledDamage = (double) baseDamage * playerAttackCooldown;
        hitCounts.merge(entityId, scaledDamage, Double::sum);

        player.sendMessage(Component.text(scaledDamage + " damage dealt to the tree!" + hitCounts.get(entityId) + " hits so far!"));

        tree.setHarvester(player);
        updateTextDisplay(entityId);
        resetInactivityTimer(player, entityId);

        updateHealthDisplay(mystPlayer, entityId, hits);
        player.playSound(player.getLocation(), "block.wood.chop3", 1, 1);

        if (hits >= skill.health()) chopTree(mystPlayer, entityId);
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

        IAnimationProperty animation = playTreeFallAnimation(tree);
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

        mystPlayer.getPlayerData().setStamina(
                PlayerData.get(mystPlayer.getPlayer().getUniqueId()).getStamina() - skill.stamina()
        );

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

    /*private void handleCriticalHit(Player player, Tree tree) {
        UUID entityId = tree.getUuid();
        player.sendMessage("§c§l*CRITICAL HIT*");
        Skill skill = getPlayerSkill(player, tree.getSkillType(), tree.getSkillData());
        if (skill == null) return;
        Axe axe = getPlayerAxe(player, tree.getAxes());
        if (axe == null) return;

        int currentHits = hitCounts.getOrDefault(entityId, 0);
        int remainingHits = (int) (skill.health() - currentHits);
        hitCounts.put(entityId, currentHits + Math.min(axe.criticalHit(), remainingHits));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        setTreeGlow(entityId, false);
    }*/

    private boolean isChoppedDown(Player player, Tree tree) {
        if (tree.isChopped()) {
            player.sendMessage("§cTree already chopped down!");
            return true;
        }
        return false;
    }

    private boolean isTreeGlowing(UUID entityId) {
        Tree tree = Tree.getTreeByUuid(entityId);
        if (tree == null) return false;
        if (glowingTrees.containsKey(entityId)) {
            return false; // Already glowing
        }
        int randomValue = new Random().nextInt(100);

        if (randomValue < tree.getGlowChance()) { // 20% chance
            setTreeGlow(entityId, true);
            glowingTrees.put(entityId, true);
            return true;
        }

        return false;
    }

    private void setTreeGlow(UUID entityId, boolean state) {
        BaseEntity<?> entity = ModelEngineAPI.getModeledEntity(entityId).getBase();
        if (!(entity instanceof Dummy<?> dummy)) return;
        dummy.setGlowing(state);
        dummy.setGlowColor(0xFFFFFF00);
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
                setTreeGlow(entityId, false);
                glowingTrees.remove(entityId);
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

    private IAnimationProperty playTreeFallAnimation(Tree tree) {
        return ModelEngineAPI.getModeledEntity(tree.getUuid())
                .getModel(tree.getModelId())
                .orElseThrow(() -> new IllegalStateException("Model not found!"))
                .getAnimationHandler()
                .playAnimation("fall", 0.3, 0.3, 1, true);
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

    /**
     * Updates the text display for the tree entity.
     *
     * @param entityId the UUID of the tree entity
     */
    private void updateTextDisplay(UUID entityId) {
        Tree tree = Tree.getTreeByUuid(entityId);
        if (tree == null) return;

        TextDisplay textDisplay = tree.getTextDisplay();
        if (textDisplay != null) {
            textDisplay.text(Component.text("§6§l[" + tree.getHarvester().getName() + "]§f\n" + tree.getEntityText()));
        }
    }

    private void updateHealthDisplay(MystPlayer mystPlayer, UUID entityId, double hits) {
        Tree tree = Tree.getTreeByUuid(entityId);
        if (tree == null) return;
        TextDisplay display = tree.getHealthDisplay();
        Skill skill = mystPlayer.getSkill(tree);
        if (skill == null) {
            PluginLogger.debug("No skill found for tree " + tree + " and type " + tree.getSkillType());
            return;
        }
        double health = skill.health() - hits;
        int maxHealth = (int) skill.health();
        health = Math.max(0, health);
        if (display == null) display = createHealthDisplay(mystPlayer, entityId);
        display.text(Component.text(getProgressBar(health, maxHealth)));
    }

    private TextDisplay createHealthDisplay(MystPlayer mystPlayer, UUID entityId) {
        Tree tree = Tree.getTreeByUuid(entityId);
        if (tree == null) return null;
        Location displayLocation = tree.getLocation().clone().add(0, -0.5, 0.5);

        Player player = mystPlayer.getPlayer();
        Location playerLoc = player.getLocation();

        double dx = playerLoc.getX() - displayLocation.getX();
        double dz = playerLoc.getZ() - displayLocation.getZ();

        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));

        displayLocation.setYaw(yaw);

        TextDisplay display = tree.getWorld().spawn(displayLocation, TextDisplay.class, textDisplay -> {
            textDisplay.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            textDisplay.setShadowed(true);
            textDisplay.setSeeThrough(false);
            textDisplay.setViewRange(0.2f);
            int health = (int) mystPlayer.getSkill(tree).health();
            textDisplay.text(Component.text(getProgressBar(health, health)));
        });

        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (!mystPlayer.getPlayer().equals(onlinePlayer))
                onlinePlayer.hideEntity(plugin, display);
        }

        tree.setHealthDisplay(display);
        return display;
    }

    private void resetTextDisplay(UUID entityId) {
        Tree tree = Tree.getTreeByUuid(entityId);
        if (tree == null) return;

        TextDisplay textDisplay = tree.getTextDisplay();
        if (textDisplay != null) {
            textDisplay.text(Component.text(tree.getEntityText()));
        }
    }

    private boolean canHarvestTree(Player player, Tree tree) {
        if (tree.getHarvester() != null && !player.equals(tree.getHarvester())) {
            player.sendMessage("§cSomeone else is harvesting this tree!");
            return false;
        }
        return true;
    }

    /*private Skill getPlayerSkill(Player player, String skillType, List<Skill> skills) {
        MystPlayer mystPlayer = MystPlayer.getMystPlayer(player);
        if (mystPlayer == null) {
            PluginLogger.debug("MystPlayer not found for player: " + player.getName());
            return null;
        }
        PlayerData playerData = mystPlayer.getPlayerData();
        int level = playerData.getCollectionSkills().getLevel(skillType);
        Skill skill = skills.stream()
                .max(Comparator.comparingInt(Skill::level))
                .orElse(null);
        if (skill == null) return null;
        if (skill.level() <= level) {
            return skill;
        }
        return null;
    }*/

    /*private Axe getPlayerAxe(Player player, List<Axe> axes) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        for (Axe axe : axes) {
            if (axe.getItemStack().equals(itemInHand)) {
                return axe;
            }
        }
        return null;
    }*/

    public String getProgressBar(double progress, double maxProgress) {
        double percentage = progress / maxProgress * 100.0;
        StringBuilder progressBar = new StringBuilder();
        double percentPerChar = 100.0 / 15; // Adjusted for 15 characters
        char character = '▍';

        for (int i = 0; i < 15; ++i) {
            double progressPassBar = percentPerChar * (i + 1);
            if (percentage >= progressPassBar) {
                progressBar.append("§c").append(character);
            } else {
                progressBar.append("§7").append(character);
            }
        }

        // Build the final string
        return progressBar.toString();
    }
}