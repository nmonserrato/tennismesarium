package dev.paloma.tennismesarium.tournament

import dev.paloma.tennismesarium.match.complete.MatchCompletedEvent
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import java.util.UUID

@RestController
@RequestMapping("/api/v2/tournament")
class TournamentControllerReactive {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val streams = HashMap<UUID, MutableList<FluxSink<Map<String, Any>>>>()

    @GetMapping("{tournamentId}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getDetails(@PathVariable("tournamentId") tournamentId: UUID): Flux<Map<String, Any>> {
        logger.info("Requested stream of brackets for tournament {}", tournamentId)
        return Flux.create<Map<String, Any>> {
            sink -> streams.getOrPut(tournamentId, { ArrayList() }).add(sink)
        }
    }

    @EventListener
    @Order(1)
    fun sendEventToFrontEnd(event: MatchCompletedEvent) {
        streams[event.tournament.identifier()]?.removeIf { it.isCancelled }
        streams[event.tournament.identifier()]?.forEach { it.next(event.tournament.toJson()) }
    }
}
