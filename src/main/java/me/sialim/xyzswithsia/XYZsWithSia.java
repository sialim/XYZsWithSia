package me.sialim.xyzswithsia;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public final class XYZsWithSia extends JavaPlugin implements Listener, CommandExecutor {
    private final List<UUID> playerList = new ArrayList<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("abcsort").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (playerList.contains(player.getUniqueId())) {
                playerList.remove(player.getUniqueId());
                player.sendMessage(ChatColor.RED + "Chest sorting disabled.");
            } else {
                playerList.add(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "Chest sorting enabled.");
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (playerList.contains(event.getPlayer().getUniqueId())) {
            Player player = (Player) event.getPlayer();
            Inventory inventory = event.getInventory();

            List<ItemStack> items = new ArrayList<>();
            for (ItemStack item : inventory.getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    items.add(item);
                }
            }

            player.sendMessage(ChatColor.YELLOW + "Items in inventory before sorting:");
            for (ItemStack item : items) {
                player.sendMessage(ChatColor.WHITE + " - " + getDisplayName(item));
            }

            // Sort by display name, stripping color codes
            items.sort(Comparator.comparing(item -> ChatColor.stripColor(getDisplayName(item)).toLowerCase()));

            player.sendMessage(ChatColor.YELLOW + "Items in inventory after sorting:");
            for (ItemStack item : items) {
                player.sendMessage(ChatColor.WHITE + " - " + getDisplayName(item));
            }

            // Clear and re-insert sorted items
            inventory.clear();
            for (int i = 0; i < items.size(); i++) {
                inventory.setItem(i, items.get(i));
            }
        }
    }

    private void sortInventoryByDisplayName(Inventory inventory) {
        List<ItemStack> items = new ArrayList<>();

        for (ItemStack item : inventory.getContents()) {
            if (item != null) {
                items.add(item);
            }
        }

        Collections.sort(items, Comparator.comparing(item -> {
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                return item.getItemMeta().getDisplayName();
            } else {
                return item.getType().name();
            }
        }));

        inventory.clear();
        for (int i = 0; i < items.size(); i++) {
            if (i < inventory.getSize()) {
                inventory.setItem(i, items.get(i));
            }
        }
    }

    private String extractLetters(String input) {
        String withoutColorCodes = input.replaceAll("(?i)&[0-9a-fk-or]", "");
        return withoutColorCodes.chars()
                .filter(Character::isLetter)
                .mapToObj(c -> String.valueOf((char) c))
                .collect(Collectors.joining());
    }

    private String getDisplayName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }

        switch (item.getType()) {
            case PLAYER_HEAD:
                return item.getItemMeta() != null && item.getItemMeta().hasDisplayName() ?
                        item.getItemMeta().getDisplayName() : "Player Head";
            case SHIELD:
                return item.getItemMeta() != null && item.getItemMeta().hasDisplayName() ?
                        item.getItemMeta().getDisplayName() : "Shield";
            default:
                return item.getType().toString().replace("_", " ").toLowerCase();
        }
    }
}

