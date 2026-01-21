package com.contractboard.vault;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class EconomyHook {
    private final JavaPlugin plugin;
    private Economy economy;

    public EconomyHook(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().info("Vault not found, economy rewards disabled.");
            return;
        }
        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (provider != null) {
            economy = provider.getProvider();
            plugin.getLogger().info("Vault economy hooked: " + economy.getName());
        } else {
            plugin.getLogger().warning("Vault found but no Economy provider available.");
        }
    }

    public void deposit(Player player, double amount) {
        if (economy == null) {
            return;
        }
        economy.depositPlayer(player, amount);
    }
}
