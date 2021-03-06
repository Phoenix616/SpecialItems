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

import de.themoep.specialitems.actions.ActionSet;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class SpecialItem {
    private String id;
    private String name;
    private ItemStack item;
    private ActionSet actions;
    private List<String> lore;

    public SpecialItem(String id, String name, ItemStack item, ActionSet actions, List<String> lore) {
        this.id = id.toLowerCase();
        this.name = name;
        this.actions = actions;
        this.lore = lore;
        this.item = buildItemStack(item);
    }

    public SpecialItem(SpecialItem item) {
        this(item.getId(), item.getName(), item.getItem(), item.getActionSet(), item.getLore());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ItemStack getItem() {
        return item;
    }

    public List<String> getLore() {
        return lore;
    }

    public ActionSet getActionSet() {
        return actions;
    }

    private ItemStack buildItemStack(ItemStack item) {
        if (item == null) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();

        if (getName() != null && !getName().isEmpty()) {
            meta.setDisplayName(ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', getName()));
        }

        List<String> lore = new ArrayList<>();
        for (String line : getLore()) {
            lore.add(ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', line));
        }
        lore.add(ChatColor.BLUE + "" + ChatColor.ITALIC + "SpecialItems");
        meta.setLore(lore);

        PersistentDataContainer tags = meta.getPersistentDataContainer();
        tags.set(SpecialItems.KEY, PersistentDataType.STRING, getId());

        item.setItemMeta(meta);

        return item;
    }

    public static String getId(ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer tags = meta.getPersistentDataContainer();
            if (tags.has(SpecialItems.KEY, PersistentDataType.STRING)) {
                return tags.get(SpecialItems.KEY, PersistentDataType.STRING);
            }
            if (meta.hasLore()
                    && meta.getLore().get(meta.getLore().size() - 1).contains("SpecialItems")) {
                String hidden = SpecialItem.getHiddenString(item);
                if (hidden == null) {
                    throw new IllegalArgumentException("Item should be a special item but no hidden id string was found?");
                }
                return hidden;
            }
        }
        return null;
    }

    /**
     * Hide a string inside another string with chat color characters
     * @param hidden The string to hide
     * @param string The string to hide in
     * @return The string with the hidden string appended
     * @deprecated Information should now be stored via a persistent data container
     */
    @Deprecated
    public static String hideString(String hidden, String string) {
        for (int i = string.length() - 1; i >= 0; i--) {
            if (string.length() - i > 2)
                break;
            if (string.charAt(i) == ChatColor.COLOR_CHAR)
                string = string.substring(0, i);
        }
        // Add hidden string
        char[] chars = new char[hidden.length() * 2];
        for (int i = 0; i < hidden.length(); i++) {
            chars[i * 2] = ChatColor.COLOR_CHAR;
            chars[i * 2 + 1] = hidden.charAt(i);
        }
        return string + new String(chars);
    }

    /**
     * Returns a hidden string in the itemstack which is hidden using the last lore line
     */
    public static String getHiddenString(ItemStack item) {
        // Only the color chars at the end of the string is it
        StringBuilder builder = new StringBuilder();
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore())
            return null;
        char[] chars = item.getItemMeta().getLore().get(item.getItemMeta().getLore().size() - 1).toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == org.bukkit.ChatColor.COLOR_CHAR)
                continue;
            if (i + 1 < chars.length) {
                if (chars[i + 1] == org.bukkit.ChatColor.COLOR_CHAR && i > 1 && chars[i - 1] == org.bukkit.ChatColor.COLOR_CHAR)
                    builder.append(c);
                else if (builder.length() > 0)
                    builder = new StringBuilder();
            } else if (i > 0 && chars[i - 1] == org.bukkit.ChatColor.COLOR_CHAR)
                builder.append(c);
        }
        if (builder.length() == 0)
            return null;
        return builder.toString();
    }

    /**
     * Search through an inventory to check whether or not it contains this special item
     * @param inventory The inventory to search for the item in
     * @return <tt>true</tt> if one was removed; <tt>false</tt> if none was found
     */
    public boolean isInInv(Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (id.equals(getId(item))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Count how many times this item is contained in an inventory
     * @param inventory The inventory to count the item in
     * @return The number of items found
     */
    public int countInInv(Inventory inventory) {
        int amount = 0;
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (id.equals(getId(item))) {
                amount += item.getAmount();
            }
        }
        return amount;
    }

    /**
     * Remove one oof this special item from an inventory
     * @param inventory The inventory to remove it from
     * @param amount The amount to remove
     * @return <tt>true</tt> if some where removed; <tt>false</tt> if none was found
     */
    public boolean removeFromInv(Inventory inventory, int amount) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (id.equals(getId(item))) {
                if (item.getAmount() > amount) {
                    item.setAmount(item.getAmount() - amount);
                } else {
                    item = null;
                }
                inventory.setItem(i, item);
                return true;
            }
        }
        return false;
    }
}
