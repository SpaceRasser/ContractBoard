package com.contractboard.config;

import com.contractboard.contracts.ContractTemplate;
import com.contractboard.contracts.Faction;
import com.contractboard.contracts.ItemReward;
import com.contractboard.contracts.TemplateObjective;
import com.contractboard.contracts.TemplateRequirements;
import com.contractboard.contracts.TemplateRewards;
import com.contractboard.objectives.ObjectiveType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class TemplateLoader {
    private final JavaPlugin plugin;
    private final List<ContractTemplate> templates = new ArrayList<>();

    public TemplateLoader(JavaPlugin plugin) {
        this.plugin = plugin;
        ensureDefaults();
    }

    private void ensureDefaults() {
        File folder = new File(plugin.getDataFolder(), "templates");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String[] defaults = {"fish_deliver_cod.yml", "mine_iron.yml", "hunt_zombie.yml"};
        for (String name : defaults) {
            File file = new File(folder, name);
            if (!file.exists()) {
                plugin.saveResource("templates/" + name, false);
            }
        }
    }

    public void reload() {
        templates.clear();
        File folder = new File(plugin.getDataFolder(), "templates");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return;
        }
        Logger logger = plugin.getLogger();
        for (File file : files) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            String id = config.getString("id");
            if (id == null || id.isBlank()) {
                logger.warning("Template missing id: " + file.getName());
                continue;
            }
            Faction faction = Faction.fromString(config.getString("faction", ""));
            if (faction == null) {
                logger.warning("Template " + id + " has invalid faction");
                continue;
            }
            int weight = config.getInt("weight", 1);
            String displayName = config.getString("displayName", "<white>Contract</white>");
            List<String> description = config.getStringList("description");

            int minRep = config.getInt("requirements.minRep", 0);
            List<String> worldWhitelist = config.getStringList("requirements.worldWhitelist");
            List<String> biomeWhitelist = config.getStringList("requirements.biomeWhitelist");
            TemplateRequirements requirements = new TemplateRequirements(minRep, worldWhitelist, biomeWhitelist);

            String objectiveTypeRaw = config.getString("objective.type", "");
            ObjectiveType objectiveType = ObjectiveType.fromString(objectiveTypeRaw);
            if (objectiveType == null) {
                logger.warning("Template " + id + " has invalid objective type");
                continue;
            }
            int target = config.getInt("objective.target", 1);
            List<String> types = config.getStringList("objective.types");
            if (types.isEmpty()) {
                String single = config.getString("objective.typeId");
                if (single != null && !single.isBlank()) {
                    types = Collections.singletonList(single);
                }
            }
            if (types.isEmpty()) {
                logger.warning("Template " + id + " missing objective types");
                continue;
            }
            if (!validateTypes(objectiveType, types, logger, id)) {
                continue;
            }
            TemplateObjective objective = new TemplateObjective(target, types);

            int rep = config.getInt("rewards.rep", 0);
            Double money = config.isSet("rewards.money") ? config.getDouble("rewards.money") : null;
            List<ItemReward> itemRewards = new ArrayList<>();
            List<?> items = config.getList("rewards.items");
            if (items != null) {
                for (Object itemObj : items) {
                    if (!(itemObj instanceof java.util.Map<?, ?> map)) {
                        continue;
                    }
                    String material = String.valueOf(map.getOrDefault("material", ""));
                    int amount = parseInt(map.get("amount"), 1);
                    String name = map.get("name") != null ? String.valueOf(map.get("name")) : null;
                    List<String> lore = map.get("lore") instanceof List<?> list ? list.stream().map(String::valueOf).toList() : List.of();
                    if (Material.matchMaterial(material) == null) {
                        logger.warning("Template " + id + " has invalid reward material: " + material);
                        continue;
                    }
                    itemRewards.add(new ItemReward(material, amount, name, lore));
                }
            }
            TemplateRewards rewards = new TemplateRewards(rep, money, itemRewards);

            templates.add(new ContractTemplate(id, faction, weight, displayName, description, requirements, objectiveType, objective, rewards));
        }
        logger.info("Loaded " + templates.size() + " contract templates.");
    }

    private boolean validateTypes(ObjectiveType objectiveType, List<String> types, Logger logger, String id) {
        return switch (objectiveType) {
            case DELIVER_ITEMS, MINE_BLOCKS, CATCH_FISH -> validateMaterials(types, logger, id);
            case KILL_MOBS -> validateEntities(types, logger, id);
        };
    }

    private boolean validateMaterials(List<String> types, Logger logger, String id) {
        for (String type : types) {
            if (Material.matchMaterial(type) == null) {
                logger.warning("Template " + id + " has invalid material: " + type);
                return false;
            }
        }
        return true;
    }

    private boolean validateEntities(List<String> types, Logger logger, String id) {
        for (String type : types) {
            try {
                EntityType entityType = EntityType.valueOf(type.toUpperCase());
                if (Registry.ENTITY_TYPE.get(NamespacedKey.minecraft(entityType.getKey().getKey())) == null) {
                    logger.warning("Template " + id + " has invalid entity type: " + type);
                    return false;
                }
            } catch (IllegalArgumentException ex) {
                logger.warning("Template " + id + " has invalid entity type: " + type);
                return false;
            }
        }
        return true;
    }

    private int parseInt(Object value, int def) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return def;
        }
    }

    public List<ContractTemplate> getTemplates() {
        return Collections.unmodifiableList(templates);
    }

    public java.util.Optional<ContractTemplate> getTemplateById(String id) {
        return templates.stream().filter(template -> template.getId().equalsIgnoreCase(id)).findFirst();
    }
}
