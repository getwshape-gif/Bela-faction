package com.belarion.factions.points;

/**
 * Registre statique et remplacable du {@link FactionPointsProvider} actif.
 * <p>
 * Par defaut, BelarionFactions installe {@link YamlFactionPointsProvider} (lecture/ecriture de
 * points-factions.yml, sans aucun calcul automatique de points : uniquement ce qui est deja
 * stocke ou ajoute/retire manuellement). Le jour ou un vrai plugin de points est developpe, il
 * suffit d'appeler :
 * <pre>
 *     BelarionPointsAPI.setProvider(monProviderDePoints);
 * </pre>
 * pour que le Top 3 (et toute autre fonctionnalite basee sur les points) utilise immediatement
 * la nouvelle source de donnees, sans recompiler ni modifier BelarionFactions.
 */
public final class BelarionPointsAPI {

    private static volatile FactionPointsProvider provider;

    private BelarionPointsAPI() {
    }

    public static FactionPointsProvider getProvider() {
        return provider;
    }

    public static void setProvider(FactionPointsProvider newProvider) {
        if (newProvider == null) {
            throw new IllegalArgumentException("newProvider ne peut pas etre null");
        }
        provider = newProvider;
    }
}

