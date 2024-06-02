package com.fluenttakeoff.search;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

import java.io.IOException;

import static com.fluenttakeoff.search.Indexing.indexDocuments;
import static com.fluenttakeoff.search.SearchIndex.searchIndex;

public class LuceneIndexingAndQuerying {

    public static void search(String[] args) throws IOException, ParseException {
        // Initialize Lucene components
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory index = new ByteBuffersDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter writer = new IndexWriter(index, config);

        // Index sample documents
        indexDocuments(writer, "data/str-doc.txt");
        indexDocuments(writer, "data/poi-doc.txt");
        indexDocuments(writer, "data/xstr-doc.txt");

        writer.close();

        // Query the index
        String queryString = "steet:Main Stret";  // Intentionally misspelled
        searchIndex(analyzer, index, queryString);
    }




}
