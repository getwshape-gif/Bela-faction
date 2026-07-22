package com.belarion.factions;

import com.belarion.factions.command.FPointsCommand;
import com.belarion.factions.command.TagsAdminCommand;
import com.belarion.factions.command.TopFactionsCommand;
import com.belarion.factions.config.MainConfig;
import com.belarion.factions.config.TagsConfig;
import com.belarion.factions.listener.BlockedCommandListener;
import com.belarion.factions.listener.PlayerConnectionListener;
import com.belarion.factions.points.BelarionPointsAPI;
import com.belarion.factions.points.YamlFactionPointsProvider;
import com.belarion.factions.tags.FactionTagManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Point d'entree du plugin compagnon BelarionFactions.
 * <p>
 * Ce plugin ne remplace ni ne modifie SaberFactions.jar (le plugin "Factions" existant) : il se
 * contente de s'y accrocher via son API publique (voir {@link com.belarion.factions.integration.SaberFactionsHook})
 * pour ajouter, par-dessus, exactement les fonctionnalites demandees :
 * <ol>
 *     <li>Nom de faction affiche au-dessus du pseudo des joueurs ;</li>
 *     <li>Couleur du nom de faction selon la relation entre les joueurs (&a meme faction, &c ennemie) ;</li>
 *     <li>Structure de classement Top 3 des factions, prete a etre branchee sur un futur systeme de points ;</li>
 *     <li>Badges de classement colores (&4/&5/&6) ;</li>
 *     <li>Interface {@link com.belarion.factions.points.FactionPointsProvider} pour brancher plus
 *     tard un vrai plugin de points sans rien recompiler.</li>
 * </ol>
 * Le blocage de sous-commandes /f deja configure (plugins/BelarionFactions/config.yml) est aussi
 * reconduit a l'identique pour ne rien casser de ce qui fonctionnait deja.
 */
public final class BelarionFactionsPlugin extends JavaPlugin {

    private MainConfig mainConfig;
    private TagsConfig tagsConfig;
    private FactionTagManager tagManager;
    private YamlFactionPointsProvider pointsProvider;

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("Factions") == null) {
            getLogger().severe("SaberFactions (Factions) est introuvable : BelarionFactions se desactive.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
            getLogger().severe("ProtocolLib est introuvable : BelarionFactions se desactive. "
                    + "Installe ProtocolLib (https://www.spigotmc.org/resources/protocollib.1997/) puis redemarre.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.mainConfig = new MainConfig(this);
        this.tagsConfig = new TagsConfig(this);

        File pointsFile = new File(getDataFolder(), "points-factions.yml");
        this.pointsProvider = new YamlFactionPointsProvider(this, pointsFile);
        BelarionPointsAPI.setProvider(pointsProvider);

        this.tagManager = new FactionTagManager(this, tagsConfig);
        this.tagManager.start();

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerConnectionListener(tagManager), this);
        pluginManager.registerEvents(new BlockedCommandListener(mainConfig), this);

        getCommand("topfactions").setExecutor(new TopFactionsCommand(mainConfig));
        getCommand("fpoints").setExecutor(new FPointsCommand(mainConfig));
        getCommand("belariontags").setExecutor(new TagsAdminCommand(tagsConfig, tagManager));

        getLogger().info("BelarionFactions active : nametags de faction, Top 3, et compatibilite "
                + "avec plugins/BelarionFactions + plugins/BelarionFactionTags existants.");
    }

    @Override
    public void onDisable() {
        if (tagManager != null) {
            tagManager.shutdown();
        }
    }
}

