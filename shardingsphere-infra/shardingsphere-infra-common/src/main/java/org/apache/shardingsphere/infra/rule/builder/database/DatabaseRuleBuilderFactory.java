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

package org.apache.shardingsphere.infra.rule.builder.database;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.type.ordered.OrderedSPIRegistry;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

/**
 * Database rule builder factory.
 */
@SuppressWarnings("rawtypes")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseRuleBuilderFactory {
    
    static {
        ShardingSphereServiceLoader.register(DatabaseRuleBuilder.class);
    }
    
    /**
     * Get instances of database rule builder.
     *
     * @return got instances
     */
    public static Collection<DatabaseRuleBuilder> getInstances() {
        return OrderedSPIRegistry.getRegisteredServices(DatabaseRuleBuilder.class);
    }
    
    /**
     * Get instance map of database rule builder.
     *
     * @param ruleConfigs rule configurations
     * @return got instance map
     */
    public static Map<RuleConfiguration, DatabaseRuleBuilder> getInstanceMap(final Collection<RuleConfiguration> ruleConfigs) {
        return OrderedSPIRegistry.getRegisteredServices(DatabaseRuleBuilder.class, ruleConfigs);
    }
    
    /**
     * Get instance map of database rule builder.
     *
     * @param ruleConfigs rule configurations
     * @param orderComparator order comparator
     * @return got instance map
     */
    public static Map<RuleConfiguration, DatabaseRuleBuilder> getInstanceMap(final Collection<RuleConfiguration> ruleConfigs, final Comparator<Integer> orderComparator) {
        return OrderedSPIRegistry.getRegisteredServices(DatabaseRuleBuilder.class, ruleConfigs, orderComparator);
    }
}
