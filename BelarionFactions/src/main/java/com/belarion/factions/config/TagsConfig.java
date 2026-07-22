package com.belarion.factions.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

/**
 * Configuration de l'affichage du nom de faction au-dessus du pseudo.
 * <p>
 * Lit/ecrit volontairement {@code plugins/BelarionFactionTags/config.yml} (et pas le dossier de
 * donnees par defaut de ce plugin) afin de reutiliser tel quel le fichier de configuration deja
 * present et deja utilise sur ce serveur (distance, update-ticks, height-offset, couleurs...).
 * Aucune valeur existante n'est perdue ou reinitialisee.
 */
public final class TagsConfig {

    private final Plugin plugin;
    private final File file;
    private YamlConfiguration config;

    public TagsConfig(Plugin plugin) {
        this.plugin = plugin;
        // plugins/BelarionFactionTags/config.yml, a cote de plugins/BelarionFactions (ce plugin)
        File pluginsFolder = plugin.getDataFolder().getParentFile();
        this.file = new File(pluginsFolder, "BelarionFactionTags" + File.separator + "config.yml");
        reload();
    }

    public void reload() {
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (InputStream defaults = plugin.getResource("default-tags-config.yml")) {
                if (defaults != null) {
                    YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                            new java.io.InputStreamReader(defaults, java.nio.charset.StandardCharsets.UTF_8));
                    defaultConfig.save(file);
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Impossible de creer " + file.getPath(), e);
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);

        // Ajoute les nouvelles cles si elles manquent (ex: config existante d'avant l'ajout de ces
        // options), sans jamais ecraser une valeur deja presente.
        boolean changed = false;
        if (!config.isSet("bracket-color")) {
            config.set("bracket-color", "&8");
            changed = true;
        }
        if (!config.isSet("chat-tag-enabled")) {
            config.set("chat-tag-enabled", true);
            changed = true;
        }
        if (changed) {
            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Impossible de sauvegarder " + file.getPath(), e);
            }
        }
    }

    public int getDistance() {
        return config.getInt("distance", 48);
    }

    public int getUpdateTicks() {
        return Math.max(1, config.getInt("update-ticks", 5));
    }

    public double getHeightOffset() {
        return config.getDouble("height-offset", 2.65);
    }

    public String getOwnColor() {
        return config.getString("own-color", "&a");
    }

    public String getEnemyColor() {
        return config.getString("enemy-color", "&c");
    }

    public String getAllyColor() {
        return config.getString("ally-color", "&f");
    }

    public String getTruceColor() {
        return config.getString("truce-color", "&f");
    }

    public String getNeutralColor() {
        return config.getString("neutral-color", "&f");
    }

    /**
     * Si false, on desactive l'envoi de paquets personnalises par joueur (ProtocolLib) et on
     * affiche une couleur unique (own-color) pour tout le monde. Permet de degrader proprement
     * le plugin si ProtocolLib venait a poser probleme, sans devoir le desinstaller.
     */
    public boolean isPerViewerColorsEnabled() {
        return config.getBoolean("per-viewer-colors", true);
    }

    public String getReloadMessage() {
        return config.getString("reload-message", "&aLes noms de factions ont ete recharges.");
    }

    /**
     * Couleur des crochets "[" et "]" entourant le nom de faction (au-dessus de la tete ET dans
     * le tchat). Le nom de la faction lui-meme garde sa propre couleur (selon la relation) ;
     * seuls les crochets utilisent cette couleur.
     */
    public String getBracketColor() {
        return config.getString("bracket-color", "&8");
    }

    /**
     * Si false, desactive uniquement l'ajout du badge + tag de faction dans le tchat (le nametag
     * au-dessus de la tete continue de fonctionner normalement).
     */
    public boolean isChatTagEnabled() {
        return config.getBoolean("chat-tag-enabled", true);
    }
}

