package com.contractboard.contracts;

import com.contractboard.config.ConfigService;
import com.contractboard.config.MessagesService;
import com.contractboard.data.PlayerContractDao;
import com.contractboard.data.PlayerProfileDao;
import com.contractboard.objectives.CatchFishData;
import com.contractboard.objectives.DeliverItemsData;
import com.contractboard.objectives.KillMobsData;
import com.contractboard.objectives.MineBlocksData;
import com.contractboard.objectives.ObjectiveType;
import com.contractboard.util.TimeUtil;
import com.contractboard.vault.EconomyHook;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.logging.Level;

public class ContractService {
    private final JavaPlugin plugin;
    private final PlayerProfileDao profileDao;
    private final PlayerContractDao contractDao;
    private final ContractGenerator generator;
    private final ConfigService configService;
    private final MessagesService messagesService;
    private final EconomyHook economyHook;
    private final Executor asyncExecutor;
    private final Executor mainExecutor;

    private final Map<UUID, PlayerProfile> profileCache = new ConcurrentHashMap<>();
    private final Map<UUID, List<ContractInstance>> contractCache = new ConcurrentHashMap<>();
    private final Map<UUID, CompletableFuture<Void>> loading = new ConcurrentHashMap<>();
    private final java.util.Set<CompletableFuture<?>> pendingSaves = ConcurrentHashMap.newKeySet();

    public ContractService(JavaPlugin plugin,
                           PlayerProfileDao profileDao,
                           PlayerContractDao contractDao,
                           ContractGenerator generator,
                           ConfigService configService,
                           MessagesService messagesService,
                           EconomyHook economyHook) {
        this.plugin = plugin;
        this.profileDao = profileDao;
        this.contractDao = contractDao;
        this.generator = generator;
        this.configService = configService;
        this.messagesService = messagesService;
        this.economyHook = economyHook;
        this.asyncExecutor = command -> Bukkit.getScheduler().runTaskAsynchronously(plugin, command);
        this.mainExecutor = command -> Bukkit.getScheduler().runTask(plugin, command);
    }

    public void ensureDailyRotation(Player player) {
        ensureLoaded(player.getUniqueId()).thenRunAsync(() -> {
            PlayerProfile profile = profileCache.get(player.getUniqueId());
            long currentDay = currentRotationKey(player);
            if (profile.getLastRotationEpochDay() == currentDay) {
                return;
            }
            List<ContractInstance> newContracts = generator.generate(profile, player.getUniqueId(), currentDay);
            profile.setLastRotationEpochDay(currentDay);
            resetClaimsIfNeeded(profile, currentDay);
            contractCache.put(player.getUniqueId(), newContracts);
            saveProfileAsync(profile);
            saveContractsAsync(newContracts, player.getUniqueId());
        }, mainExecutor);
    }

    public CompletableFuture<Void> ensureLoaded(UUID uuid) {
        if (profileCache.containsKey(uuid)) {
            return CompletableFuture.completedFuture(null);
        }
        return loading.computeIfAbsent(uuid, ignored -> CompletableFuture.runAsync(() -> {
            try {
                PlayerProfile profile = profileDao.findByUuid(uuid)
                    .orElseGet(() -> new PlayerProfile(uuid, 0, 0, 0, 0, 0, 0));
                List<ContractInstance> contracts = contractDao.findByPlayer(uuid);
                profileCache.put(uuid, profile);
                contractCache.put(uuid, new ArrayList<>(contracts));
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load contracts for " + uuid, ex);
            }
        }, asyncExecutor).whenComplete((res, ex) -> loading.remove(uuid)));
    }

    public CompletableFuture<List<ContractInstance>> getContracts(UUID uuid) {
        return ensureLoaded(uuid).thenApply(ignored -> contractCache.getOrDefault(uuid, Collections.emptyList()));
    }

    public PlayerProfile getProfile(UUID uuid) {
        return profileCache.get(uuid);
    }

    public void progressKillMobs(Player player, EntityType type) {
        progressContracts(player, ObjectiveType.KILL_MOBS, type.name());
    }

    public void progressMineBlocks(Player player, Material material) {
        progressContracts(player, ObjectiveType.MINE_BLOCKS, material.name());
    }

