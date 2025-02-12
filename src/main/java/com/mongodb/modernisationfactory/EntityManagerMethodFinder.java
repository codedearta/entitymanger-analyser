package com.mongodb.modernisationfactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

public class EntityManagerMethodFinder {
    public static void main(String[] args) throws Exception {
        // Initialize JavaParser with Symbol Solver
        CombinedTypeSolver typeSolver = new CombinedTypeSolver(new ReflectionTypeSolver());
        JavaParser parser = new JavaParser();

        // The Java file to analyze
        String packageName = EntityManagerMethodFinder.class.getPackage().getName();

        // Convert package name to a directory path
        String packagePath = packageName.replace(".", File.separator);

        // Construct the relative path to the Java file
        File file = Paths.get("src/main/java", packagePath, "LargeUserRepository.java").toFile();
        CompilationUnit cu = parser.parse(file).getResult().orElseThrow();

        // Find all method calls
        List<MethodCallExpr> methodCalls = cu.findAll(MethodCallExpr.class);

        for (MethodCallExpr call : methodCalls) {
            if (call.getScope().isPresent()) {
                try {
                    // Resolve the type of the method caller
                    ResolvedType resolvedType = JavaParserFacade.get(typeSolver).getType(call.getScope().get());

                    // Ensure the resolved type is a reference type before casting
                    if (resolvedType.isReferenceType()) {
                        ResolvedReferenceType refType = resolvedType.asReferenceType();

                        // Check if the caller is of type "javax.persistence.EntityManager"
                        if (refType.getQualifiedName().equals("javax.persistence.EntityManager")) {
                            int lineNumber = call.getRange().map(r -> r.begin.line).orElse(-1);
                            System.out.println("Found EntityManager method call:");
                            System.out.println("File: " + file.getName() + " | Line: " + lineNumber);
                            System.out.println("Surrounding method: \n" + call.findAncestor(MethodDeclaration.class).orElse(null));
                            System.out.println("-------------------------------------------------");
                        }
                    }
                } catch (Exception e) {
                    // Ignore resolution errors (could be unresolved symbols)
                }
            }
        }
    }
}