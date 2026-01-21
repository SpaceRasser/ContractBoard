package com.contractboard.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public class MessagesService {
    private final JavaPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private YamlConfiguration messages;

    public MessagesService(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public Component getMessage(String path) {
        String raw = messages.getString(path, "<red>Missing message: " + path + "</red>");
        return miniMessage.deserialize(raw);
    }

    public Component getMessage(String path, String placeholder, String value) {
        String raw = messages.getString(path, "<red>Missing message: " + path + "</red>");
        return miniMessage.deserialize(raw.replace(placeholder, value));
    }

    public List<String> getStringList(String path) {
        return messages.getStringList(path);
    }

    public MiniMessage miniMessage() {
        return miniMessage;
    }
}
