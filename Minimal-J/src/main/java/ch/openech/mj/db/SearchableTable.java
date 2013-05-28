package ch.openech.mj.db;//

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import ch.openech.mj.model.Keys;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.util.StringUtils;

public abstract class SearchableTable<T> extends HistorizedTable<T> {
	private static final Logger logger = Logger.getLogger(SearchableTable.class.getName());
	
	private static final Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_34);
	private RAMDirectory directory;
	private IndexWriter iwriter;
	private PreparedStatement selectAll;

	private final Object[] keys;
	private final PropertyInterface[] properties;
	private final String[] propertyPaths;
	
	public SearchableTable(DbPersistence dbPersistence, Class<T> clazz, Object[] keys) {
		super(dbPersistence, clazz);
		this.keys = keys;
		this.properties = Keys.getProperties(keys);
		this.propertyPaths = getPropertyPaths(properties);
	}
	
	private static String[] getPropertyPaths(PropertyInterface[] properties) {
		String[] paths = new String[properties.length];
		for (int i = 0; i<properties.length; i++) {
			paths[i] = properties[i].getFieldPath();
		}
		return paths;
	}
	
	@Override
	public void initialize() throws SQLException {
		super.initialize();
		selectAll = prepareSelectAll();
	}
	
	@SuppressWarnings("deprecation")
	private void initializeIndex() {
		if (directory == null) {
			directory = new RAMDirectory();
			try {
				iwriter = new IndexWriter(directory, analyzer, true, new IndexWriter.MaxFieldLength(25000));
				refillIndex();
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Initialize index failed", e);
				throw new RuntimeException("Initialize index failed");
			}
		}
	}

	protected List<T> getAll() throws SQLException {
		return executeSelectAll(selectAll);
	}
	
	protected void refillIndex() {
		try {
			ResultSet resultSet = selectAll.executeQuery();
			int count = 0;
			while (resultSet.next()) {
				ObjectWithId<T> objectWithId = readResultSetRow(resultSet, null);
				writeInIndex(objectWithId.id, objectWithId.object);
				if (logger.isLoggable(Level.FINER)) logger.finer("RefillIndex: " + getClazz() + " / " + objectWithId.id);
				count++;
			}
			resultSet.close();
			logger.fine("Refilled the index " + getClazz() +" with " + count);
			logger.fine("Size of index is " + directory.sizeInBytes());
		} catch (SQLException x) {
			logger.log(Level.SEVERE, "Couldn't refill index on " + getTableName(), x);
			throw new RuntimeException("Couldn't refill index on " + getTableName());
		}
	}
	
	@Override
	public int insert(T object) {
		initializeIndex();
		int id = super.insert(object);
		writeInIndex(id, object);

		Query query = queryById(id);
		try (IndexSearcher isearcher = new IndexSearcher(directory, true)) {
			ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;
			if (hits.length > 1) throw new IllegalStateException("Twice in index : " + id);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Couldn't search after insert");
			throw new RuntimeException("Couldn't search after insert");
		} 	
		return id;
	}

	@Override
	public void update(T object) {
		super.update(object);

		Integer id = getId(object);
		Query query = queryById(id);
		
		try (IndexSearcher isearcher = new IndexSearcher(directory, true)) {
			ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;
			if (hits.length != 1) throw new IllegalStateException("Id : " + id);

			iwriter.deleteDocuments(query);
			writeInIndex(id, object);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Couldn't search after update");
			throw new RuntimeException("Couldn't search after update");
		} 	
	}

	private Query queryById(Integer id) {
		QueryParser	parser = new QueryParser(Version.LUCENE_34, "id", analyzer);
		Query query;
		try {
			query = parser.parse(Integer.toString(id));
		} catch (ParseException e) {
			logger.log(Level.SEVERE, "Couldn't parse id " + id, e);
			throw new RuntimeException("Couldn't parse id " + id);
		}
		return query;
	}
	
	@Override
	public void clear() {
		super.clear();
		try {
			if (iwriter != null) {
				iwriter.deleteAll();
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Couldn't clear " + getTableName(), e);
			throw new RuntimeException("Couldn't clear " + getTableName());
		}	
	}

	public int count(String text) {
		return find(text).size();
	}
	
	/**
	 * 
	 * @param property a (minimal-j) property
	 * @param object
	 * @return a field (in terms of lucene) with the value of the property
	 */
	protected abstract Field getField(PropertyInterface property, T object);

	protected Field createIdField(int id) {
		Field.Index index = Field.Index.NOT_ANALYZED;
		
		return new Field("id", Integer.toString(id), Field.Store.YES, index);
	}
	
	private void writeInIndex(int id, T object) {
		try {
			Document doc = new Document();
			doc.add(createIdField(id));
			for (PropertyInterface property : properties) {
				Field field = getField(property, object);
				if (field != null) {
					doc.add(field);
				}
			}
		    iwriter.addDocument(doc);
		    iwriter.commit();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<T> find(String text) {
		return find(text, keys);
	}
	
	public List<T> find(String text, Object... searchKeys) {
		PropertyInterface[] searchProperties = Keys.getProperties(searchKeys);
		String[] searchPaths = getPropertyPaths(searchProperties);
		
		List<T> result = new ArrayList<T>();
		initializeIndex();
		if (directory.listAll().length == 0 || StringUtils.isBlank(text)) return result;

		try (IndexSearcher isearcher = new IndexSearcher(directory, true)) {
			QueryParser parser;
			if (searchPaths.length > 1) {
				parser = new MultiFieldQueryParser(Version.LUCENE_34, searchPaths, analyzer);
			} else {
				parser = new QueryParser(Version.LUCENE_34, searchPaths[0], analyzer);
			}
			Query query = parser.parse(text);
			ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;
			
			for (int i = 0; i < hits.length; i++) {
				Document hitDoc = isearcher.doc(hits[i].doc);
				T object = documentToObject(hitDoc);
				result.add(object);
			}
		} catch (ParseException x) {
			// user entered something like "*" which is not allowed
			logger.info(x.getLocalizedMessage());
		} catch (Exception x) {
			logger.log(Level.SEVERE, x.getLocalizedMessage(), x);
			x.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 
	 * @return an instance of the type of the class. Normally just a <code>new T()</code>
	 */
	protected abstract T createResultObject();

	protected T documentToObject(Document document) {
		T result = createResultObject();
		
		for (PropertyInterface property : properties) {
			property.setValue(result, document.get(property.getFieldName()));
		}
		return result;
	}
	
	// Statements

	protected PreparedStatement prepareSelectAll() throws SQLException {
		StringBuilder s = new StringBuilder();
		s.append("SELECT * FROM "); s.append(getTableName()); s.append(" WHERE version = 0");
		return getConnection().prepareStatement(s.toString());
	}
}
