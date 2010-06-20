/*
 * Copyright 2007-2008 Michele Mostarda ( michele.mostarda@gmail.com ).
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.asemantics.rdfcoder.model.java;

import com.asemantics.rdfcoder.model.ErrorListener;
import com.asemantics.rdfcoder.model.Identifier;
import com.asemantics.rdfcoder.sourceparse.JavadocEntry;
import com.asemantics.rdfcoder.sourceparse.ObjectsTable;
import org.apache.log4j.Logger;

/**
 * A <code>CodeHandler</code> implementation used to perform debugging.
 */
public class JavaCodeHandlerDebugImpl implements JavaCodeHandler {

    private static final Logger logger = Logger.getLogger(JavaCodeHandlerDebugImpl.class);

    public void startParsing(String libraryName, String location) {
        if(logger.isDebugEnabled()) {
            logger.debug("Start parsing process of library '" + libraryName + "' at location '" + location + "'");
        }
    }

    public void endParsing() {
        if(logger.isDebugEnabled()) {
            logger.debug("End parsing process");
        }
    }

    public void startCompilationUnit(String identifier) {
        if(logger.isDebugEnabled()) {
            logger.debug("Start compilation unit: " + identifier);
        }
    }

    public void endCompilationUnit() {
        if(logger.isDebugEnabled()) {
            logger.debug("End compilation unit");
        }
    }

    public void startPackage(Identifier pathToPackage) {
        if(logger.isDebugEnabled()) {
            logger.debug("Start package: " + pathToPackage);
        }
    }

    public void endPackage() {
        if(logger.isDebugEnabled()) {
            logger.debug("End package");
        }
    }

    public void startInterface(
            Identifier pathToInterface,
            Identifier[] extendedInterfaces
    ) {
        if(logger.isDebugEnabled()) {
            logger.debug("Start interface " + pathToInterface + " extending < ");
            for(Identifier extendedInterface : extendedInterfaces) {
                logger.debug(" " + extendedInterface + " ");
            }
            logger.debug(">");
        }
    }

    public void endInterface() {
        if(logger.isDebugEnabled()) {
            logger.debug("End interface");
        }
    }

    public void startClass(
            JavaCodeModel.JModifier[] modifiers,
            JavaCodeModel.JVisibility visibility,
            Identifier pathToClass,
            Identifier extendedClass,
            Identifier[] implementedInterfaces
    ) {
        if(logger.isDebugEnabled()) {
            logger.debug(
                    "Start class " + pathToClass +
                            " visibility: " + visibility +
                            " modifiers: " + JavaCodeModel.JModifier.toByte(modifiers)
            );
            logger.debug(" extending class " + extendedClass);
            logger.debug(" implemented interfaces < ");
            for (Identifier implementedInterface : implementedInterfaces) {
                logger.debug(" " + implementedInterface + " ");
            }
            logger.debug(">");
        }
    }

    public void endClass() {
        if(logger.isDebugEnabled()) {
            logger.debug("End class");
        }
    }

    public void startEnumeration(
            JavaCodeModel.JModifier[] modifiers,
            JavaCodeModel.JVisibility visibility,
            Identifier pathToEnumeration,
            String[] elements
    ) {
        if(logger.isDebugEnabled()) {
            logger.debug(
                    "start enumeration " + pathToEnumeration +
                            " visibility " + visibility  +
                            " modifiers "  + JavaCodeModel.JModifier.toByte(modifiers)
            );
            logger.debug("{");
            for(String element : elements) {
                logger.debug(element);
            }
            logger.debug("}");
        }
    }

    public void endEnumeration() {
        if(logger.isDebugEnabled()) {
            logger.debug("end enumeration");
        }
    }

    public void attribute(
            JavaCodeModel.JModifier[] modifiers,
            JavaCodeModel.JVisibility visibility,
            Identifier pathToAttribute,
            JavaCodeModel.JType type,
            String value
    ) {
        if(logger.isDebugEnabled()) {
            logger.debug(
                    "attribute " + pathToAttribute +
                            " visibility " + visibility +
                            " modifiers " + JavaCodeModel.JModifier.toByte(modifiers)
            );
            logger.debug(" of type " + type + " ");
            logger.debug(" with value " + value);
        }
    }

