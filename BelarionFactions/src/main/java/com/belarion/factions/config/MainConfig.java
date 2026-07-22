package com.belarion.factions.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Configuration principale : reprend {@code plugins/BelarionFactions/config.yml} deja present
 * (prefixe, messages, commandes bloquees...) et y ajoute uniquement une nouvelle section "top3"
 * pour l'affichage du classement (badges/couleurs), sans toucher aux cles existantes.
 */
public final class MainConfig {

    private final Plugin plugin;
    private final File file;
    private YamlConfiguration config;

    public MainConfig(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "config.yml");
        reload();
    }

    public void reload() {
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (InputStreamReader defaults = new InputStreamReader(
                    plugin.getResource("default-factions-config.yml"), StandardCharsets.UTF_8)) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaults);
                defaultConfig.save(file);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Impossible de creer " + file.getPath(), e);
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);

        // Ajoute la section top3 si elle manque (ex: config existante d'avant ce plugin),
        // sans jamais ecraser une valeur deja presente.
        boolean changed = false;
        if (!config.isSet("top3.enabled")) {
            config.set("top3.enabled", true);
            changed = true;
        }
        if (!config.isSet("top3.badge-top1")) {
            config.set("top3.badge-top1", "①");
            changed = true;
        }
        if (!config.isSet("top3.badge-top2")) {
            config.set("top3.badge-top2", "②");
            changed = true;
        }
        if (!config.isSet("top3.badge-top3")) {
            config.set("top3.badge-top3", "③");
            changed = true;
        }
        if (!config.isSet("top3.color-top1")) {
            config.set("top3.color-top1", "&4");
            changed = true;
        }
        if (!config.isSet("top3.color-top2")) {
            config.set("top3.color-top2", "&5");
            changed = true;
        }
        if (!config.isSet("top3.color-top3")) {
            config.set("top3.color-top3", "&6");
            changed = true;
        }
        if (!config.isSet("top3.header")) {
            config.set("top3.header", "&8&m----------&8[ &6Top 3 Factions &8]&8&m----------");
            changed = true;
        }
        if (!config.isSet("top3.footer")) {
            config.set("top3.footer", "&8&m--------------------------------------------------");
            changed = true;
        }
        if (!config.isSet("top3.no-data")) {
            config.set("top3.no-data", "&7Aucune faction classee pour le moment.");
            changed = true;
        }
        if (!config.isSet("messages.faction-introuvable-points")) {
            config.set("messages.faction-introuvable-points", "&8[&8&lBELARION&8] &8» &8Faction introuvable.");
            changed = true;
        }
        if (!config.isSet("messages.usage-fpoints")) {
            config.set("messages.usage-fpoints",
                    "&8[&8&lBELARION&8] &8» &eUtilisation : &f/fpoints <add|remove|set> <faction> <montant>");
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

    public String getPrefix() {
        return config.getString("prefix", "");
    }

    public int getMaximumMembres() {
        return config.getInt("maximum-membres", 12);
    }

    public List<String> getBlockedCommands() {
        List<String> list = config.getStringList("commandes-bloquees");
        return list == null ? new ArrayList<>() : list;
    }

    public String getMessage(String key) {
        return config.getString("messages." + key, "");
    }

    public boolean isTop3Enabled() {
        return config.getBoolean("top3.enabled", true);
    }

    public String getTop3Badge(int rank) {
        switch (rank) {
            case 1:
                return config.getString("top3.badge-top1", "①");
            case 2:
                return config.getString("top3.badge-top2", "②");
            case 3:
                return config.getString("top3.badge-top3", "③");
            default:
                return "";
        }
    }

    public String getTop3Color(int rank) {
        switch (rank) {
            case 1:
                return config.getString("top3.color-top1", "&4");
            case 2:
                return config.getString("top3.color-top2", "&5");
            case 3:
                return config.getString("top3.color-top3", "&6");
            default:
                return "&7";
        }
    }

    public String getTop3Header() {
        return config.getString("top3.header", "");
    }

    public String getTop3Footer() {
        return config.getString("top3.footer", "");
    }

    public String getTop3NoData() {
        return config.getString("top3.no-data", "");
    }
}

