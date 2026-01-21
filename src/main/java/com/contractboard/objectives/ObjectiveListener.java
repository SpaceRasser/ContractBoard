package com.contractboard.objectives;

import com.contractboard.contracts.ContractService;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class ObjectiveListener implements Listener {
    private final ContractService contractService;
    private final PlacedBlockTracker placedBlockTracker;

    public ObjectiveListener(ContractService contractService, PlacedBlockTracker placedBlockTracker) {
        this.contractService = contractService;
        this.placedBlockTracker = placedBlockTracker;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        placedBlockTracker.track(event.getBlock().getLocation());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (placedBlockTracker.wasPlacedByPlayer(event.getBlock().getLocation())) {
            return;
        }
        Player player = event.getPlayer();
        Material material = event.getBlock().getType();
        contractService.progressMineBlocks(player, material);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player player)) {
            return;
        }
        EntityType type = event.getEntity().getType();
        contractService.progressKillMobs(player, type);
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH && event.getState() != PlayerFishEvent.State.CAUGHT_ENTITY) {
            return;
        }
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        ItemStack caught = null;
        if (event.getCaught() instanceof Item item) {
            caught = item.getItemStack();
        }
        if (caught == null) {
            return;
        }
        contractService.progressCatchFish(player, caught.getType());
    }
}
