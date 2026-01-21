package com.contractboard.npc;

import com.contractboard.contracts.Faction;
import com.contractboard.gui.GuiController;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class NpcListener implements Listener {
    private final GuiController guiController;
    private final NpcService npcService;

    public NpcListener(GuiController guiController, NpcService npcService) {
        this.guiController = guiController;
        this.npcService = npcService;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Villager villager)) {
            return;
        }
        Faction faction = npcService.getNpcFaction(villager);
        if (faction == null) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        guiController.openFactionMenu(player, faction);
    }
}
