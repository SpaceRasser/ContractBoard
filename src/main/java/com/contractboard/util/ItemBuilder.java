package com.contractboard.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {
    private final ItemStack itemStack;
    private final ItemMeta meta;
    private final MiniMessage miniMessage;

    public ItemBuilder(Material material, MiniMessage miniMessage) {
        this.itemStack = new ItemStack(material);
        this.meta = itemStack.getItemMeta();
        this.miniMessage = miniMessage;
    }

    public ItemBuilder name(String name) {
        if (name != null) {
            meta.displayName(miniMessage.deserialize(name));
        }
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        if (lore != null && !lore.isEmpty()) {
            List<Component> components = new ArrayList<>();
            for (String line : lore) {
                components.add(miniMessage.deserialize(line));
            }
            meta.lore(components);
        }
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        meta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder amount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemStack build() {
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
