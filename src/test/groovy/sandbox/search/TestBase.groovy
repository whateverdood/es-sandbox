package sandbox.search

import static org.elasticsearch.node.NodeBuilder.*

import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Logger 
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest 
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.settings.Settings.Builder 
import org.elasticsearch.node.Node
import org.junit.After 
import org.junit.Before

class TestBase {
    
    static {
        BasicConfigurator.resetConfiguration()
        BasicConfigurator.configure()
    }
    
    static Logger LOG = Logger.getLogger(TestBase)
    
    protected Node node
    
    @Before
    public void prepare() throws Exception {
        // start an in-JVM data storing ElasticSearch node
        // kind of like EmbeddedSolrServer only better :)
        node = nodeBuilder().local(true).settings(
            ImmutableSettings.settingsBuilder().
                put("node.data", true).
                put("index.number_of_shards", 1).
                put("index.number_of_replicas", 0)).build().start()

        def schema = '''{ 
                            "doc" : {
                                "properties" : {
                                    "id" : {"type": "string", "store": "yes", "index": "not_analyzed"},
                                    "title" : {"type": "string", "store": "yes", "index": "analyzed"},
                                    "published" : {"type": "date", "store": "yes", "index": "analyzed"},
                                    "content" : {"type": "string", "store": "yes", "index": "analyzed"}
                                }
                            }
                        }'''
        
        Client client = node.client()

        if ("yes".equals(System.getProperty("clean"))) {
            try {
                client.admin().indices().delete(new DeleteIndexRequest("sandbox")).
                    actionGet()
            } catch (Exception e) {
                LOG.warn "Exception while deleting previous test index: ${e.message}"
            }
            client.admin().indices().create(new CreateIndexRequest("sandbox").
                mapping("doc", schema)).actionGet()            
        }
        
        client.admin().cluster().health(
            new ClusterHealthRequest("sandbox").waitForGreenStatus()).actionGet()        
    }
    
    @After
    public void cleanup() throws Exception {
        node.close()
    }
    
}
