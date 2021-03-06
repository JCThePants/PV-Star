/*
 * This file is part of PV-Star for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.jcwhatever.pvs.scripting;

import com.jcwhatever.nucleus.mixins.IDisposable;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.player.PlayerUtils;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;

import org.bukkit.entity.Player;

/**
 * Script API to help convert between a Bukkit
 * {@link org.bukkit.entity.Player} object and a PV-Star
 * {@link IArenaPlayer}.
 */
public class PlayersApiObject implements IDisposable {

    @Override
    public boolean isDisposed() {
        return false;
    }

    @Override
    public void dispose() {
        // do nothing
    }

    /**
     * Ensure an object that represents a player is returned
     * as an {@link IArenaPlayer} object.
     *
     * @param player  The player object.
     */
    public IArenaPlayer get(Object player) {
        PreCon.notNull(player);

        return PVStarAPI.getArenaPlayer(player);
    }

    /**
     * Ensure an object that represents a player is returned
     * as an {@link Player} object.
     *
     * @param player  The player object.
     */
    public Player getBukkitPlayer(Object player) {
        PreCon.notNull(player);

        Player p = PlayerUtils.getPlayer(player);
        PreCon.notNull(p);

        return p;
    }

}
