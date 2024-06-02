package com.fluenttakeoff.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import static com.fluenttakeoff.search.QueryParser.parseLine;

public class Indexing {
    private static void extracted(IndexWriter writer) throws IOException {
        // Index sample documents
        indexDocuments(writer, "data/str-doc.txt");
        indexDocuments(writer, "data/poi-doc.txt");
        indexDocuments(writer, "data/xstr-doc.txt");
    }

    public static void indexDocuments(IndexWriter writer, String filePath) throws IOException {
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

}
