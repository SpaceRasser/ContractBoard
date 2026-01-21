package com.contractboard.npc;

import com.contractboard.contracts.Faction;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class NpcService {
    private final JavaPlugin plugin;
    private final NamespacedKey factionKey;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public NpcService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.factionKey = new NamespacedKey(plugin, "npc_faction");
    }

    public Villager createNpc(Location location, Faction faction) {
        Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.customName(miniMessage.deserialize("<gold>" + faction.getDisplayName() + " Contracts</gold>"));
        villager.setCustomNameVisible(true);
        villager.getPersistentDataContainer().set(factionKey, PersistentDataType.STRING, faction.name());
        return villager;
    }

    public void bindNpc(Villager villager, Faction faction) {
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.customName(miniMessage.deserialize("<gold>" + faction.getDisplayName() + " Contracts</gold>"));
        villager.setCustomNameVisible(true);
        villager.getPersistentDataContainer().set(factionKey, PersistentDataType.STRING, faction.name());
    }

    public Faction getNpcFaction(Villager villager) {
        String raw = villager.getPersistentDataContainer().get(factionKey, PersistentDataType.STRING);
        if (raw == null) {
            return null;
        }
        return Faction.fromString(raw);
    }
}
