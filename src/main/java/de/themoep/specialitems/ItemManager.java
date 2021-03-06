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
import de.themoep.specialitems.actions.Trigger;
import de.themoep.specialitems.actions.TriggerType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.permissions.Permission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ItemManager {
    private final SpecialItems plugin;

    private Map<String, SpecialItem> itemMap;

    public ItemManager(SpecialItems plugin) {
        this.plugin = plugin;
        int itemsLoaded = loadItems();
        plugin.getLogger().log(Level.INFO, itemsLoaded + " special items loaded!");
    }

    /**
     * Load the items from the plugin's config.yml
     * @return
     */
    private int loadItems() {
        // reset item map
        itemMap = new HashMap<>();

        // reset recipes
        Iterator<Recipe> recipes = plugin.getServer().recipeIterator();
        while (recipes.hasNext()) {
            Recipe recipe = recipes.next();
            if (SpecialItem.getId(recipe.getResult()) != null) {
                recipes.remove();
            }
        }

        ConfigurationSection items = plugin.getConfig().getConfigurationSection("items");
        if (items == null || items.getKeys(false).size() == 0) {
            plugin.getLogger().log(Level.WARNING, "No special items configured?");
            return 0;
        }
        List<String> failedRecipes = new ArrayList<>();
        for (String id : items.getKeys(false)) {
            try {
                ConfigurationSection itemSection = items.getConfigurationSection(id);
                SpecialItem item = new SpecialItem(
                        id,
                        itemSection.getString("displayname"),
                        itemSection.getItemStack("item"),
                        new ActionSet(itemSection.getConfigurationSection("actions")),
                        itemSection.getStringList("lore")
                );
                itemMap.put(item.getId(), item);

                // Register recipe
                ConfigurationSection recipeSection = itemSection.getConfigurationSection("recipe");
                if (recipeSection != null && item.getItem() != null) {
                    Recipe recipe = null;
                    try {
                        String recipeType = recipeSection.getString("type");
                        if ("shapeless".equalsIgnoreCase(recipeType)) {
                            recipe = new ShapelessRecipe(new NamespacedKey(plugin, item.getId()), item.getItem());
                            for (String matStr : recipeSection.getConfigurationSection("materials").getKeys(false)) {
                                Material mat = Material.valueOf(matStr.toUpperCase());
                                ((ShapelessRecipe) recipe).addIngredient(
                                        recipeSection.getInt("materials." + matStr), mat
                                );
                            }
                        } else if ("shaped".equalsIgnoreCase(recipeType)) {
                            recipe = new ShapedRecipe(new NamespacedKey(plugin, item.getId()), item.getItem());
                            List<String> shape = recipeSection.getStringList("shape");
                            ((ShapedRecipe) recipe).shape(shape.toArray(new String[shape.size()]));
                            for (String rKey : recipeSection.getConfigurationSection("keys").getKeys(false)) {
                                if (rKey.length() > 1) {
                                    throw new IllegalArgumentException(
                                            "Shaped craft key " + rKey + " has to be a char and only be 1 long!"
                                    );
                                }
                                Material mat = Material.valueOf(recipeSection.getString("keys." + rKey).toUpperCase());
                                ((ShapedRecipe) recipe).setIngredient(rKey.toCharArray()[0], mat);
                            }
                        } else if ("furnace".equalsIgnoreCase(recipeType)) {
                            recipe = new FurnaceRecipe(new NamespacedKey(
                                    plugin,
                                    item.getId()),
                                    item.getItem(),
                                    Material.valueOf(recipeSection.getString("input")),
                                    (float) recipeSection.getDouble("experience", 0),
                                    recipeSection.getInt("time", 200)
                            );
                            ((FurnaceRecipe) recipe).setExperience((float) recipeSection.getDouble("exp"));
                        } else {
                            throw new IllegalArgumentException(recipeType + " is not a supported or valid recipe type!");
                        }
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().log(Level.SEVERE, "Could not load recipe for " + id + "!", e);
                    }
                    if (recipe != null) {
                        try {
                            if (!plugin.getServer().addRecipe(recipe)) {
                                failedRecipes.add(id);
                            }
                        } catch (IllegalStateException e) {
                            failedRecipes.add(id);
                        }
                    }
                }

                // Register permissions
                if (plugin.getConfig().getBoolean("permissions.use")) {
                    Permission usePerm = new Permission("specialitems.item." + id.toLowerCase() + ".use");
                    try {
                        plugin.getServer().getPluginManager().addPermission(usePerm);
                    } catch (IllegalArgumentException e) {
                        // Permission is already defined
                        usePerm = plugin.getServer().getPluginManager().getPermission(usePerm.getName());
                    }
                    if (plugin.getConfig().getBoolean("permissions.usepertrigger")) {
                        for (TriggerType trigger : TriggerType.values()) {
                            Permission triggerPerm = new Permission("specialitems.item." + id.toLowerCase() + ".use." + trigger.toString().toLowerCase());
                            triggerPerm.addParent(usePerm, true);
                            try {
                                plugin.getServer().getPluginManager().addPermission(usePerm);
                            } catch (IllegalArgumentException e) {
                                // Permission is already defined
                            }
                        }
                    }
                }
                if (plugin.getConfig().getBoolean("permissions.craft")) {
                    Permission craftPerm = new Permission("specialitems.item." + id.toLowerCase() + ".craft");
                    try {
                        plugin.getServer().getPluginManager().addPermission(craftPerm);
                    } catch (IllegalArgumentException e) {
                        // Permission is already defined
                    }
                }
                if (plugin.getConfig().getBoolean("permissions.drop")) {
                    Permission dropPerm = new Permission("specialitems.item." + id.toLowerCase() + ".drop");
                    try {
                        plugin.getServer().getPluginManager().addPermission(dropPerm);
                    } catch (IllegalArgumentException e) {
                        // Permission is already defined
                    }
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while loading item " + id + "!", e);
            }
        }

        if (!failedRecipes.isEmpty()) {
            plugin.getLogger().log(Level.WARNING, "Unable to add recipe for items " + failedRecipes + ". If this is a reload and you edited the config then existing recipes will not be edited due to limitations in the server!");
        }

        return itemMap.size();
    }

    public Collection<SpecialItem> getSpecialItems() {
        return itemMap.values();
    }

    /**
     * Get a skull item by it's name.
     * @param id The id of the special item
     * @return The saved special item. Null if none was found. (Use the manipulation methods to change configs!)
     */
    public SpecialItem getSpecialItem(String id) {
        return itemMap.get(id.toLowerCase());
    }

    /**
     * Get a the SpecialItem object from an ItemStack
     * @param item The ItemStack to get the SpecialItem from
     * @return The SpecialItem or <tt>null</tt> if it isn't one or none was found with the encoded item name
     */
    public SpecialItem getSpecialItem(ItemStack item) throws IllegalArgumentException {
        String id = SpecialItem.getId(item);
        if (id == null) {
            return null;
        }
        return getSpecialItem(id);
    }

    /**
     * Execute all actions for a specific trigger
     * @param trigger The info about the trigger of this action
     * @return Whether or not the event that triggered this should be cancelled,
     * <tt>true</tt> if there are actions and the do not contain ItemActionType.DONT_CANCEL,
     * <tt>false</tt> if there are no actions
     */
    public Trigger executeActions(Trigger trigger) {
        try {
            SpecialItem item = trigger.hasSpecialItem() ? trigger.getSpecialItem() : getSpecialItem(trigger.getItem());
            if (item != null) {
                trigger.setSpecialItem(item);
                boolean hasPermission = true;
                if (plugin.getConfig().getBoolean("permissions.usepertrigger")) {
                    hasPermission = plugin.checkPerm(
                            trigger.getPlayer(),
                            "specialitems.item." + item.getId() + ".use." + trigger.toString().toLowerCase(),
                            "use"
                    );
                } else if (plugin.getConfig().getBoolean("permissions.use")) {
                    hasPermission = plugin.checkPerm(
                            trigger.getPlayer(),
                            "specialitems.item." + item.getId() + ".use",
                            "use"
                    );
                }
                if (hasPermission) {
                    return item.getActionSet().execute(trigger);
                }
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, trigger.getPlayer().getName() + " has an invalid item! " + e.getMessage());
        }
        return trigger;
    }

    public void setValue(String id, String key, Object object) {
        SpecialItem item = itemMap.get(id.toLowerCase());
        if (item != null) {
            plugin.getConfig().set("items." + item.getId() + "." + key.toLowerCase(), object);
            ConfigurationSection itemSection = plugin.getConfig().getConfigurationSection("items." + item.getId());
            item = new SpecialItem(
                    item.getId(),
                    itemSection.getString("displayname"),
                    itemSection.getItemStack("item"),
                    new ActionSet(itemSection.getConfigurationSection("actions")),
                    itemSection.getStringList("lore")
            );
            itemMap.put(item.getId(), item);
            plugin.saveConfig();
        }
    }
}
