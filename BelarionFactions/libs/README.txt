Ce dossier doit contenir SaberFactions.jar pour compiler ce projet localement.

Il n'est pas versionne ici (pour ne pas dupliquer les 10 Mo deja presents dans
SaberFactions.zip a la racine du depot).

Pour compiler en local :
  unzip -o -j ../../SaberFactions.zip "SaberFactions.jar" -d .

Le workflow GitHub Actions (.github/workflows/build.yml) fait deja cette
extraction automatiquement a chaque build : vous n'avez rien a faire pour
recuperer le .jar compile, il est disponible dans l'onglet "Actions" du
depot, sous "Artifacts", apres chaque push.

