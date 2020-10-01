package fr.convergence.proddoc.controller

import fr.convergence.proddoc.util.FichierCache
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/cache/fichierpdf/")
@Produces("application/pdf")
@ApplicationScoped
class FichierPDFCache {

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(FichierPDFCache::class.java)
    }

    //   si appel sur le path, retourne le kbis pdf en le récupérant dans le cache
    @GET
    @Path("{identifiant}")
    fun fichierParIdentifiantCache(@PathParam("identifiant") identifiant: String): Response {

        requireNotNull(identifiant, {"L'identifiant reçu est null"})
        val monFichier = FichierCache.recupFichierCache(identifiant)

        if (monFichier==null) {
            LOG.debug("Fichier $identifiant introuvable dans le cache")
            return Response
                .status(Response.Status.NOT_FOUND)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                .entity("fichier non trouvé")
                .build()
        }
        else {
            LOG.debug("Fichier $identifiant trouvé dans le cache")
            return Response
                .status(Response.Status.OK)
                .entity(monFichier.readBytes())
                .build()
        }

    }
}