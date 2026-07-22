package com.belarion.factions.filter;

import com.belarion.factions.config.TagsConfig;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Locale;

/**
 * Filtre les paquets de tchat SORTANTS (ceux que le serveur envoie a un client), y compris les
 * lignes generees par des commandes d'autres plugins comme {@code /f show} de SaberFactions, pour
 * en retirer certains textes indesirables.
 * <p>
 * Ce filtre ne touche JAMAIS a SaberFactions.jar (ni a aucun autre plugin) : exactement comme pour
 * la couleur personnalisee des noms de faction (voir
 * {@link com.belarion.factions.tags.FactionTagManager}), on intercepte uniquement le paquet reseau
 * juste avant qu'il n'atteigne le client, via ProtocolLib. Ca fonctionne quelle que soit l'origine
 * reelle du texte (fichier de configuration d'un autre plugin, ou texte fige dans son code),
 * puisqu'on n'agit qu'au niveau du paquet final envoye au joueur - jamais sur le plugin lui-meme.
 */
public final class OutgoingChatFilter extends PacketAdapter {

    private final TagsConfig config;

    public OutgoingChatFilter(Plugin plugin, TagsConfig config) {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.CHAT);
        this.config = config;
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (!config.isChatFilterEnabled()) {
            return;
        }

        WrappedChatComponent component = event.getPacket().getChatComponents().readSafely(0);
        if (component == null) {
            return;
        }
        String json = component.getJson();
        if (json == null || json.isEmpty()) {
            return;
        }

        String lowerJson = json.toLowerCase(Locale.ROOT);
        List<String> dropTriggers = config.getChatFilterDropIfContains();
        for (String trigger : dropTriggers) {
            if (trigger != null && !trigger.isEmpty() && lowerJson.contains(trigger.toLowerCase(Locale.ROOT))) {
                // Ligne entiere indesirable (ex: rappel "/setpaypal") : on annule tout le paquet,
                // le joueur ne la voit jamais.
                event.setCancelled(true);
                return;
            }
        }

        String updatedJson = json;
        boolean changed = false;
        for (String needle : config.getChatFilterStripSubstrings()) {
            if (needle != null && !needle.isEmpty() && updatedJson.contains(needle)) {
                // On retire uniquement ce fragment directement dans le JSON du composant (les
                // couleurs/formatage du reste du message sont donc preserves), plutot que de
                // reconstruire tout le texte en clair.
                updatedJson = updatedJson.replace(needle, "");
                changed = true;
            }
        }

        if (changed) {
            event.getPacket().getChatComponents().writeSafely(0, WrappedChatComponent.fromJson(updatedJson));
        }
    }
}
