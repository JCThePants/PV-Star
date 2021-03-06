/*
 * This file is part of PV-Star for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


package com.jcwhatever.pvs.listeners;

import com.jcwhatever.nucleus.events.manager.BukkitEventForwarder;
import com.jcwhatever.nucleus.providers.npc.INpc;
import com.jcwhatever.nucleus.providers.npc.Npcs;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.utils.Msg;
import com.jcwhatever.pvs.players.ArenaPlayer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Forward Bukkit events to the appropriate arena.
 */
public class ArenaEventForwarder extends BukkitEventForwarder {

    private static final Location LOCATION = new Location(null, 0, 0, 0);

    /**
     * Constructor.
     */
    public ArenaEventForwarder() {
        super(PVStarAPI.getPlugin(), EventPriority.HIGHEST);
    }

    @Override
    protected void onEvent(Event event) {
        // do nothing
    }

    @Override
    protected void onBlockEvent(BlockEvent event) {
        callEvent(event.getBlock(), event);
    }

    @Override
    protected void onPlayerEvent(PlayerEvent event) {

        if (event instanceof PlayerInteractEvent) {
            PlayerInteractEvent interactEvent = (PlayerInteractEvent) event;

            if (interactEvent.hasBlock()) {
                callEvent(interactEvent.getClickedBlock(), event);
            } else {
                callEvent(interactEvent.getPlayer(), event);
            }
        }
        else {
            callEvent(event.getPlayer(), event);
        }
    }

    @Override
    protected void onInventoryEvent(InventoryEvent event) {
        if (event instanceof EnchantItemEvent) {
            callEvent(((EnchantItemEvent) event).getEnchanter(), event);
        }
        else if (event instanceof PrepareItemEnchantEvent) {

            callEvent(((PrepareItemEnchantEvent) event).getEnchanter(), event);
        }
        else {
            InventoryHolder holder = event.getInventory().getHolder();

            if (holder instanceof Player) {
                callEvent((Player)holder, event);
            }
        }
    }

    @Override
    protected void onHangingEvent(HangingEvent event) {

    }

    @Override
    protected void onVehicleEvent(VehicleEvent event) {
        callEvent(event.getVehicle(), event);
    }

    @Override
    protected void onEntityEvent(EntityEvent event) {
        Entity entity = event.getEntity();
        if (entity != null) {
            callEvent(entity, event);
        }
        else if (event instanceof EntityExplodeEvent) {
            callEvent(((EntityExplodeEvent) event).getLocation(), event);
        }
        else {
            Msg.debug("Failed to forward bukkit EntityEvent event because it has no entity.");
        }
    }

    private <T extends Event> void callEvent(Block block, T event) {
        callEvent(block.getLocation(LOCATION), event);
    }

    private <T extends Event> void callEvent(Player p, T event) {
        IArenaPlayer player = ArenaPlayer.get(p);
        if (player == null || player.getArena() == null)
            return;

        player.getArena().getEventManager().call(this, event);
    }

    private <T extends Event> void callEvent(Entity entity, T event) {

        if (isInvalidNpc(entity))
            return;

        if (entity instanceof Player) {
            callEvent((Player)entity, event);
        }
        else {
            callEvent(entity.getLocation(LOCATION), event);
        }
    }

    private <T extends Event> void callEvent(Location location, T event) {

        IArena arena = PVStarAPI.getArenaManager().getArena(location);
        if (arena == null)
            return;

        arena.getEventManager().call(this, event);
    }

    private boolean isInvalidNpc(Entity entity) {

        if (Npcs.hasProvider() && Npcs.isNpc(entity)) {

            INpc npc = Npcs.getNpc(entity);
            if (npc == null || npc.isDisposed())
                return true;
        }
        return false;
    }
}
