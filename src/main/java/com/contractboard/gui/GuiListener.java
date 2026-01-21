package com.contractboard.gui;

import com.contractboard.config.MessagesService;
import com.contractboard.contracts.ContractInstance;
import com.contractboard.contracts.ContractService;
import com.contractboard.contracts.Faction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class GuiListener implements Listener {
    private final JavaPlugin plugin;
    private final GuiController guiController;
    private final ContractService contractService;
    private final MessagesService messagesService;

    public GuiListener(JavaPlugin plugin, GuiController guiController, ContractService contractService, MessagesService messagesService) {
        this.plugin = plugin;
        this.guiController = guiController;
        this.contractService = contractService;
        this.messagesService = messagesService;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getItemMeta() == null) {
            return;
        }
        var container = item.getItemMeta().getPersistentDataContainer();
        if (container.has(guiController.getFactionKey(), PersistentDataType.STRING)) {
            event.setCancelled(true);
            String factionRaw = container.get(guiController.getFactionKey(), PersistentDataType.STRING);
            Faction faction = Faction.fromString(factionRaw);
            if (faction != null) {
                guiController.openFactionMenu(player, faction);
            }
            return;
        }
        if (!container.has(guiController.getContractIdKey(), PersistentDataType.STRING)) {
            return;
        }
        event.setCancelled(true);
        String contractIdRaw = container.get(guiController.getContractIdKey(), PersistentDataType.STRING);
        UUID contractId = UUID.fromString(contractIdRaw);
        String action = container.get(guiController.getActionKey(), PersistentDataType.STRING);
        contractService.getContracts(player.getUniqueId()).thenAccept(contracts -> Bukkit.getScheduler().runTask(plugin, () -> {
            for (ContractInstance contract : contracts) {
                if (!contract.getContractId().equals(contractId)) {
                    continue;
                }
                if ("deliver".equals(action)) {
                    contractService.attemptDeliver(player, contract);
                } else if ("claim".equals(action)) {
                    contractService.attemptClaim(player, contract);
                } else {
                    player.sendMessage(messagesService.getMessage("messages.contractNotReady"));
                }
                break;
            }
        }));
    }
}
