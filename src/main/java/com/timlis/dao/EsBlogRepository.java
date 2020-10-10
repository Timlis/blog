package com.timlis.dao;

import com.timlis.document.EsBlog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface EsBlogRepository extends ElasticsearchRepository<EsBlog,Long> {

    /**
     * {"multi_match": {"query": "小米","fields": ["title","description"]}
     *
     * @param keywords
     * @return
     */
    @Query("{\"multi_match\":{\"query\": \"?0\",\"fields\":[\"title\",\"description\"]}}")
    Page<EsBlog> findByKeywords(String keywords, Pageable pageable);
}
