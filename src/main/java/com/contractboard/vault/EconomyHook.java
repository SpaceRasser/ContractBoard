package com.contractboard.vault;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EconomyHook {
    private final JavaPlugin plugin;
    private Object economy;
    private Method depositMethod;

    public EconomyHook(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().info("Vault not found, economy rewards disabled.");
            return;
        }
        try {
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            RegisteredServiceProvider<?> provider = Bukkit.getServicesManager().getRegistration(economyClass);
            if (provider != null) {
                economy = provider.getProvider();
                depositMethod = economy.getClass().getMethod("depositPlayer", Player.class, double.class);
                plugin.getLogger().info("Vault economy hooked: " + getEconomyName());
            } else {
                plugin.getLogger().warning("Vault found but no Economy provider available.");
            }
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().warning("Vault found but Economy class missing.");
        } catch (NoSuchMethodException ex) {
            plugin.getLogger().warning("Vault economy provider missing depositPlayer method.");
        }
    }

    public void deposit(Player player, double amount) {
        if (economy == null || depositMethod == null) {
            return;
        }
        try {
            depositMethod.invoke(economy, player, amount);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            plugin.getLogger().warning("Failed to deposit money via Vault: " + ex.getMessage());
        }
    }

    private String getEconomyName() {
        try {
            Method nameMethod = economy.getClass().getMethod("getName");
            Object name = nameMethod.invoke(economy);
            return String.valueOf(name);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            return "Unknown";
        }
    }
}
