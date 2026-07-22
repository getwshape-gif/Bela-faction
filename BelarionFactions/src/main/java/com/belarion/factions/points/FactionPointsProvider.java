package com.belarion.factions.points;

import java.util.List;
import java.util.Map;

/**
 * Interface de branchement pour un systeme de points de faction.
 * <p>
 * Objectif (demande explicite du proprietaire du serveur) : le systeme de points n'existe pas
 * encore aujourd'hui. Cette interface permet au Top 3 (et a toute autre fonctionnalite) de
 * fonctionner des maintenant SANS points reels bases sur un mecanisme "en dur", et d'etre
 * reconnectee plus tard a un vrai plugin de points sans toucher au code du Top 3.
 * <p>
 * Un futur plugin de points n'a qu'a fournir sa propre implementation de cette interface et
 * l'enregistrer via {@link BelarionPointsAPI#setProvider(FactionPointsProvider)} : tout le reste
 * (affichage du Top 3, badges, couleurs) continue de fonctionner sans modification.
 */
public interface FactionPointsProvider {

    /**
     * @param factionId identifiant unique de la faction (Faction#getId() cote SaberFactions)
     * @return le nombre de points actuel de la faction (0 si inconnue)
     */
    int getPoints(String factionId);

    /**
     * Definit le total de points d'une faction.
     */
    void setPoints(String factionId, int points);

    /**
     * Ajoute (ou retire, si amount est negatif) des points a une faction.
     */
    void addPoints(String factionId, int amount);

    /**
     * @return une vue de tous les points connus, indexes par identifiant de faction.
     * Ne doit jamais etre modifie hors de l'implementation (copie defensive attendue).
     */
    Map<String, Integer> getAllPoints();

    /**
     * @param limit nombre maximum d'entrees a retourner (ex: 3 pour un Top 3)
     * @return le classement des factions par points decroissants, deja trie, deja limite a
     * {@code limit} entrees. Ne renvoie jamais null (liste vide si aucune donnee).
     */
    List<FactionRanking> getTopFactions(int limit);
}

