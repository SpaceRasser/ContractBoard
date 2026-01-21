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
import org.bukkit.plugin.java.JavaPlugin;

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
        getCommand("contracts").setExecutor(contractsCommand);
        getCommand("contracts").setTabCompleter(contractsCommand);

        AdminCommand adminCommand = new AdminCommand(this, contractService, templateLoader, configService, messagesService, npcService);
        getCommand("contractsadmin").setExecutor(adminCommand);
        getCommand("contractsadmin").setTabCompleter(adminCommand);

        if (configService.isGenerateOnJoin()) {
            Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(contractService), this);
        }

        getLogger().info("ContractBoard enabled.");
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
