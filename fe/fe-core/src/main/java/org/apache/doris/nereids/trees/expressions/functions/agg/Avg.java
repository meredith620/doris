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
import org.apache.doris.catalog.ScalarType;
import org.apache.doris.nereids.exceptions.AnalysisException;
import org.apache.doris.nereids.trees.expressions.Expression;
import org.apache.doris.nereids.trees.expressions.functions.ComputePrecision;
import org.apache.doris.nereids.trees.expressions.functions.ExplicitlyCastableSignature;
import org.apache.doris.nereids.trees.expressions.shape.UnaryExpression;
import org.apache.doris.nereids.trees.expressions.visitor.ExpressionVisitor;
import org.apache.doris.nereids.types.BigIntType;
import org.apache.doris.nereids.types.DataType;
import org.apache.doris.nereids.types.DecimalV2Type;
import org.apache.doris.nereids.types.DecimalV3Type;
import org.apache.doris.nereids.types.DoubleType;
import org.apache.doris.nereids.types.IntegerType;
import org.apache.doris.nereids.types.SmallIntType;
import org.apache.doris.nereids.types.TinyIntType;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * AggregateFunction 'avg'. This class is generated by GenerateFunction.
 */
public class Avg extends NullableAggregateFunction
        implements UnaryExpression, ExplicitlyCastableSignature, ComputePrecision {

    public static final List<FunctionSignature> SIGNATURES = ImmutableList.of(
            FunctionSignature.ret(DoubleType.INSTANCE).args(TinyIntType.INSTANCE),
            FunctionSignature.ret(DoubleType.INSTANCE).args(SmallIntType.INSTANCE),
            FunctionSignature.ret(DoubleType.INSTANCE).args(IntegerType.INSTANCE),
            FunctionSignature.ret(DoubleType.INSTANCE).args(BigIntType.INSTANCE),
            FunctionSignature.ret(DoubleType.INSTANCE).args(DoubleType.INSTANCE),
            FunctionSignature.ret(DecimalV2Type.SYSTEM_DEFAULT).args(DecimalV2Type.SYSTEM_DEFAULT),
            FunctionSignature.ret(DecimalV3Type.DEFAULT_DECIMAL128).args(DecimalV3Type.DEFAULT_DECIMAL32),
            FunctionSignature.ret(DecimalV3Type.DEFAULT_DECIMAL128).args(DecimalV3Type.DEFAULT_DECIMAL64),
            FunctionSignature.ret(DecimalV3Type.DEFAULT_DECIMAL128).args(DecimalV3Type.DEFAULT_DECIMAL128)
    );

    /**
     * constructor with 1 argument.
     */
    public Avg(Expression child) {
        this(false, false, child);
    }

    /**
     * constructor with 1 argument.
     */
    public Avg(boolean distinct, Expression arg) {
        this(distinct, false, arg);
    }

    private Avg(boolean distinct, boolean alwaysNullable, Expression arg) {
        super("avg", distinct, alwaysNullable, arg);
    }

    @Override
    public void checkLegalityBeforeTypeCoercion() {
        DataType argType = child().getDataType();
        if (((!argType.isNumericType() && !argType.isNullType()) || argType.isOnlyMetricType())) {
            throw new AnalysisException("avg requires a numeric parameter: " + toSql());
        }
    }

    @Override
    public FunctionSignature computePrecision(FunctionSignature signature) {
        DataType argumentType = getArgumentType(0);
        if (argumentType.isDecimalV3Type()) {
            DecimalV3Type decimalV3Type = (DecimalV3Type) argumentType;

            // DecimalV3 scale lower than DEFAULT_MIN_AVG_DECIMAL128_SCALE should do cast
            if (argumentType.toCatalogDataType().getDecimalDigits() < ScalarType.DEFAULT_MIN_AVG_DECIMAL128_SCALE) {
                signature = signature.withArgumentTypes(getArguments(), (index, type, arg) -> {
                    if (index == 0) {
                        return DecimalV3Type.createDecimalV3Type(
                                decimalV3Type.getPrecision(),
                                Math.max(decimalV3Type.getScale(), 4));
                    } else {
                        return type;
                    }
                });
            }

            return signature.withReturnType(DecimalV3Type.createDecimalV3Type(
                    DecimalV3Type.MAX_DECIMAL128_PRECISION,
                    Math.max(decimalV3Type.getScale(), 4)
            ));
        } else {
            return signature;
        }
    }

    @Override
    protected List<DataType> intermediateTypes() {
        DataType sumType = getDataType();
        BigIntType countType = BigIntType.INSTANCE;
        return ImmutableList.of(sumType, countType);
    }

    /**
     * withDistinctAndChildren.
     */
    @Override
    public Avg withDistinctAndChildren(boolean distinct, List<Expression> children) {
        Preconditions.checkArgument(children.size() == 1);
        return new Avg(distinct, alwaysNullable, children.get(0));
    }

    @Override
    public NullableAggregateFunction withAlwaysNullable(boolean alwaysNullable) {
        return new Avg(distinct, alwaysNullable, children.get(0));
    }

    @Override
    public <R, C> R accept(ExpressionVisitor<R, C> visitor, C context) {
        return visitor.visitAvg(this, context);
    }

    @Override
    public List<FunctionSignature> getSignatures() {
        return SIGNATURES;
    }
}
