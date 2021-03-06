package streams

import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.kernel.internal.GraphDatabaseAPI
import org.neo4j.logging.Log
import streams.utils.Neo4jUtils
import streams.utils.StreamsUtils

class StreamsEventSinkQueryExecution(private val streamsTopicService: StreamsTopicService, private val db: GraphDatabaseAPI, val log: Log) {

    fun execute(topic: String, params: Collection<Any>) {
        val cypherQuery = streamsTopicService.get(topic)
        if (cypherQuery == null) {
            return
        }
        val query = "${StreamsUtils.UNWIND} $cypherQuery"
        if(log.isDebugEnabled){
            log.debug("Processing ${params.size} events with query: $query")
        }
        if (Neo4jUtils.isWriteableInstance(db)) {
            try {
                db.execute(query, mapOf("events" to params)).close()
            } catch (e: Exception) {
                log.error("Error while executing the query", e)
            }
        } else {
            if(log.isDebugEnabled){
                log.debug("Not writeable instance")
            }
        }

    }

    fun execute(map: Map<String, Collection<Any>>) {
        map.entries.forEach{ execute(it.key, it.value) }
    }

}

