/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.batoo.jpa.core.impl.jdbc;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.JoinType;

import org.apache.commons.dbutils.QueryRunner;
import org.batoo.jpa.core.impl.model.type.EntityTypeImpl;
import org.batoo.jpa.parser.metadata.JoinColumnMetadata;
import org.batoo.jpa.parser.metadata.JoinTableMetadata;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * 
 * 
 * @author hceylan
 * @since $version
 */
public class JoinTable extends AbstractTable {

	private final ForeignKey sourceKey;
	private final ForeignKey destinationKey;
	private final EntityTypeImpl<?> entity;

	private String removeSql;
	private JoinColumn[] sourceRemoveColumns;
	private JoinColumn[] destinationRemoveColumns;

	/**
	 * @param entity
	 *            the owner entity
	 * @param metadata
	 *            the metadata
	 * 
	 * @since $version
	 * @author hceylan
	 */
	public JoinTable(EntityTypeImpl<?> entity, JoinTableMetadata metadata) {
		super(metadata);

		this.entity = entity;

		this.sourceKey = new ForeignKey(metadata != null ? metadata.getJoinColumns() : Collections.<JoinColumnMetadata> emptyList());
		this.destinationKey = new ForeignKey(metadata != null ? metadata.getInverseJoinColumns() : Collections.<JoinColumnMetadata> emptyList());
	}

	/**
	 * Creates a join between the source and destination entities
	 * 
	 * @param joinType
	 *            the type of the join
	 * @param parentAlias
	 *            the alias of the parent table
	 * @param alias
	 *            the alias of the table
	 * @param forward
	 *            if the join if forward or backwards
	 * @return the join SQL fragment
	 * 
	 * @since $version
	 * @author hceylan
	 */
	public String createJoin(JoinType joinType, String parentAlias, String alias, boolean forward) {
		String sourceJoin, destinationJoin;

		if (forward) {
			sourceJoin = this.sourceKey.createSourceJoin(joinType, parentAlias, alias + "_J");
			destinationJoin = this.destinationKey.createDestinationJoin(joinType, alias + "_J", alias);
		}
		else {
			sourceJoin = this.destinationKey.createSourceJoin(joinType, parentAlias, alias + "_J");
			destinationJoin = this.sourceKey.createDestinationJoin(joinType, alias + "_J", alias);
		}

		return sourceJoin + "\n" + destinationJoin;
	}

	/**
	 * Returns the destinationKey of the JoinTable.
	 * 
	 * @return the destinationKey of the JoinTable
	 * 
	 * @since $version
	 * @author hceylan
	 */
	public ForeignKey getDestinationKey() {
		return this.destinationKey;
	}

	/**
	 * Returns the oner entity of the table.
	 * 
	 * @return the oner entity of the table
	 * 
	 * @since $version
	 * @author hceylan
	 */
	public EntityTypeImpl<?> getEntity() {
		return this.entity;
	}

	private String getRemoveSql() {
		if (this.removeSql != null) {
			return this.removeSql;
		}

		synchronized (this) {
			if (this.removeSql != null) {
				return this.removeSql;
			}

			final List<String> restrictions = Lists.newArrayList();
			this.sourceRemoveColumns = new JoinColumn[this.sourceKey.getJoinColumns().size()];
			this.destinationRemoveColumns = new JoinColumn[this.sourceKey.getJoinColumns().size()];

			int i = 0;
			for (final JoinColumn column : this.sourceKey.getJoinColumns()) {
				restrictions.add(column.getName() + " = ?");
				this.sourceRemoveColumns[i++] = column;
			}

			i = 0;
			for (final JoinColumn column : this.destinationKey.getJoinColumns()) {
				restrictions.add(column.getName() + " = ?");
				this.destinationRemoveColumns[i++] = column;
			}

			return this.removeSql = "DELETE FROM " + this.getQName() + " WHERE " + Joiner.on(" AND ").join(restrictions);
		}
	}

	/**
	 * Returns the sourceKey of the JoinTable.
	 * 
	 * @return the sourceKey of the JoinTable
	 * 
	 * @since $version
	 * @author hceylan
	 */
	public ForeignKey getSourceKey() {
		return this.sourceKey;
	}

	/**
	 * @param source
	 *            the source entity
	 * @param destination
	 *            the destination entity
	 * 
	 * @since $version
	 * @author hceylan
	 */
	public void link(EntityTypeImpl<?> source, EntityTypeImpl<?> destination) {
		if (this.getName() == null) {
			this.setName(source.getName() + "_" + destination.getName());
		}

		this.sourceKey.link(null, source);
		this.destinationKey.link(null, destination);

		this.sourceKey.setTable(this);
		this.destinationKey.setTable(this);
	}

	/**
	 * Performs the insert for the join.
	 * 
	 * @param connection
	 *            the connection
	 * @param source
	 *            the source instance
	 * @param destination
	 *            the destination instance
	 * 
	 * @throws SQLException
	 *             thrown if there is an underlying SQL Exception
	 * 
	 * @since $version
	 * @author hceylan
	 */
	public void performInsert(ConnectionImpl connection, Object source, Object destination) throws SQLException {
		final String insertSql = this.getInsertSql(null);
		final AbstractColumn[] insertColumns = this.getInsertColumns(null);

		// prepare the parameters
		final Object[] params = new Object[insertColumns.length];
		for (int i = 0; i < insertColumns.length; i++) {
			final AbstractColumn column = insertColumns[i];

			final Object object = this.sourceKey.getJoinColumns().contains(column) ? source : destination;
			params[i] = column.getValue(object);
		}

		new QueryRunner().update(connection, insertSql, params);
	}

	/**
	 * Performs the remove for the join.
	 * 
	 * @param connection
	 *            the connection
	 * @param source
	 *            the source instance
	 * @param destination
	 *            the destination instance
	 * 
	 * @throws SQLException
	 *             thrown if there is an underlying SQL Exception
	 * 
	 * @since $version
	 * @author hceylan
	 */
	public void performRemove(ConnectionImpl connection, Object source, Object destination) throws SQLException {
		final String removeSql = this.getRemoveSql();

		final Object[] params = new Object[this.sourceKey.getJoinColumns().size() + this.destinationKey.getJoinColumns().size()];

		int i = 0;
		for (final JoinColumn sourceRemoveColumn : this.sourceRemoveColumns) {
			params[i++] = sourceRemoveColumn.getValue(source);
		}

		for (final JoinColumn destinationRemoveColumn : this.destinationRemoveColumns) {
			params[i++] = destinationRemoveColumn.getValue(destination);
		}

		new QueryRunner().update(connection, removeSql, params);
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
}
