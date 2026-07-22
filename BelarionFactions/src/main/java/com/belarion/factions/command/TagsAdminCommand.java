package com.belarion.factions.command;

import com.belarion.factions.config.TagsConfig;
import com.belarion.factions.tags.FactionTagManager;
import com.belarion.factions.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * /belariontags reload : recharge plugins/BelarionFactionTags/config.yml a chaud.
 * /belariontags chattag <on|off> : active/desactive a chaud le tag de faction dans le tchat (ex:
 * "[bm]" devant le pseudo), sans avoir besoin d'un acces au systeme de fichiers du serveur.
 */
public final class TagsAdminCommand implements CommandExecutor {

    private final TagsConfig config;
    private final FactionTagManager tagManager;

    public TagsAdminCommand(TagsConfig config, FactionTagManager tagManager) {
        this.config = config;
        this.tagManager = tagManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("belarion.tags.admin")) {
            sender.sendMessage(ColorUtil.colorize("&cTu n'as pas la permission d'utiliser cette commande."));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            config.reload();
            tagManager.start();
            sender.sendMessage(ColorUtil.colorize(config.getReloadMessage()));
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("chattag")
                && (args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("off"))) {
            boolean enabled = args[1].equalsIgnoreCase("on");
            config.setChatTagEnabled(enabled);
            sender.sendMessage(ColorUtil.colorize(enabled
                    ? "&aLe tag de faction dans le tchat est maintenant &lactive&r&a."
                    : "&aLe tag de faction dans le tchat est maintenant &ldesactive&r&a."));
            return true;
        }

        sender.sendMessage(ColorUtil.colorize(
                "&eUtilisation : &f/belariontags reload &7ou &f/belariontags chattag <on|off>"));
        return true;
    }
}

