package fr.convergence.proddoc.controller

import fr.convergence.proddoc.controller.StockageControleur.Companion.PATH_VERS_STINGER_STREAM
import fr.convergence.proddoc.util.FichierCache
import org.slf4j.LoggerFactory.getLogger
import java.io.FileOutputStream
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.NOT_FOUND
import javax.ws.rs.core.Response.Status.OK
import javax.ws.rs.core.Response.status

@Path(PATH_VERS_STINGER_STREAM)
@ApplicationScoped
class StockageControleur(@Inject var fichierCache: FichierCache) {

    companion object {

        const val PATH_VERS_STINGER_STREAM = "/cache"
        const val PATH_VERS_NAVIGATEUR = "/navigateur"
        const val PATH_VERS_STINGER_NAVIGATEUR = "$PATH_VERS_STINGER_STREAM$PATH_VERS_NAVIGATEUR"

        private val LOG = getLogger(StockageControleur::class.java)
    }

    //   si appel sur le path, retourne le kbis pdf en le récupérant dans le cache
    @GET
    @Produces(APPLICATION_OCTET_STREAM)
    @Path("{identifiant}")
    fun fichierParIdentifiantCache(@PathParam("identifiant") identifiant: String): Response {

        requireNotNull(identifiant, { "L'identifiant reçu est null" })
        val monFichier = fichierCache.recupFichierCache(identifiant)

        return if (monFichier == null) {
            LOG.debug("Fichier $identifiant introuvable dans le cache")
            status(NOT_FOUND).entity("Fichier non trouvé pour cet identifiant : $identifiant")
        } else {
            LOG.debug("Fichier $identifiant trouvé dans le cache")
            status(OK).entity(FileOutputStream(monFichier))
        }.build()
    }

    @GET
    @Path("$PATH_VERS_NAVIGATEUR/{identifiant}")
    fun fichierParIdentifiantNavigateur(@PathParam("identifiant") identifiant: String): Response {

        requireNotNull(identifiant, { "L'identifiant reçu est null" })
        val monFichier = fichierCache.recupFichierCache(identifiant)
        val mediaType = fichierCache.recupFichierMediaType(identifiant)!!
        LOG.info("On va renvoyer le fichier $monFichier de type $mediaType")

        return if (monFichier == null) {
            LOG.debug("Fichier $identifiant introuvable dans le cache")
            status(NOT_FOUND).entity("fichier non trouvé")
        } else {
            LOG.debug("Fichier $identifiant trouvé dans le cache")
            status(OK)
                    .entity(monFichier.readBytes())
                    .header("Content-type", mediaType)
        }.build()

    }
}