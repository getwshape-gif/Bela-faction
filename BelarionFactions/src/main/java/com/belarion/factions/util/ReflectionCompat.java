package com.belarion.factions.util;

import org.bukkit.entity.ArmorStand;

import java.lang.reflect.Method;

/**
 * Petits appels reflectifs "best effort" pour des methodes qui n'existent pas forcement sur
 * toutes les versions/patchs de l'API Spigot 1.8 (ex: ArmorStand#setMarker, Entity#setGravity
 * n'ont ete officiellement ajoutees a l'API Bukkit qu'a partir de versions ulterieures a 1.8.8).
 * <p>
 * Si la methode n'existe pas, on ne fait rien : le plugin repose alors sur une re-teleportation
 * frequente (voir FactionTagManager) pour compenser visuellement l'absence de "no gravity".
 * Cela evite de faire planter la compilation ou le chargement du plugin sur un build de serveur
 * qui n'expose pas ces methodes.
 */
public final class ReflectionCompat {

    private ReflectionCompat() {
    }

    public static void trySetGravity(ArmorStand stand, boolean gravity) {
        try {
            Method method = stand.getClass().getMethod("setGravity", boolean.class);
            method.invoke(stand, gravity);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // API absente sur cette version : on se rabat sur la re-teleportation reguliere.
        }
    }

    public static void trySetMarker(ArmorStand stand, boolean marker) {
        try {
            Method method = stand.getClass().getMethod("setMarker", boolean.class);
            method.invoke(stand, marker);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // API absente sur cette version : la armor stand garde une (petite) hitbox.
        }
    }

    public static void trySetAI(ArmorStand stand, boolean ai) {
        try {
            Method method = stand.getClass().getMethod("setAI", boolean.class);
            method.invoke(stand, ai);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // Pas grave : une armor stand n'a de toute facon pas d'IA de deplacement.
        }
    }
}

