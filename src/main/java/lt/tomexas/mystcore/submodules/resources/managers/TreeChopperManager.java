package lt.tomexas.mystcore.submodules.resources.managers;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.entity.Dummy;
import lt.tomexas.mystcore.Main;
import lt.tomexas.mystcore.PluginLogger;
import lt.tomexas.mystcore.submodules.resources.data.trees.Axe;
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
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;

import java.util.*;

public class TreeChopperManager {
    private final Main plugin = Main.getInstance();

    private static final int INACTIVITY_TIMEOUT = 30; // seconds
    private BukkitTask choppingTask;

    // Maps to track player interactions with trees
    private final Map<UUID, Integer> hitCounts = new HashMap<>();
    private final Map<UUID, BukkitRunnable> inactivityTimers = new HashMap<>();
    private final Map<UUID, UUID> playerHarvestingTree = new HashMap<>();
    private final Map<UUID, Boolean> glowingTrees = new HashMap<>();
    private final Map<UUID, UUID> healthDisplay = new HashMap<>();

    public void startChopping(Player player, UUID entityId) {
        Tree tree = Tree.getTreeByUuid(entityId);
        if (tree == null) return;
        if (player == null || entityId == null) return;

        if (isChoppedDown(player, tree)) return; // Check if the tree is already chopped down
        if (!canHarvestTree(player, tree)) return; // Check if the player can harvest the tree
        if (isAlreadyHarvestingDifferentTree(player, tree)) return; // Check if the player is already harvesting a different tree
        if (Boolean.TRUE.equals(glowingTrees.remove(entityId))) {
            handleCriticalHit(player, tree);
            return;
        } // If the tree was glowing, remove the glow and handle critical hit
        if (isAlreadyHarvestingTree(player)) return; // Check if the player is already harvesting this tree

        playerHarvestingTree.put(player.getUniqueId(), entityId);
        tree.setHarvester(player);

        updateTextDisplay(entityId);
        createHealthDisplay(player, entityId);

        handleTask();

        player.sendMessage("§aYou started chopping the tree!");
    }

