package fr.convergence.proddoc.util

import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import java.io.File
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
object FichierCache {

    private val LOG: Logger = LoggerFactory.getLogger(FichierCache::class.java)

    private var FichierMap: MutableMap<String, File> = mutableMapOf()

    /**
     *  crée et retourne l'URL de récupération du Kbis à partir d'un identifiant du cache
     *  l'URL de base est un paramètre "applicatif"...
     */
    fun creeURLKbisLocale(numGestion: String): String {

        val baseURL = "http://127.0.0.1:8095/"
        val pathURL = "kbis/pdfnumgestion/$numGestion"
        return (baseURL + pathURL)
    }
    /**
     * met le fichier reçu dans une map
     * ne retourne rien ; si ça pète, ça lève une exception à gérer par l'appelant
     */
    fun deposeFichierCache(fichier :File, idFichier :String){
        FichierMap.put(idFichier, fichier)
        LOG.debug("fichier $idFichier mis en cache, taille de la map en sortie: ${FichierMap.size}")
    }

    /**
     *  récupère un fichier dans le cache à partir de son identifiant
     *  retourne le fichier ou null si rien trouvé
     */
    fun recupFichierCache(identifant: String): File? {
        if (FichierMap.isNotEmpty()) {
            return FichierMap.get(identifant)
        } else {
            return (null)
        }
    }
}
