package sandbox.search;

import static org.elasticsearch.common.xcontent.XContentFactory.*

import org.apache.log4j.Logger;
import org.elasticsearch.action.index.IndexResponse 
import org.elasticsearch.client.Client 
import org.elasticsearch.common.xcontent.XContentBuilder

class Indexer {
    
    final static Logger LOG = Logger.getLogger(Indexer)
    
    Client client
    
    void index(Map doc) {
        
        if (LOG.isTraceEnabled()) {
            LOG.trace "Indexing [$doc]"
        }
        
        XContentBuilder o = jsonBuilder().startObject()
        doc.each { k, v -> 
            o.field(k, v)
        }
        o.endObject()

        if (LOG.isTraceEnabled()) {
            LOG.trace "[$doc] transformed to JSON [$o]"
        }

        IndexResponse result = client.prepareIndex("sandbox", "doc", doc.id).
            setSource(o).execute().actionGet()

        if (LOG.isDebugEnabled()) {
            LOG.debug "Doc id [${result.id}] indexed with version: [${result.version}]"            
        }
    }

}
