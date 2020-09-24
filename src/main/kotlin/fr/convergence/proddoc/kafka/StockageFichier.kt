package fr.convergence.proddoc.kafka

import fr.convergence.proddoc.model.lib.obj.MaskMessage
import fr.convergence.proddoc.model.metier.FichierAccessible
import fr.convergence.proddoc.model.metier.FichierEcrit
import fr.convergence.proddoc.util.FichierCache
import fr.convergence.proddoc.util.FichierCache.creeURLKbisLocale
import fr.convergence.proddoc.util.maskIOHandler
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory.getLogger
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eclipse.microprofile.reactive.messaging.Outgoing
import java.io.File
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class StockageFichier() {

    companion object {
        private val LOG: Logger = getLogger(StockageFichier::class.java)
    }

    /**
     * si un MaskMessage arrive sur le topic "fichier" (Incoming) :
     * 1) récupère dans le message : l'URL absolue, l'URL relative
     * 2) stocke le fichier dans le système de stockage (actuellement Map en mémoire)
     * 3) publie un message de type MaskMessage donnant l'URL d'accès au fichier
     *  ou bien "KO" + exception
     **/

    @Incoming("fichiercache")
    @Outgoing("fichiercache_fini")
    fun traitementEvenementReceptionFichierEcrit(message: MaskMessage): MaskMessage = maskIOHandler(message) {

        //@TODO ces requires sont à basculer dans le maskIOHadler
        requireNotNull(message.entete.typeDemande) { "message.entete.typeDemande est null" }
        requireNotNull(message.objetMetier) { "message.objectMetier est null" }

        val fichierEcrit = message.recupererObjetMetier<FichierEcrit>()
        val cheminCompletFichier = fichierEcrit.fichierURLAbs
        val identifiantFichier = fichierEcrit.idMetierFichier
        LOG.debug("Réception évènement fichier écrit : $cheminCompletFichier sous l'identifiant $identifiantFichier")

        val fichier = File(cheminCompletFichier)
        FichierCache.deposeFichierCache(fichier, identifiantFichier,)
        val urlFichier = creeURLKbisLocale(identifiantFichier)
        LOG.debug("URL du fichier accessible sur Stinger : $urlFichier")

        FichierAccessible(urlFichier)
    }
}