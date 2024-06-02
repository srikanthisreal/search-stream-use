package com.fluenttakeoff.search;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class SearchIndex {

    public static void searchIndex(StandardAnalyzer analyzer, Directory index, String queryString) throws IOException, ParseException {
        // Spell Checker Directory
        Directory spellIndex = FSDirectory.open(Paths.get("spellchecker"));

        // Create the SpellChecker
        SpellChecker spellChecker = new SpellChecker(spellIndex);
        try (IndexWriter spellWriter = new IndexWriter(spellIndex, new IndexWriterConfig(new WhitespaceAnalyzer()))) {
            DirectoryReader reader = DirectoryReader.open(index);
            LuceneDictionary luceneDictionary = new LuceneDictionary(reader, "content");
            spellChecker.indexDictionary(luceneDictionary, new IndexWriterConfig(new WhitespaceAnalyzer()), false);
        }

        // Measure query parsing time
        long queryStartTime = System.currentTimeMillis();
        QueryParser parser = new QueryParser("content", analyzer);

        // Check for spelling correction
        String[] words = queryString.split("\\s+");
        StringBuilder correctedQuery = new StringBuilder();
        for (String word : words) {
            String[] suggestions = spellChecker.suggestSimilar(word, 1);
            if (suggestions.length > 0) {
                correctedQuery.append(suggestions[0]).append(" ");
            } else {
                correctedQuery.append(word).append(" ");
            }
        }

        Query query = parser.parse(correctedQuery.toString().trim());
        long queryEndTime = System.currentTimeMillis();
        long queryParsingTime = queryEndTime - queryStartTime;

        // Measure execution time
        long executionStartTime = System.currentTimeMillis();
        DirectoryReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs results = searcher.search(query, 10);
        long executionEndTime = System.currentTimeMillis();
        long executionTime = executionEndTime - executionStartTime;

        // Display results
        System.out.println("Original Query: " + queryString);
        System.out.println("Corrected Query: " + correctedQuery.toString().trim());
        System.out.println("Query parsing time: " + queryParsingTime + " ms");
        System.out.println("Execution time: " + executionTime + " ms");
        System.out.println("Total hits: " + results.totalHits);

        for (ScoreDoc scoreDoc : results.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            System.out.println("Score: " + scoreDoc.score);
            System.out.println("Document: " + doc);
        }

        reader.close();
    }
}
