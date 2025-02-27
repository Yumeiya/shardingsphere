/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.federation.optimizer.context.planner;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelOptTable.ViewExpander;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.sql2rel.SqlToRelConverter.Config;
import org.apache.calcite.sql2rel.StandardConvertletTable;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationDatabaseMetaData;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationMetaData;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.calcite.FederationDatabase;
import org.apache.shardingsphere.infra.federation.optimizer.planner.QueryOptimizePlannerFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Optimizer planner context factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OptimizerPlannerContextFactory {
    
    /**
     * Create optimizer planner context map.
     *
     * @param metaData federation meta data
     * @return created optimizer planner context map
     */
    public static Map<String, OptimizerPlannerContext> create(final FederationMetaData metaData) {
        Map<String, OptimizerPlannerContext> result = new ConcurrentHashMap<>(metaData.getDatabases().size(), 1);
        for (Entry<String, FederationDatabaseMetaData> entry : metaData.getDatabases().entrySet()) {
            result.put(entry.getKey(), create(entry.getValue()));
        }
        return result;
    }
    
    /**
     * Create optimizer planner context.
     *
     * @param databaseMetaData federation database meta data
     * @return created optimizer planner context
     */
    public static OptimizerPlannerContext create(final FederationDatabaseMetaData databaseMetaData) {
        Map<String, SqlValidator> validators = new LinkedHashMap<>();
        Map<String, SqlToRelConverter> converters = new LinkedHashMap<>();
        Map<String, RelOptPlanner> hepPlanners = new LinkedHashMap<>();
        FederationDatabase federationDatabase = new FederationDatabase(databaseMetaData);
        for (Entry<String, Schema> entry : federationDatabase.getSubSchemaMap().entrySet()) {
            CalciteConnectionConfig connectionConfig = new CalciteConnectionConfigImpl(createConnectionProperties());
            RelDataTypeFactory relDataTypeFactory = new JavaTypeFactoryImpl();
            CalciteCatalogReader catalogReader = createCatalogReader(entry.getKey(), entry.getValue(), relDataTypeFactory, connectionConfig);
            SqlValidator validator = createValidator(catalogReader, relDataTypeFactory, connectionConfig);
            SqlToRelConverter converter = createConverter(catalogReader, validator, relDataTypeFactory);
            validators.put(entry.getKey(), validator);
            converters.put(entry.getKey(), converter);
            hepPlanners.put(entry.getKey(), QueryOptimizePlannerFactory.createHepPlanner());
        }
        return new OptimizerPlannerContext(validators, converters, hepPlanners);
    }
    
    /**
     * Create connection properties.
     * 
     * @return properties
     */
    public static Properties createConnectionProperties() {
        Properties result = new Properties();
        result.setProperty(CalciteConnectionProperty.TIME_ZONE.camelName(), "UTC");
        return result;
    }
    
    /**
     * Create catalog reader.
     * 
     * @param schemaName schema name
     * @param schema schema
     * @param relDataTypeFactory rel data type factory
     * @param connectionConfig connection config
     * @return calcite catalog reader
     */
    public static CalciteCatalogReader createCatalogReader(final String schemaName, final Schema schema, final RelDataTypeFactory relDataTypeFactory, final CalciteConnectionConfig connectionConfig) {
        CalciteSchema rootSchema = CalciteSchema.createRootSchema(true);
        rootSchema.add(schemaName, schema);
        return new CalciteCatalogReader(rootSchema, Collections.singletonList(schemaName), relDataTypeFactory, connectionConfig);
    }
    
    /**
     * Create validator.
     * 
     * @param catalogReader catalog reader
     * @param relDataTypeFactory rel data type factory
     * @param connectionConfig connection config
     * @return sql validator
     */
    public static SqlValidator createValidator(final CalciteCatalogReader catalogReader, final RelDataTypeFactory relDataTypeFactory, final CalciteConnectionConfig connectionConfig) {
        SqlValidator.Config validatorConfig = SqlValidator.Config.DEFAULT
                .withLenientOperatorLookup(connectionConfig.lenientOperatorLookup())
                .withSqlConformance(connectionConfig.conformance())
                .withDefaultNullCollation(connectionConfig.defaultNullCollation())
                .withIdentifierExpansion(true);
        return SqlValidatorUtil.newValidator(SqlStdOperatorTable.instance(), catalogReader, relDataTypeFactory, validatorConfig);
    }
    
    /**
     * Create Converter.
     * 
     * @param catalogReader catalog reader
     * @param validator validator
     * @param relDataTypeFactory rel data type factory
     * @return sql to rel converter
     */
    public static SqlToRelConverter createConverter(final CalciteCatalogReader catalogReader, final SqlValidator validator, final RelDataTypeFactory relDataTypeFactory) {
        ViewExpander expander = (rowType, queryString, schemaPath, viewPath) -> null;
        Config converterConfig = SqlToRelConverter.config().withTrimUnusedFields(true);
        RelOptCluster cluster = RelOptCluster.create(QueryOptimizePlannerFactory.createVolcanoPlanner(), new RexBuilder(relDataTypeFactory));
        return new SqlToRelConverter(expander, validator, catalogReader, cluster, StandardConvertletTable.INSTANCE, converterConfig);
    }
}
