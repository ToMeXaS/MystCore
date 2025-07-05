package lt.tomexas.mystcore.resources;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.animation.handler.AnimationHandler;
import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.entity.Dummy;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import lt.tomexas.mystcore.Main;
import lt.tomexas.mystcore.resources.data.trees.Axe;
import lt.tomexas.mystcore.resources.data.trees.Skill;
import lt.tomexas.mystcore.resources.data.trees.Tree;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ResourcesDatabase {

    private final Main plugin = Main.getInstance();
    private final Logger logger = plugin.getLogger();

    private static final String DB_URL = "jdbc:sqlite:"; // Database URL prefix
    private static final String DB_NAME = "resources.db"; // Database name
    private static final String DB_TABLE = "trees"; // Table name

    // Database connection
    private static Connection connection;

    public ResourcesDatabase(String path) throws SQLException {
        connection = DriverManager.getConnection(DB_URL + path + "/" + DB_NAME);

        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS
                """ + DB_TABLE + """
                (uuid TEXT PRIMARY KEY,
                textEntityId TEXT NOT NULL,
                healthEntityId TEXT,
                location TEXT NOT NULL,
                barrierBlocks TEXT NOT NULL,
                modelId TEXT NOT NULL,
                respawnTime INTEGER NOT NULL,
                glowChance INTEGER NOT NULL,
                skillType TEXT NOT NULL,
                skillData TEXT NOT NULL,
                axes TEXT NOT NULL,
                drops TEXT NOT NULL)
            """);
        }
    }

    public void addOrUpdateTrees(Set<UUID> uuids) {
        if (connection == null) {
            logger.severe("Database connection is null. Cannot add or update trees.");
            return;
        }

        try (PreparedStatement preparedStatement =
            connection.prepareStatement("""
                INSERT INTO
                """ + DB_TABLE + """
                (uuid, textEntityId, healthEntityId, location, barrierBlocks, modelId, respawnTime, glowChance, skillType, skillData, axes, drops)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(uuid) DO UPDATE SET
                textEntityId = excluded.textEntityId,
                healthEntityId = excluded.healthEntityId,
                location = excluded.location,
                barrierBlocks = excluded.barrierBlocks,
                modelId = excluded.modelId,
                respawnTime = excluded.respawnTime,
                glowChance = excluded.glowChance,
                skillType = excluded.skillType,
                skillData = excluded.skillData,
                axes = excluded.axes,
                drops = excluded.drops
            """)) {

            connection.setAutoCommit(false); // Start transaction

            for (UUID uuid : uuids) {
                Tree tree = Tree.getTree(uuid);
                if (tree == null) {
                    logger.warning("Tree with UUID " + uuid + " not found. Skipping.");
                    continue;
                }

                String healthEntityId = "";
                if (tree.getHealthEntityId() != null) healthEntityId = tree.getHealthEntityId().toString();

                preparedStatement.setString(1, uuid.toString());
                preparedStatement.setString(2, tree.getTextEntityId().toString());
                preparedStatement.setString(3, healthEntityId);
                preparedStatement.setString(4, serializeLocation(tree.getLocation()));
                preparedStatement.setString(5, serializeBlocks(tree.getBarrierBlocks()));
                preparedStatement.setString(6, tree.getModelId());
                preparedStatement.setInt(7, tree.getRespawnTime());
                preparedStatement.setInt(8, tree.getGlowChance());
                preparedStatement.setString(9, tree.getSkillType());
                preparedStatement.setString(10, serializeSkillData(tree.getSkillData()));
                preparedStatement.setString(11, serializeAxes(tree.getAxes()));
                preparedStatement.setString(12, serializeItemStackList(tree.getDrops()));
                preparedStatement.addBatch(); // Add to batch
            }

            preparedStatement.executeBatch(); // Execute batch
            connection.commit(); // Commit transaction
        } catch (SQLException e) {
            try {
                connection.rollback(); // Rollback transaction on error
            } catch (SQLException rollbackEx) {
                logger.severe("Failed to rollback transaction: " + rollbackEx.getMessage());
            }
            Main.getInstance().getLogger().severe("Failed to add or update trees in database: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true); // Reset auto-commit
            } catch (SQLException e) {
                logger.severe("Failed to reset auto-commit: " + e.getMessage());
            }
        }
    }

    public void loadAllTrees() {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM trees")) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) { // Process all rows
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                UUID textEntityId = UUID.fromString(resultSet.getString("textEntityId"));
                UUID healthEntityId = null;
                if (!resultSet.getString("healthEntityId").isEmpty())
                    healthEntityId = UUID.fromString(resultSet.getString("healthEntityId"));
                Location location = deserializeLocation(resultSet.getString("location"));
                if (location == null) {
                    logger.warning("Skipping tree with UUID " + uuid + " due to invalid location.");
                    continue;
                }
                List<Block> barrierBlocks = deserializeBlocks(resultSet.getString("barrierBlocks"));
                if (barrierBlocks.isEmpty()) {
                    logger.warning("Skipping tree with UUID " + uuid + " due to invalid barrier blocks.");
                    continue;
                }
                String modelId = resultSet.getString("modelId");
                if (modelId == null || modelId.isEmpty()) {
                    logger.warning("Skipping tree with UUID " + uuid + " due to invalid model ID.");
                    continue;
                }
                int respawnTime = resultSet.getInt("respawnTime");
                if (respawnTime <= 0) {
                    logger.warning("Skipping tree with UUID " + uuid + " due to invalid respawn time.");
                    continue;
                }
                int glowChance = resultSet.getInt("glowChance");
                if (glowChance < 0 || glowChance > 100) {
                    logger.warning("Skipping tree with UUID " + uuid + " due to invalid glow chance.");
                    continue;
                }
                String skillType = resultSet.getString("skillType");
                if (skillType == null || skillType.isEmpty()) {
                    logger.warning("Skipping tree with UUID " + uuid + " due to invalid skill type.");
                    continue;
                }
                List<Skill> skillData = deserializeSkillData(resultSet.getString("skillData"));
                if (skillData.isEmpty()) {
                    logger.warning("Skipping tree with UUID " + uuid + " due to invalid skill data.");
                    continue;
                }
                List<Axe> axes = deserializeAxes(resultSet.getString("axes"));
                if (axes.isEmpty()) {
                    logger.warning("Skipping tree with UUID " + uuid + " due to invalid axes.");
                    continue;
                }
                List<ItemStack> drops = deserializeItemStackList(resultSet.getString("drops"));
                if (drops.isEmpty()) {
                    logger.warning("Skipping tree with UUID " + uuid + " due to invalid drops.");
                    continue;
                }
                BaseEntity<?> entity = ModelEngineAPI.getModeledEntity(uuid).getBase();
                if (!(entity instanceof Dummy<?> dummy)) {
                    logger.warning("Skipping tree with UUID " + uuid + " due to missing entity.");
                    continue;
                }
                if (dummy.isGlowing()) dummy.setGlowing(false);
                stopFallingAnimation(uuid, modelId);
                if (healthEntityId != null) {
                    Entity healthEntity = location.getWorld().getEntity(healthEntityId);
                    if (healthEntity != null)
                        healthEntity.remove();
                }

                new Tree(uuid,
                        textEntityId,
                        location,
                        barrierBlocks,
                        modelId,
                        respawnTime,
                        glowChance,
                        skillType,
                        skillData,
                        axes,
                        drops);
            }
        } catch (SQLException e) {
            logger.severe("Failed to retrieve trees from database! " + e.getMessage());
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public void removeTree(UUID uuid) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "DELETE FROM trees WHERE uuid = ?")) {
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            Main.getInstance().getLogger().severe("Failed to remove tree from database: " + e.getMessage());
        }
    }

    private void stopFallingAnimation(UUID uuid, String modelId) {
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(uuid);
        if (modeledEntity == null) {

            ActiveModel activeModel = modeledEntity.getModel(modelId).orElse(null);
            if (activeModel != null) {
                AnimationHandler animationHandler = activeModel.getAnimationHandler();
                if (animationHandler != null) {
                    animationHandler.stopAnimation("fall");
                }
            }
        }
    }

    // Serialization and Deserialization methods
    private String serializeLocation(Location location) {
        return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ();
    }

    private Location deserializeLocation(String locationString) {
        String[] parts = locationString.split(",");
        World world = Main.getInstance().getServer().getWorld(parts[0]);
        if (world == null) {
            Main.getInstance().getLogger().severe("World '" + parts[0] + "' not found. Cannot deserialize location: " + locationString);
            return null; // Return null if the world is not found
        }
        return new Location(world, Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
    }

    private String serializeBlocks(List<Block> blocks) {
        return blocks.stream()
                .map(block -> block.getWorld().getName() + "," + block.getX() + "," + block.getY() + "," + block.getZ())
                .collect(Collectors.joining(";"));
    }

    private List<Block> deserializeBlocks(String blocksString) {
        if (blocksString == null || blocksString.isEmpty()) return new ArrayList<>();
        String[] blockStrings = blocksString.split(";");
        List<Block> blocks = new ArrayList<>();
        for (String blockString : blockStrings) {
            String[] parts = blockString.split(",");
            if (parts.length < 4) { // Ensure there are at least 4 parts
                Main.getInstance().getLogger().warning("Invalid block data: " + blockString);
                continue;
            }
            Location location = deserializeLocation(parts[0] + "," + parts[1] + "," + parts[2] + "," + parts[3]);
            if (location == null) {
                Main.getInstance().getLogger().warning("Skipping block deserialization due to invalid location.");
                continue;
            }
            blocks.add(location.getBlock());
        }
        return blocks;
    }

    private String serializeSkillData(List<Skill> skills) {
        return skills.stream()
                .map(Skill::serialize)
                .collect(Collectors.joining(";"));
    }

    private List<Skill> deserializeSkillData(String skillsString) {
        if (skillsString == null || skillsString.isEmpty()) return new ArrayList<>();
        return Arrays.stream(skillsString.split(";"))
                .map(Skill::deserialize)
                .collect(Collectors.toList());
    }

    private String serializeAxes(List<Axe> axes) {
        return axes.stream()
                .map(Axe::serialize)
                .collect(Collectors.joining(";"));
    }

    private List<Axe> deserializeAxes(String axesString) {
        if (axesString == null || axesString.isEmpty()) return new ArrayList<>();
        return Arrays.stream(axesString.split(";"))
                .map(Axe::deserialize)
                .collect(Collectors.toList());
    }

    private String serializeItemStackList(List<ItemStack> items) {
        if (items == null || items.isEmpty()) {
            return ""; // Return an empty string if the list is null or empty
        }

        return items.stream()
                .map(item -> {
                    YamlConfiguration config = new YamlConfiguration();
                    config.set("item", item); // Store the ItemStack under a key
                    return config.saveToString(); // Serialize to a YAML string
                })
                .collect(Collectors.joining(";")); // Join serialized strings with a semicolon
    }

    public static List<ItemStack> deserializeItemStackList(String serializedDrops) {
        if (serializedDrops == null || serializedDrops.isEmpty()) {
            return List.of(); // Return an empty list if the input is null or empty
        }

        return Arrays.stream(serializedDrops.split(";"))
                .map(itemString -> {
                    YamlConfiguration config = new YamlConfiguration();
                    try {
                        config.loadFromString(itemString); // Load the YAML string
                        return config.getItemStack("item"); // Retrieve the ItemStack
                    } catch (Exception e) {
                        Main.getInstance().getLogger().warning("Failed to deserialize item stack: " + itemString);
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull) // Filter out any null values
                .toList();
    }

}
