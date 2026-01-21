package com.contractboard.commands;

import com.contractboard.ContractBoardPlugin;
import com.contractboard.config.ConfigService;
import com.contractboard.config.MessagesService;
import com.contractboard.config.TemplateLoader;
import com.contractboard.contracts.ContractInstance;
import com.contractboard.contracts.ContractService;
import com.contractboard.contracts.Faction;
import com.contractboard.npc.NpcService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminCommand implements CommandExecutor, TabCompleter {
    private final ContractBoardPlugin plugin;
    private final ContractService contractService;
    private final TemplateLoader templateLoader;
    private final ConfigService configService;
    private final MessagesService messagesService;
    private final NpcService npcService;

    public AdminCommand(ContractBoardPlugin plugin,
                        ContractService contractService,
                        TemplateLoader templateLoader,
                        ConfigService configService,
                        MessagesService messagesService,
                        NpcService npcService) {
        this.plugin = plugin;
        this.contractService = contractService;
        this.templateLoader = templateLoader;
        this.configService = configService;
        this.messagesService = messagesService;
        this.npcService = npcService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /contractsadmin <reload|setrep|addrep|resetday|npc|debug>");
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload" -> {
                configService.reload();
                messagesService.reload();
                templateLoader.reload();
                sender.sendMessage("ContractBoard reloaded.");
            }
            case "setrep" -> handleSetRep(sender, args);
            case "addrep" -> handleAddRep(sender, args);
            case "resetday" -> handleResetDay(sender, args);
            case "npc" -> handleNpc(sender, args);
            case "debug" -> handleDebug(sender, args);
            default -> sender.sendMessage("Unknown subcommand.");
        }
        return true;
    }

    private void handleSetRep(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("Usage: /contractsadmin setrep <player> <faction> <value>");
            return;
        }
        Player target = plugin.getServer().getPlayer(args[1]);
        Faction faction = Faction.fromString(args[2]);
        int value;
        try {
            value = Integer.parseInt(args[3]);
        } catch (NumberFormatException ex) {
            sender.sendMessage("Invalid value.");
            return;
        }
        if (target == null || faction == null) {
            sender.sendMessage("Invalid player or faction.");
            return;
        }
        contractService.setRep(target.getUniqueId(), faction, value);
        sender.sendMessage("Reputation updated.");
    }

    private void handleAddRep(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("Usage: /contractsadmin addrep <player> <faction> <delta>");
            return;
        }
        Player target = plugin.getServer().getPlayer(args[1]);
        Faction faction = Faction.fromString(args[2]);
        int value;
        try {
            value = Integer.parseInt(args[3]);
        } catch (NumberFormatException ex) {
            sender.sendMessage("Invalid value.");
            return;
        }
        if (target == null || faction == null) {
            sender.sendMessage("Invalid player or faction.");
            return;
        }
        contractService.addRep(target.getUniqueId(), faction, value);
        sender.sendMessage("Reputation updated.");
    }

    private void handleResetDay(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /contractsadmin resetday <player>");
            return;
        }
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("Player not found.");
            return;
        }
        contractService.resetDay(target.getUniqueId());
        sender.sendMessage("Rotation reset.");
    }

    private void handleNpc(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use NPC commands.");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("Usage: /contractsadmin npc <create|bind> <faction>");
            return;
        }
        Faction faction = Faction.fromString(args[2]);
        if (faction == null) {
            sender.sendMessage("Invalid faction.");
            return;
        }
        if ("create".equalsIgnoreCase(args[1])) {
            npcService.createNpc(player.getLocation(), faction);
            sender.sendMessage("NPC created.");
        } else if ("bind".equalsIgnoreCase(args[1])) {
            RayTraceResult result = player.rayTraceEntities(5);
            if (result == null || !(result.getHitEntity() instanceof Villager villager)) {
                sender.sendMessage("Look at a villager to bind.");
                return;
            }
            npcService.bindNpc(villager, faction);
            sender.sendMessage("NPC bound.");
        } else {
            sender.sendMessage("Usage: /contractsadmin npc <create|bind> <faction>");
        }
    }

    private void handleDebug(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /contractsadmin debug <player>");
            return;
        }
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("Player not found.");
            return;
        }
        contractService.getContracts(target.getUniqueId()).thenAccept(contracts -> plugin.getServer().getScheduler().runTask(plugin, () -> {
            sender.sendMessage("Contracts for " + target.getName() + ":");
            for (ContractInstance contract : contracts) {
                sender.sendMessage("- " + contract.getTemplateId() + " " + contract.getStatus() + " " + contract.getProgress() + "/" + contract.getTarget());
            }
        }));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("reload", "setrep", "addrep", "resetday", "npc", "debug");
        }
        if (args.length == 2 && ("setrep".equalsIgnoreCase(args[0]) || "addrep".equalsIgnoreCase(args[0]) || "resetday".equalsIgnoreCase(args[0]) || "debug".equalsIgnoreCase(args[0]))) {
            return null;
        }
        if (args.length == 3 && "npc".equalsIgnoreCase(args[0])) {
            return List.of("create", "bind");
        }
        if (args.length == 3 && ("setrep".equalsIgnoreCase(args[0]) || "addrep".equalsIgnoreCase(args[0]) || "npc".equalsIgnoreCase(args[0]))) {
            return List.of("FISHERS", "MINERS", "HUNTERS");
        }
        return List.of();
    }
}
