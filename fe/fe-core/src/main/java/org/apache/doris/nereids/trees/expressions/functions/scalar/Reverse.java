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

package org.apache.doris.nereids.trees.expressions.functions.scalar;

import org.apache.doris.catalog.FunctionSignature;
import org.apache.doris.nereids.trees.expressions.Expression;
import org.apache.doris.nereids.trees.expressions.functions.ExplicitlyCastableSignature;
import org.apache.doris.nereids.trees.expressions.functions.PropagateNullable;
import org.apache.doris.nereids.trees.expressions.shape.UnaryExpression;
import org.apache.doris.nereids.trees.expressions.visitor.ExpressionVisitor;
import org.apache.doris.nereids.types.ArrayType;
import org.apache.doris.nereids.types.BigIntType;
import org.apache.doris.nereids.types.DateTimeType;
import org.apache.doris.nereids.types.DateType;
import org.apache.doris.nereids.types.DecimalV2Type;
import org.apache.doris.nereids.types.DoubleType;
import org.apache.doris.nereids.types.FloatType;
import org.apache.doris.nereids.types.IntegerType;
import org.apache.doris.nereids.types.LargeIntType;
import org.apache.doris.nereids.types.SmallIntType;
import org.apache.doris.nereids.types.StringType;
import org.apache.doris.nereids.types.TinyIntType;
import org.apache.doris.nereids.types.VarcharType;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * ScalarFunction 'reverse'. This class is generated by GenerateFunction.
 */
public class Reverse extends ScalarFunction
        implements UnaryExpression, ExplicitlyCastableSignature, PropagateNullable {

    public static final List<FunctionSignature> SIGNATURES = ImmutableList.of(
            FunctionSignature.ret(VarcharType.SYSTEM_DEFAULT).args(VarcharType.SYSTEM_DEFAULT),
            FunctionSignature.ret(StringType.INSTANCE).args(StringType.INSTANCE),
            FunctionSignature.ret(ArrayType.of(TinyIntType.INSTANCE)).args(ArrayType.of(TinyIntType.INSTANCE)),
            FunctionSignature.ret(ArrayType.of(SmallIntType.INSTANCE)).args(ArrayType.of(SmallIntType.INSTANCE)),
            FunctionSignature.ret(ArrayType.of(IntegerType.INSTANCE)).args(ArrayType.of(IntegerType.INSTANCE)),
            FunctionSignature.ret(ArrayType.of(BigIntType.INSTANCE)).args(ArrayType.of(BigIntType.INSTANCE)),
            FunctionSignature.ret(ArrayType.of(LargeIntType.INSTANCE)).args(ArrayType.of(LargeIntType.INSTANCE)),
            FunctionSignature.ret(ArrayType.of(DateTimeType.INSTANCE)).args(ArrayType.of(DateTimeType.INSTANCE)),
            FunctionSignature.ret(ArrayType.of(DateType.INSTANCE)).args(ArrayType.of(DateType.INSTANCE)),
            FunctionSignature.ret(ArrayType.of(FloatType.INSTANCE)).args(ArrayType.of(FloatType.INSTANCE)),
            FunctionSignature.ret(ArrayType.of(DoubleType.INSTANCE)).args(ArrayType.of(DoubleType.INSTANCE)),
            FunctionSignature.ret(ArrayType.of(DecimalV2Type.SYSTEM_DEFAULT))
                    .args(ArrayType.of(DecimalV2Type.SYSTEM_DEFAULT)),
            FunctionSignature.ret(ArrayType.of(VarcharType.SYSTEM_DEFAULT))
                    .args(ArrayType.of(VarcharType.SYSTEM_DEFAULT)),
            FunctionSignature.ret(ArrayType.of(StringType.INSTANCE)).args(ArrayType.of(StringType.INSTANCE))
    );

    /**
     * constructor with 1 argument.
     */
    public Reverse(Expression arg) {
        super("reverse", arg);
    }

    /**
     * withChildren.
     */
    @Override
    public Reverse withChildren(List<Expression> children) {
        Preconditions.checkArgument(children.size() == 1);
        return new Reverse(children.get(0));
    }

    @Override
    public <R, C> R accept(ExpressionVisitor<R, C> visitor, C context) {
        return visitor.visitReverse(this, context);
    }

    @Override
    public List<FunctionSignature> getSignatures() {
        return SIGNATURES;
    }
}
