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


package com.jcwhatever.pvs.signs;

import com.jcwhatever.nucleus.managed.scheduler.Scheduler;
import com.jcwhatever.nucleus.managed.signs.ISignContainer;
import com.jcwhatever.nucleus.managed.signs.SignHandler;
import com.jcwhatever.nucleus.utils.text.TextColor;
import com.jcwhatever.nucleus.utils.text.TextUtils;
import com.jcwhatever.pvs.players.ArenaPlayer;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.arena.options.NameMatchMode;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Matcher;

/**
 * An sign players can click to join an arena.
 */
public class PveSignHandler extends SignHandler {

    /**
     * Constructor.
     */
    public PveSignHandler() {
        super(PVStarAPI.getPlugin(), "PVE");
    }

    public PveSignHandler(String name) {
        super(PVStarAPI.getPlugin(), name);
    }

    @Override
    public String getDescription() {
        return "An arena join sign. Player joins an arena by clicking on the sign.";
    }

    @Override
    public String[] getUsage() {
        return new String[] {
                "PVE",
                "<arenaName>",
                "--anything--",
                "--anything--"
        };
    }

    @Override
    public String getHeaderPrefix() {
        return TextColor.DARK_BLUE.toString();
    }

    @Override
    protected void onSignLoad(ISignContainer sign) {
        // do nothing
    }

    @Override
    protected SignChangeResult onSignChange(Player player, ISignContainer sign) {
        String rawName = sign.getRawLine(1);

        Matcher matcher = TextUtils.PATTERN_SPACE.matcher(rawName);

        String arenaName = matcher.replaceAll("_");

        List<IArena> arenas = PVStarAPI.getArenaManager().getArena(arenaName, NameMatchMode.CASE_INSENSITIVE);
        return arenas.size() == 1 ?
                SignChangeResult.VALID :
                SignChangeResult.INVALID;
    }

    @Override
    protected SignClickResult onSignClick(Player p, ISignContainer sign) {
        String rawName = sign.getRawLine(1);
        Matcher matcher = TextUtils.PATTERN_SPACE.matcher(rawName);

        final String arenaName = matcher.replaceAll("_");

        List<IArena> arenas = PVStarAPI.getArenaManager().getArena(arenaName, NameMatchMode.CASE_INSENSITIVE);
        if (arenas.size() != 1)
            return SignClickResult.IGNORED;

        final IArena arena =  arenas.get(0);

        final IArenaPlayer player = ArenaPlayer.get(p);
        if (player == null)
            return SignClickResult.IGNORED;

        Scheduler.runTaskLater(PVStarAPI.getPlugin(), new Runnable() {

            @Override
            public void run() {
                // Add player to arena
                arena.join(player);
            }
        });

        return SignClickResult.HANDLED;
    }

    @Override
    protected SignBreakResult onSignBreak(Player player, ISignContainer sign) {
        return SignBreakResult.ALLOW;
    }
}
