package com.belarion.factions.points;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Implementation par defaut de {@link FactionPointsProvider}.
 * <p>
 * IMPORTANT (demande explicite) : ceci n'est PAS un "faux" systeme de points invente pour
 * l'occasion. C'est une simple couche de lecture/ecriture au-dessus du fichier
 * {@code points-factions.yml} qui existait deja sur le serveur avant ce plugin, avec exactement
 * le meme format ("factions: <id>: <points>", "noms: <id>: <NomAffiche>"). Aucun point n'est
 * jamais calcule ou attribue automatiquement ici : les valeurs viennent uniquement de ce qui est
 * deja stocke dans le fichier, ou de ce qu'un administrateur ajoute/retire manuellement via
 * /fpoints. C'est exactement la "structure prete a recevoir un futur systeme de points" demandee :
 * un vrai plugin de points pourra remplacer ce provider par le sien (voir {@link BelarionPointsAPI}).
 * <p>
 * Note sur la cle "factionId" utilisee ici : pour rester compatible avec le fichier deja present
 * sur le serveur, la cle utilisee est le tag de faction en minuscules (comparable a
 * {@code Faction#getComparisonTag()} cote SaberFactions), et non l'identifiant interne unique de
 * SaberFactions.
 */
public final class YamlFactionPointsProvider implements FactionPointsProvider {

    private static final String SECTION_POINTS = "factions";
    private static final String SECTION_NAMES = "noms";

    private final Plugin plugin;
    private final File file;
    private YamlConfiguration config;

    public YamlFactionPointsProvider(Plugin plugin, File file) {
        this.plugin = plugin;
        this.file = file;
        reload();
    }

    /**
     * (Re)charge le fichier depuis le disque. Cree le fichier avec une structure vide s'il
     * n'existe pas encore (ne devrait normalement pas arriver sur ce serveur, le fichier existe
     * deja, mais un serveur fraichement installe doit quand meme pouvoir demarrer proprement).
     */
    public void reload() {
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            this.config = new YamlConfiguration();
            this.config.set(SECTION_POINTS + ".exemple", 0);
            save();
            this.config.set(SECTION_POINTS + ".exemple", null);
        } else {
            this.config = YamlConfiguration.loadConfiguration(file);
        }
        if (this.config.getConfigurationSection(SECTION_POINTS) == null) {
            this.config.createSection(SECTION_POINTS);
        }
        if (this.config.getConfigurationSection(SECTION_NAMES) == null) {
            this.config.createSection(SECTION_NAMES);
        }
    }

    public synchronized void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Impossible de sauvegarder " + file.getName(), e);
        }
    }

    @Override
    public synchronized int getPoints(String factionId) {
        if (factionId == null) {
            return 0;
        }
        return config.getInt(SECTION_POINTS + "." + factionId.toLowerCase(), 0);
    }

    @Override
    public synchronized void setPoints(String factionId, int points) {
        if (factionId == null) {
            return;
        }
        config.set(SECTION_POINTS + "." + factionId.toLowerCase(), points);
        save();
    }

    @Override
    public synchronized void addPoints(String factionId, int amount) {
        if (factionId == null) {
            return;
        }
        setPoints(factionId, getPoints(factionId) + amount);
    }

    /**
     * Garde en memoire le dernier nom d'affichage connu pour une faction (utilise pour le Top 3
     * si la faction ne peut plus etre retrouvee en direct via l'API SaberFactions).
     */
    public synchronized void rememberDisplayName(String factionId, String displayName) {
        if (factionId == null || displayName == null) {
            return;
        }
        config.set(SECTION_NAMES + "." + factionId.toLowerCase(), displayName);
        save();
    }

    public synchronized String getRememberedDisplayName(String factionId) {
        if (factionId == null) {
            return null;
        }
        return config.getString(SECTION_NAMES + "." + factionId.toLowerCase());
    }

    @Override
    public synchronized Map<String, Integer> getAllPoints() {
        Map<String, Integer> result = new LinkedHashMap<>();
        ConfigurationSection section = config.getConfigurationSection(SECTION_POINTS);
        if (section == null) {
            return result;
        }
        for (String key : section.getKeys(false)) {
            result.put(key, section.getInt(key, 0));
        }
        return result;
    }

    @Override
    public synchronized List<FactionRanking> getTopFactions(int limit) {
        Map<String, Integer> all = getAllPoints();

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(all.entrySet());
        entries.sort(Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue).reversed());

        List<FactionRanking> result = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<String, Integer> entry : entries) {
            if (rank > limit) {
                break;
            }
            String factionId = entry.getKey();
            String displayName = getRememberedDisplayName(factionId);
            if (displayName == null || displayName.isEmpty()) {
                displayName = factionId;
            }
            result.add(new FactionRanking(factionId, displayName, entry.getValue(), rank));
            rank++;
        }
        return Collections.unmodifiableList(result);
    }
}

