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

package org.apache.shardingsphere.infra.federation.optimizer.converter.segment.expression.impl;

import lombok.RequiredArgsConstructor;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * In expression converter.
 */
@RequiredArgsConstructor
public final class InExpressionConverter implements SQLSegmentConverter<InExpression, SqlBasicCall> {
    
    private final boolean not;
    
    public InExpressionConverter() {
        not = false;
    }
    
    @Override
    public Optional<SqlBasicCall> convertToSQLNode(final InExpression expression) {
        if (null == expression) {
            return Optional.empty();
        }
        Collection<SqlNode> sqlNodes = new LinkedList<>();
        ExpressionConverter expressionConverter = new ExpressionConverter();
        expressionConverter.convertToSQLNode(expression.getLeft()).ifPresent(sqlNodes::add);
        expressionConverter.convertToSQLNode(expression.getRight()).ifPresent(optional -> {
            if (optional instanceof SqlBasicCall) {
                sqlNodes.add(new SqlNodeList(((SqlBasicCall) optional).getOperandList(), SqlParserPos.ZERO));
            } else {
                sqlNodes.add(optional);
            }
        });
        SqlBasicCall sqlNode = new SqlBasicCall(SqlStdOperatorTable.IN, new ArrayList<>(sqlNodes), SqlParserPos.ZERO);
        return expression.isNot() ? Optional.of(new SqlBasicCall(SqlStdOperatorTable.NOT, Collections.singletonList(sqlNode), SqlParserPos.ZERO)) : Optional.of(sqlNode);
    }
    
    @Override
    public Optional<InExpression> convertToSQLSegment(final SqlBasicCall sqlBasicCall) {
        if (null == sqlBasicCall) {
            return Optional.empty();
        }
        ExpressionConverter expressionConverter = new ExpressionConverter();
        ExpressionSegment left = expressionConverter.convertToSQLSegment(sqlBasicCall.getOperandList().get(0)).orElseThrow(IllegalStateException::new);
        ExpressionSegment right = expressionConverter.convertToSQLSegment(sqlBasicCall.getOperandList().get(1)).orElseThrow(IllegalStateException::new);
        return Optional.of(new InExpression(getStartIndex(sqlBasicCall),
                sqlBasicCall.getOperandList().get(1) instanceof SqlSelect ? getStopIndex(sqlBasicCall) + 1 : getStopIndex(sqlBasicCall), left, right, not));
    }
}
