package fr.convergence.proddoc.kafka

import fr.convergence.proddoc.model.lib.obj.MaskMessage
import fr.convergence.proddoc.model.metier.FichierAccessible
import fr.convergence.proddoc.model.metier.StockageFichier
import fr.convergence.proddoc.util.FichierCache
import fr.convergence.proddoc.util.FichierCache.creeURLKbisLocale
import fr.convergence.proddoc.util.FichiersUtils.copyInputStreamToFile
import fr.convergence.proddoc.util.WSUtils.getOctetStreamREST
import fr.convergence.proddoc.util.maskIOHandler
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory.getLogger
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eclipse.microprofile.reactive.messaging.Outgoing
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class StockageFichier() {

    companion object {
        private val LOG: Logger = getLogger(StockageFichier::class.java)
    }

    /**
     * si un MaskMessage arrive sur le topic "STOCKER_FICHIER_DEMANDE" (Incoming) :
     * 1) récupère dans le message : l'URL du fichier où le demander
     * 2) stocke le fichier dans le système de stockage (actuellement Map en mémoire)
     * 3) publie un message de type MaskMessage donnant l'URL d'accès au fichier
     * sortie sur le topic "STOCKER_FICHIER_DEMANDE"
     *  ou bien "KO" + exception
     **/

    @Incoming("stocker_fichier_demande")
    @Outgoing("stocker_fichier_reponse")
    fun traitementEvenementReceptionFichierEcrit(message: MaskMessage): MaskMessage = maskIOHandler(message) {

        //@TODO ces requires sont à basculer dans le maskIOHadler
        requireNotNull(message.entete.typeDemande) { "message.entete.typeDemande est null" }
        requireNotNull(message.objetMetier) { "message.objectMetier est null" }

        // décorticage du message reçu
        val msgStockageFichier = message.recupererObjetMetier<StockageFichier>()
        val identifiantFichier = msgStockageFichier.idMetierFichier
        val urlAbsFichier = msgStockageFichier.fichierURLAbs
        LOG.debug("Réception évènement demande de stockage : $urlAbsFichier sous l'identifiant $identifiantFichier")

        // récupération du fichier en application_octet_stream
        val inputStream = getOctetStreamREST(urlAbsFichier.toString())
        LOG.debug("fichier $identifiantFichier téléchargé")

        // écrire fichier sur file system
        val fichierTemp = createTempFile(identifiantFichier, suffix = ".fic")
        copyInputStreamToFile(inputStream, fichierTemp)
        LOG.debug("fichier $fichierTemp écrit sur file system")

        // déposer fichier dans cache
        FichierCache.deposeFichierCache(fichierTemp, identifiantFichier)
        LOG.debug("fichier $identifiantFichier déposé dans cache local")

        // publier réponse avec URL d'accès au fichier dans le cache
        val urlFichier = creeURLKbisLocale(identifiantFichier)
        LOG.debug("URL du fichier accessible sur Stinger : $urlFichier")
        FichierAccessible(urlFichier)

    }
}