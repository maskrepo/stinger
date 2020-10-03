package fr.convergence.proddoc.service

import fr.convergence.proddoc.controller.StockageControleur.Companion.PATH_VERS_STINGER_NAVIGATEUR
import fr.convergence.proddoc.controller.StockageControleur.Companion.PATH_VERS_STINGER_STREAM
import fr.convergence.proddoc.model.lib.obj.MaskMessage
import fr.convergence.proddoc.model.metier.DemandeStockageFichier
import fr.convergence.proddoc.model.metier.FichierStocke
import fr.convergence.proddoc.util.FichierCache
import fr.convergence.proddoc.util.WSUtils
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import org.apache.commons.io.FileUtils
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eclipse.microprofile.reactive.messaging.Outgoing
import javax.inject.Inject

open class StockageFichierService(
        @ConfigProperty(name = "quarkus.http.host") open val host: String,
        @ConfigProperty(name = "quarkus.http.port") open val port: String,
        @Inject val fichierCache: FichierCache
) {

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(FichierCache::class.java)
    }

    fun creerURLFichierCache(identifiant: String) = "http://$host:$port${PATH_VERS_STINGER_STREAM}/$identifiant"
    fun creerURLFichierNavigateur(identifiant: String) = "http://$host:$port${PATH_VERS_STINGER_NAVIGATEUR}/$identifiant"
    fun creerURLFichierRelative(identifiant: String) = "${PATH_VERS_STINGER_STREAM}/$identifiant"

    /**
     * si un MaskMessage arrive sur le topic "STOCKER_FICHIER_DEMANDE" (Incoming) :
     * 1) récupère dans le message : l'URL du fichier où le demander
     * 2) stocke le fichier dans le système de stockage (actuellement Map en mémoire)
     * 3) publie un message de type MaskMessage donnant l'URL d'accès au fichier
     * sortie sur le topic "STOCKER_FICHIER_DEMANDE"
     *  ou bien "KO" + exception
     **/
    @Incoming("stocker_fichier_demande_stinger")
    @Outgoing("stocker_fichier_reponse_stinger")
    fun traitementEvenementReceptionDemandeStockage(message: MaskMessage): MaskMessage {

        //@TODO ces requires sont à basculer dans le maskIOHadler
        requireNotNull(message.entete.typeDemande) { "message.entete.typeDemande est null" }
        requireNotNull(message.objetMetier) { "message.objectMetier est null" }

        // décorticage du message reçu
        val msgStockageFichier = message.recupererObjetMetier<DemandeStockageFichier>()
        val identifiantFichier = message.entete.idReference
        val urlOuAllerChercherLeStreamSurLeFichier = msgStockageFichier.urlCallback
        LOG.debug("Réception évènement demande de stockage : $urlOuAllerChercherLeStreamSurLeFichier sous l'identifiant $identifiantFichier")

        // récupération du fichier en application_octet_stream
        val inputStream = WSUtils.postOctetStreamREST(urlOuAllerChercherLeStreamSurLeFichier, msgStockageFichier.maskMessage)
        LOG.debug("fichier $identifiantFichier téléchargé")

        // écrire fichier sur file system
        val fichierTemp = createTempFile(identifiantFichier, suffix = ".fic")
        FileUtils.copyInputStreamToFile(inputStream, fichierTemp)
        LOG.debug("fichier $fichierTemp écrit sur file system")

        // déposer fichier dans cache
        fichierCache.deposeFichierCache(fichierTemp, identifiantFichier, msgStockageFichier.mediaType)
        LOG.debug("fichier $identifiantFichier déposé dans cache local")

        // publier réponse avec URL d'accès au fichier dans le cache
        val urlFichierStream = creerURLFichierCache(identifiantFichier)
        val urlFichierNavigateur = creerURLFichierNavigateur(identifiantFichier)
        val urlFichierRelative = creerURLFichierRelative(identifiantFichier)
        LOG.debug("URL du fichier accessible sur Stinger : $urlFichierNavigateur")
        val fichierAccessible = FichierStocke(identifiantFichier, urlFichierStream, urlFichierNavigateur, urlFichierRelative)

        return MaskMessage.reponseOk(fichierAccessible, message, message.entete.idReference)
    }
}