    public void constructor(
            JavaCodeModel.JModifier[] modifiers,
            JavaCodeModel.JVisibility visibility,
            int overloadIndex,
            String[] parameterNames,
            JavaCodeModel.JType[] parameterTypes,
            JavaCodeModel.ExceptionType[] exceptions
    ) {
        if(logger.isDebugEnabled()) {
            logger.debug(
                    "constructor " + overloadIndex +
                            " visibility " + visibility +
                            " modifiers "  + JavaCodeModel.JModifier.toByte(modifiers)
            );
            logger.debug("{");
            printParameters(parameterNames, parameterTypes);
            logger.debug("}");
            printExceptions(exceptions);
        }
    }

    public void method(
            JavaCodeModel.JModifier[] modifiers,
            JavaCodeModel.JVisibility visibility,
            Identifier pathToMethod,
            int overloadIndex,
            String[] parameterNames,
            JavaCodeModel.JType[] parameterTypes,
            JavaCodeModel.JType returnType,
            JavaCodeModel.ExceptionType[] exceptions
    ) {
        if(logger.isDebugEnabled()) {
            logger.debug(
                    "method " + pathToMethod + " index " + overloadIndex +
                            " visibility " + visibility +
                            " modifiers " + JavaCodeModel.JModifier.toByte(modifiers)
            );
            logger.debug(" return type " + returnType);
            logger.debug("{");
            printParameters(parameterNames, parameterTypes);
            logger.debug("}");
            printExceptions(exceptions);
        }
    }

    public void parseError(String location, String description) {
        if(logger.isDebugEnabled()) {
            logger.debug("parseError: location: " + location + " description: " + description);
        }
    }

    public void unresolvedTypes(String[] types) {
        if(logger.isDebugEnabled()) {
            logger.debug("unresolvedTypes {");
            for(String type : types) {
                logger.debug(type);
            }
            logger.debug("}");
        }
    }

    public void preloadObjectsFromModel(ObjectsTable objectsTable) {
        throw new UnsupportedOperationException();
    }

    public void addErrorListener(ErrorListener errorListener) {
        throw new UnsupportedOperationException();
    }

    public void removeErrorListener(ErrorListener errorListener) {
        throw new UnsupportedOperationException();
    }

    public void classJavadoc(JavadocEntry entry, Identifier pathToClass) {
        if(logger.isDebugEnabled()) {
            logger.debug("classJavadoc:" + entry + " at location: " + pathToClass );
        }
    }

    public void fieldJavadoc(JavadocEntry entry, Identifier pathToField) {
        if(logger.isDebugEnabled()) {
            logger.debug("fieldJavadoc:" + entry + " at location: " + pathToField );
        }
    }

    public void constructorJavadoc(JavadocEntry entry, Identifier pathToConstructor, String[] signature) {
        if(logger.isDebugEnabled()) {
            logger.debug("constructorJavadoc: " + entry + " at location: " + pathToConstructor);
            logger.debug("(");
            for(String s: signature) { logger.debug(s + ", "); }
            logger.debug(")");
        }

    }

    public void methodJavadoc(JavadocEntry entry, Identifier pathToMethod, String[] signature) {
        if(logger.isDebugEnabled()) {
            logger.debug("methodJavadoc: " + entry + " at location: " + pathToMethod);
            logger.debug("(");
            for(String s: signature) { logger.debug(s + ", "); }
            logger.debug(")");
        }
    }

    public Identifier generateTempUniqueIdentifier() {
        throw new UnsupportedOperationException();
    }

    public int replaceIdentifierWithQualifiedType(Identifier identifier, Identifier qualifiedType) {
        if(logger.isDebugEnabled()) {
            logger.debug("replaceIdentifierWithQualifiedType " + identifier + " => " + qualifiedType);
        }
        return 0;
    }

    private void printParameters(String[] names, JavaCodeModel.JType[] types) {
        if(logger.isDebugEnabled()) {
            for(int i = 0; i < names.length; i++) {
                logger.debug(names[i] + ": " + types[i]);
            }
        }
    }

    private void printExceptions(JavaCodeModel.ExceptionType[] exceptions) {
        if(exceptions == null || exceptions.length == 0) {
            return;
        }
        if(logger.isDebugEnabled()) {
            logger.debug(" throws ");
            logger.debug("<");
            for (JavaCodeModel.ExceptionType exception : exceptions) {
                logger.debug(" " + exception + " ");
            }
            logger.debug(">");
        }
    }
    
}
