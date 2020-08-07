package com.bing.lan.es;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ItemRepository extends ElasticsearchRepository<Item, Long> {

    Iterable<Item> findByTitle(String title);

    List<Item> findByPriceBetween(double v, double v1);
}
