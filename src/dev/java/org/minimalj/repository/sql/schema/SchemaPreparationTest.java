package org.minimalj.repository.sql.schema;

//import java.io.File;
//import java.io.FileInputStream;
//import java.io.PrintWriter;
//import java.util.Map;
//
//import org.junit.Test;
//
//import cz.startnet.utils.pgdiff.PgDiff;
//import cz.startnet.utils.pgdiff.PgDiffArguments;
//import cz.startnet.utils.pgdiff.loader.PgDumpLoader;
//import cz.startnet.utils.pgdiff.schema.PgDatabase;

public class SchemaPreparationTest {

//	@Test
//	public void export() throws Exception {
//		File file = File.createTempFile("backup", "bk");
//		String path = file.getAbsolutePath();
//		System.out.println(path);
//
//		String cmd = "C:\\Data\\programme\\postgresql\\bin\\pg_dump.exe";
//		
//		System.out.println(cmd);
//		System.out.println(path);
//        
//        ProcessBuilder pb = new ProcessBuilder(cmd, "-s", "--username=postgres", "--file=\"" + path + "\"", "postgres");
//        Map<String, String> env = pb.environment();
//        env.put("PGPASSWORD", "Jodo23");
//        Process process = pb.start();
//        process.waitFor();
//        
//        PgDiffArguments arguments = new PgDiffArguments();
//        arguments.setOldDumpFile(path);
//        arguments.setNewDumpFile(path);
//        
//        PgDatabase oldDatabase = PgDumpLoader.loadDatabaseSchema(
//                path, arguments.getInCharsetName(),
//                arguments.isOutputIgnoredStatements(),
//                arguments.isIgnoreSlonyTriggers(),
//                arguments.isIgnoreSchemaCreation());
//        final PgDatabase newDatabase = PgDumpLoader.loadDatabaseSchema(
//                arguments.getNewDumpFile(), arguments.getInCharsetName(),
//                arguments.isOutputIgnoredStatements(),
//                arguments.isIgnoreSlonyTriggers(),
//                arguments.isIgnoreSchemaCreation());
//
//        
//        final PrintWriter writer = new PrintWriter(System.out, true);
//        PgDiff.createDiff(writer, arguments, new FileInputStream(path), new FileInputStream(path));
//        Thread.sleep(200);
//        writer.close();
////        java.lang.Process p = rt.exec(cmd);
//	}
}
