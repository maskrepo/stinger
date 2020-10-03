package fr.convergence.proddoc.util

import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import java.io.File
import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.core.MediaType

@ApplicationScoped
class FichierCache {

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(FichierCache::class.java)
    }

    private var fichierMap: MutableMap<String, Pair<File, String>> = mutableMapOf()


    /**
     * met le fichier reçu dans une map
     * ne retourne rien ; si ça pète, ça lève une exception à gérer par l'appelant
     */
    fun deposeFichierCache(fichier: File, idFichier: String, mediaType: String) {
        fichierMap.put(idFichier, Pair(fichier, mediaType))
        LOG.debug("fichier $idFichier mis en cache, taille de la map en sortie: ${fichierMap.size}")
    }

    /**
     *  récupère un fichier dans le cache à partir de son identifiant
     *  retourne le fichier ou null si rien trouvé
     */
    fun recupFichierCache(identifant: String): File? = fichierMap.get(identifant)?.first
    fun recupFichierMediaType(identifant: String): String? = fichierMap.get(identifant)?.second
}