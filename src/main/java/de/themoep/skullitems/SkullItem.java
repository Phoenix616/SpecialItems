package de.themoep.skullitems;

import de.themoep.skullitems.actions.ActionSet;
import de.themoep.skullitems.actions.ActionTrigger;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Copyright 2016 Max Lee (https://github.com/Phoenix616/)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Mozilla Public License as published by
 * the Mozilla Foundation, version 2.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Mozilla Public License v2.0 for more details.
 * <p/>
 * You should have received a copy of the Mozilla Public License v2.0
 * along with this program. If not, see <http://mozilla.org/MPL/2.0/>.
 */
public class SkullItem {
    private String id;
    private String name;
    private ItemStack item;
    private ActionSet actions;
    private List<String> lore;

    public SkullItem(String id, String name, ItemStack item, ActionSet actions, List<String> lore) {
        this.id = id.toLowerCase();
        this.name = name;
        this.item = item;
        this.actions = actions;
        this.lore = lore;
    }

    public SkullItem(SkullItem skullItem) {
        this(skullItem.getId(), skullItem.getName(), skullItem.getItem(), skullItem.getActions(), skullItem.getLore());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public ActionSet getActions() {
        return actions;
    }
}
