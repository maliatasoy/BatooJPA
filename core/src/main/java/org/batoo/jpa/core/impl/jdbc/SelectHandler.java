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

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.ResultSetHandler;
import org.batoo.jpa.core.impl.SessionImpl;
import org.batoo.jpa.core.impl.instance.ManagedInstance;
import org.batoo.jpa.core.impl.mapping.Association;
import org.batoo.jpa.core.impl.types.EntityTypeImpl;

/**
 * A {@link ResultSetHandler} to load a single result from resultset.
 * 
 * @author hceylan
 * @since $version
 */
public class SelectHandler<X> extends BaseSelectHandler<X> {

	/**
	 * @param session
	 *            the active session
	 * @param rootType
	 *            the root type
	 * @param columnAliases
	 *            the aliases for the columns
	 * @param tableAliases
	 *            the aliases for all the tables
	 * @param entityPaths
	 *            the entity path
	 * @param lazyPaths
	 *            the inverse paths
	 * @param lazyPaths
	 *            the lazy paths
	 * @since $version
	 * @author hceylan
	 */
	public SelectHandler(SessionImpl session, EntityTypeImpl<X> rootType, Map<Integer, Map<PhysicalColumn, String>> columnAliases,
		List<Deque<Association<?, ?>>> entityPaths, Set<Deque<Association<?, ?>>> inversePaths, Set<Deque<Association<?, ?>>> lazyPaths) {
		super(session, rootType, columnAliases, entityPaths, inversePaths, lazyPaths);
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	protected boolean shouldHandle(ManagedInstance<?> managedInstance) {
		return (managedInstance != null) && (!managedInstance.isLoaded());
	}

}