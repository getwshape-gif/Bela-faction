package com.belarion.factions.command;

import com.belarion.factions.config.MainConfig;
import com.belarion.factions.points.BelarionPointsAPI;
import com.belarion.factions.points.FactionPointsProvider;
import com.belarion.factions.points.YamlFactionPointsProvider;
import com.belarion.factions.util.ColorUtil;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Locale;

/**
 * /fpoints <add|remove|set> <faction> <montant> : administration manuelle des points de faction,
 * en attendant qu'un vrai plugin de points existe. Reutilise les messages deja presents dans
 * plugins/BelarionFactions/config.yml (points-ajoutes / points-retires).
 */
public final class FPointsCommand implements CommandExecutor {

    private final MainConfig config;

    public FPointsCommand(MainConfig config) {
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("belarion.points.admin")) {
            sender.sendMessage(ColorUtil.colorize("&cTu n'as pas la permission d'utiliser cette commande."));
            return true;
        }

        if (args.length != 3) {
            sender.sendMessage(ColorUtil.colorize(config.getMessage("usage-fpoints")));
            return true;
        }

        String action = args[0].toLowerCase(Locale.ROOT);
        String factionTagArg = args[1];
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtil.colorize(config.getMessage("usage-fpoints")));
            return true;
        }

        Faction faction = Factions.getInstance().getByTag(factionTagArg);
        if (faction == null || !faction.isNormal()) {
            sender.sendMessage(ColorUtil.colorize(config.getMessage("faction-introuvable-points")));
            return true;
        }

        FactionPointsProvider provider = BelarionPointsAPI.getProvider();
        if (provider == null) {
            sender.sendMessage(ColorUtil.colorize("&cAucune source de points n'est configuree pour le moment."));
            return true;
        }

        String factionKey = faction.getComparisonTag();
        if (provider instanceof YamlFactionPointsProvider) {
            ((YamlFactionPointsProvider) provider).rememberDisplayName(factionKey, faction.getTag());
        }

        switch (action) {
            case "add":
                provider.addPoints(factionKey, amount);
                sender.sendMessage(ColorUtil.colorize(config.getMessage("points-ajoutes")
                        .replace("%points%", String.valueOf(amount))
                        .replace("%faction%", faction.getTag())));
                break;
            case "remove":
                provider.addPoints(factionKey, -amount);
                sender.sendMessage(ColorUtil.colorize(config.getMessage("points-retires")
                        .replace("%points%", String.valueOf(amount))
                        .replace("%faction%", faction.getTag())));
                break;
            case "set":
                provider.setPoints(factionKey, amount);
                sender.sendMessage(ColorUtil.colorize("&aPoints de &f" + faction.getTag() + "&a fixes a &f" + amount + "&a."));
                break;
            default:
                sender.sendMessage(ColorUtil.colorize(config.getMessage("usage-fpoints")));
                break;
        }
        return true;
    }
}

