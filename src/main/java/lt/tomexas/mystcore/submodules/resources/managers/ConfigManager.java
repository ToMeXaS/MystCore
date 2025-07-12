package lt.tomexas.mystcore.submodules.resources.managers;

import lombok.Getter;
import lt.tomexas.mystcore.Main;
import lt.tomexas.mystcore.PluginLogger;
import lt.tomexas.mystcore.submodules.resources.data.trees.Tree;
import lt.tomexas.mystcore.submodules.resources.data.trees.config.TreeConfigConstructor;
import lt.tomexas.mystcore.submodules.resources.data.trees.config.TreeConfigRepresenter;
import lt.tomexas.mystcore.submodules.resources.data.trees.config.TreeConfig;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final Main plugin = Main.getInstance();
    private final String CONFIG_PATH = plugin.getDataFolder().getPath();

    @Getter
    private final Map<String, TreeConfig> fileConfigurations = new HashMap<>();

    public ConfigManager() {
        loadTreeConfigurations();
    }

    private void loadTreeConfigurations() {
        File configDir = new File(CONFIG_PATH + "/trees/");

        if (!configDir.exists()) {
            saveDefaultConfig(configDir);
        }

        File[] files = configDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".yml")) {
                    plugin.getLogger().info("Found tree config file: " + file.getName());
                    TreeConfig config = loadConfig(file);
                    this.fileConfigurations.put(file.getName(), config);

                    for (Tree tree : Tree.getAllTrees().values()) {
                        tree.setRespawnTime(config.getRespawnTime());
                        tree.setGlowChance(config.getGlowChance());
                        tree.setAxes(config.getAxes());
                        tree.setSkillType(config.getSkillType());
                        tree.setSkillData(config.getSkills());
                        tree.setDrops(config.getDrops());
                    }
                }
            }
        }
    }

    public void saveDefaultConfig(File configDir) {
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        File configFile = new File(configDir.getPath(), "oak_tree.yml");

        if (!configFile.exists()) {
            TreeConfig defaultSpawner = new TreeConfig();

            DumperOptions dumperOptions = new DumperOptions();
            dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            dumperOptions.setPrettyFlow(true);

            TreeConfigRepresenter representer = new TreeConfigRepresenter(dumperOptions);

            Yaml yaml = new Yaml(representer);
            try (FileWriter writer = new FileWriter(configFile)) {
                yaml.dump(defaultSpawner, writer);
                PluginLogger.debug("Default config created: " + configFile.getPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private TreeConfig loadConfig(File file) {
        LoaderOptions loaderOptions = new LoaderOptions();
        TreeConfigConstructor constructor = new TreeConfigConstructor(TreeConfig.class, loaderOptions);

        Yaml yaml = new Yaml(constructor);
        try (FileReader reader = new FileReader(file)) {
            return yaml.loadAs(reader, TreeConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
