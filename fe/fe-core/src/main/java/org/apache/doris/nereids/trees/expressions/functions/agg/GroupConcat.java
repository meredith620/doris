// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.nereids.trees.expressions.functions.agg;

import org.apache.doris.catalog.FunctionSignature;
import org.apache.doris.nereids.exceptions.AnalysisException;
import org.apache.doris.nereids.trees.expressions.Expression;
import org.apache.doris.nereids.trees.expressions.OrderExpression;
import org.apache.doris.nereids.trees.expressions.functions.ExplicitlyCastableSignature;
import org.apache.doris.nereids.trees.expressions.functions.PropagateNullable;
import org.apache.doris.nereids.trees.expressions.visitor.ExpressionVisitor;
import org.apache.doris.nereids.types.DataType;
import org.apache.doris.nereids.types.VarcharType;
import org.apache.doris.nereids.types.coercion.AnyDataType;
import org.apache.doris.nereids.util.ExpressionUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * AggregateFunction 'group_concat'. This class is generated by GenerateFunction.
 */
public class GroupConcat extends AggregateFunction
        implements ExplicitlyCastableSignature, PropagateNullable {

    public static final List<FunctionSignature> SIGNATURES = ImmutableList.of(
            FunctionSignature.ret(VarcharType.SYSTEM_DEFAULT)
                    .varArgs(VarcharType.SYSTEM_DEFAULT, AnyDataType.INSTANCE),
            FunctionSignature.ret(VarcharType.SYSTEM_DEFAULT)
                    .varArgs(VarcharType.SYSTEM_DEFAULT, VarcharType.SYSTEM_DEFAULT, AnyDataType.INSTANCE)
    );

    private int nonOrderArguments;

    /**
     * constructor with 1 argument.
     */
    public GroupConcat(Expression arg, OrderExpression... orders) {
        super("group_concat", ExpressionUtils.mergeArguments(arg, orders));
        this.nonOrderArguments = 1;
    }

    /**
     * constructor with 1 argument.
     */
    public GroupConcat(boolean distinct, Expression arg, OrderExpression... orders) {
        super("group_concat", distinct, ExpressionUtils.mergeArguments(arg, orders));
        this.nonOrderArguments = 1;
    }

    /**
     * constructor with 2 arguments.
     */
    public GroupConcat(Expression arg0, Expression arg1, OrderExpression... orders) {
        super("group_concat", ExpressionUtils.mergeArguments(arg0, arg1, orders));
        this.nonOrderArguments = 2;
    }

    /**
     * constructor with 2 arguments.
     */
    public GroupConcat(boolean distinct, Expression arg0, Expression arg1, OrderExpression... orders) {
        super("group_concat", distinct, ExpressionUtils.mergeArguments(arg0, arg1, orders));
        this.nonOrderArguments = 2;
    }

    @Override
    public void checkLegalityBeforeTypeCoercion() {
        DataType typeOrArg0 = getArgumentType(0);
        if (!typeOrArg0.isStringLikeType() && !typeOrArg0.isNullType()) {
            throw new AnalysisException(
                    "group_concat requires first parameter to be of type STRING: " + this.toSql());
        }

        if (nonOrderArguments == 2) {
            DataType typeOrArg1 = getArgumentType(1);
            if (!typeOrArg1.isStringLikeType() && !typeOrArg1.isNullType()) {
                throw new AnalysisException(
                        "group_concat requires second parameter to be of type STRING: " + this.toSql());
            }
        }
    }

    /**
     * withDistinctAndChildren.
     */
    @Override
    public GroupConcat withDistinctAndChildren(boolean distinct, List<Expression> children) {
        Preconditions.checkArgument(children().size() > 1);

        boolean foundOrderExpr = false;
        int firstOrderExrIndex = 0;
        for (int i = 0; i < children.size(); i++) {
            Expression child = children.get(i);
            if (child instanceof OrderExpression) {
                foundOrderExpr = true;
            } else if (!foundOrderExpr) {
                firstOrderExrIndex++;
            } else {
                throw new AnalysisException("invalid group_concat parameters: " + children);
            }
        }

        if (firstOrderExrIndex == 1) {
            List<OrderExpression> orders = (List) children.subList(firstOrderExrIndex, children.size());
            return new GroupConcat(distinct, children.get(0), orders.toArray(new OrderExpression[0]));
        } else if (firstOrderExrIndex == 2) {
            List<OrderExpression> orders = (List) children.subList(firstOrderExrIndex, children.size());
            return new GroupConcat(distinct, children.get(0), children.get(1), orders.toArray(new OrderExpression[0]));
        } else {
            throw new AnalysisException("group_concat requires one or two parameters: " + children);
        }
    }

    @Override
    public <R, C> R accept(ExpressionVisitor<R, C> visitor, C context) {
        return visitor.visitGroupConcat(this, context);
    }

    @Override
    public List<FunctionSignature> getSignatures() {
        return SIGNATURES;
    }
}
