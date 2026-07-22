package com.belarion.factions.command;

import com.belarion.factions.config.TagsConfig;
import com.belarion.factions.tags.FactionTagManager;
import com.belarion.factions.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * /belariontags reload : recharge plugins/BelarionFactionTags/config.yml a chaud.
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

        if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(ColorUtil.colorize("&eUtilisation : &f/belariontags reload"));
            return true;
        }

        config.reload();
        tagManager.start();
        sender.sendMessage(ColorUtil.colorize(config.getReloadMessage()));
        return true;
    }
}

