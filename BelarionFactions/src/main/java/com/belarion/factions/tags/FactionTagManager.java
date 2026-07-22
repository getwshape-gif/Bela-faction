package com.belarion.factions.tags;

import com.belarion.factions.config.TagsConfig;
import com.belarion.factions.integration.SaberFactionsHook;
import com.belarion.factions.util.FactionDisplayUtil;
import com.belarion.factions.util.ReflectionCompat;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Gere l'affichage du nom de faction au-dessus du pseudo des joueurs (fonctionnalites 1 et 2 de
 * la demande) :
 * <ul>
 *     <li>Un ArmorStand invisible (marqueur) est maintenu au-dessus de chaque joueur en ligne,
 *     un peu plus haut que son pseudo habituel (qui, lui, n'est jamais touche).</li>
 *     <li>La couleur du texte affiche depend de la relation entre celui qui REGARDE et celui qui
 *     est affiche : &a (own-color) si meme faction, &c (enemy-color) si faction ennemie. Comme
 *     Minecraft ne permet pas nativement d'afficher un texte different selon qui regarde, on
 *     utilise ProtocolLib pour envoyer, a chaque joueur, un paquet de metadonnees personnalise
 *     pour cette entite (le texte au-dessus de la tete reste identique pour tout le monde en
 *     apparence globale, mais sa couleur differe reellement d'un joueur a l'autre).</li>
 * </ul>
 * SaberFactions n'est jamais modifie : seules ses methodes publiques (voir
 * {@link SaberFactionsHook}) sont utilisees pour connaitre faction et relation.
 */
public final class FactionTagManager {

    // Index du DataWatcher (protocole 1.8) pour les champs communs a toute Entity.
    private static final int WATCHER_INDEX_CUSTOM_NAME = 2;
    private static final int WATCHER_INDEX_CUSTOM_NAME_VISIBLE = 3;

    private final Plugin plugin;
    private final TagsConfig config;
    private final ProtocolManager protocolManager;

    private final Map<UUID, ArmorStand> armorStands = new HashMap<>();
    private BukkitTask task;

    public FactionTagManager(Plugin plugin, TagsConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    public void start() {
        stop();
        int period = config.getUpdateTicks();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(plugin, period, period);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * Supprime proprement toutes les armor stands geres par ce plugin (appelee au onDisable, et
     * quand un joueur se deconnecte pour son propre tag).
     */
    public void shutdown() {
        stop();
        for (ArmorStand stand : armorStands.values()) {
            if (stand != null && !stand.isDead()) {
                stand.remove();
            }
        }
        armorStands.clear();
    }

    public void removeTag(UUID playerId) {
        ArmorStand stand = armorStands.remove(playerId);
        if (stand != null && !stand.isDead()) {
            stand.remove();
        }
    }

    private void tick() {
        for (Player target : plugin.getServer().getOnlinePlayers()) {
            updateTargetTag(target);
        }
        // Nettoyage des armor stands orphelines (joueur deconnecte entre deux ticks sans passer
        // par PlayerQuitEvent, ex: crash serveur / kick tres precoce).
        armorStands.keySet().removeIf(uuid -> plugin.getServer().getPlayer(uuid) == null);
    }

    private void updateTargetTag(Player target) {
        String factionTag = SaberFactionsHook.getFactionTag(target);

        ArmorStand stand = getOrCreateStand(target);
        if (stand == null) {
            return;
        }
        stand.teleport(computeTagLocation(target));

        if (factionTag == null) {
            // Pas de faction (ou wilderness) : rien a afficher au-dessus de la tete.
            stand.setCustomNameVisible(false);
            return;
        }

        if (!config.isPerViewerColorsEnabled()) {
            // Mode degrade : une seule couleur pour tout le monde, sans ProtocolLib.
            stand.setCustomName(FactionDisplayUtil.buildBracketedTag(
                    config.getBracketColor(), config.getOwnColor(), factionTag));
            stand.setCustomNameVisible(true);
            return;
        }

        // Le nom "de base" (visible par defaut / pour les cas non couverts par le paquet
        // personnalise, ex: joueur sans faction qui regarde) reste sur une couleur neutre.
        stand.setCustomName(FactionDisplayUtil.buildBracketedTag(
                config.getBracketColor(), config.getNeutralColor(), factionTag));
        stand.setCustomNameVisible(true);

        int distance = config.getDistance();
        int entityId = stand.getEntityId();
        Location targetLocation = target.getLocation();

        for (Player viewer : plugin.getServer().getOnlinePlayers()) {
            if (distance > 0 && !viewer.getWorld().equals(target.getWorld())) {
                continue;
            }
            if (distance > 0 && viewer.getLocation().distanceSquared(targetLocation) > (double) distance * distance) {
                continue;
            }
            String colored = buildColoredTag(viewer, target, factionTag);
            if (colored != null) {
                sendPersonalizedName(viewer, entityId, colored);
            }
        }
    }

    private String buildColoredTag(Player viewer, Player target, String factionTag) {
        String nameColor = FactionDisplayUtil.resolveRelationColor(config, viewer, target);
        return FactionDisplayUtil.buildBracketedTag(config.getBracketColor(), nameColor, factionTag);
    }

    private ArmorStand getOrCreateStand(Player target) {
        ArmorStand stand = armorStands.get(target.getUniqueId());
        if (stand != null && !stand.isDead() && stand.getWorld().equals(target.getWorld())) {
            return stand;
        }
        if (stand != null) {
            // Monde different ou entite invalide : on la recree proprement.
            if (!stand.isDead()) {
                stand.remove();
            }
            armorStands.remove(target.getUniqueId());
        }

        Location location = computeTagLocation(target);
        ArmorStand created = (ArmorStand) target.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        created.setVisible(false);
        created.setSmall(true);
        created.setArms(false);
        created.setBasePlate(false);
        created.setCustomNameVisible(false);
        created.setCanPickupItems(false);
        created.setRemoveWhenFarAway(false);
        ReflectionCompat.trySetGravity(created, false);
        ReflectionCompat.trySetMarker(created, true);
        ReflectionCompat.trySetAI(created, false);

        armorStands.put(target.getUniqueId(), created);
        return created;
    }

    private Location computeTagLocation(Player target) {
        Location location = target.getLocation().clone();
        location.setY(location.getY() + config.getHeightOffset());
        return location;
    }

    private void sendPersonalizedName(Player viewer, int entityId, String coloredName) {
        try {
            WrappedDataWatcher watcher = new WrappedDataWatcher();
            watcher.setObject(WATCHER_INDEX_CUSTOM_NAME, coloredName);
            watcher.setObject(WATCHER_INDEX_CUSTOM_NAME_VISIBLE, (byte) 1);

            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
            packet.getIntegers().write(0, entityId);
            packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

            protocolManager.sendServerPacket(viewer, packet);
        } catch (RuntimeException e) {
            // ProtocolLib recent (5.x) ne declare plus d'exception verifiee ici, mais peut
            // toujours lever une exception non verifiee (joueur deconnecte entre-temps, etc.).
            plugin.getLogger().log(Level.WARNING, "Impossible d'envoyer le paquet de nametag a "
                    + viewer.getName(), e);
        }
    }
}
