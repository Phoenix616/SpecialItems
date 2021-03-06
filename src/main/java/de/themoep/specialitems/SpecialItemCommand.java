package de.themoep.specialitems;

/*
 * SpecialItems
 * Copyright (c) 2020 Max Lee aka Phoenix616 (mail@moep.tv)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import de.themoep.specialitems.actions.ItemAction;
import de.themoep.specialitems.actions.TriggerType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class SpecialItemCommand implements CommandExecutor {
    private final SpecialItems plugin;

    public SpecialItemCommand(SpecialItems plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            if ("reload".equalsIgnoreCase(args[0])) {
                if (plugin.checkPerm(sender, "specialitems.command.reload")) {
                    plugin.loadConfig();
                    sender.sendMessage(plugin.getTag() + ChatColor.YELLOW + " Config reloaded!");
                }
            } else if ("list".equalsIgnoreCase(args[0])) {
                if (plugin.checkPerm(sender, "specialitems.command.list")) {
                    sender.sendMessage(ChatColor.YELLOW + "Available special items:");
                    if (plugin.getItemManager().getSpecialItems().size() == 0) {
                        sender.sendMessage(ChatColor.RED + "None");
                    } else {
                        for (SpecialItem item : plugin.getItemManager().getSpecialItems()) {
                            sender.sendMessage(
                                    ChatColor.YELLOW + "Name: "
                                            + ChatColor.RESET + item.getId()
                                            + ChatColor.YELLOW + " - Displayname: "
                                            + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', item.getName())
                            );
                        }
                    }
                }
            } else if ("gui".equalsIgnoreCase(args[0])) {
                if (plugin.checkPerm(sender, "specialitems.command.gui")) {
                    Player player = null;
                    if (args.length > 2) {
                        if (plugin.checkPerm(sender, "specialitems.command.gui.others")) {
                            player = plugin.getServer().getPlayer(args[2]);
                            if (player == null || !player.isOnline()) {
                                sender.sendMessage(plugin.getTag() + ChatColor.RED + " No player with the name "
                                        + ChatColor.YELLOW + args[2] + ChatColor.RED + " found!");
                                return true;
                            }
                        } else {
                            return true;
                        }
                    } else if (sender instanceof Player) {
                        player = (Player) sender;
                    } else {
                        sender.sendMessage(ChatColor.RED
                                + " Use /" + label + " gui <playername> " +
                                "to open the GUI for a player from the console!");
                        return true;
                    }
                    if (sender != player) {
                        sender.sendMessage(ChatColor.YELLOW + "Opened gui for " + player.getName());
                    }
                    plugin.getGui().show(player);
                }
            } else if ("get".equalsIgnoreCase(args[0])) {
                if (plugin.checkPerm(sender, "specialitems.command.get")) {
                    if (args.length > 1) {
                        SpecialItem item = plugin.getItemManager().getSpecialItem(args[1]);
                        if (item != null) {
                            Player player = null;
                            if (args.length > 2) {
                                if (plugin.checkPerm(sender, "specialitems.command.give.others")) {
                                    player = plugin.getServer().getPlayer(args[2]);
                                    if (player == null || !player.isOnline()) {
                                        sender.sendMessage(plugin.getTag() + ChatColor.RED + " No player with the name "
                                                + ChatColor.YELLOW + args[2] + ChatColor.RED + " found!");
                                        return true;
                                    }
                                } else {
                                    return true;
                                }
                            } else if (sender instanceof Player) {
                                player = (Player) sender;
                            } else {
                                sender.sendMessage(ChatColor.RED
                                        + " Use /" + label + " get <itemname> <playername> " +
                                        "to give a special item to a player from the console!");
                                return true;
                            }

                            if (player.getInventory().addItem(item.getItem()).size() > 0) {
                                sender.sendMessage(plugin.getTag() + ChatColor.RED
                                        + " Could not give item as you don't have any space iny our inventory!");
                            } else {
                                sender.sendMessage(plugin.getTag() + ChatColor.YELLOW + " Gave "
                                        + ChatColor.RESET + item.getItem().getItemMeta().getDisplayName()
                                        + ChatColor.YELLOW + " to " + ChatColor.RESET + player.getName());
                            }
                        } else {
                            sender.sendMessage(plugin.getTag() + ChatColor.RED + " No item for the name "
                                    + ChatColor.YELLOW + args[1] + ChatColor.YELLOW + " configured!");
                        }
                    } else {
                        sender.sendMessage(plugin.getTag() + ChatColor.RED + " Usage: /" + label + " get <itemname>");
                    }
                }
            } else if ("set".equals(args[0])) {
                if (plugin.checkPerm(sender, "specialitems.command.set")) {
                    if (args.length > 2) {
                        SpecialItem item = plugin.getItemManager().getSpecialItem(args[1]);
                        if (item != null) {
                            Object value = null;
                            if ("item".equalsIgnoreCase(args[2])) {
                                if (args.length > 3) {
                                    try {
                                        value = new ItemStack(Material.valueOf(args[3].toUpperCase()));
                                    } catch (IllegalArgumentException e) {
                                        sender.sendMessage(plugin.getTag() + ChatColor.RED + " No material with the name "
                                                + ChatColor.YELLOW + args[3] + ChatColor.YELLOW + " found!");
                                    }
                                } else if (sender instanceof Player) {
                                    value = ((Player) sender).getInventory().getItemInHand();
                                } else {
                                    sender.sendMessage(plugin.getTag() + ChatColor.RED
                                            + " Use /" + label + " set <itemname> item <material> " +
                                            "to set the item from the console!");
                                    return true;
                                }
                            } else if ("name".equalsIgnoreCase(args[2]) || "displayname".equalsIgnoreCase(args[2])) {
                                if (args.length > 3) {
                                    StringBuilder sb = new StringBuilder(args[3]);
                                    for (int i = 4; i < args.length; i++) {
                                        sb.append(" ").append(args[i]);
                                    }
                                    value = sb.toString();
                                } else {
                                    sender.sendMessage(plugin.getTag() + ChatColor.RED
                                            + " Use /" + label + " set <itemname> displayname <name> " +
                                            "to set the displayname!");
                                    return true;
                                }
                            }

                            if (value != null) {
                                plugin.getItemManager().setValue(item.getId(), args[2], value);
                                sender.sendMessage(plugin.getTag() + ChatColor.YELLOW + " Set "
                                        + ChatColor.RESET + args[2] + ChatColor.YELLOW + " for "
                                        + ChatColor.RESET + item.getId() + ChatColor.YELLOW + "!");
                                return true;
                            }
                        } else {
                            sender.sendMessage(plugin.getTag() + ChatColor.RED + " No item with the name "
                                    + ChatColor.YELLOW + args[1] + ChatColor.YELLOW + " found!");
                            return true;
                        }
                    }
                    sender.sendMessage(plugin.getTag() + ChatColor.RED + " Usage: /" + label + " set <id> [name <name> | item [<material>] | <configpath> <value>]");
                }

            } else if ("info".equals(args[0])) {
                if (plugin.checkPerm(sender, "specialitems.command.info")) {
                    if (args.length > 1) {
                        SpecialItem item = plugin.getItemManager().getSpecialItem(args[1]);
                        if (item != null) {
                            sender.sendMessage( new String[]{
                                    ChatColor.YELLOW + "Info for " + ChatColor.RESET + args[1] + ChatColor.YELLOW + ":",
                                    ChatColor.YELLOW + " Type: " + ChatColor.RESET + item.getItem().getType(),
                                    ChatColor.YELLOW + " Displayname: " + ChatColor.RESET + item.getName(),
                            });
                            if (item.getLore().size() > 0) {
                                sender.sendMessage(ChatColor.YELLOW + " Lore: ");
                            }
                            for (String line : item.getLore()) {
                                sender.sendMessage(" - " + line);
                            }
                            if (item.getActionSet().size() > 0) {
                                sender.sendMessage(ChatColor.YELLOW + " Actions:");
                            }
                            for (Map.Entry<TriggerType, List<ItemAction>> entry : item.getActionSet().entrySet()) {
                                sender.sendMessage(" - " + entry.getKey() + ":");
                                for (ItemAction action : entry.getValue()) {
                                    sender.sendMessage(ChatColor.GRAY + "    > " + action);
                                }
                            }
                        } else {
                            sender.sendMessage(plugin.getTag() + ChatColor.RED + " No item with the name "
                                    + ChatColor.YELLOW + args[1] + ChatColor.YELLOW + " found!");
                            return true;
                        }
                    } else {
                        sender.sendMessage(plugin.getTag() + ChatColor.RED + " Usage: /" + label + " info <itemname>");
                    }
                }
            } else {
                return false;
            }
            return true;
        }
        return false;
    }
}
