/*
 * Copyright (c) 2012 - Batoo Software ve Consultancy Ltd.
 * 
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.batoo.jpa.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Abstract class for implementing pluggable data source
 * 
 * @author asimarslan
 * @since $version
 */
public abstract class AbstractDataSource implements DataSource {

	/**
	 * finalize the underlining implementation
	 * 
	 * @since $version
	 */
	public abstract void close();

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public abstract Connection getConnection() throws SQLException;

	/**
	 * initialize the underlining implementation
	 * 
	 * @param persistanceUnitName
	 *            the name of the persistence unit
	 * @param hintName
	 *            the hint to be passed
	 * 
	 * @since $version
	 */

	public abstract void open(String persistanceUnitName, String hintName);

	/**
	 * release the connection
	 * 
	 * @param connection
	 *            the connection
	 * 
	 * @since $version
	 */
	public abstract void releaseConnection(Connection connection);

}
