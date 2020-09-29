package fr.convergence.proddoc.kafka

import fr.convergence.proddoc.model.lib.obj.MaskMessage
import fr.convergence.proddoc.model.metier.FichierAccessible
import fr.convergence.proddoc.model.metier.StockageFichier
import fr.convergence.proddoc.util.FichierCache
import fr.convergence.proddoc.util.FichierCache.creeURLKbisLocale
import fr.convergence.proddoc.util.maskIOHandler
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory.getLogger
import org.apache.commons.io.FileUtils
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eclipse.microprofile.reactive.messaging.Outgoing
import java.io.File
import java.io.InputStream
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE

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

        val fichierEcrit = message.recupererObjetMetier<StockageFichier>()
        val identifiantFichier = fichierEcrit.idMetierFichier
        val urlAbsFichier = fichierEcrit.fichierURLAbs
        LOG.debug("Réception évènement demande de stockage : $urlAbsFichier sous l'identifiant $identifiantFichier")

        val fichierastocker = message.recupererObjetMetier<StockageFichier>()
        val inputStream = ClientBuilder.newClient()
                .target(fichierastocker.fichierURLAbs)
                .request(APPLICATION_OCTET_STREAM_TYPE)
                .get(InputStream::class.java)
        LOG.debug("fichier $identifiantFichier téléchargé")

        // écrire fichier sur file system
        val tempDir = System.getProperty("java.io.tmpdir")
        LOG.debug("tempDir : $tempDir")
        val cheminCompletFichier = "$tempDir/$identifiantFichier${UUID.randomUUID()}.fic"

        FileUtils.copyInputStreamToFile(inputStream, File(cheminCompletFichier))
        LOG.debug("fichier $cheminCompletFichier écrit sur file system")

        // déposer fichier dans cache
        val fichier = File(cheminCompletFichier)
        FichierCache.deposeFichierCache(fichier, identifiantFichier,)
        LOG.debug("fichier $identifiantFichier déposé dans cache local")

        val urlFichier = creeURLKbisLocale(identifiantFichier)
        LOG.debug("URL du fichier accessible sur Stinger : $urlFichier")
        FichierAccessible(urlFichier)

    }
}