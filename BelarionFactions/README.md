# BelarionFactions

Plugin compagnon pour **SaberFactions** (serveur PvP Faction 1.8 Belarion).

Ce plugin **ne modifie jamais `SaberFactions.jar`** : il s'y accroche uniquement via son API
publique (`com.massivecraft.factions.*`), exactement comme le font Vault, PlaceholderAPI, etc.

## Fonctionnalites

1. **Nom de faction au-dessus du pseudo** — un tag flottant colore (`[NomFaction]`), affiche
   au-dessus du pseudo habituel du joueur (qui n'est jamais modifie). Les crochets `[` `]` sont
   toujours gris (`&8`, configurable via `bracket-color`) ; seul le nom de la faction a l'interieur
   change de couleur.
2. **Couleur selon la relation** — `&a` (vert) si le joueur qui regarde est dans la meme faction,
   `&c` (rouge) s'il est dans une faction ennemie. Necessite [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)
   pour envoyer une couleur differente a chaque joueur qui regarde.
3. **Top 3 des factions** (`/topfactions`, alias `/ftop`) — classement pret a etre branche sur un
   futur systeme de points (voir `FactionPointsProvider`). Aucune donnee n'est inventee : les
   points affiches viennent de `points-factions.yml` (deja present sur le serveur) ou de
   `/fpoints add|remove|set` en attendant un vrai plugin de points.
4. **Badges de classement colores** — rond numerote (`①`/`②`/`③`, personnalisable) colore
   `&4`/`&5`/`&6` pour les 3 premieres places.
5. **Interface branchable** — `com.belarion.factions.points.FactionPointsProvider` +
   `BelarionPointsAPI.setProvider(...)` permettent a un futur plugin de points de remplacer la
   source de donnees par defaut sans toucher a ce plugin.
6. **Badge + tag de faction dans le tchat** — quand un joueur d'une faction parle, le tchat affiche
   en plus, juste devant le message deja existant : `Badge [NomFaction] ` + le format de tchat
   habituel (grade, pseudo, message...). Le badge Top 3 (`①`/`②`/`③`) n'apparait que si la faction
   du joueur est dans le Top 3, sinon rien n'est ajoute devant les crochets. Les crochets restent
   gris et le nom de la faction est colore selon la relation, exactement comme au-dessus de la tete
   (vert si meme faction que le lecteur, rouge si ennemie, etc. — chaque joueur voit donc une
   couleur potentiellement differente pour le meme message). Le grade affiche en jeu (deja gere par
   un autre mecanisme existant du serveur) n'est jamais touche ni recalcule par cette fonctionnalite
   : elle se contente d'ajouter son prefixe devant le format de tchat deja en place. Desactivable
   independamment du tag au-dessus de la tete via `chat-tag-enabled: false` dans
   `plugins/BelarionFactionTags/config.yml`.

Le blocage des sous-commandes `/f` deja configure (`commandes-bloquees` dans
`plugins/BelarionFactions/config.yml`) est reconduit a l'identique.

**Non reconstruit** (hors perimetre de la demande, et non recuperable sans le jar d'origine) : les
anciennes commandes personnalisees `/f chef` et `/f mod` de l'ancien `BelarionFactions.jar`. Si ces
commandes te manquent, garde l'ancien jar installe pour cette seule partie, ou envoie-le pour que
je les reintegre precisement.

## Compatibilite des donnees existantes

Ce plugin lit/ecrit exactement les memes fichiers qu'avant, aux memes emplacements :

- `plugins/BelarionFactionTags/config.yml` (distance, update-ticks, height-offset, own-color, enemy-color...)
- `plugins/BelarionFactions/config.yml` (prefix, messages, commandes-bloquees...)
- `plugins/BelarionFactions/points-factions.yml` (points de faction)

Aucune donnee existante n'est perdue ni reinitialisee a l'installation.

## Installation

**Prerequis important : Java 17 ou plus recent sur le serveur.** La derniere version de
ProtocolLib (5.4.0, seule version encore publiee et maintenue par son auteur) est distribuee en
class-file Java 17, meme si elle continue de supporter le protocole Minecraft 1.8 a 1.21.8. Un
serveur qui tourne encore sous Java 8 doit d'abord etre mis a jour vers Java 17+ (le noyau Spigot
1.8.8 lui-meme fonctionne normalement sous Java 17). Sans cela, ProtocolLib 5.4.0 refusera de se
charger.

1. Installer la derniere version de [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)
   (5.4.0) dans `plugins/` (obligatoire).
2. Recuperer `BelarionFactions.jar` compile automatiquement par GitHub Actions : onglet **Actions**
   du depot -> dernier run -> **Artifacts** -> `BelarionFactions-jar`.
3. Placer le `.jar` dans `plugins/` a cote de `SaberFactions.jar` et `ProtocolLib.jar`.
4. Redemarrer le serveur (sous Java 17+).

## Compiler soi-meme

```bash
unzip -o -j SaberFactions.zip "SaberFactions.jar" -d BelarionFactions/libs
cd BelarionFactions
mvn clean package
# jar genere dans target/BelarionFactions.jar
```

## Commandes

| Commande | Description | Permission |
|---|---|---|
| `/topfactions` (alias `/ftop`, `/topf`) | Affiche le Top 3 des factions | `belarion.topfactions` (defaut: tous) |
| `/fpoints <add\|remove\|set> <faction> <montant>` | Administration manuelle des points | `belarion.points.admin` (defaut: op) |
| `/belariontags reload` | Recharge la config des nametags | `belarion.tags.admin` (defaut: op) |
