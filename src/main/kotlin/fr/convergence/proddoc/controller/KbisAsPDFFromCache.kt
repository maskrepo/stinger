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

@Path("/kbis/pdfnumgestion/")
@Produces("application/pdf")
@ApplicationScoped
class KbisAsPDFFromCache {

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(KbisAsPDFFromCache::class.java)
    }

    //   si appel sur le path, retourne le kbis pdf en le récupérant dans le cache
    @GET
    @Path("{numgestion}")
    fun numGestionKbis(@PathParam("numgestion") numgestion: String): Response {

        requireNotNull(numgestion, {"L'identifiant reçu est null"})
        val myPDF = FichierCache.recupFichierCache(numgestion)

        if (myPDF==null) {
            LOG.debug("Kbis $numgestion introuvable dans le cache")
            return Response
                .status(Response.Status.NOT_FOUND)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                .entity("fichier non trouvé")
                .build()
        }
        else {
            LOG.debug("Kbis $numgestion trouvé dans le cache")
            return Response
                .status(Response.Status.OK)
                .entity(myPDF.readBytes())
                .build()
        }

    }
}