    public void progressCatchFish(Player player, Material material) {
        progressContracts(player, ObjectiveType.CATCH_FISH, material.name());
    }

    private void progressContracts(Player player, ObjectiveType objectiveType, String typeId) {
        UUID uuid = player.getUniqueId();
        ensureLoaded(uuid).thenRunAsync(() -> {
            List<ContractInstance> contracts = contractCache.getOrDefault(uuid, List.of());
            boolean updated = false;
            for (ContractInstance contract : contracts) {
                if (contract.getObjectiveType() != objectiveType) {
                    continue;
                }
                if (contract.getStatus() != ContractStatus.ACTIVE) {
                    continue;
                }
                boolean matches = switch (objectiveType) {
                    case KILL_MOBS -> ((KillMobsData) contract.getObjectiveData()).types().stream()
                        .anyMatch(type -> type.equalsIgnoreCase(typeId));
                    case MINE_BLOCKS -> ((MineBlocksData) contract.getObjectiveData()).types().stream()
                        .anyMatch(type -> type.equalsIgnoreCase(typeId));
                    case CATCH_FISH -> ((CatchFishData) contract.getObjectiveData()).types().stream()
                        .anyMatch(type -> type.equalsIgnoreCase(typeId));
                    default -> false;
                };
                if (!matches) {
                    continue;
                }
                contract.setProgress(Math.min(contract.getProgress() + 1, contract.getTarget()));
                if (contract.getProgress() >= contract.getTarget()) {
                    contract.setStatus(ContractStatus.COMPLETED);
                    player.sendMessage(messagesService.getMessage("messages.contractCompleted"));
                }
                contract.setUpdatedAt(System.currentTimeMillis());
                saveContractAsync(contract);
                updated = true;
            }
            if (updated) {
                contractCache.put(uuid, contracts);
            }
        }, mainExecutor);
    }

