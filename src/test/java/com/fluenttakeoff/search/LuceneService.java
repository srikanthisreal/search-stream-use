package com.fluenttakeoff.search;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class LuceneService {

    private Directory index;

    @PostConstruct
    public void init() throws IOException {
        index = FSDirectory.open(Paths.get("src/main/resources/lucene/index"));
        try (IndexWriter writer = new IndexWriter(index, new IndexWriterConfig(new StandardAnalyzer()))) {
            indexDocuments(writer, "src\\test\\resources\\data\\str-doc.txt");
            indexDocuments(writer, "src\\test\\resources\\data\\poi-doc.txt");
            indexDocuments(writer, "src\\test\\resources\\data\\xstr-doc.txt");
        }
    }

    private void indexDocuments(IndexWriter writer, String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Map<String, String> fields = parseLine(line);
                Document doc = new Document();
                for (Map.Entry<String, String> entry : fields.entrySet()) {
                    if (entry.getKey().equals("NO") || entry.getKey().equals("postcode")) {
                        doc.add(new StringField(entry.getKey(), entry.getValue(), org.apache.lucene.document.Field.Store.YES));
                    } else {
                        doc.add(new TextField(entry.getKey(), entry.getValue(), org.apache.lucene.document.Field.Store.YES));
                    }
                }
                writer.addDocument(doc);
            }
        }
    }

    private Map<String, String> parseLine(String line) {
        Map<String, String> fields = new HashMap<>();
        String[] keyValuePairs = line.split("\\|");
        for (String pair : keyValuePairs) {
            String[] entry = pair.split("=");
            if (entry.length == 2) {
                fields.put(entry[0], entry[1]);
            }
        }
        return fields;
    }

    public TopDocs searchIndex(String queryString) throws Exception {
        try (DirectoryReader reader = DirectoryReader.open(index)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser parser = new QueryParser("content", new StandardAnalyzer());
            Query query = parser.parse(queryString);
            return searcher.search(query, 10);
        }
    }

    public Document getDocument(int docId) throws IOException {
        try (DirectoryReader reader = DirectoryReader.open(index)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            return searcher.doc(docId);
        }
    }
}
