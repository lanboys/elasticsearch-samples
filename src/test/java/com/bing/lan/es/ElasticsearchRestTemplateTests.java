package com.bing.lan.es;

import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.valuecount.ParsedValueCount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

/**
 * https://www.cnblogs.com/TJ21/p/12652308.html
 */
@Slf4j
@SpringBootTest
public class ElasticsearchRestTemplateTests {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    public void addIndexTest() {
        //elasticsearchRestTemplate.createIndex(Item.class);
        //elasticsearchRestTemplate.putMapping(Item.class);
    }

    @Test
    public void deleteIndex() {
        this.elasticsearchRestTemplate.deleteIndex("item");
    }

    /**
     * 新增和批量新增
     */
    @Test
    public void create() {
        /*新增*/
        //        Item item = new Item(1L, "小米手机7", " 手机", "小米", 3499.00, "http://image.leyou.com/13123.jpg");
        //        this.itemRepository.save(item);

        List<Item> list = new ArrayList<>();
        list.add(new Item(2L, "坚果手机R1", " 手机", "锤子", 3699.00, "http://image.leyou.com/123.jpg", ""));
        list.add(new Item(3L, "华为META10", " 手机", "华为", 4499.00, "http://image.leyou.com/3.jpg", ""));
        // 接收对象集合，实现批量新增
        this.itemRepository.saveAll(list);
    }

    /**
     * 查询全部
     */
    @Test
    public void find() {
        Optional<Item> item = this.itemRepository.findById(1L);
        System.out.println("item.get() = " + item.get());
    }

    /**
     * 查询并排序
     */
    @Test
    public void findAllSort() {
        Iterable<Item> items = this.itemRepository.findAll(Sort.by("price").descending());

        items.forEach(System.out::println);
    }

    /**
     * 根据title查询
     */
    @Test
    public void findByTitle() {
        Iterable<Item> items = this.itemRepository.findByTitle("手机");
        items.forEach(System.out::println);
    }

    /**
     * 查询价格区间
     */
    @Test
    public void findByPrice() {
        List<Item> byPriceBetween = this.itemRepository.findByPriceBetween(3699d, 4499d);
        byPriceBetween.forEach(System.out::println);
    }

    /**
     * 添加测试数据
     */
    @Test
    public void indexList() {
        List<Item> list = new ArrayList<>();
        list.add(new Item(11L, "小米手机7", "手机", "小米", 3299.00, "http://image.leyou.com/13123.jpg", "接收对象集合，实现批量新增"));
        list.add(new Item(12L, "坚果手机R1", "手机", "锤子", 3699.00, "http://image.leyou.com/13123.jpg", "冬天手机暖心路由羽绒服"));
        list.add(new Item(13L, "华为META10", "手机", "华为", 4499.00, "http://image.leyou.com/13123.jpg", "冬日羽绒服"));
        list.add(new Item(14L, "小米Mix2S", "手机", "小米", 4299.00, "http://image.leyou.com/13123.jpg", "花花的公子小米羽绒服"));
        list.add(new Item(15L, "荣耀V10", "手机", "华为", 2799.00, "http://image.leyou.com/13123.jpg", "花花小姐暖心的羽绒服"));
        list.add(new Item(16L, "小米手表", "手表", "小米", 3099.00, "http://image.leyou.com/13123.jpg", "花暖超级冷羽绒服"));
        list.add(new Item(17L, "荣耀V10", "路由器", "华为", 799.00, "http://image.leyou.com/13123.jpg", "花日历暖心超羽级绒服"));
        list.add(new Item(18L, "坚果手机R5", "手机", "锤子", 4699.00, "http://image.leyou.com/13123.jpg", "妹子暖心绒服"));
        list.add(new Item(19L, "华表", "手表", "华为", 499.00, "http://image.leyou.com/13123.jpg", "帅哥暖心羽绒"));
        // 接收对象集合，实现批量新增
        itemRepository.saveAll(list);
    }

    /**
     * 通过标题查询
     */
    @Test
    public void testSearch() {
        // 通过查询构建器构建查询条件
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("title", "手机");
        //执行查询
        Iterable<Item> items = this.itemRepository.search(matchQueryBuilder);
        items.forEach(System.out::println);
    }

    @Test
    public void testNative() {
        //构建自定义查询构建器
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //添加基本的查询条件
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("title", "手机");
        //执行查询获取分页结果集
        nativeSearchQueryBuilder.withQuery(matchQueryBuilder);

        Page<Item> items = this.itemRepository.search(nativeSearchQueryBuilder.build());
        System.out.println("items.getTotalElements() = " + items.getTotalElements());
        System.out.println("items.getTotalPages() = " + items.getTotalPages());
        items.forEach(System.out::println);
    }