    public boolean attemptDeliver(Player player, ContractInstance contract) {
        if (contract.getObjectiveType() != ObjectiveType.DELIVER_ITEMS) {
            return false;
        }
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            return false;
        }
        DeliverItemsData data = (DeliverItemsData) contract.getObjectiveData();
        Material material = Material.matchMaterial(data.material());
        if (material == null) {
            return false;
        }
        int amount = data.amount();
        Inventory inventory = player.getInventory();
        int available = countItems(inventory, material);
        if (available < amount) {
            player.sendMessage(messagesService.getMessage("messages.notEnoughItems"));
            return false;
        }
        removeItems(inventory, material, amount);
        contract.setProgress(contract.getTarget());
        contract.setStatus(ContractStatus.COMPLETED);
        contract.setUpdatedAt(System.currentTimeMillis());
        saveContractAsync(contract);
        player.sendMessage(messagesService.getMessage("messages.itemsDelivered"));
        return true;
    }

    public boolean attemptClaim(Player player, ContractInstance contract) {
        if (contract.getStatus() != ContractStatus.COMPLETED) {
            return false;
        }
        ContractTemplate template = getTemplateById(contract.getTemplateId()).orElse(null);
        if (template == null) {
            return false;
        }
        PlayerProfile profile = profileCache.get(player.getUniqueId());
        if (profile == null) {
            return false;
        }
        long currentDay = currentRotationKey(player);
        resetClaimsIfNeeded(profile, currentDay);
        if (profile.getRewardsClaimedToday() >= configService.getMaxClaimsPerDay()) {
            player.sendMessage(messagesService.getMessage("messages.rewardLimitReached"));
            return false;
        }
        TemplateRewards rewards = template.getRewards();
        if (!rewards.getItems().isEmpty()) {
            for (ItemReward itemReward : rewards.getItems()) {
                Material material = Material.matchMaterial(itemReward.getMaterial());
                if (material == null) {
                    continue;
                }
                ItemStack item = new ItemStack(material, itemReward.getAmount());
                var meta = item.getItemMeta();
                if (itemReward.getName() != null) {
                    meta.displayName(messagesService.miniMessage().deserialize(itemReward.getName()));
                }
                if (itemReward.getLore() != null && !itemReward.getLore().isEmpty()) {
                    List<Component> lore = new ArrayList<>();
                    for (String line : itemReward.getLore()) {
                        lore.add(messagesService.miniMessage().deserialize(line));
                    }
                    meta.lore(lore);
                }
                item.setItemMeta(meta);
                player.getInventory().addItem(item);
            }
        }
        if (rewards.getMoney() != null) {
            economyHook.deposit(player, rewards.getMoney());
        }
        if (rewards.getRep() != 0) {
            profile.addRep(template.getFaction(), rewards.getRep());
        }
        profile.incrementRewardsClaimedToday();
        saveProfileAsync(profile);
        contract.setStatus(ContractStatus.REWARDED);
        contract.setUpdatedAt(System.currentTimeMillis());
        saveContractAsync(contract);
        player.sendMessage(messagesService.getMessage("messages.rewardClaimed"));
        return true;
    }

    public void resetDay(UUID playerUuid) {
        ensureLoaded(playerUuid).thenRun(() -> {
            PlayerProfile profile = profileCache.get(playerUuid);
            profile.setLastRotationEpochDay(0);
            profile.setLastRewardDay(0);
            profile.setRewardsClaimedToday(0);
            saveProfileAsync(profile);
        });
    }

    public void setRep(UUID playerUuid, Faction faction, int value) {
        ensureLoaded(playerUuid).thenRun(() -> {
            PlayerProfile profile = profileCache.get(playerUuid);
            profile.setRep(faction, value);
            saveProfileAsync(profile);
        });
    }

    public void addRep(UUID playerUuid, Faction faction, int delta) {
        ensureLoaded(playerUuid).thenRun(() -> {
            PlayerProfile profile = profileCache.get(playerUuid);
            profile.addRep(faction, delta);
            saveProfileAsync(profile);
        });
    }

    public void saveProfileAsync(PlayerProfile profile) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                profileDao.upsert(profile);
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save player profile", ex);
            }
        }, asyncExecutor);
        trackSave(future);
    }

    public void saveContractAsync(ContractInstance contract) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                contractDao.upsert(contract);
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save contract", ex);
            }
        }, asyncExecutor);
        trackSave(future);
    }

    public void saveContractsAsync(List<ContractInstance> contracts, UUID playerUuid) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                contractDao.deleteByPlayer(playerUuid);
                for (ContractInstance contract : contracts) {
                    contractDao.upsert(contract);
                }
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save contracts", ex);
            }
        }, asyncExecutor);
        trackSave(future);
    }

    private int countItems(Inventory inventory, Material material) {
        int total = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                total += item.getAmount();
            }
        }
        return total;
    }

    private void removeItems(Inventory inventory, Material material, int amount) {
        int remaining = amount;
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType() != material) {
                continue;
            }
            int stackAmount = item.getAmount();
            if (stackAmount <= remaining) {
                inventory.setItem(i, null);
                remaining -= stackAmount;
            } else {
                item.setAmount(stackAmount - remaining);
                remaining = 0;
            }
            if (remaining == 0) {
                break;
            }
        }
    }

    public String getRankTitle(int reputation) {
        var entry = configService.getRankThresholds().floorEntry(reputation);
        if (entry == null) {
            return configService.getRankThresholds().firstEntry().getValue();
        }
        return entry.getValue();
    }

    public Optional<ContractTemplate> getTemplateById(String id) {
        return generator.getTemplateLoader().getTemplateById(id);
    }

    public void shutdown() {
        for (CompletableFuture<?> future : pendingSaves) {
            try {
                future.join();
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed while waiting for save tasks", ex);
            }
        }
    }

    private void trackSave(CompletableFuture<?> future) {
        pendingSaves.add(future);
        future.whenComplete((res, ex) -> pendingSaves.remove(future));
    }

    private long currentRotationKey(Player player) {
        return switch (configService.getRotationMode()) {
            case MINECRAFT -> TimeUtil.currentMinecraftDay(player.getWorld());
            case REAL -> TimeUtil.currentEpochDay(configService.getDailyResetTime());
        };
    }

    private void resetClaimsIfNeeded(PlayerProfile profile, long currentDay) {
        if (profile.getLastRewardDay() != currentDay) {
            profile.setLastRewardDay(currentDay);
            profile.setRewardsClaimedToday(0);
        }
    }
}
