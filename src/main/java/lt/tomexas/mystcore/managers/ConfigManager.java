package lt.tomexas.mystcore.managers;

import lt.tomexas.mystcore.PluginLogger;
import lt.tomexas.mystcore.submodules.resources.data.interfaces.ModelIdentifiable;
import lt.tomexas.mystcore.submodules.resources.data.trees.Tree;
import lt.tomexas.mystcore.submodules.resources.data.trees.config.TreeConfig;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager<T extends ModelIdentifiable> {

    private final Class<T> configClass;
    private final File configFile;
    private final Constructor yamlConstructor;
    private final Representer yamlRepresenter;

    public ConfigManager(Class<T> configClass, String filePath, Constructor yamlConstructor, Representer yamlRepresenter) {
        this.configClass = configClass;
        this.configFile = new File(filePath);
        this.yamlConstructor = yamlConstructor;
        this.yamlRepresenter = yamlRepresenter;

        saveDefaultConfig();
    }

    public Map<String, T> loadConfigDir() {
        Map<String, T> configs = new HashMap<>();
        File dir = configFile.getParentFile();

        File[] files = dir.listFiles((d, name) -> name.endsWith(".yml"));

        if (files != null) {
            for (File file : files) {
                T config = loadConfig(file); // Pass current file
                String modelId = config.getModelId();
                configs.put(modelId, config);

                // If T is TreeConfig, this works; otherwise adjust as needed
                if (config instanceof TreeConfig treeConfig) {
                    for (Tree tree : Tree.getAllTrees().values()) {
                        tree.setRespawnTime(treeConfig.getRespawnTime());
                        tree.setGlowChance(treeConfig.getGlowChance());
                        tree.setAxes(treeConfig.getAxes());
                        tree.setSkillType(treeConfig.getSkillType());
                        tree.setSkillData(treeConfig.getSkillLevelData());
                        tree.setDrops(treeConfig.getDrops());
                    }
                }
            }
        }
        return configs;
    }

    private void saveDefaultConfig() {
        if (!configFile.getParentFile().exists()) {
            configFile.getParentFile().mkdirs();
        }

        if (!configFile.exists()) {
            Yaml yaml = new Yaml(yamlRepresenter);
            try (FileWriter writer = new FileWriter(configFile)) {
                T defaultConfig = configClass.getDeclaredConstructor().newInstance();
                yaml.dump(defaultConfig, writer);
                PluginLogger.debug("Default config created: " + configFile.getPath());
            } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public T loadConfig(File file) {
        Yaml yaml = new Yaml(yamlConstructor);
        try (FileReader reader = new FileReader(file)) {
            return yaml.loadAs(reader, configClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
