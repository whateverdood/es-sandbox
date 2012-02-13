package sandbox.search;

import static org.elasticsearch.index.query.QueryBuilders.*
import static org.junit.Assert.*

import com.googlecode.shawty.XPathExtractor 

import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.search.SearchResponse 
import org.elasticsearch.client.Client 
import org.elasticsearch.search.SearchHit 
import org.elasticsearch.search.SearchHits;
import org.junit.Before 
import org.junit.Test

class IndexingTest extends TestBase {
    
    def entries = []
    
    @Before
    public void setUp() throws Exception {
        def forEach = "/atom:feed/atom:entry"
        def xpaths = ["id": "atom:id",
                      "title": "atom:title",
                      "published": "atom:updated",
                      "content": "descendant::*"]
        def namespaces = ["atom": "http://www.w3.org/2005/Atom"]
        
        XPathExtractor extractor = new XPathExtractor(
            forEach: forEach, fieldMappings: xpaths, namespaces: namespaces)
        entries = extractor.extract(feed)
        
        assertEquals "Couldn't setup test data", 2, entries.size()
    }
    
    @Test
    public void indexTwoDocs() throws Exception {
        
        Client client = super.node.client()
        Indexer indexer = new Indexer(client: client)
        
        entries.each { entry ->
            indexer.index entry
            
            client.admin().indices().refresh(new RefreshRequest("sandbox")).
                actionGet()
            
            String q = "title:\"${entry.title}\""
            SearchResponse response = search(client, q)
            SearchHits hits = response.getHits()
            int found = hits.getTotalHits()
            assertEquals("Should only find 1 hit for [$q]", 1, found)
        }
    }
    
    SearchResponse search(Client client, String query) {
        return client.prepareSearch("sandbox")
            .setQuery(queryString(query)).setExplain(true).execute().actionGet()
    }
    
    def feed = '''<?xml version="1.0" encoding="utf-8"?>
        <feed xmlns="http://www.w3.org/2005/Atom">
            <title>Example Feed</title>
            <subtitle>A subtitle.</subtitle>
            <link href="http://example.org/feed/" rel="self" />
            <link href="http://example.org/" />
            <id>urn:uuid:60a76c80-d399-11d9-b91C-0003939e0af6</id>
            <updated>2003-12-13T18:30:02Z</updated>
            <author>
                <name>John Doe</name>
                <email>johndoe@example.com</email>
            </author>
            <entry>
                <title>Atom-Powered Robots Run Amok</title>
                <link href="http://example.org/2003/12/13/atom03" />
                <link rel="alternate" type="text/html" href="http://example.org/2003/12/13/atom03.html"/>
                <link rel="edit" href="http://example.org/2003/12/13/atom03/edit"/>
                <id>1</id>
                <updated>2003-12-13T18:30:02Z</updated>
                <summary>Run for your life!</summary>
            </entry>
            <entry>
                <title>Foo</title>
                <link href="http://example.org/ 2003/12/14/foo" />
                <link rel="alternate" type="text/html" href="http://example.org/2003/12/14/foo.html"/>
                <link rel="edit" href="http://example.org/2003/12/14/foo/edit"/>
                <id>2</id>
                <updated>2003-12-14T18:30:02Z</updated>
                <summary>Foo, bar, and baz appear way too frequently in tests.</summary>
            </entry>
        </feed>'''
    
}
