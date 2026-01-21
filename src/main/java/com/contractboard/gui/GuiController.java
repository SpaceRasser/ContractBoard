package com.contractboard.gui;

import com.contractboard.config.ConfigService;
import com.contractboard.config.MessagesService;
import com.contractboard.contracts.ContractInstance;
import com.contractboard.contracts.ContractService;
import com.contractboard.contracts.ContractStatus;
import com.contractboard.contracts.ContractTemplate;
import com.contractboard.contracts.Faction;
import com.contractboard.objectives.ObjectiveType;
import com.contractboard.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GuiController {
    private final JavaPlugin plugin;
    private final ContractService contractService;
    private final ConfigService configService;
    private final MessagesService messagesService;

    private final NamespacedKey contractIdKey;
    private final NamespacedKey actionKey;
    private final NamespacedKey factionKey;

    public GuiController(JavaPlugin plugin, ContractService contractService, ConfigService configService, MessagesService messagesService) {
        this.plugin = plugin;
        this.contractService = contractService;
        this.configService = configService;
        this.messagesService = messagesService;
        this.contractIdKey = new NamespacedKey(plugin, "contract_id");
        this.actionKey = new NamespacedKey(plugin, "action");
        this.factionKey = new NamespacedKey(plugin, "faction");
    }

    public void openMainMenu(Player player) {
        contractService.ensureDailyRotation(player);
        contractService.ensureLoaded(player.getUniqueId()).thenRunAsync(() -> {
            Inventory inventory = Bukkit.createInventory(player, 27, messagesService.miniMessage().deserialize(configService.getTitleMain()));
            int slot = 11;
            for (Faction faction : Faction.values()) {
                int rep = getRep(player, faction);
                ItemStack item = new ItemBuilder(Material.PAPER, messagesService.miniMessage())
                    .name("<gold>" + faction.getDisplayName() + "</gold>")
                    .lore(List.of(
                        "<gray>Reputation: <white>" + rep + "</white>",
                        "<gray>Rank: <white>" + contractService.getRankTitle(rep) + "</white>"
                    ))
                    .flags(ItemFlag.HIDE_ATTRIBUTES)
                    .build();
                ItemMeta meta = item.getItemMeta();
                meta.getPersistentDataContainer().set(factionKey, PersistentDataType.STRING, faction.name());
                item.setItemMeta(meta);
                inventory.setItem(slot, item);
                slot += 2;
            }
            player.openInventory(inventory);
        }, Bukkit.getScheduler().getMainThreadExecutor(plugin));
    }

    public void openFactionMenu(Player player, Faction faction) {
        contractService.ensureDailyRotation(player);
        contractService.getContracts(player.getUniqueId()).thenAccept(contracts -> Bukkit.getScheduler().runTask(plugin, () -> {
            Inventory inventory = Bukkit.createInventory(player, 36, messagesService.miniMessage().deserialize(
                configService.getTitleFaction().replace("%faction%", faction.getDisplayName())));
            int slot = 10;
            for (ContractInstance contract : contracts) {
                if (contract.getFaction() != faction) {
                    continue;
                }
                ContractTemplate template = contractService.getTemplateById(contract.getTemplateId()).orElse(null);
                if (template == null) {
                    continue;
                }
                List<String> lore = new ArrayList<>(template.getDescription());
                lore.add("<gray>Progress: <white>" + contract.getProgress() + "/" + contract.getTarget());
                lore.add(statusLine(contract));
                ItemStack item = new ItemBuilder(Material.BOOK, messagesService.miniMessage())
                    .name(template.getDisplayName())
                    .lore(lore)
                    .flags(ItemFlag.HIDE_ATTRIBUTES)
                    .build();
                ItemMeta meta = item.getItemMeta();
                meta.getPersistentDataContainer().set(contractIdKey, PersistentDataType.STRING, contract.getContractId().toString());
                String action = actionFor(contract);
                if (action != null) {
                    meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action);
                }
                item.setItemMeta(meta);
                inventory.setItem(slot, item);
                slot++;
            }
            player.openInventory(inventory);
        }));
    }

    private String statusLine(ContractInstance contract) {
        return switch (contract.getStatus()) {
            case ACTIVE -> "<gray>Status: <yellow>Active";
            case COMPLETED -> "<gray>Status: <green>Completed <gray>(click to claim)";
            case REWARDED -> "<gray>Status: <green>Claimed";
        };
    }

    private String actionFor(ContractInstance contract) {
        if (contract.getStatus() == ContractStatus.COMPLETED) {
            return "claim";
        }
        if (contract.getObjectiveType() == ObjectiveType.DELIVER_ITEMS && contract.getStatus() == ContractStatus.ACTIVE) {
            return "deliver";
        }
        return null;
    }

    private int getRep(Player player, Faction faction) {
        var profile = contractService.getProfile(player.getUniqueId());
        if (profile == null) {
            return 0;
        }
        return profile.getRep(faction);
    }

    public NamespacedKey getContractIdKey() {
        return contractIdKey;
    }

    public NamespacedKey getActionKey() {
        return actionKey;
    }

    public NamespacedKey getFactionKey() {
        return factionKey;
    }
}
