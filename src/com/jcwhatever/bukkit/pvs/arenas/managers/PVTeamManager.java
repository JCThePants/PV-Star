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


package com.jcwhatever.bukkit.pvs.arenas.managers;

import com.jcwhatever.bukkit.generic.collections.EntryCounter;
import com.jcwhatever.bukkit.generic.collections.EntryCounter.RemovalPolicy;
import com.jcwhatever.bukkit.generic.events.GenericsEventHandler;
import com.jcwhatever.bukkit.generic.events.GenericsEventListener;
import com.jcwhatever.bukkit.generic.events.GenericsEventPriority;
import com.jcwhatever.bukkit.pvs.PVArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaTeam;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaTeam.TeamDistributor;
import com.jcwhatever.bukkit.pvs.api.arena.managers.TeamManager;
import com.jcwhatever.bukkit.pvs.api.arena.options.AddPlayerReason;
import com.jcwhatever.bukkit.pvs.api.arena.options.RemovePlayerReason;
import com.jcwhatever.bukkit.pvs.api.arena.options.TeamChangeReason;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerPreAddEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerRemovedEvent;
import com.jcwhatever.bukkit.pvs.api.events.spawns.AddSpawnEvent;
import com.jcwhatever.bukkit.pvs.api.events.spawns.RemoveSpawnEvent;
import com.jcwhatever.bukkit.pvs.api.spawns.Spawnpoint;

import java.util.List;
import java.util.Set;

/**
 * Team manager implementation.
 */
public class PVTeamManager implements TeamManager, GenericsEventListener {

    private Arena _arena;
    private TeamDistributor _teamDistributor;
    private EntryCounter<ArenaTeam> _teams = new EntryCounter<>(RemovalPolicy.REMOVE);
    private EntryCounter<ArenaTeam> _currentTeams = new EntryCounter<>(RemovalPolicy.REMOVE);

    /*
     * Constructor.
     */
    public PVTeamManager (Arena arena) {
        _arena = arena;

        arena.getEventManager().register(this);

        loadSettings();
    }

    /*
     * Get the owning arena.
     */
    @Override
    public Arena getArena() {
        return _arena;
    }

    /*
     * Get available teams in the arena. Available teams
     * are determined by the teams set on spawnpoints in
     * the spawn manager.
     */
    @Override
    public Set<ArenaTeam> getTeams() {
        return _teams.getTypesCounted();
    }

    /**
     * Get the teams currently in the arena.
     */
    @Override
    public Set<ArenaTeam> getCurrentTeams() {
        return _currentTeams.getTypesCounted();
    }

    @Override
    public int totalTeams() {
        return _teams.getTypeSize();
    }

    /**
     * Get the number of teams currently in the arena.
     */
    @Override
    public int totalCurrentTeams() {
        return _currentTeams.getTypeSize();
    }

    /*
     * Get the next available team from the team distributor.
     */
    protected ArenaTeam nextTeam() {
        return getTeamDistributor().next();
    }

    /*
     * Place a team back into circulation. Use when a player leaves the arena
     * to prevent issues with the distribution of teams.
     */
    protected void recycleTeam(ArenaTeam team) {
        getTeamDistributor().recycle(team);
    }

    /*
     * Get the distributor responsible for distributing teams to players
     * as they join.
     */
    protected TeamDistributor getTeamDistributor() {
        if (_teamDistributor == null)
            _teamDistributor = new TeamDistributor(getTeams());

        return _teamDistributor;
    }

    /*
     * Set a players team when they are added to an arena.
     */
    @GenericsEventHandler(priority = GenericsEventPriority.FIRST)
    private void onPlayerAdd(PlayerPreAddEvent event) {

        if (!(event.getPlayer() instanceof PVArenaPlayer))
            return;

        if (event.getReason() != AddPlayerReason.FORWARDING &&
                event.getReason() != AddPlayerReason.ARENA_RELATION_CHANGE) {

            ArenaTeam team = nextTeam();

            _currentTeams.add(team);

            event.getPlayer().setTeam(team, TeamChangeReason.JOIN_ARENA);
        }
    }

    /*
     * Recycle the a players team when they are removed from the arena.
     * Ensures an even distribution of teams.
     */
    @GenericsEventHandler(priority = GenericsEventPriority.FIRST)
    private void onPlayerRemove(PlayerRemovedEvent event) {

        if (event.getReason() != RemovePlayerReason.FORWARDING &&
                event.getReason() != RemovePlayerReason.ARENA_RELATION_CHANGE) {

            ArenaTeam team = event.getPlayer().getTeam();

            _currentTeams.subtract(team);

            recycleTeam(team);
        }
    }

    /*
     * Add spawn team.
     */
    @GenericsEventHandler(priority = GenericsEventPriority.LAST)
    private void onAddSpawn(AddSpawnEvent event) {

        if (event.getSpawnpoint().getTeam() == ArenaTeam.NONE)
            return;

        _teams.add(event.getSpawnpoint().getTeam());
    }

    /*
     * Remove team when spawn is removed.
     */
    @GenericsEventHandler(priority = GenericsEventPriority.LAST)
    private void onRemoveSpawn(RemoveSpawnEvent event) {

        if (event.getSpawnpoint().getTeam() == ArenaTeam.NONE)
            return;

        _teams.subtract(event.getSpawnpoint().getTeam());
    }

    // load settings
    private void loadSettings() {

        List<Spawnpoint> spawnpoints = getArena().getSpawnManager().getGameSpawns();

        for (Spawnpoint spawn : spawnpoints) {
            if (spawn.getTeam() == ArenaTeam.NONE)
                continue;

            _teams.add(spawn.getTeam());
        }
    }
}
