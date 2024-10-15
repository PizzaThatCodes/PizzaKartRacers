package me.pizzathatcodes.pizzakartracers.utils;


import me.pizzathatcodes.pizzakartracers.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class configManager {
    String fileName;

    public File configFile;
    public FileConfiguration databaseConfig;

    public configManager(String fileName) {
        this.fileName = fileName;
        configFile = new File(Main.getInstance().getDataFolder(), fileName);
        databaseConfig = YamlConfiguration.loadConfiguration(configFile);
    }


    public FileConfiguration getConfig() {
        return databaseConfig;
    }

    public File getConfigFile() {
        return configFile;
    }

    public void saveConfig() {
        configFile = new File(Main.getInstance().getDataFolder(), fileName);
        databaseConfig = YamlConfiguration.loadConfiguration(configFile);
        try {
            databaseConfig.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setConfig(String path, Object value) {
        databaseConfig.set(path, value);
        try {
            getConfig().save(getConfigFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        reloadConfig();
    }
    public void setConfig(String path, List<?> value) {
        databaseConfig.set(path, value);
        try {
            getConfig().save(getConfigFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        reloadConfig();
    }

    public void reloadConfig() {
        if (configFile == null) {
            configFile = new File(Main.getInstance().getDataFolder(), fileName);
        }
        databaseConfig = YamlConfiguration.loadConfiguration(configFile);

        // Look for defaults in the jar
        File defConfigStream = new File(Main.getInstance().getDataFolder(), fileName);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            databaseConfig.setDefaults(defConfig);
        }
    }


    public void updateConfig(List<String> exclude) {
        if (!new File(Main.getInstance().getDataFolder() + "/" + fileName).exists()) {
            Main.getInstance().saveResource(fileName, false);
        }

        File langFile = new File(Main.getInstance().getDataFolder() + "/" + fileName);
        YamlConfiguration externalYamlConfig = YamlConfiguration.loadConfiguration(langFile);

        InputStreamReader defConfigStream = new InputStreamReader(Main.getInstance().getResource(fileName), StandardCharsets.UTF_8);
        YamlConfiguration internalLangConfig = YamlConfiguration.loadConfiguration(defConfigStream);

        // Gets all the keys inside the internal file and iterates through all of it's key pairs
        check:
        for (String string : internalLangConfig.getKeys(true)) {
            if(exclude != null && !exclude.isEmpty()) {
                for(String excludeString : exclude) {
                    if(string.startsWith(excludeString)) continue check;
                }
            }
            // Checks if the external file contains the key already.
            if (!externalYamlConfig.contains(string)) {
                // If it doesn't contain the key, we set the key based off what was found inside the plugin jar
                externalYamlConfig.set(string, internalLangConfig.get(string));
            }
        }
        try {
            externalYamlConfig.save(langFile);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

}
