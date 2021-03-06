/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.kie.pmml.compiler.commons.testutils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.kie.pmml.compiler.commons.utils.CommonCodegenUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Utility methods for Codegen-related tests
 */
public class CodegenTestUtils {

    public static void commonValidateCompilation(BlockStmt body, List<Parameter> parameters) {
        ClassOrInterfaceDeclaration classOrInterfaceType = new ClassOrInterfaceDeclaration();
        classOrInterfaceType.setName("CommCodeTest");
        MethodDeclaration toAdd = new MethodDeclaration();
        toAdd.setType("void");
        toAdd.setName("TestingMethod");
        toAdd.setParameters(NodeList.nodeList(parameters));
        toAdd.setBody(body);
        classOrInterfaceType.addMember(toAdd);
        CompilationUnit compilationUnit =  StaticJavaParser.parse("");
        compilationUnit.setPackageDeclaration("org.kie.pmml.compiler.commons.utils");
        compilationUnit.addType(classOrInterfaceType);
        Map<String, String> sourcesMap = Collections.singletonMap("org.kie.pmml.compiler.commons.utils.CommCodeTest", compilationUnit.toString());
        try {
            KieMemoryCompiler.compile(sourcesMap, Thread.currentThread().getContextClassLoader());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public static void commonValidateCompilation(MethodDeclaration methodDeclaration) {
        ClassOrInterfaceDeclaration classOrInterfaceType = new ClassOrInterfaceDeclaration();
        classOrInterfaceType.setName("CommCodeTest");
        classOrInterfaceType.addMember(methodDeclaration);
        CompilationUnit compilationUnit =  StaticJavaParser.parse("");
        compilationUnit.setPackageDeclaration("org.kie.pmml.compiler.commons.utils");
        compilationUnit.addType(classOrInterfaceType);
        Map<String, String> sourcesMap = Collections.singletonMap("org.kie.pmml.compiler.commons.utils.CommCodeTest", compilationUnit.toString());
        try {
            KieMemoryCompiler.compile(sourcesMap, Thread.currentThread().getContextClassLoader());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public static void commonValidateCompilation(Map<String, String> sourcesMap) {
        try {
            KieMemoryCompiler.compile(sourcesMap, Thread.currentThread().getContextClassLoader());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public static void commonEvaluateConstructor(ConstructorDeclaration constructorDeclaration,
                                                 String generatedClassName,
                                                 Map<Integer, Expression> superInvocationExpressionsMap,
                                                 Map<String, Expression> assignExpressionsMap) {
        assertEquals(new SimpleName(generatedClassName), constructorDeclaration.getName());
        final BlockStmt body = constructorDeclaration.getBody();
        commonEvaluateSuperInvocationExpr(body, superInvocationExpressionsMap);
        commonEvaluateAssignExpr(body, assignExpressionsMap);
    }

    public static void commonEvaluateSuperInvocationExpr(BlockStmt body, Map<Integer, Expression> superInvocationExpressionsMap) {
        Optional<ExplicitConstructorInvocationStmt> retrieved = CommonCodegenUtils.getExplicitConstructorInvocationStmt(body);
        retrieved.ifPresent(explicitConstructorInvocationStmt -> superInvocationExpressionsMap.forEach(new BiConsumer<Integer, Expression>() {
            @Override
            public void accept(Integer integer, Expression expression) {
                assertEquals(expression, explicitConstructorInvocationStmt.getArgument(integer));
            }
        }));
    }

    public static void commonEvaluateAssignExpr(BlockStmt blockStmt, Map<String, Expression> assignExpressionMap) {
        List<AssignExpr> retrieved = blockStmt.findAll(AssignExpr.class);
        for (Map.Entry<String, Expression> entry : assignExpressionMap.entrySet()) {
            assertTrue(retrieved.stream()
                               .filter(assignExpr -> assignExpr.getTarget().asNameExpr().equals(new NameExpr(entry.getKey())))
                               .anyMatch(assignExpr -> assignExpr.getValue().equals(entry.getValue())));
        }
    }

}
