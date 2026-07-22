package com.belarion.factions.listener;

import com.belarion.factions.config.MainConfig;
import com.belarion.factions.util.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Reconduit le blocage de sous-commandes /f deja configure dans
 * plugins/BelarionFactions/config.yml (cle "commandes-bloquees"), avec le meme message qu'avant.
 * <p>
 * Ne touche a AUCUNE autre commande : seules les sous-commandes explicitement listees dans la
 * configuration sont interceptees, tout le reste de /f (et toutes les autres commandes du
 * serveur) continue de fonctionner exactement comme avant ce plugin.
 */
public final class BlockedCommandListener implements Listener {

    // Alias connus de la commande principale de SaberFactions (voir son plugin.yml : name
    // "factions", alias "f"). Si votre serveur ajoute d'autres alias via un autre plugin de
    // commandes, ajoutez-les ici.
    private static final Set<String> FACTION_COMMAND_LABELS = new HashSet<>();

    static {
        FACTION_COMMAND_LABELS.add("f");
        FACTION_COMMAND_LABELS.add("factions");
    }

    private final MainConfig config;

    public BlockedCommandListener(MainConfig config) {
        this.config = config;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (message.isEmpty() || message.charAt(0) != '/') {
            return;
        }

        String[] parts = message.substring(1).split("\\s+");
        if (parts.length < 2) {
            return;
        }

        String label = parts[0].toLowerCase(Locale.ROOT);
        if (!FACTION_COMMAND_LABELS.contains(label)) {
            return;
        }

        String subCommand = parts[1].toLowerCase(Locale.ROOT);
        List<String> blocked = config.getBlockedCommands();
        for (String entry : blocked) {
            if (entry != null && entry.trim().toLowerCase(Locale.ROOT).equals(subCommand)) {
                event.setCancelled(true);
                Player player = event.getPlayer();
                String msg = config.getMessage("commande-bloquee");
                if (msg != null && !msg.isEmpty()) {
                    player.sendMessage(ColorUtil.colorize(msg));
                }
                return;
            }
        }
    }
}

