package net.hyperlowmc.plugins.punishPaper.gui;

import net.hyperlowmc.plugins.punishPaper.PunishPaper;
import net.hyperlowmc.plugins.punishPaper.models.PlayerData;
import net.hyperlowmc.plugins.punishPaper.models.Punishment;
import net.hyperlowmc.plugins.punishPaper.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CheckGUI {

    public static void openPunishmentGUI(Player player, Punishment punishment, PunishPaper plugin) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Punishment #" + punishment.getBanId());

        // Info item
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(ChatColor.YELLOW + "Punishment Information");
        List<String> infoLore = new ArrayList<>();
        infoLore.add(ChatColor.GRAY + "Player: " + ChatColor.WHITE + punishment.getPlayerName());
        infoLore.add(ChatColor.GRAY + "Type: " + ChatColor.WHITE + punishment.getType());
        infoLore.add(ChatColor.GRAY + "Reason: " + ChatColor.WHITE + punishment.getReason());
        infoLore.add(ChatColor.GRAY + "Category: " + ChatColor.WHITE +
                (punishment.getCategory() != null ? punishment.getCategory() : "Manual"));
        infoLore.add(ChatColor.GRAY + "Punished by: " + ChatColor.WHITE + punishment.getPunishedBy());
        infoLore.add(ChatColor.GRAY + "Active: " + ChatColor.WHITE + punishment.isActive());

        if (punishment.getDuration() == -1) {
            infoLore.add(ChatColor.GRAY + "Duration: " + ChatColor.WHITE + "Permanent");
        } else {
            infoLore.add(ChatColor.GRAY + "Remaining: " + ChatColor.WHITE +
                    TimeUtil.formatDuration(punishment.getRemainingTime()));
        }

        infoMeta.setLore(infoLore);
        info.setItemMeta(infoMeta);
        gui.setItem(13, info);

        // Forgive button
        if (player.hasPermission("punishments.forgive") && punishment.isActive()) {
            ItemStack forgive = new ItemStack(Material.LIME_DYE);
            ItemMeta forgiveMeta = forgive.getItemMeta();
            forgiveMeta.setDisplayName(ChatColor.GREEN + "Forgive Punishment");
            List<String> forgiveLore = new ArrayList<>();
            forgiveLore.add(ChatColor.GRAY + "Click to forgive this punishment");
            forgiveLore.add(ChatColor.GRAY + "This will remove the punishment");
            forgiveLore.add(ChatColor.GRAY + "and decrement offense count");
            forgiveMeta.setLore(forgiveLore);
            forgive.setItemMeta(forgiveMeta);
            gui.setItem(11, forgive);
        }

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "Close");
        close.setItemMeta(closeMeta);
        gui.setItem(15, close);

        player.openInventory(gui);

        // Register click handler
        Listener listener = new Listener() {
            @EventHandler
            public void onClick(InventoryClickEvent e) {
                if (!e.getView().getTitle().equals(ChatColor.GOLD + "Punishment #" + punishment.getBanId())) {
                    return;
                }

                e.setCancelled(true);

                if (e.getCurrentItem() == null) return;

                Player clicker = (Player) e.getWhoClicked();

                if (e.getSlot() == 11 && e.getCurrentItem().getType() == Material.LIME_DYE) {
                    // Forgive
                    plugin.getPunishmentManager().forgivePunishment(punishment.getBanId());
                    clicker.sendMessage(ChatColor.GREEN + "Forgiven punishment #" + punishment.getBanId());
                    clicker.closeInventory();
                    HandlerList.unregisterAll(this);
                } else if (e.getSlot() == 15) {
                    // Close
                    clicker.closeInventory();
                    HandlerList.unregisterAll(this);
                }
            }
        };

        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }

    public static void openPlayerHistoryGUI(Player player, UUID targetUuid, String targetName, PunishPaper plugin) {
        PlayerData data = plugin.getDataManager().getPlayerData(targetUuid);
        if (data == null) {
            player.sendMessage(ChatColor.RED + "No punishment history found!");
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.GOLD + targetName + "'s History");

        List<Punishment> punishments = data.getPunishments();
        int slot = 0;

        for (int i = Math.max(0, punishments.size() - 45); i < punishments.size() && slot < 54; i++) {
            Punishment p = punishments.get(i);

            Material material = p.isActive() ? Material.RED_WOOL : Material.GRAY_WOOL;
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();

            String status = p.isActive() ? ChatColor.RED + "[ACTIVE] " : ChatColor.GRAY + "[INACTIVE] ";
            meta.setDisplayName(status + ChatColor.WHITE + "#" + p.getBanId());

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Type: " + ChatColor.WHITE + p.getType());
            lore.add(ChatColor.GRAY + "Reason: " + ChatColor.WHITE + p.getReason());
            lore.add(ChatColor.GRAY + "Category: " + ChatColor.WHITE +
                    (p.getCategory() != null ? p.getCategory() : "Manual"));

            if (p.getDuration() == -1) {
                lore.add(ChatColor.GRAY + "Duration: " + ChatColor.WHITE + "Permanent");
            } else {
                lore.add(ChatColor.GRAY + "Remaining: " + ChatColor.WHITE +
                        TimeUtil.formatDuration(p.getRemainingTime()));
            }

            lore.add("");
            lore.add(ChatColor.YELLOW + "Click for more details");

            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.setItem(slot, item);
            slot++;
        }

        player.openInventory(gui);

        // Register click handler
        Listener listener = new Listener() {
            @EventHandler
            public void onClick(InventoryClickEvent e) {
                if (!e.getView().getTitle().equals(ChatColor.GOLD + targetName + "'s History")) {
                    return;
                }

                e.setCancelled(true);

                if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

                // Extract ban ID from display name
                ItemMeta meta = e.getCurrentItem().getItemMeta();
                if (meta != null && meta.getDisplayName() != null) {
                    String displayName = ChatColor.stripColor(meta.getDisplayName());
                    String banId = displayName.replaceAll(".*#", "").trim();

                    Punishment punishment = plugin.getDataManager().getPunishmentById(banId);
                    if (punishment != null) {
                        HandlerList.unregisterAll(this);
                        openPunishmentGUI((Player) e.getWhoClicked(), punishment, plugin);
                    }
                }
            }
        };

        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }
}