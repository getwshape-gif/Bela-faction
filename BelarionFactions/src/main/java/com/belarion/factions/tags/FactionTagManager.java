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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Gere l'affichage du nom de faction au-dessus du pseudo des joueurs (fonctionnalites 1 et 2 de
 * la demande) :
 * <ul>
 *     <li>Deux ArmorStand invisibles sont attaches au joueur comme MONTURE ("passenger") plutot
 *     que re-teleportes manuellement a chaque tick : joueur -> support (porteur, sans nom,
 *     donne juste la hauteur) -> tag (porte le nom de faction). Le client Minecraft synchronise
 *     alors nativement la position de toute la chaine avec celle du joueur, EXACTEMENT comme le
 *     pseudo lui-meme, sans le moindre decalage/tremblement visible (contrairement a l'ancienne
 *     methode qui re-teleportait l'entite toutes les {@code update-ticks} ticks et donnait
 *     l'impression d'un hologramme qui "flotte" independamment).</li>
 *     <li>La couleur du texte affiche depend de la relation entre celui qui REGARDE et celui qui
 *     est affiche : &a (own-color) si meme faction, &c (enemy-color) si faction ennemie. Comme
 *     Minecraft ne permet pas nativement d'afficher un texte different selon qui regarde, on
 *     utilise ProtocolLib pour envoyer, a chaque joueur, un paquet de metadonnees personalise
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

    // "tag" = l'armor stand visible qui porte le nom de faction (celle dont on envoie l'ID aux
    // paquets ProtocolLib). "base" = l'armor stand invisible et sans nom qui sert uniquement de
    // support pour donner de la hauteur (le joueur porte "base", qui porte "tag").
    private final Map<UUID, ArmorStand> armorStands = new HashMap<>();
    private final Map<UUID, ArmorStand> baseStands = new HashMap<>();
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
        for (ArmorStand stand : baseStands.values()) {
            if (stand != null && !stand.isDead()) {
                stand.remove();
            }
        }
        baseStands.clear();
    }

    public void removeTag(UUID playerId) {
        ArmorStand tagStand = armorStands.remove(playerId);
        if (tagStand != null && !tagStand.isDead()) {
            tagStand.remove();
        }
        ArmorStand baseStand = baseStands.remove(playerId);
        if (baseStand != null && !baseStand.isDead()) {
            baseStand.remove();
        }
    }

    private void tick() {
        for (Player target : plugin.getServer().getOnlinePlayers()) {
            updateTargetTag(target);
        }
        // Nettoyage des armor stands orphelines (joueur deconnecte entre deux ticks sans passer
        // par PlayerQuitEvent, ex: crash serveur / kick tres precoce).
        Set<UUID> orphans = new HashSet<>();
        orphans.addAll(armorStands.keySet());
        orphans.addAll(baseStands.keySet());
        for (UUID uuid : orphans) {
            if (plugin.getServer().getPlayer(uuid) == null) {
                removeTag(uuid);
            }
        }
    }

    private void updateTargetTag(Player target) {
        String factionTag = SaberFactionsHook.getFactionTag(target);

        ArmorStand stand = getOrCreateStand(target);
        if (stand == null) {
            return;
        }

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

    /**
     * Recree la chaine "joueur -> support -> tag" si besoin (premiere fois, monde change, ou une
     * des deux entites a disparu), puis s'assure qu'elle est bien attachee comme monture avant de
     * renvoyer l'armor stand visible (celle qui porte le nom). Attacher les armor stands comme
     * "passenger" plutot que les teleporter a chaque tick est ce qui rend le tag parfaitement
     * stable : le client suit alors la position du joueur nativement, comme pour son pseudo.
     */
    private ArmorStand getOrCreateStand(Player target) {
        UUID id = target.getUniqueId();
        ArmorStand base = baseStands.get(id);
        ArmorStand tag = armorStands.get(id);

        boolean invalid = base == null || tag == null || base.isDead() || tag.isDead()
                || !base.getWorld().equals(target.getWorld());

        if (invalid) {
            removeTag(id);

            Location spawnLocation = target.getLocation();

            // Support invisible et sans nom : sa seule utilite est sa hauteur normale (pas
            // "small"), ce qui place le tag qu'il porte au-dessus du pseudo du joueur plutot que
            // colle dessus.
            base = (ArmorStand) target.getWorld().spawnEntity(spawnLocation, EntityType.ARMOR_STAND);
            configureCarrierStand(base, false);

            // Armor stand qui porte reellement le nom de faction affiche.
            tag = (ArmorStand) target.getWorld().spawnEntity(spawnLocation, EntityType.ARMOR_STAND);
            configureCarrierStand(tag, true);

            baseStands.put(id, base);
            armorStands.put(id, tag);
        }

        if (!base.equals(target.getPassenger())) {
            target.setPassenger(base);
        }
        if (!tag.equals(base.getPassenger())) {
            base.setPassenger(tag);
        }

        return tag;
    }

    private void configureCarrierStand(ArmorStand stand, boolean small) {
        stand.setVisible(false);
        stand.setSmall(small);
        stand.setArms(false);
        stand.setBasePlate(false);
        stand.setCustomNameVisible(false);
        stand.setCanPickupItems(false);
        stand.setRemoveWhenFarAway(false);
        ReflectionCompat.trySetGravity(stand, false);
        ReflectionCompat.trySetMarker(stand, true);
        ReflectionCompat.trySetAI(stand, false);
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
