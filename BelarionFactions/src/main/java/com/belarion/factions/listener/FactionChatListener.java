package com.belarion.factions.listener;

import com.belarion.factions.config.MainConfig;
import com.belarion.factions.config.TagsConfig;
import com.belarion.factions.integration.SaberFactionsHook;
import com.belarion.factions.points.BelarionPointsAPI;
import com.belarion.factions.points.FactionPointsProvider;
import com.belarion.factions.points.FactionRanking;
import com.belarion.factions.util.ColorUtil;
import com.belarion.factions.util.FactionDisplayUtil;
import com.massivecraft.factions.Faction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

/**
 * Ajoute, devant le format de tchat existant (grade + pseudo deja affiches par ailleurs, jamais
 * touches ici), le badge de classement Top 3 (si la faction y figure) et le tag de faction entre
 * crochets gris : "Badge &#91;Faction&#93; " + ce que montrait deja le tchat.
 * <p>
 * Comme pour le nametag au-dessus de la tete, la couleur du NOM de faction (pas les crochets, eux
 * toujours gris) depend de qui LIT le message : vert (own-color) si meme faction que l'auteur,
 * rouge (enemy-color) si faction ennemie, etc. Minecraft ne permettant pas nativement un texte de
 * tchat different par destinataire, le message par defaut est annule puis renvoye individuellement
 * a chaque destinataire deja prevu par l'evenement ({@link AsyncPlayerChatEvent#getRecipients()}),
 * afin de rester compatible avec d'eventuels filtrages deja en place (tchat local, etc.).
 * <p>
 * Priorite HIGH : on lit event.getFormat() une fois que d'autres plugins de tchat (grade, couleurs
 * de rang...) l'ont deja personnalise, pour ne faire qu'ajouter notre prefixe par-dessus sans rien
 * ecraser. ignoreCancelled=true : si un message est deja annule avant nous (joueur mute, filtre
 * anti-spam...), on n'y touche pas.
 */
public final class FactionChatListener implements Listener {

    private final MainConfig mainConfig;
    private final TagsConfig tagsConfig;

    public FactionChatListener(MainConfig mainConfig, TagsConfig tagsConfig) {
        this.mainConfig = mainConfig;
        this.tagsConfig = tagsConfig;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!tagsConfig.isChatTagEnabled()) {
            return;
        }

        Player sender = event.getPlayer();
        Faction faction = SaberFactionsHook.getFaction(sender);
        if (faction == null) {
            // Pas de faction (ou wilderness) : on ne touche a rien, le tchat existant reste identique.
            return;
        }

        String factionTag = faction.getTag();
        String badge = resolveBadge(faction);
        String format = event.getFormat();
        String message = event.getMessage();

        event.setCancelled(true);
        for (Player recipient : event.getRecipients()) {
            String nameColor = FactionDisplayUtil.resolveRelationColor(tagsConfig, recipient, sender);
            String tag = FactionDisplayUtil.buildBracketedTag(tagsConfig.getBracketColor(), nameColor, factionTag);
            String line = badge + tag + " " + String.format(format, sender.getDisplayName(), message);
            recipient.sendMessage(line);
        }
    }

    /**
     * @return le badge colore (ex: rouge ①) si la faction figure dans le Top 3, sinon une chaine
     * vide (rien affiche pour les factions hors Top 3, comme au-dessus de la tete).
     */
    private String resolveBadge(Faction faction) {
        if (!mainConfig.isTop3Enabled()) {
            return "";
        }
        FactionPointsProvider provider = BelarionPointsAPI.getProvider();
        if (provider == null) {
            return "";
        }
        List<FactionRanking> top3 = provider.getTopFactions(3);
        String comparisonTag = faction.getComparisonTag();
        for (FactionRanking ranking : top3) {
            if (ranking.getFactionId() != null && ranking.getFactionId().equalsIgnoreCase(comparisonTag)) {
                String color = mainConfig.getTop3Color(ranking.getRank());
                String badgeSymbol = mainConfig.getTop3Badge(ranking.getRank());
                return ColorUtil.colorize(color + badgeSymbol + "&r ");
            }
        }
        return "";
    }
}
