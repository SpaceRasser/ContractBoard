package com.contractboard;

import com.contractboard.contracts.ContractService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final ContractService contractService;

    public PlayerJoinListener(ContractService contractService) {
        this.contractService = contractService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        contractService.ensureDailyRotation(event.getPlayer());
    }
}
