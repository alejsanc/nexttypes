/*
 * Copyright 2015-2018 Alejandro Sánchez <alex@nexttypes.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nexttypes.system;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import com.nexttypes.enums.NodeMode;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.settings.Settings;

public class DatabaseConnection {

	public static class DatabaseConnectionPool {
		protected DataSource read;
		protected DataSource write;
		protected DataSource admin;

		protected DatabaseConnectionPool(Settings settings, String schema, String driver) {
			
			String url = url(settings, schema);

			read = createDataSource(url, NodeMode.READ, settings, driver);
			write = createDataSource(url, NodeMode.WRITE, settings, driver);
			admin = createDataSource(url, NodeMode.ADMIN, settings, driver);
		}

		public Connection getConnection(NodeMode mode) {
			Connection connection = null;
    
			try {
				switch (mode) {
				case READ:
					connection = read.getConnection();
					break;
				case WRITE:
					connection = write.getConnection();
					break;
				case ADMIN:
					connection = admin.getConnection();
					break;
				}

			} catch (SQLException e) {
				throw new NXException(e);
			}

			return connection;
		}
		
		public void close() {
			read.close();
			write.close();
			admin.close();
		}

		protected DataSource createDataSource(String url, NodeMode mode, Settings settings, String driver) {
			String user = settings.getString(mode + "_" + Constants.USER);
			String password = settings.getString(mode + "_" + Constants.PASSWORD);
			int maxConnections = settings.getInt32(mode + "_" + Constants.MAX_CONNECTIONS);
			int maxTime = settings.getInt32(mode + "_" + Constants.MAX_TIME);

			PoolProperties p = new PoolProperties();
			p.setUrl(url); 
			p.setUsername(user);
			p.setPassword(password);
			p.setDriverClassName(driver);
			p.setJmxEnabled(true);
			p.setTestWhileIdle(false);
			p.setTestOnBorrow(true);
			p.setValidationQuery("SELECT 1");
			p.setTestOnReturn(false);
			p.setValidationInterval(30000);
			p.setTimeBetweenEvictionRunsMillis(30000);
			p.setMaxActive(maxConnections);
			p.setInitialSize(maxConnections / 10);
			p.setMaxWait(20000);
			p.setRemoveAbandonedTimeout(maxTime * 60);
			p.setMinEvictableIdleTimeMillis(30000);
			p.setMinIdle(maxConnections / 10);
			p.setMaxIdle(maxConnections / 2);
			p.setRemoveAbandoned(true);
			p.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
					+ "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");

			if (NodeMode.READ.equals(mode)) {
				p.setDefaultReadOnly(true);
			}
			p.setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			p.setDefaultAutoCommit(false);
			p.setRollbackOnReturn(true);

			DataSource dataSource = new DataSource();
			dataSource.setPoolProperties(p);
			return dataSource;
		}
	}

	protected static String url(Settings settings, String schema) {
		String host = settings.getString(Constants.HOST);
		String port = settings.getString(Constants.PORT);
		String database = settings.getString(Constants.DATABASE);
		return "jdbc:" + schema + "://" + host + ":" + port + "/" + database;
	}

	public static Connection getConnection(Settings settings, String schema, NodeMode mode) {
		String user = settings.getString(mode + "_" + Constants.USER);
		String password = settings.getString(mode + "_" + Constants.PASSWORD);
		String url = url(settings, schema);

		try {
			Connection connection = DriverManager.getConnection(url, user, password);
			connection.setAutoCommit(false);
			return connection;
		} catch (SQLException e) {
			throw new NXException(e);
		}
	}

	public static DatabaseConnectionPool getConnectionPool(Settings settings, String schema, String driver) {
		return new DatabaseConnectionPool(settings, schema, driver);
	}
}