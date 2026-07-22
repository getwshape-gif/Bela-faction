package com.belarion.factions.listener;

import com.belarion.factions.tags.FactionTagManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerKickEvent;

/**
 * Nettoie l'armor stand de tag de faction d'un joueur quand il quitte le serveur, pour ne pas
 * laisser une entite fantome flotter a l'endroit ou il s'est deconnecte.
 */
public final class PlayerConnectionListener implements Listener {

    private final FactionTagManager tagManager;

    public PlayerConnectionListener(FactionTagManager tagManager) {
        this.tagManager = tagManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        tagManager.removeTag(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        tagManager.removeTag(player.getUniqueId());
    }
}

