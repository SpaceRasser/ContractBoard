package com.contractboard;

import com.contractboard.commands.AdminCommand;
import com.contractboard.commands.ContractsCommand;
import com.contractboard.config.ConfigService;
import com.contractboard.config.MessagesService;
import com.contractboard.config.TemplateLoader;
import com.contractboard.contracts.ContractGenerator;
import com.contractboard.contracts.ContractService;
import com.contractboard.data.Database;
import com.contractboard.data.PlayerContractDao;
import com.contractboard.data.PlayerProfileDao;
import com.contractboard.gui.GuiController;
import com.contractboard.gui.GuiListener;
import com.contractboard.npc.NpcListener;
import com.contractboard.npc.NpcService;
import com.contractboard.objectives.ObjectiveListener;
import com.contractboard.objectives.PlacedBlockTracker;
import com.contractboard.vault.EconomyHook;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import java.util.logging.Level;

public class ContractBoardPlugin extends JavaPlugin {
    private ConfigService configService;
    private MessagesService messagesService;
    private TemplateLoader templateLoader;
    private Database database;
    private ContractService contractService;
    private GuiController guiController;
    private NpcService npcService;
    private PlacedBlockTracker placedBlockTracker;
    private EconomyHook economyHook;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configService = new ConfigService(this);
        messagesService = new MessagesService(this);
        messagesService.reload();

        templateLoader = new TemplateLoader(this);
        templateLoader.reload();

        economyHook = new EconomyHook(this);
        economyHook.hook();

        database = new Database(this);
        database.init();

        PlayerProfileDao profileDao = new PlayerProfileDao(database);
        PlayerContractDao contractDao = new PlayerContractDao(database);

        ContractGenerator generator = new ContractGenerator(templateLoader, configService);
        contractService = new ContractService(this, profileDao, contractDao, generator, configService, messagesService, economyHook);

        guiController = new GuiController(this, contractService, configService, messagesService);
        npcService = new NpcService(this);

        placedBlockTracker = new PlacedBlockTracker(this, configService);
        placedBlockTracker.startCleanupTask();

        Bukkit.getPluginManager().registerEvents(new GuiListener(this, guiController, contractService, messagesService), this);
        Bukkit.getPluginManager().registerEvents(new ObjectiveListener(contractService, placedBlockTracker), this);
        Bukkit.getPluginManager().registerEvents(new NpcListener(guiController, npcService), this);

        ContractsCommand contractsCommand = new ContractsCommand(guiController);
        if (!registerCommand("contracts", contractsCommand, "Open contracts GUI", List.of("cb"), "contractboard.use")) {
            return;
        }

        AdminCommand adminCommand = new AdminCommand(this, contractService, templateLoader, configService, messagesService, npcService);
        if (!registerCommand("contractsadmin", adminCommand, "ContractBoard admin commands", List.of("cba"), "contractboard.admin")) {
            return;
        }

        if (configService.isGenerateOnJoin()) {
            Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(contractService), this);
        }

        getLogger().info("ContractBoard enabled.");
    }

    private boolean registerCommand(String name, CommandExecutor executor, String description, List<String> aliases, String permission) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            command = registerDynamicCommand(name, description, aliases, permission);
        }
        if (command == null) {
            getLogger().severe("Command '" + name + "' is not registered. Check plugin.yml or paper-plugin.yml.");
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        }
        command.setExecutor(executor);
        if (executor instanceof org.bukkit.command.TabCompleter tabCompleter) {
            command.setTabCompleter(tabCompleter);
        }
        return true;
    }

    private PluginCommand registerDynamicCommand(String name, String description, List<String> aliases, String permission) {
        CommandMap commandMap = resolveCommandMap();
        if (commandMap == null) {
            getLogger().severe("Unable to access the Bukkit CommandMap to register '" + name + "'.");
            return null;
        }
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            PluginCommand command = constructor.newInstance(name, this);
            command.setDescription(description);
            command.setAliases(aliases);
            command.setPermission(permission);
            commandMap.register(getDescription().getName(), command);
            getLogger().warning("Command '" + name + "' was missing from metadata; registered dynamically.");
            return command;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            getLogger().log(Level.SEVERE, "Failed to register command '" + name + "' dynamically.", ex);
            return null;
        }
    }

    private CommandMap resolveCommandMap() {
        try {
            Method getCommandMap = Bukkit.getServer().getClass().getMethod("getCommandMap");
            Object result = getCommandMap.invoke(Bukkit.getServer());
            if (result instanceof CommandMap commandMap) {
                return commandMap;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            getLogger().log(Level.SEVERE, "Failed to resolve Bukkit CommandMap.", ex);
        }
        return null;
    }

    @Override
    public void onDisable() {
        try {
            if (placedBlockTracker != null) {
                placedBlockTracker.shutdown();
            }
            if (contractService != null) {
                contractService.shutdown();
            }
            if (database != null) {
                database.shutdown();
            }
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Failed to shutdown ContractBoard cleanly", ex);
        }
    }
}
