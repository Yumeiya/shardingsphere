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

package org.apache.shardingsphere.infra.yaml.config.swapper;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRuleConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * YAML rule configuration swapper engine.
 */
public final class YamlRuleConfigurationSwapperEngine {
    
    /**
     * Swap to YAML rule configurations.
     *
     * @param ruleConfigs rule configurations
     * @return YAML rule configurations
     */
    @SuppressWarnings("unchecked")
    public Collection<YamlRuleConfiguration> swapToYamlRuleConfigurations(final Collection<RuleConfiguration> ruleConfigs) {
        return YamlRuleConfigurationSwapperFactory.getInstanceMapByRuleConfigurations(ruleConfigs).entrySet().stream()
                .map(entry -> (YamlRuleConfiguration) entry.getValue().swapToYamlConfiguration(entry.getKey())).collect(Collectors.toList());
    }
    
    /**
     * Swap from YAML rule configurations to rule configurations.
     *
     * @param yamlRuleConfigs YAML rule configurations
     * @return rule configurations
     */
    @SuppressWarnings("rawtypes")
    public Collection<RuleConfiguration> swapToRuleConfigurations(final Collection<YamlRuleConfiguration> yamlRuleConfigs) {
        Collection<RuleConfiguration> result = new LinkedList<>();
        Collection<Class<?>> ruleConfigTypes = yamlRuleConfigs.stream().map(YamlRuleConfiguration::getRuleConfigurationType).collect(Collectors.toList());
        for (Entry<Class<?>, YamlRuleConfigurationSwapper> entry : YamlRuleConfigurationSwapperFactory.getInstanceMapByRuleConfigurationClasses(ruleConfigTypes).entrySet()) {
            result.addAll(swapToRuleConfigurations(yamlRuleConfigs, entry.getKey(), entry.getValue()));
        }
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Collection<RuleConfiguration> swapToRuleConfigurations(final Collection<YamlRuleConfiguration> yamlRuleConfigs,
                                                                   final Class<?> ruleConfigType, final YamlRuleConfigurationSwapper swapper) {
        return yamlRuleConfigs.stream()
                .filter(each -> each.getRuleConfigurationType().equals(ruleConfigType)).map(each -> (RuleConfiguration) swapper.swapToObject(each)).collect(Collectors.toList());
    }
    
    /**
     * Swap from YAML rule configuration to rule configuration.
     *
     * @param yamlRuleConfig YAML rule configuration
     * @return rule configuration
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public RuleConfiguration swapToRuleConfiguration(final YamlRuleConfiguration yamlRuleConfig) {
        Collection<Class<?>> types = Collections.singletonList(yamlRuleConfig.getRuleConfigurationType());
        YamlRuleConfigurationSwapper swapper = YamlRuleConfigurationSwapperFactory.getInstanceMapByRuleConfigurationClasses(types).get(yamlRuleConfig.getRuleConfigurationType());
        return (RuleConfiguration) swapper.swapToObject(yamlRuleConfig);
    }
}
