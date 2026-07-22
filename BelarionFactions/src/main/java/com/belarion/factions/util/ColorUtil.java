package com.belarion.factions.util;

import org.bukkit.ChatColor;

/**
 * Petits utilitaires de couleur partages par tout le plugin.
 */
public final class ColorUtil {

    private ColorUtil() {
    }

    /**
     * Traduit les codes '&x' en codes couleur Minecraft (section sign).
     * Ne fait rien de plus (pas de placeholders) : chaque appelant gere ses propres remplacements
     * avant d'appeler cette methode.
     */
    public static String colorize(String input) {
        if (input == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}