    private void handleTask() {
        if (this.choppingTask != null && !this.choppingTask.isCancelled()) return;
        this.choppingTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (playerHarvestingTree.isEmpty()) this.cancel();
                for (Map.Entry<UUID, UUID> entry : playerHarvestingTree.entrySet()) {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    UUID entityId = entry.getValue();
                    if (player == null || !isPlayerOnline(player, entityId)) continue;
                    if (!isPlayerTargetingTree(player, entityId)) continue;
                    if (isTreeGlowing(entityId)) {
                        player.sendMessage("§aThe tree starts to glow! Hit it to do a §c§l*CRITICAL HIT*§a!");
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    }

                    resetInactivityTimer(player, entityId);

                    int hits = hitCounts.getOrDefault(entityId, 0);
                    Tree tree = Tree.getTreeByUuid(entityId);
                    if (tree == null) continue;
                    Skill skill = getPlayerSkill(player, tree.getSkillType(), tree.getSkillData());
                    Axe axe = getPlayerAxe(player, tree.getAxes());

                    if (skill == null || axe == null) continue;

                    if (hits >= skill.health()) finishChopping(player, entityId);

                    hitCounts.merge(entityId, axe.damage(), Integer::sum);
                    updateHealthDisplay(player, entityId, hits);
                    player.playSound(player.getLocation(), "block.wood.chop3", 1, 1);
                    player.swingMainHand();
                    PluginLogger.debug("Running chopping task!");
                }
            }
        }.runTaskTimer(plugin, 0, 20L);
    }

    private void finishChopping(Player player, UUID entityId) {
        hitCounts.remove(entityId);
        playerHarvestingTree.remove(player.getUniqueId());
        cancelInactivityTimer(entityId);
        chopTree(player, entityId);
    }

    private void chopTree(Player player, UUID entityId) {
        Tree tree = Tree.getTreeByUuid(entityId);
        if (tree == null) return;
        Skill skill = getPlayerSkill(player, tree.getSkillType(), tree.getSkillData());
        if (skill == null) return;

        PlayerData playerData = PlayerData.get(player.getUniqueId());

        tree.setChopped(true);
        tree.setHarvester(null);
        tree.getBarrierBlocks().stream()
                .sorted(Comparator.comparingInt(block -> -block.getY()))
                .limit(2)
                .forEach(block -> block.setType(Material.AIR));

        IAnimationProperty animation = playTreeFallAnimation(tree);
        if (animation == null) return;

        if (Boolean.TRUE.equals(glowingTrees.remove(entityId))) setTreeGlow(entityId, false);

        resetHealthDisplay(entityId);

        if (tree.getDrops() != null) {
            List<ItemStack> drops = new ArrayList<>(tree.getDrops()); // Clone the drops list
            for (ItemStack drop : drops) {
                HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(drop);
                if (!remaining.isEmpty()) {
                    // If the item couldn't be fully added, drop the remaining items on the ground
                    for (ItemStack leftover : remaining.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), leftover);
                    }
                    player.sendMessage("§eSome items were dropped on the ground due to insufficient inventory space.");
                }
            }
        }

        playerData.setStamina(
                PlayerData.get(player.getUniqueId()).getStamina() - skill.stamina()
        );

        Profession profession = MMOCore.plugin.professionManager.get("woodcutting");
        playerData.getCollectionSkills().giveExperience(profession, skill.experience(), EXPSource.SOURCE);

        scheduleTreeRespawn(tree, animation);
    }

    private boolean isAlreadyHarvestingDifferentTree(Player player, Tree tree) {
        UUID playerId = player.getUniqueId();
        UUID currentTree = playerHarvestingTree.get(playerId);

        if (currentTree != null && !currentTree.equals(tree.getUuid())) {
            player.sendMessage("§cYou are already harvesting a different tree!");
            return true;
        }
        return false;
    }

    private boolean isAlreadyHarvestingTree(Player player) {
        UUID playerId = player.getUniqueId();
        if (playerHarvestingTree.containsKey(playerId)) {
            player.sendMessage("§cYou are already harvesting this tree!");
            return true;
        }
        return false;
    }

    private void handleCriticalHit(Player player, Tree tree) {
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
    }

    private boolean isChoppedDown(Player player, Tree tree) {
        if (tree.isChopped()) {
            player.sendMessage("§cTree already chopped down!");
            return true;
        }
        return false;
    }

    private boolean isPlayerOnline(Player player, UUID entityId) {
        Tree tree = Tree.getTreeByUuid(entityId);
        if (!player.isOnline() && tree != null) {
            hitCounts.remove(entityId);
            playerHarvestingTree.remove(player.getUniqueId());
            cancelInactivityTimer(entityId);
            tree.setHarvester(null);
            resetTextDisplay(entityId);
            if (Boolean.TRUE.equals(glowingTrees.remove(entityId))) setTreeGlow(entityId, false);
            return false;
        }
        return true;
    }

    private boolean isPlayerTargetingTree(Player player, UUID entityId) {
        RayTraceResult result = player.rayTraceBlocks(3.0);
        Tree tree = Tree.getByRayTraceResult(result);
        //player.sendMessage("§cYou stopped harvesting the tree!");
        //playerHarvestingTree.remove(player.getUniqueId());
        return tree != null && tree.getUuid().equals(entityId);
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
                resetTextDisplay(entityId);
                resetHealthDisplay(entityId);
                playerHarvestingTree.remove(player.getUniqueId());
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
        TextDisplay textDisplay = (TextDisplay) tree.getWorld().getEntity(tree.getTextEntityId());
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

        TextDisplay textDisplay = (TextDisplay) tree.getWorld().getEntity(tree.getTextEntityId());
        if (textDisplay != null) {
            textDisplay.text(Component.text("§6§l[" + tree.getHarvester().getName() + "]§f\n" + tree.getEntityText()));
        }
    }

    private void resetHealthDisplay(UUID entityId) {
        Tree tree = Tree.getTreeByUuid(entityId);
        if (tree == null) return;
        World world = tree.getWorld();
        if (healthDisplay.containsKey(entityId)) {
            Entity entity = world.getEntity(healthDisplay.get(entityId));
            if (entity != null) entity.remove();
            tree.setHealthEntityId(null);
            healthDisplay.remove(entityId);
        }
    }

    private void updateHealthDisplay(Player player, UUID entityId, int hits) {
        Tree tree = Tree.getTreeByUuid(entityId);
        if (tree == null) return;
        World world = tree.getWorld();
        if (!healthDisplay.containsKey(entityId)) return;
        TextDisplay display = (TextDisplay) world.getEntity(healthDisplay.get(entityId));
        if (display == null) return;
        int health = (int) getPlayerSkill(player, tree.getSkillType(), tree.getSkillData()).health()-hits;
        int maxHealth = (int) getPlayerSkill(player, tree.getSkillType(), tree.getSkillData()).health();
        display.text(Component.text(getProgressBar(health, maxHealth)));
    }

    private void createHealthDisplay(Player player, UUID entityId) {
        Tree tree = Tree.getTreeByUuid(entityId);
        if (tree == null) return;
        Location displayLocation = tree.getLocation().clone().add(0, -0.5, 0.5);
        displayLocation.setYaw(180f);
        TextDisplay display = tree.getWorld().spawn(displayLocation, TextDisplay.class, textDisplay -> {
            textDisplay.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            textDisplay.setShadowed(true);
            textDisplay.setSeeThrough(false);
            textDisplay.setViewRange(0.2f);
            int health = (int) getPlayerSkill(player, tree.getSkillType(), tree.getSkillData()).health();
            textDisplay.text(Component.text(getProgressBar(health, health)));
        });

        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (!player.equals(onlinePlayer))
                onlinePlayer.hideEntity(plugin, display);
        }

        tree.setHealthEntityId(display.getUniqueId());
        healthDisplay.put(entityId, display.getUniqueId());
    }

    private void resetTextDisplay(UUID entityId) {
        Tree tree = Tree.getTreeByUuid(entityId);
        if (tree == null) return;

        TextDisplay textDisplay = (TextDisplay) tree.getWorld().getEntity(tree.getTextEntityId());
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

    private Skill getPlayerSkill(Player player, String skillType, List<Skill> skills) {
        UUID playerId = player.getUniqueId();
        PlayerData playerData = PlayerData.get(playerId);
        int level = playerData.getCollectionSkills().getLevel(skillType);
        Skill skill = skills.stream()
                .max(Comparator.comparingInt(Skill::level))
                .orElse(null);
        if (skill == null) return null;
        if (skill.level() <= level) {
            return skill;
        }
        return null;
    }

    private Axe getPlayerAxe(Player player, List<Axe> axes) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        for (Axe axe : axes) {
            if (axe.getItemStack().equals(itemInHand)) {
                return axe;
            }
        }
        return null;
    }

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