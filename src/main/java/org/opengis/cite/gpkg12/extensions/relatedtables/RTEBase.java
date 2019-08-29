package org.opengis.cite.gpkg12.extensions.relatedtables;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.opengis.cite.gpkg12.CommonFixture;
import org.opengis.cite.gpkg12.ErrorMessage;
import org.opengis.cite.gpkg12.ErrorMessageKeys;
import org.opengis.cite.gpkg12.util.DatabaseUtility;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;

public abstract class RTEBase extends CommonFixture {

	@BeforeClass
	public void activeExtension(ITestContext testContext) throws SQLException {
		Assert.assertTrue(DatabaseUtility.doesTableOrViewExist(this.databaseConnection, "gpkg_extensions"), 
				ErrorMessage.format(ErrorMessageKeys.CONFORMANCE_CLASS_NOT_USED, "Related Tables Extension"));
		
		try (
				final Statement statement = this.databaseConnection.createStatement();
				
				final ResultSet resultSet = statement.executeQuery(
						"SELECT count(*) from gpkg_extensions WHERE extension_name IN ('related_tables', 'gpkg_related_tables');");
				) {
			resultSet.next();
			
			Assert.assertTrue(resultSet.getInt(1) > 0, 
					ErrorMessage.format(ErrorMessageKeys.CONFORMANCE_CLASS_NOT_USED, "Related Tables Extension"));				
		}
	}
	
	public void testRequirementsClassActive(String relationName, String className) throws SQLException {
		try (
				final Statement statement = this.databaseConnection.createStatement();
				
				final ResultSet resultSet = statement.executeQuery(
						String.format("SELECT COUNT(*) FROM gpkgext_relations WHERE relation_name = '%s'", relationName));
				) {
			resultSet.next();
			
			Assert.assertTrue(resultSet.getInt(1) > 0, 
					ErrorMessage.format(ErrorMessageKeys.CONFORMANCE_CLASS_NOT_USED, 
							String.format("Related Tables Extension, %s Requirements Class", className)));				
		}
	}
	
	public void testRelatedType(String relationName, String requiredType) throws SQLException {
		try (
				final Statement statement = this.databaseConnection.createStatement();
				
				final ResultSet resultSet = statement.executeQuery(
						String.format("SELECT related_table_name FROM gpkgext_relations WHERE relation_name = '%s';", relationName));
				) {
			while (resultSet.next()) {
				
				final String relatedTableName = resultSet.getString("related_table_name");
				
				try (
						final Statement statement2 = this.databaseConnection.createStatement();
						
						final ResultSet resultSet2 = statement2.executeQuery(
								String.format("SELECT data_type FROM gpkg_contents WHERE table_name = '%s';", relatedTableName));
						) {

					Assert.assertTrue(resultSet2.next(), 
							ErrorMessage.format(ErrorMessageKeys.MISSING_REFERENCE, "gpkg_contents", "table_name", relatedTableName));
					final String dataType = resultSet2.getString("data_type");
					Assert.assertEquals(dataType, requiredType, 
							ErrorMessage.format(ErrorMessageKeys.INVALID_DATA_TYPE, dataType, relatedTableName, "gpkgext_relations"));
				}
			}
		}
	}
}
