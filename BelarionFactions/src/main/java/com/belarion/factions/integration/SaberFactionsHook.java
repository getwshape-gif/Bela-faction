package com.belarion.factions.integration;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.struct.Relation;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Point d'acces unique vers l'API PUBLIQUE de SaberFactions.
 * <p>
 * Cette classe ne fait QUE lire des informations via l'API officielle de SaberFactions
 * (com.massivecraft.factions.*). Elle ne modifie jamais son etat interne, ne remplace aucune
 * classe, et ne touche pas au fichier SaberFactions.jar : c'est le mecanisme d'integration
 * "propre" recommande pour un plugin compagnon (au meme titre que Vault, PlaceholderAPI, etc.
 * qui sont deja references dans le plugin.yml de SaberFactions).
 */
public final class SaberFactionsHook {

    private SaberFactionsHook() {
    }

    /**
     * @return le FPlayer (joueur cote Factions) correspondant a ce joueur Bukkit, ou null si
     * l'API n'a pas encore d'entree pour lui (tres rare, uniquement dans une fenetre de connexion).
     */
    public static FPlayer getFPlayer(Player player) {
        if (player == null) {
            return null;
        }
        return FPlayers.getInstance().getByPlayer(player);
    }

    /**
     * @return la faction du joueur, ou null s'il n'est dans aucune faction (ou faction "wilderness").
     */
    public static Faction getFaction(Player player) {
        FPlayer fPlayer = getFPlayer(player);
        if (fPlayer == null) {
            return null;
        }
        Faction faction = fPlayer.getFaction();
        if (faction == null || !faction.isNormal()) {
            return null;
        }
        return faction;
    }

    /**
     * @return le tag/nom d'affichage de la faction du joueur (sans code couleur), ou null.
     */
    public static String getFactionTag(Player player) {
        Faction faction = getFaction(player);
        return faction == null ? null : faction.getTag();
    }

    /**
     * Calcule la relation entre deux joueurs (du point de vue de "viewer" regardant "target").
     *
     * @return la relation Factions (MEMBER, ALLY, TRUCE, NEUTRAL, ENEMY) ou null si l'un des deux
     * joueurs n'a pas d'entree Factions valide.
     */
    public static Relation getRelation(Player viewer, Player target) {
        FPlayer viewerFPlayer = getFPlayer(viewer);
        FPlayer targetFPlayer = getFPlayer(target);
        if (viewerFPlayer == null || targetFPlayer == null) {
            return null;
        }
        return viewerFPlayer.getRelationTo(targetFPlayer);
    }

    /**
     * @return toutes les factions "normales" actuellement enregistrees (exclut wilderness/safezone/warzone).
     */
    public static List<Faction> getAllNormalFactions() {
        List<Faction> result = new ArrayList<>();
        for (Faction faction : Factions.getInstance().getAllFactions()) {
            if (faction != null && faction.isNormal()) {
                result.add(faction);
            }
        }
        return result;
    }

    /**
     * @return la faction correspondant a cet identifiant unique Factions, ou null si elle n'existe
     * plus (ex: faction disparue depuis).
     */
    public static Faction getFactionById(String factionId) {
        if (factionId == null) {
            return null;
        }
        return Factions.getInstance().getFactionById(factionId);
    }
}

