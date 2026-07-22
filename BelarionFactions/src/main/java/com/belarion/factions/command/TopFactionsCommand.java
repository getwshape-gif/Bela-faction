package com.belarion.factions.command;

import com.belarion.factions.config.MainConfig;
import com.belarion.factions.points.BelarionPointsAPI;
import com.belarion.factions.points.FactionPointsProvider;
import com.belarion.factions.points.FactionRanking;
import com.belarion.factions.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * /topfactions : affiche le Top 3 des factions (fonctionnalites 3 et 4 de la demande).
 * <p>
 * Les points affiches viennent uniquement de {@link FactionPointsProvider#getTopFactions(int)},
 * qui pourra plus tard etre fourni par un vrai plugin de points (voir {@link BelarionPointsAPI}).
 * Cette commande ne connait aucune source de donnees "en dur".
 */
public final class TopFactionsCommand implements CommandExecutor {

    private final MainConfig config;

    public TopFactionsCommand(MainConfig config) {
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!config.isTop3Enabled()) {
            sender.sendMessage(ColorUtil.colorize("&cLe classement des factions est desactive."));
            return true;
        }

        FactionPointsProvider provider = BelarionPointsAPI.getProvider();
        if (provider == null) {
            sender.sendMessage(ColorUtil.colorize("&cAucune source de points n'est configuree pour le moment."));
            return true;
        }

        List<FactionRanking> top = provider.getTopFactions(3);

        String header = config.getTop3Header();
        if (header != null && !header.isEmpty()) {
            sender.sendMessage(ColorUtil.colorize(header));
        }

        if (top.isEmpty()) {
            sender.sendMessage(ColorUtil.colorize(config.getTop3NoData()));
        } else {
            for (FactionRanking ranking : top) {
                String color = config.getTop3Color(ranking.getRank());
                String badge = config.getTop3Badge(ranking.getRank());
                String line = color + badge + " &f[" + ranking.getFactionTag() + "]"
                        + " &7(" + ranking.getPoints() + " pts)";
                sender.sendMessage(ColorUtil.colorize(line));
            }
        }

        String footer = config.getTop3Footer();
        if (footer != null && !footer.isEmpty()) {
            sender.sendMessage(ColorUtil.colorize(footer));
        }
        return true;
    }
}

