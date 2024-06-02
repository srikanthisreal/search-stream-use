package com.fluenttakeoff.search.controller;

import com.fluenttakeoff.search.LuceneService;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class LuceneController {

    @Autowired
    private LuceneService luceneService;

    @GetMapping("/search")
    public Flux<String> search(@RequestParam String query) {
        return Flux.create(sink -> {
            try {
                TopDocs results = luceneService.searchIndex(query);
                for (ScoreDoc scoreDoc : results.scoreDocs) {
                    Document doc = luceneService.getDocument(scoreDoc.doc);
                    sink.next(doc.toString());
                }
                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }
}

