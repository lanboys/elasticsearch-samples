package com.bing.lan.es;

import com.alibaba.fastjson.JSON;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexResponse;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

/**
 * https://blog.csdn.net/qq_38360901/article/details/105539222?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-2.edu_weight
 */
@SpringBootTest
@Slf4j
public class RestHighLevelClientTests {

    @Autowired
    @Qualifier("elasticsearchRestHighLevelClient")
    private RestHighLevelClient client;

    /**
     * 创建索引
     */
    @Test
    public void testCreateIndex() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("lan6");
        request.settings(Settings.builder()
                .put("index.number_of_shards", 5)
                .put("index.number_of_replicas", 1)
        );

        Map<String, Object> message = new HashMap<>();
        message.put("type", "text");
        Map<String, Object> name = new HashMap<>();
        name.put("type", "keyword");
        Map<String, Object> title = new HashMap<>();
        title.put("type", "text");
        Map<String, Object> properties = new HashMap<>();
        properties.put("message", message);
        properties.put("name", name);
        properties.put("title", title);
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("properties", properties);
        request.mapping(mapping);

        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(createIndexResponse.toString());
    }

    /**
     * 创建索引2
     */
    @Test
    public void testCreateIndex2() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("lan");
        request.settings(Settings.builder()
                .put("index.number_of_shards", 5)
                .put("index.number_of_replicas", 1)
        );

        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            builder.startObject("properties");
            {
                builder.startObject("message");
                {
                    builder.field("type", "text");
                }
                builder.endObject();
                builder.startObject("title");
                {
                    builder.field("type", "text");
                    //builder.field("analyzer", "ik_max_word");
                }
                builder.endObject();
            }
            builder.endObject();
        }
        builder.endObject();
        request.mapping(builder);

        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(createIndexResponse.index());
    }

    /**
     * 删除索引
     */
    @Test
    public void testDeleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("lan");

        AcknowledgedResponse deleteIndexResponse = client.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(deleteIndexResponse.isAcknowledged());
    }

    /**
     * 判断索引是否存在
     */
    @Test
    public void testExistsIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("lan");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    @Test
    public void testOpenIndex() throws IOException {
        OpenIndexRequest request = new OpenIndexRequest("lan");
        OpenIndexResponse openIndexResponse = client.indices().open(request, RequestOptions.DEFAULT);
        System.out.println(JSON.toJSONString(openIndexResponse));
    }

    /**
     * 获取索引的mapping
     * throws IOException
     */
    @Test
    public void testGetMapping() throws IOException {
        GetMappingsRequest request = new GetMappingsRequest();
        request.indices("lan");
        GetMappingsResponse getMappingResponse = client.indices().getMapping(request, RequestOptions.DEFAULT);
        Map<String, MappingMetaData> allMappings = getMappingResponse.mappings();
        MappingMetaData indexMapping = allMappings.get("lan");
        Map<String, Object> mapping = indexMapping.sourceAsMap();

        for (String s : mapping.keySet()) {
            System.out.println("key : " + s + " value : " + mapping.get(s));
        }
    }

    /**
     * 获取索引的Settings信息
     */
    @Test
    public void testGetSettings() throws IOException {
        GetSettingsRequest request = new GetSettingsRequest().indices("lan");
        GetSettingsResponse getSettingsResponse = client.indices().getSettings(request, RequestOptions.DEFAULT);

        String numberOfShardsString = getSettingsResponse.getSetting("lan", "index.number_of_shards");
        String numberOfReplicasString = getSettingsResponse.getSetting("lan", "index.number_of_replicas");

        System.out.println(numberOfShardsString);
        System.out.println(numberOfReplicasString);
    }

    /**
     * 添加文档 Document
     */
    @Test
    public void testInsertDocument() throws IOException {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("number", 125L);
        jsonMap.put("create_time", new Date());
        jsonMap.put("price", 35.5d);
        jsonMap.put("name", "测试一下");
        jsonMap.put("title", "测试数据001");
        IndexRequest indexRequest = new IndexRequest("book").id("1").source(jsonMap);
        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        log.info("indexResponse:{}", indexResponse.toString());
    }

    /**
     * 根据文档id查询
     */
    @Test
    public void testGetDocumentById() throws IOException {
        GetRequest getRequest = new GetRequest("book", "_doc", "8");
        /*
         * 查询指定字段
         */
        //        String[] includes = new String[]{"name", "create_time","title"};
        //        String[] excludes = Strings.EMPTY_ARRAY;
        //        FetchSourceContext fetchSourceContext =
        //                new FetchSourceContext(true, includes, excludes);
        //        getRequest.fetchSourceContext(fetchSourceContext);

        /*
          排除指定字段
         */
        String[] includes = Strings.EMPTY_ARRAY;
        String[] excludes = new String[]{"number"};
        FetchSourceContext fetchSourceContext =
                new FetchSourceContext(true, includes, excludes);
        getRequest.fetchSourceContext(fetchSourceContext);

        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);

        String index = getResponse.getIndex();
        String id = getResponse.getId();
        if (getResponse.isExists()) {
            long version = getResponse.getVersion();
            log.info("查询版本号，version:{}", version);
            String sourceAsString = getResponse.getSourceAsString();
            log.info("查询结果string类型，sourceAsString:{}", sourceAsString);
            Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
            log.info("查询结果map类型，sourceAsMap:{}", sourceAsMap);
            byte[] sourceAsBytes = getResponse.getSourceAsBytes();
            log.info("查询结果byte类型，sourceAsBytes:{}", sourceAsBytes);
        } else {
            log.info("暂无数据");
        }
        log.info("index:{}", index);
        log.info("id:{}", id);
    }

    /**
     * 判断文档是否存在
     */
    @Test
    public void testExistsDocument() throws IOException {
        GetRequest getRequest = new GetRequest("book", "_doc", "10");
        //禁用获取_source
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        //禁用获取存储的字段。
        getRequest.storedFields("_none_");

        boolean exists = client.exists(getRequest, RequestOptions.DEFAULT);

        System.out.println(exists);
    }

    /**
     * 修改文档
     */
    @Test
    public void testUpdateDocument() throws IOException {
        UpdateRequest request = new UpdateRequest("book", "_doc", "8")
                .doc("name", "测试一下3", "title", "我就是在测试更新3");
        UpdateResponse updateResponse = client.update(request, RequestOptions.DEFAULT);
        String index = updateResponse.getIndex();
        log.info("index:{}", index);
        String id = updateResponse.getId();
        log.info("id:{}", id);
        long version = updateResponse.getVersion();
        log.info("version:{}", version);

        if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
            log.info("处理首次创建文档的情况");
        } else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            log.info("处理文档更新的情况");
        } else if (updateResponse.getResult() == DocWriteResponse.Result.DELETED) {
            log.info("处理文件被删除的情况");
        } else if (updateResponse.getResult() == DocWriteResponse.Result.NOOP) {
            log.info("处理文档不受更新影响的情况，即未对文档执行任何操作（空转）");
        }
    }

    /**
     * 删除文档
     */
    @Test
    public void testDeleteDocument() throws IOException {
        DeleteRequest request = new DeleteRequest("book", "_doc", "8");
        DeleteResponse deleteResponse = client.delete(request, RequestOptions.DEFAULT);
        log.info("deleteResponse:{}", deleteResponse);
    }

    /**
     * 查询文档
     */
    @Test
    public void testSearch() throws IOException {
        SearchRequest searchRequest = new SearchRequest("book");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("name", "建南"));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        float maxScore = searchResponse.getHits().getMaxScore();
        long totalHits = searchResponse.getHits().getTotalHits();
        org.elasticsearch.search.SearchHit[] hits = searchResponse.getHits().getHits();

        for (org.elasticsearch.search.SearchHit hit : hits) {
            log.info("id:{}", hit.getId());
            log.info("索引名:{}", hit.getIndex());
            log.info("分数:{}", hit.getScore());
            log.info("string:{}", hit.getSourceAsString());
            log.info("map:{}", hit.getSourceAsMap());
        }

        log.info("totalHits value:{}", totalHits);
        //log.info("totalHits relation:{}", totalHits.relation);
        log.info("maxScore:{}", maxScore);
        log.info("searchResponse:{}", searchResponse);
    }

    @Test
    public void testSearch2() throws IOException {
        /*
         指定查询book索引，不指定查询所有索引
         */
        SearchRequest searchRequest = new SearchRequest("book");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchQuery("title", "The Ztr"));
        sourceBuilder.from(0);
        sourceBuilder.size(1);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        org.elasticsearch.search.SearchHit[] hits;
        hits = searchResponse.getHits().getHits();

        for (org.elasticsearch.search.SearchHit hit : hits) {
            log.info("id:{}", hit.getId());
            log.info("索引名:{}", hit.getIndex());
            log.info("分数:{}", hit.getScore());
            log.info("string:{}", hit.getSourceAsString());
            log.info("map:{}", hit.getSourceAsMap());
        }
    }

    /**
     * 统计
     * throws IOException
     */
    @Test
    public void testSearch3() throws IOException {
        CountRequest countRequest = new CountRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        countRequest.source(searchSourceBuilder);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchQuery("name", "php"));
        countRequest.source(sourceBuilder);
        CountResponse countResponse = client
                .count(countRequest, RequestOptions.DEFAULT);

        long count = countResponse.getCount();
        log.info("count:{}", count);
    }

    @Test
    public void testInsertBook() throws IOException {
        Book book = new Book(10, 126L, LocalDateTime.now(), 99.9, "秃头程序员教你学es", "由秃头程序员呕心沥血创作完成的作品");
        IndexRequest request = new IndexRequest("book");
        request.id(book.getId().toString());
        request.timeout(TimeValue.timeValueSeconds(5));
        //        IndexRequest indexRequest = new IndexRequest("book")
        //                .id("8").source(jsonMap);
        //        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        //        log.info("indexResponse:{}",indexResponse.toString());

        request.source(JSON.toJSONString(book), XContentType.JSON);
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);

        System.out.println(response.toString());
        System.out.println(response.status());
    }

    @Test
    public void testExistsBook() throws IOException {
        GetRequest getRequest = new GetRequest("book").id("10");

        boolean exists = client.exists(getRequest, RequestOptions.DEFAULT);
        if (exists) {
            GetResponse response = client.get(getRequest, RequestOptions.DEFAULT);
            System.out.println(response.getSourceAsString());
            System.out.println(response);
        }
    }

    @Test
    public void testUpdateBook() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("book", "_doc", "10");
        updateRequest.timeout("5s");
        Book book = new Book();
        book.setName("秃头程序员再次教你学习es");
        updateRequest.doc(JSON.toJSONString(book), XContentType.JSON);
        UpdateResponse update = client.update(updateRequest, RequestOptions.DEFAULT);
        System.out.println(update.status());
    }

    /**
     * 批量插入
     */
    @Test
    public void testBulkInsertBook() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("10s");

        ArrayList<Book> books = new ArrayList<>();
        books.add(new Book(21, 127L, LocalDateTime.now(), 19.9, "秃头程序员教你学C", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(22, 128L, LocalDateTime.now(), 29.9, "秃头程序员教你学C++", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(23, 129L, LocalDateTime.now(), 39.9, "秃头程序员教你学C#", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(24, 130L, LocalDateTime.now(), 49.9, "秃头程序员教你学RABBIT", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(25, 131L, LocalDateTime.now(), 59.9, "秃头程序员教你学KAFKA", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(26, 132L, LocalDateTime.now(), 69.9, "秃头程序员教你学MYSQL", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(27, 133L, LocalDateTime.now(), 79.9, "秃头程序员教你学ORACLE", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(28, 134L, LocalDateTime.now(), 89.9, "秃头程序员教你学DB2", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(29, 135L, LocalDateTime.now(), 99.9, "秃头程序员教你学SQL-SERVER", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(30, 136L, LocalDateTime.now(), 109.9, "秃头程序员教你学DBA", "由秃头程序员呕心沥血创作完成的作品"));

        for (Book book : books) {
            bulkRequest.add(new IndexRequest("book")
                    .id(book.getId().toString())
                    .source(JSON.toJSONString(book), XContentType.JSON));
        }

        BulkResponse bulk = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(bulk.hasFailures());//是否失败
    }

    @Test
    public void testBulkUpdateBook() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("10s");

        ArrayList<Book> books = new ArrayList<>();
        books.add(new Book(21, 127L, LocalDateTime.now(), 19.9, "2秃头程序员教你学JAVA", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(22, 128L, LocalDateTime.now(), 29.9, "2秃头程序员教你学PHP", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(23, 129L, LocalDateTime.now(), 39.9, "2秃头程序员教你学PYTHON", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(24, 130L, LocalDateTime.now(), 49.9, "2秃头程序员教你学JSON", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(25, 131L, LocalDateTime.now(), 59.9, "2秃头程序员教你学HTML", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(26, 132L, LocalDateTime.now(), 69.9, "2秃头程序员教你学CSS", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(27, 133L, LocalDateTime.now(), 79.9, "2秃头程序员教你学JAVASCRIPT", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(28, 134L, LocalDateTime.now(), 89.9, "2秃头程序员教你学SPRING", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(29, 135L, LocalDateTime.now(), 99.9, "2秃头程序员教你学MYBATIS", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(30, 136L, LocalDateTime.now(), 109.9, "2秃头程序员教你学JPA", "由秃头程序员呕心沥血创作完成的作品"));

        for (Book book : books) {
            bulkRequest.add(new UpdateRequest("book", "_doc", book.getId().toString())
                    .doc(JSON.toJSONString(book), XContentType.JSON));
        }

        BulkResponse bulk = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(bulk.hasFailures());
    }

    /**
     * 批量删除
     */
    @Test
    public void testBulkDeleteBook() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("10s");

        ArrayList<Book> books = new ArrayList<>();
        books.add(new Book(21, 127L, LocalDateTime.now(), 19.9, "2秃头程序员教你学JAVA", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(22, 128L, LocalDateTime.now(), 29.9, "2秃头程序员教你学PHP", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(23, 129L, LocalDateTime.now(), 39.9, "2秃头程序员教你学PYTHON", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(24, 130L, LocalDateTime.now(), 49.9, "2秃头程序员教你学JSON", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(25, 131L, LocalDateTime.now(), 59.9, "2秃头程序员教你学HTML", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(26, 132L, LocalDateTime.now(), 69.9, "2秃头程序员教你学CSS", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(27, 133L, LocalDateTime.now(), 79.9, "2秃头程序员教你学JAVASCRIPT", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(28, 134L, LocalDateTime.now(), 89.9, "2秃头程序员教你学SPRING", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(29, 135L, LocalDateTime.now(), 99.9, "2秃头程序员教你学MYBATIS", "由秃头程序员呕心沥血创作完成的作品"));
        books.add(new Book(30, 136L, LocalDateTime.now(), 109.9, "2秃头程序员教你学JPA", "由秃头程序员呕心沥血创作完成的作品"));

        for (Book book : books) {
            bulkRequest.add(new DeleteRequest("book", "_doc", book.getId().toString()));
        }

        BulkResponse bulk = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(bulk.hasFailures());
    }

    /**
     * 分页查询
     */
    @Test
    public void testSearchBook() throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(10);
        searchSourceBuilder.size(5);
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "秃头程序员");
        searchSourceBuilder.query(termQueryBuilder);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);

        SearchHits hits = search.getHits();
        System.out.println(JSON.toJSONString(hits));

        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println(hit.getSourceAsMap());
        }
    }

    /**
     * 高亮分页搜索
     *
     * @param keyWord
     * @param page
     * @param size
     * @return
     * @throws IOException
     */
    public List<Map<String, Object>> searchHighPage(String keyWord, Integer page, Integer size) throws IOException {

        SearchRequest searchRequest = new SearchRequest("book");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //精准匹配关键字
        TermQueryBuilder termQuery = QueryBuilders.termQuery("title", keyWord);
        sourceBuilder.query(termQuery);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //分页
        sourceBuilder.from(page);
        sourceBuilder.size(size);
        HighlightBuilder highlightBuilder = new HighlightBuilder();

        highlightBuilder.field(new HighlightBuilder.Field("title"));
        //关闭多次高亮
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);
        searchRequest.source(sourceBuilder);

        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        ArrayList<Map<String, Object>> list = new ArrayList<>();
        //解析结果
        for (org.elasticsearch.search.SearchHit hit : response.getHits().getHits()) {
            //解析高亮字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField title = highlightFields.get("title");

            Map<String, Object> sourceAsMap = hit.getSourceAsMap();

            if (title != null) {
                Text[] fragments = title.fragments();
                String n_title = "";
                for (Text text : fragments) {
                    n_title += text;
                }
                sourceAsMap.put("title", n_title);
            }

            list.add(sourceAsMap);
        }
        return list;
    }
}