    /**
     * 分页查询
     */
    @Test
    public void testNativeQuery() {
        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 添加基本的分词查询
        TermQueryBuilder queryBuilder1 = QueryBuilders.termQuery("brand", "小米");
        queryBuilder.withQuery(queryBuilder1);
        // 初始化分页参数
        int page = 0;
        int size = 30;
        // 设置分页参数
        queryBuilder.withPageable(PageRequest.of(page, size));

        // 执行搜索，获取结果
        Page<Item> items = this.itemRepository.search(queryBuilder.build());

        // 打印总条数
        System.out.println(items.getTotalElements());
        // 打印总页数
        System.out.println(items.getTotalPages());
        // 每页大小
        System.out.println(items.getSize());
        // 当前页
        System.out.println(items.getNumber());
        items.forEach(System.out::println);
    }

    /**
     * 分页查询
     */
    @Test
    public void testNativeQuery1() {
        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 添加基本的分词查询
        TermQueryBuilder queryBuilder1 = QueryBuilders.termQuery("brand", "小米");
        queryBuilder.withQuery(queryBuilder1);

        // 设置分页参数
        queryBuilder.withPageable(PageRequest.of(0, 30));
        // 执行搜索，获取结果
        Page<Item> items = this.itemRepository.search(queryBuilder.build());
        // 打印总条数
        System.out.println(items.getTotalElements());
        // 打印总页数
        System.out.println(items.getTotalPages());
        // 每页大小
        System.out.println(items.getSize());
        // 当前页
        System.out.println(items.getNumber());
        items.forEach(System.out::println);
    }

    @Test
    public void testAggs() {
        //初始化自定义构建查询器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加聚合
        queryBuilder.addAggregation(AggregationBuilders.terms("brandAgg").field("brand"));
        //添加结果集过滤不包括任何字段
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{}, null));
        //执行查询
        AggregatedPage<Item> itemPage = (AggregatedPage<Item>) this.itemRepository.search(queryBuilder.build());
        /*
        解析聚合结果集,根据聚合的类型以及字段类型,要进行强转,不然无法获取桶
         brand-是字符串类型的,聚合类型是词条类型的
        brandAgg-通过聚合名称获取聚合对象
        使用StringTerms强转的时候出现错误
         */
        ParsedStringTerms brandAgg = (ParsedStringTerms) itemPage.getAggregation("brandAgg");
        //获取桶
        List<? extends Terms.Bucket> buckets = brandAgg.getBuckets();
        //遍历输出
        buckets.forEach(bucket -> {
            System.out.println("bucket.getKeyAsString() = " + bucket.getKeyAsString());
            //获取条数
            System.out.println("bucket.getDocCount() = " + bucket.getDocCount());
        });
    }

    /**
     * 子聚合
     */
    @Test
    public void testSubAggs() {
        //初始化自定义构建查询器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加聚合
        queryBuilder.addAggregation(
                AggregationBuilders.terms("brandAgg").field("brand")
                        .subAggregation(AggregationBuilders.avg("price_avg").field("price"))
                        .subAggregation(AggregationBuilders.terms("categoryAgg").field("category"))
        );

        queryBuilder.addAggregation(
                AggregationBuilders.avg("priceAvg").field("price")
        );

        //添加结果集过滤不包括任何字段
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{}, null));
        //执行查询
        AggregatedPage<Item> itemPage = (AggregatedPage<Item>) this.itemRepository.search(queryBuilder.build());
        /*
        解析聚合结果集,根据聚合的类型以及字段类型,要进行强转,不然无法获取桶
         brand-是字符串类型的,聚合类型是词条类型的
        brandAgg-通过聚合名称获取聚合对象
        使用StringTerms强转的时候出现错误
         */
        ParsedStringTerms brandAgg = (ParsedStringTerms) itemPage.getAggregation("brandAgg");
        ParsedValueCount priceAvg = (ParsedValueCount) itemPage.getAggregation("priceAvg");
        System.out.println("priceAvg.getValue() = " + priceAvg.getValue());
        //获取桶
        List<? extends Terms.Bucket> buckets = brandAgg.getBuckets();
        //遍历输出
        buckets.forEach(bucket -> {

            System.out.println("bucket.getKeyAsString() = " + bucket.getKeyAsString());
            //获取条数
            System.out.println("bucket.getDocCount() = " + bucket.getDocCount());
            //获取子聚合的map集合:key-聚合名称,value-对应的子聚合对象
            Map<String, Aggregation> stringAggregationMap = bucket.getAggregations().asMap();
            /*
            以前使用的InternalAvg强转出现转换异常
             */
            ParsedValueCount price_avg = (ParsedValueCount) stringAggregationMap.get("price_avg");
            System.out.println("price_avg.getValue() = " + price_avg.getValue());

            ParsedStringTerms categoryAgg = (ParsedStringTerms) stringAggregationMap.get("categoryAgg");
            List<? extends Terms.Bucket> categoryAggBuckets = categoryAgg.getBuckets();
            System.out.println("categoryAggBuckets(): " + categoryAggBuckets);
        });
    }
}
