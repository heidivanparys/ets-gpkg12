package org.opengis.cite.gpkg12.extensions.relatedtables;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.opengis.cite.gpkg12.ErrorMessage;
import org.opengis.cite.gpkg12.ErrorMessageKeys;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Defines test methods that apply to the "simple attributes" 
 * requirements class of the Related Tables Extension.
 *
 * <p style="margin-bottom: 0.5em">
 * <strong>Sources</strong>
 * </p>
 * <ul>
 * <li><a href="http://docs.opengeospatial.org/is/18-000/18-000.html#sa_rc" target= "_blank">
 * GeoPackage Related Tables Extension</a> (OGC 18-000)</li>
 * </ul>
 *
 * @author Jeff Yutzler
 */
public class SimpleAttributesTests extends RTEBase
{
	@BeforeClass
	public void activeExtension(ITestContext testContext) throws SQLException {
		super.activeExtension(testContext);
		try (
				final Statement statement = this.databaseConnection.createStatement();
				
				final ResultSet resultSet = statement.executeQuery(
						"SELECT COUNT(*) FROM gpkgext_relations WHERE relation_name = 'simple_attributes'");
				) {
			resultSet.next();
			
			Assert.assertTrue(resultSet.getInt(1) > 0, 
					ErrorMessage.format(ErrorMessageKeys.CONFORMANCE_CLASS_NOT_USED, "Related Tables Extension, Simple Attributes Requirements Class"));				
		}
	}
	
    /**
     * A user-defined simple attribute table or view SHALL contain the 
     * primary key column and at least one other column as defined in 
     * User-Defined Simple Attributes Table Definition. A user-defined 
     * simple attribute table SHALL only contain data belonging to the TEXT, 
     * INTEGER, or REAL storage classes and SHALL NOT contain NULL or BLOB 
     * storage classes (e.g., GEOMETRY).
     * 
     * @see <a href="http://docs.opengeospatial.org/is/18-000/18-000.html#r15" target=
     *      "_blank">OGC 18-000 Requirement 15</a> 
     */
    @Test(description = "See OGC 18-000: Requirement 15")
    public void attributesTableDefinition() throws SQLException {
    	
		try (
				final Statement statement = this.databaseConnection.createStatement();
				
				final ResultSet resultSet = statement.executeQuery(
						"SELECT related_table_name FROM gpkgext_relations WHERE relation_name = 'simple_attributes';");
				) {
			
			while (resultSet.next()) {
	    		final String simpleAttributesTableName = resultSet.getString("related_table_name");
	    		getPrimaryKeyColumn(simpleAttributesTableName, true);
	    		try (
	    				final Statement statement2 = this.databaseConnection.createStatement();
	    				
	    				final ResultSet resultSet2 = statement2.executeQuery(
	    						String.format("PRAGMA table_info(%s)", simpleAttributesTableName));
	    				) {
		    		while (resultSet2.next()) {
		    			final String type = resultSet2.getString("type");
		    			final String name = resultSet2.getString("name");
		    			switch (type) {
			    			case "INTEGER":
			    			case "REAL":
			    			case "TEXT":
			    				break;
		    				default:
		    					Assert.fail(ErrorMessage.format(ErrorMessageKeys.INVALID_DATA_TYPE, type, name, simpleAttributesTableName));
		    			}
		    			final int notNull = resultSet2.getInt("notnull");
		    			Assert.assertEquals(notNull, 1, ErrorMessage.format(ErrorMessageKeys.INVALID_COLUMN_DEFINITION, 
		    					name, simpleAttributesTableName, "notnull", 1, notNull));
		    		} 
	    		}
			}
		}
    }
}
