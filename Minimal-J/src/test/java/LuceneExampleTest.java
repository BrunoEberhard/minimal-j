import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;

public class LuceneExampleTest {

	/**
	 * This mostly the example found in the Lucene Javadoc
	 * 
	 * @throws Exception
	 */
	@Test
	public void simpleSearch() throws Exception {
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_45);

		Directory directory = new RAMDirectory();
		// To store an index on disk, use this instead: Directory directory =
		// FSDirectory.open("/tmp/testindex");

		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_45, analyzer);
		IndexWriter iwriter = new IndexWriter(directory, config);

		Document doc = new Document();
		String text = "This is the text to be indexed.";
		doc.add(new TextField("fieldname", text, Field.Store.YES));
		iwriter.addDocument(doc);
		iwriter.close();

		IndexReader ireader = DirectoryReader.open(directory);
		IndexSearcher isearcher = new IndexSearcher(ireader);

		QueryParser parser = new QueryParser(Version.LUCENE_45, "fieldname", analyzer);
		Query query = parser.parse("text");
		ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;

		Assert.assertEquals(1, hits.length);
		for (int i = 0; i < hits.length; i++) {
			Document hitDoc = isearcher.doc(hits[i].doc);
			Assert.assertEquals("This is the text to be indexed.", hitDoc.get("fieldname"));
		}

		ireader.close();
		directory.close();
	}

	/**
	 * Strange behaviour for UUID search
	 * 
	 * @throws Exception
	 */
	@Test
	public void searchUUID() throws Exception {
		final String ID_TEXT = "2d15810b-b7bc-4393-b58a-88fddc885656";

		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_45);

		Directory directory = new RAMDirectory();
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_45, analyzer);
		IndexWriter iwriter = new IndexWriter(directory, config);

		Document doc = new Document();
		doc.add(new StringField("fieldname", ID_TEXT, Field.Store.YES));
		iwriter.addDocument(doc);
		iwriter.close();

		IndexReader ireader = DirectoryReader.open(directory);
		IndexSearcher isearcher = new IndexSearcher(ireader);
		QueryParser parser = new QueryParser(Version.LUCENE_45, "fieldname", analyzer);
		// WHY IS THE ASTERISK NEEDED??
		Query query = parser.parse(ID_TEXT + "*");
		ScoreDoc[] hits = isearcher.search(query, 1).scoreDocs;

		Assert.assertEquals(1, hits.length);
		for (int i = 0; i < hits.length; i++) {
			Document hitDoc = isearcher.doc(hits[i].doc);
			System.out.println(hitDoc.get("fieldname"));
			Assert.assertEquals(ID_TEXT, hitDoc.get("fieldname"));
		}
		
		ireader.close();
		directory.close();
	}

	/**
	 * Search in more than one field
	 * 
	 * @throws Exception
	 */
	@Test
	public void searchMultiFieldQuery() throws Exception {
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_45);

		Directory directory = new RAMDirectory();

		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_45, analyzer);
		IndexWriter iwriter = new IndexWriter(directory, config);
		writeDocument(analyzer, directory, iwriter);
		writeDocument(analyzer, directory, iwriter);
		iwriter.close();

		IndexReader ireader = DirectoryReader.open(directory);
		IndexSearcher isearcher = new IndexSearcher(ireader);
		MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_45, new String[] { "a.fieldname",
				"a.fieldname2", "datum" }, analyzer);
		Query query = parser.parse("Zweiter");

		ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;
		Assert.assertEquals(2, hits.length);

		for (int i = 0; i < hits.length; i++) {
			Document hitDoc = isearcher.doc(hits[i].doc);
			Assert.assertNotNull(hitDoc.get("a.fieldname"));
			Assert.assertNotNull(hitDoc.get("datum"));
		}
		
		ireader.close();
		directory.close();
	}

	static int count = 13;

	private static void writeDocument(Analyzer analyzer, Directory d, IndexWriter iwriter)
			throws CorruptIndexException, LockObtainFailedException, IOException {
		Document doc = new Document();
		String text = "This is the text to be indexed.";
		doc.add(new StringField("id", "" + count++, Field.Store.YES));
		doc.add(new TextField("a.fieldname", text, Field.Store.YES));
		doc.add(new TextField("a.fieldname2", "Zweiter text" + count, Field.Store.YES));
		doc.add(new TextField("datum", DateTools.dateToString(new Date(), DateTools.Resolution.DAY), Field.Store.YES));
		iwriter.addDocument(doc);
	}

	@Test
	public void format() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 2008);
		c.set(Calendar.MONTH, 6);
		c.set(Calendar.DATE, 4);
		Date date = c.getTime();
		Assert.assertEquals("20080704", DateTools.dateToString(date, DateTools.Resolution.DAY));
	}
}
