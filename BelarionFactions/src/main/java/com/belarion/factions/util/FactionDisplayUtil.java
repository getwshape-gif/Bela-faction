package com.belarion.factions.util;

import com.belarion.factions.config.TagsConfig;
import com.belarion.factions.integration.SaberFactionsHook;
import com.massivecraft.factions.struct.Relation;
import org.bukkit.entity.Player;

/**
 * Logique de couleur/format du tag de faction partagee entre l'affichage au-dessus de la tete
 * (voir FactionTagManager) et l'affichage dans le tchat (voir FactionChatListener), pour que les
 * deux restent strictement coherents et que la couleur des crochets (grise par defaut) ne soit
 * definie qu'a un seul endroit.
 */
public final class FactionDisplayUtil {

    private FactionDisplayUtil() {
    }

    /**
     * @return la couleur a utiliser pour le NOM de la faction (pas les crochets), du point de vue
     * de "viewer" qui regarde/lit "target" : own-color si meme faction (y compris si viewer ==
     * target), enemy-color si ennemie, etc.
     */
    public static String resolveRelationColor(TagsConfig config, Player viewer, Player target) {
        if (viewer.getUniqueId().equals(target.getUniqueId())) {
            return config.getOwnColor();
        }

        Relation relation = SaberFactionsHook.getRelation(viewer, target);
        if (relation == null) {
            return config.getNeutralColor();
        } else if (relation.isMember()) {
            return config.getOwnColor();
        } else if (relation.isEnemy()) {
            return config.getEnemyColor();
        } else if (relation.isAlly()) {
            return config.getAllyColor();
        } else if (relation.isTruce()) {
            return config.getTruceColor();
        }
        return config.getNeutralColor();
    }

    /**
     * @return le tag de faction complet "&#91;NomFaction&#93;", avec les crochets dans
     * {@code bracketColor} (gris par defaut, &8) et le nom de la faction dans {@code nameColor}
     * (colore selon la relation, voir {@link #resolveRelationColor}).
     */
    public static String buildBracketedTag(String bracketColor, String nameColor, String factionTag) {
        return ColorUtil.colorize(bracketColor + "[" + nameColor + factionTag + bracketColor + "]");
    }
}
