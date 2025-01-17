/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.test.schemaupdate.foreignkeys.crossschema;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.junit4.BaseUnitTestCase;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Andrea Boriero
 */

public class CrossSchemaForeignKeyGenerationTest extends BaseUnitTestCase {
	private File output;
	private StandardServiceRegistry ssr;
	private MetadataImplementor metadata;

	@Before
	public void setUp() throws IOException {
		output = File.createTempFile( "update_script", ".sql" );
		output.deleteOnExit();
		ssr = new StandardServiceRegistryBuilder().build();
	}

	@After
	public void tearsDown() {
		StandardServiceRegistryBuilder.destroy( ssr );
	}

	@Test
	@TestForIssue(jiraKey = "HHH-10420")
	public void testForeignKeysAreGeneratedAfterAllTheTablesAreCreated() throws Exception {

		final MetadataSources metadataSources = new MetadataSources( ssr );

		metadataSources.addAnnotatedClass( SchemaOneEntity.class );
		metadataSources.addAnnotatedClass( SchemaTwoEntity.class );
		metadata = (MetadataImplementor) metadataSources.buildMetadata();
		metadata.validate();
		final SchemaExport schemaExport = new SchemaExport( metadata )
				.setHaltOnError( true )
				.setOutputFile( output.getAbsolutePath() )
				.setFormat( false );
		schemaExport.create( true, false );

		final List<String> sqlLines = Files.readAllLines( output.toPath() );
		assertThat(
				"Expected alter table SCHEMA1.Child add constraint but is : " + sqlLines.get( 4 ),
				sqlLines.get( 4 ).startsWith( "alter table " ),
				is( true )
		);
	}
}
