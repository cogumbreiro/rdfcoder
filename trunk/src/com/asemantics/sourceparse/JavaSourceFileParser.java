package com.asemantics.sourceparse;

import net.sourceforge.jrefactory.ast.*;
import net.sourceforge.jrefactory.parser.JavaParser;
import net.sourceforge.jrefactory.parser.ParseException;
import com.asemantics.model.CodeHandler;
import com.asemantics.model.CodeModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * This class is able to scan a java souce file and generate
 * corresponding triples.
 */
public class JavaSourceFileParser extends FileParser {

    private class Method {
        CodeModel.JVisibility visibility;
        private String methodPath;
        private String[] parameterNames;
        private CodeModel.JType[] parameterTypes;
        CodeModel.JType returnType;
        CodeModel.ExceptionType[] exceptions;

        Method(CodeModel.JVisibility v, String mp, String[] pns, CodeModel.JType[] pts, CodeModel.JType rt, CodeModel.ExceptionType[] excs) {
            visibility = v;
            methodPath = mp;
            parameterNames = pns;
            parameterTypes = pts;
            returnType = rt;
            exceptions = excs;            
        }
    }

    public void processCompilationUnit(String location, ASTCompilationUnit ast) {

        getCodeHandler().startCompilationUnit(location);

        // Retrieve package.
        List packs = ast.findChildrenOfType(ASTPackageDeclaration.class);
        String packagePath;
        if (packs.size() > 0) {
            packagePath = ((ASTName) ((ASTPackageDeclaration) packs.get(0)).findChildrenOfType(ASTName.class).get(0)).getName();
        } else {
            packagePath = ""; // Default package.
        }
        getCodeHandler().startPackage(packagePath);

        try {

            // Define inports context.
            ImportsContext importsContext = new ImportsContext();
            importsContext.setContextPackage(packagePath);

            // Retrieve imports.
            List imports = ast.findChildrenOfType(ASTImportDeclaration.class);
            for (int i = 0; i < imports.size(); i++) {
                ASTImportDeclaration importDeclaration = (ASTImportDeclaration) imports.get(i);
                String importEntry = ((ASTName) importDeclaration.findChildrenOfType(ASTName.class).get(0)).getName();
                if (importDeclaration.isImportOnDemand()) { //Starred package.
                    try {
                        importsContext.addStarredPackage(importEntry);
                    } catch(IllegalArgumentException iae) {
                        getCodeHandler().parseError( location, iae.getClass().getName()  + "[" + iae.getMessage() + "]" );
                    }
                } else {
                    importsContext.addFullyQualifiedObject(importEntry);
                }
            }

            // Note: enumerations can be present only at first level.
            extractEnumerations(
                    packagePath,
                    getCodeHandler(),
                    (ASTCompilationUnit) ast.findChildrenOfType(ASTCompilationUnit.class).get(0)
            );

            // Process first level entities.
            processLevel(packagePath, importsContext, ast);

        } finally {
            getCodeHandler().endPackage();
            getCodeHandler().endCompilationUnit();
        }
    }

    private void processLevel(String packagePath, ImportsContext importsContext, SimpleNode simpleNode) {

        // Find classes.
        List<ASTClassDeclaration> classes = findChildrenAtSameLevel(simpleNode, ASTClassDeclaration.class);

        // Find interfaces.
        List<ASTInterfaceDeclaration> interfaces = findChildrenAtSameLevel(simpleNode, ASTInterfaceDeclaration.class);

        // Process Class.
        String className;
        String superClass = null;
        for (ASTClassDeclaration classDeclaration : classes ) {

            List<ASTUnmodifiedClassDeclaration> unmodifiedClassDeclarations = classDeclaration.findChildrenOfType(ASTUnmodifiedClassDeclaration.class);
            for( NamedNode unmodifiedClassDeclaration : unmodifiedClassDeclarations) {

                // Class name.
                className = unmodifiedClassDeclaration.getName(); // Class name.

                getObjectsTable().addObject(packagePath, className);

                // Class visibility.
                CodeModel.JVisibility clsOrIntVisibility = retrieveVisibility(classDeclaration);

                // Find super class.
                List classOrInterface = unmodifiedClassDeclaration.findChildrenOfType(ASTClassOrInterfaceType.class);
                if (classOrInterface.size() > 0) {   // Has super Class.
                    superClass = retrieveObjectName( ((ASTClassOrInterfaceType) classOrInterface.get(0)).findChildrenOfType(ASTIdentifier.class) );
                }

                getCodeHandler().startClass(
                        clsOrIntVisibility,
                        packagePath + CodeHandler.PACKAGE_SEPARATOR + className,
                        superClass != null ? qualifyType(importsContext, superClass, new CodeModel.ObjectType(null) ) : null,
                        extractImplementedInterfaces(importsContext, unmodifiedClassDeclaration)
                );
                try {
                    extractEnumerations(packagePath, getCodeHandler(), unmodifiedClassDeclaration);
                    extractAttributes  (packagePath, getCodeHandler(), importsContext, unmodifiedClassDeclaration);
                    extractContructors (getCodeHandler(), importsContext, unmodifiedClassDeclaration);
                    extractMethods     (packagePath, getCodeHandler(), importsContext, unmodifiedClassDeclaration);

                    // Recursive invocation.
                    processLevel(packagePath + CodeHandler.PACKAGE_SEPARATOR + className, importsContext, unmodifiedClassDeclaration);

                } finally {
                    getCodeHandler().endClass();
                }

            }
        }

        // Process interface.
        String interfaceName = null;
        for(ASTInterfaceDeclaration interfaceDeclaration : interfaces ) {

            List<ASTUnmodifiedInterfaceDeclaration> unmodifiedInterfaceDeclarations = interfaceDeclaration.findChildrenOfType(ASTUnmodifiedInterfaceDeclaration.class);
            for(NamedNode unmodifiedInterfaceDeclaration : unmodifiedInterfaceDeclarations) {

                // Interface name.
                interfaceName = unmodifiedInterfaceDeclaration.getName();

                getObjectsTable().addObject(packagePath, interfaceName);

                getCodeHandler().startInterface(
                        packagePath + CodeHandler.PACKAGE_SEPARATOR + interfaceName,
                        extractImplementedInterfaces(importsContext, unmodifiedInterfaceDeclaration)
                );
                try {
                    extractEnumerations(packagePath, getCodeHandler(), unmodifiedInterfaceDeclaration);
                    extractAttributes  (packagePath, getCodeHandler(), importsContext, unmodifiedInterfaceDeclaration);
                    extractMethods     (packagePath, getCodeHandler(), importsContext, unmodifiedInterfaceDeclaration);

                    // Recursive invocation.
                    processLevel(packagePath + CodeHandler.PACKAGE_SEPARATOR + interfaceName, importsContext, unmodifiedInterfaceDeclaration);

                } finally {
                    getCodeHandler().endInterface();
                }

            }
        }

    }

    /**
     * Returns an array containing the fully quelified types of the implemented interfaces
     * of the current simple node.
     *
     * @param importsContext
     * @param simpleNode
     * @return
     */
    private String[] extractImplementedInterfaces(ImportsContext importsContext, SimpleNode simpleNode) {
        String[] interfacesArray = null;
        List genericNameList = simpleNode.findChildrenOfType(ASTGenericNameList.class);
        for(int i = 0; i < genericNameList.size(); i++) {
            List classOrInterfaceTypes = ( (ASTGenericNameList) genericNameList.get(i) ).findChildrenOfType(ASTClassOrInterfaceType.class);
            interfacesArray = new String[classOrInterfaceTypes.size()];
            for(int j = 0; j < classOrInterfaceTypes.size(); j++) {
                List identifierList = ( (ASTClassOrInterfaceType) classOrInterfaceTypes.get(j)).findChildrenOfType(ASTIdentifier.class);
                interfacesArray[j] = qualifyType(importsContext, retrieveObjectName(identifierList), new CodeModel.InterfaceType(null) );
            }
        }
        return interfacesArray;
    }

    private void extractAttributes(String packagePath, CodeHandler ch, ImportsContext importsContext, NamedNode namedNode) {
        List attributes = namedNode.findChildrenOfType(ASTFieldDeclaration.class);
        String attributeName;
        CodeModel.JVisibility attributeVisibility;
        CodeModel.JType attributeType;
        for (int f = 0; f < attributes.size(); f++) {
            ASTFieldDeclaration fieldDeclaration = (ASTFieldDeclaration) attributes.get(f);

            // Retrieve name.
            ASTVariableDeclaratorId variableDeclaratorId =
                    (ASTVariableDeclaratorId) fieldDeclaration.findChildrenOfType(ASTVariableDeclaratorId.class).get(0);
            attributeName = variableDeclaratorId.getName();

            // Visibility.
            attributeVisibility = retrieveVisibility(fieldDeclaration);
            
            // Retrieve array size.
            int arraySize;
             boolean isArray;
            List referenceTypes = fieldDeclaration.findChildrenOfType(ASTReferenceType.class);
            if( referenceTypes.size() > 0 ) {
                 ASTReferenceType astReferenceType = (ASTReferenceType) referenceTypes.get(0);
                 arraySize = astReferenceType.getArrayCount();
                 isArray = arraySize > 0;
            } else {
                arraySize = 0;
                isArray = false;
            }

            // Retrieve type.
            List primitiveType = fieldDeclaration.findChildrenOfType(ASTPrimitiveType.class);
            if (primitiveType.size() > 0) { // Primitive type declared.
                CodeModel.JType type = CodeModel.javaTypeToJType(((ASTPrimitiveType) primitiveType.get(0)).getName());
                attributeType =
                        isArray
                                ?
                        new CodeModel.ArrayType(type, arraySize)
                                :
                        type;
            } else { // Obj identifier declared.
                String typeName = retrieveObjectName( fieldDeclaration.findChildrenOfType(ASTIdentifier.class) );
                CodeModel.JType type = new CodeModel.ObjectType(
                        qualifyType(
                                importsContext,
                                typeName,
                                isArray
                                        ?
                                        new CodeModel.ArrayType(
                                            new CodeModel.ObjectType(null),
                                            arraySize
                                        )
                                        :
                                        new CodeModel.ObjectType(null)
                        )
                );
                attributeType = isArray ? new CodeModel.ArrayType(type, variableDeclaratorId.getArrayCount()) : type;
            }

            ch.attribute(attributeVisibility, qualifyAttribute(packagePath, attributeName), attributeType, null);
        }
    }

    private void extractContructors(CodeHandler ch, ImportsContext importsContext, NamedNode namedNode) {
        // Constructors.
        List constructors = namedNode.findChildrenOfType(ASTConstructorDeclaration.class);
        ASTConstructorDeclaration constructorDeclaration;
        for(int c = 0; c < constructors.size(); c++) {

            constructorDeclaration = ((ASTConstructorDeclaration) constructors.get(c));
            List formalParameters = constructorDeclaration.findChildrenOfType(ASTFormalParameter.class);

            // Extract parameters.
            ASTFormalParameter formalParameter;
            List<String> parameterNames = new ArrayList<String>();
            List<CodeModel.JType> parameterTypes = new ArrayList<CodeModel.JType>();
            for( int fp = 0; fp < formalParameters.size(); fp++ ) {
                formalParameter = (ASTFormalParameter) formalParameters.get(fp);
                parameterNames.add(
                        ( (ASTVariableDeclaratorId) formalParameter.findChildrenOfType(ASTVariableDeclaratorId.class).get(0)).getName()
                );
                parameterTypes.add( extractFormalparameterType(importsContext, formalParameter) );
            }

            // Extract exceptions.
            CodeModel.ExceptionType[] exceptions = extractExceptions(importsContext, constructorDeclaration);

            ch.constructor(
                    retrieveVisibility(constructorDeclaration),
                    c,
                    parameterNames.toArray( new String[parameterNames.size()] ),
                    parameterTypes.toArray( new CodeModel.JType[parameterTypes.size()] ),
                    exceptions
            );
        }
    }

    private void extractMethods(String packagePath, CodeHandler ch, ImportsContext importsContext, NamedNode namedNode) {
        List methods = namedNode.findChildrenOfType(ASTMethodDeclaration.class);
        String methodName;
        CodeModel.JVisibility methodVisibility;
        boolean returnTypeIsArray;
        int returnTypeArraySize;
        CodeModel.JType returnType;
        List<JavaSourceFileParser.Method> methodsBuffer = new ArrayList<JavaSourceFileParser.Method>();
        for (int m = 0; m < methods.size(); m++) {
            ASTMethodDeclaration methodDeclaration = (ASTMethodDeclaration) methods.get(m);

            // Retrieve method name.
            methodName = ((ASTMethodDeclarator) methodDeclaration.findChildrenOfType(ASTMethodDeclarator.class).get(0)).getName();

            // Retrieve method visibility.
            methodVisibility = retrieveVisibility(methodDeclaration);

            // Return type is array.
            List referenceType = methodDeclaration.findChildrenOfType(ASTReferenceType.class);
            if (referenceType.size() > 0) {
                returnTypeArraySize = ((ASTReferenceType) referenceType.get(0)).getArrayCount();
                returnTypeIsArray = returnTypeArraySize > 0;
            } else {
                returnTypeArraySize = 0;                
                returnTypeIsArray = false;
            }


            // Find return type.
            ASTResultType resultType = (ASTResultType) methodDeclaration.findChildrenOfType(ASTResultType.class).get(0);
            List primitiveType = resultType.findChildrenOfType(ASTPrimitiveType.class);
            if (primitiveType.size() > 0) { // Result type is primitive.
                CodeModel.JType type = CodeModel.javaTypeToJType(((ASTPrimitiveType) primitiveType.get(0)).getName());
                if( returnTypeIsArray ) {
                    returnType = new CodeModel.ArrayType(type, returnTypeArraySize);
                } else {
                   returnType = type;
                }
            } else if (resultType.findChildrenOfType(ASTIdentifier.class).size() > 0) { // Result type is Object.
                String qualifiedType = qualifyType(
                        importsContext,
                        retrieveObjectName( resultType.findChildrenOfType(ASTIdentifier.class) ),
                        returnTypeIsArray
                                ?
                                new CodeModel.ArrayType(
                                    new CodeModel.ObjectType(null),
                                    returnTypeArraySize
                                )
                                :
                                new CodeModel.ObjectType(null)
                );
                returnType = (
                        returnTypeIsArray
                                ?
                        new CodeModel.ArrayType( new CodeModel.ObjectType(qualifiedType), returnTypeArraySize)
                                :
                        new CodeModel.ObjectType(qualifiedType)
                );
            } else { // Void type.
                returnType = CodeModel.VOID;
            }

            // Extract formal parameters.
            List formalParameters = methodDeclaration.findChildrenOfType(ASTFormalParameter.class);
            ASTFormalParameter formalParameter;
            List<String> parameterNames = new ArrayList();
            List<CodeModel.JType> parameterTypes = new ArrayList();
            for (int p = 0; p < formalParameters.size(); p++) {
                formalParameter = (ASTFormalParameter) formalParameters.get(p);
                // Parameter identifier.
                parameterNames.add(((ASTVariableDeclaratorId) formalParameter.findChildrenOfType(ASTVariableDeclaratorId.class).get(0)).getName());
                // Parameter type.
                parameterTypes.add(extractFormalparameterType(importsContext, formalParameter));
            }

            // Extract exceptions.
            CodeModel.ExceptionType[] exceptions = extractExceptions(importsContext, methodDeclaration);

            // Storing method.
            methodsBuffer.add(
                    new JavaSourceFileParser.Method(
                            methodVisibility,
                            qualifyMethod(packagePath, methodName),
                            parameterNames.toArray(new String[parameterNames.size()]),
                            parameterTypes.toArray(new CodeModel.JType[parameterTypes.size()]),
                            returnType,
                            exceptions

                    )
            );
        }

        // Group signatures by name.
        Map<String, List<Method>> methodsMap = new HashMap<String, List<Method>>();
        Iterator<Method> iter = methodsBuffer.iterator();
        JavaSourceFileParser.Method curr;
        while (iter.hasNext()) {
            curr = iter.next();
            List signatures = methodsMap.get(curr.methodPath);
            if (signatures == null) {
                signatures = new ArrayList();
                methodsMap.put(curr.methodPath, signatures);
            }
            signatures.add(curr);
        }

        // Store methods.
        Iterator<Map.Entry<String, List<JavaSourceFileParser.Method>>> entries = methodsMap.entrySet().iterator();
        Map.Entry<String, List<JavaSourceFileParser.Method>> entry;
        while (entries.hasNext()) {
            entry = entries.next();
            for (int i = 0; i < entry.getValue().size(); i++) {
                ch.method(
                        entry.getValue().get(i).visibility,
                        entry.getKey(),
                        i,
                        entry.getValue().get(i).parameterNames,
                        entry.getValue().get(i).parameterTypes,
                        entry.getValue().get(i).returnType,
                        entry.getValue().get(i).exceptions
                );
            }
        }
    }

    private void extractEnumerations(String packagePath, CodeHandler ch, SimpleNode simpleNode) {
        List enumDeclarations = simpleNode.findChildrenOfType(ASTEnumDeclaration.class);
        ASTEnumDeclaration enumDeclaration = null;
        for(int ed = 0; ed < enumDeclarations.size(); ed++) {
            enumDeclaration = (ASTEnumDeclaration) enumDeclarations.get(ed);
             CodeModel.JVisibility visibility = retrieveVisibility(enumDeclaration);
            ASTIdentifier indentifier = (ASTIdentifier) enumDeclaration.findChildrenOfType(ASTIdentifier.class).get(0);
            String identifier = indentifier.getName();
            List enumElements = enumDeclaration.findChildrenOfType(ASTEnumElement.class);
            List elements = new ArrayList();
            for(int ee = 0; ee < enumElements.size(); ee++ ) {
                elements.add( ((ASTEnumElement) enumElements.get(ee)).getName() );
            }
            ch.startEnumeration(
                    visibility,
                    qualifyEnumeration(packagePath, identifier),
                    (String[]) elements.toArray(new String[elements.size()])
            );
            //TODO: HIGH - extract fields + methods.
            ch.endEnumeration();
            elements.clear();
        }
    }

    private CodeModel.JType extractFormalparameterType(ImportsContext importsContext, ASTFormalParameter formalParameter) {

        // Retrieve array size.
        int arraySize;
        List referenceTypes = formalParameter.findChildrenOfType(ASTReferenceType.class);
        if( referenceTypes.size() > 0 ) {
            ASTReferenceType referenceType = (ASTReferenceType) referenceTypes.get(0);
            arraySize = referenceType.getArrayCount();
        } else {
            arraySize = 0;
        }

        // Retrieve type.
        List primitiveType = formalParameter.findChildrenOfType(ASTPrimitiveType.class);
        if (primitiveType.size() > 0) { // Result type is Primitive.
            return CodeModel.javaTypeToJType(((ASTPrimitiveType) primitiveType.get(0)).getName());
        } else { // Result type is Object.
            String typeName =  retrieveObjectName( formalParameter.findChildrenOfType(ASTIdentifier.class) );
            if(arraySize > 0) {
                return new CodeModel.ArrayType(
                        new CodeModel.ObjectType(
                            qualifyType(
                                    importsContext,
                                    typeName,
                                    new CodeModel.ArrayType(new CodeModel.ObjectType(null), arraySize)
                            )
                        ),
                         arraySize
                );
            } else {
                return new CodeModel.ObjectType(
                        qualifyType(
                            importsContext,
                            typeName,
                            new CodeModel.ObjectType(null)
                        )
                );
            }
        }
    }

    private CodeModel.ExceptionType[] extractExceptions(ImportsContext importsContext,AccessNode accessNode) {
        List nameLists = accessNode.findChildrenOfType(ASTNameList.class);
        CodeModel.ExceptionType[] exceptions = null;
        if(nameLists.size() > 0) {
            ASTNameList nameList = (ASTNameList) nameLists.get(0);
            List namesList = nameList.findChildrenOfType(ASTName.class);
            String exceptionName;
            exceptions = new CodeModel.ExceptionType[namesList.size()];
            for( int i = 0; i < namesList.size(); i++ ) {
                ASTName name = (ASTName) namesList.get(i);
                exceptionName = retrieveObjectName(name.findChildrenOfType(ASTIdentifier.class));
                exceptions[i] = new CodeModel.ExceptionType(
                    qualifyType(importsContext, exceptionName, new CodeModel.ExceptionType(null))
                );
            }
        }
        return exceptions;
    }

    private static String printArray(final int arrayCount) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < arrayCount; i++) {
            sb.append("[]");
        }
        return sb.toString();
    }

    private static String tab(int t) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < t; i++) {
            sb.append("-");
        }
        return sb.toString();
    }

    public void parse(File file) throws ParserException, IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException fnfe) {
            throw new ParserException(fnfe, file.getAbsolutePath());
        }
        JavaParser jp = new JavaParser(fis);
        ASTCompilationUnit ast = null;
        try {
            ast = jp.CompilationUnit();
            processCompilationUnit(file.getAbsolutePath(), ast);
        } catch (ParseException pe) {
            throw new ParserException(pe, file.getAbsolutePath());
        } finally {
            fis.close();
        }
    }

    /**
       * Returns the relative depth of a descendant in respect with the parent.
       * @param parent
       * @param descendant
       * @return
       */
      private int childRelativeDepth(Node parent, Node descendant) {
          Node childParent = descendant;
          int depth = 0;
          while(childParent != parent) {
              childParent = childParent.jjtGetParent();
              depth++;
              if(childParent == null) {
                  throw new IllegalStateException();
              }
          }
          return depth;
      }

      /**
       * Returns a list of descendant of simpleNode of type maching the given clazz
       * where all results are at the same depth level of the first child found.
       *
       * @param simpleNode
       * @param clazz
       * @return
       */
      private List findChildrenAtSameLevel(SimpleNode simpleNode, Class clazz) {
          List<SimpleNode> children = simpleNode.findChildrenOfType(clazz);
          if(children.size() == 0) {
              return children;
          }
          final int depthLevel = childRelativeDepth(simpleNode, children.get(0));
          List sameDepth = new ArrayList();
          for(SimpleNode child : children) {
              if( childRelativeDepth(simpleNode, child ) == depthLevel ) {
                  sameDepth.add(child);
              }
          }
          return sameDepth;
      }

      /**
       * Returns the comlete name of an object.
       *
       * @param list
       * @return
       */
      private String retrieveObjectName(List list) {
          String type = "";
          for(int i = 0; i < list.size(); i++) {
              type += ( (ASTIdentifier) list.get(i) ).getName() + ( i < list.size() - 1 ? "." : "");
          }
          return type;
      }

      /**
       * Returns the JVisibility of an access node.
       *
       * @param an
       * @return
       */
      private CodeModel.JVisibility retrieveVisibility(AccessNode an) {
          if (an.isPublic()) {
              return CodeModel.JVisibility.PUBLIC;
          } else if (an.isProtected()) {
              return CodeModel.JVisibility.PROTECTED;
          } else if (an.isPackage()) {
              return CodeModel.JVisibility.DEFAULT;
          } else {
              return CodeModel.JVisibility.PRIVATE;
          }
      }

      /**
       * Retruns the qualified typeName for a typeName string.
       *
       * @param typeName
       * @return
       */
      private String qualifyType(ImportsContext importsContext, String typeName, CodeModel.JType type) {
          String qualifiedObject = importsContext.qualifyType(getObjectsTable(), typeName);
          if (qualifiedObject == null) {
              String tempId = getCodeHandler().generateTempUniqueIdentifier();
              if(type instanceof CodeModel.ObjectType) {
                  ((CodeModel.ObjectType) type).setInternalIdentifier(tempId);
              } else if( type instanceof CodeModel.InterfaceType) {
                  ((CodeModel.InterfaceType) type).setInternalIdentifier(tempId);
              } else if( type instanceof CodeModel.ArrayType) {
                  ((CodeModel.ArrayType) type).setInternalIdentifier(tempId);
              } else {
                  throw new IllegalStateException();
              }
              getObjectsTable().addUnresolvedType(typeName, type, importsContext);
              return tempId;
          }
          return qualifiedObject;
      }

      private String qualifyAttribute(String packagePath, String attributeName) {
          return packagePath + CodeHandler.PACKAGE_SEPARATOR + attributeName;
      }

      private String qualifyMethod(String packagePath, String methodName) {
          return packagePath + CodeHandler.PACKAGE_SEPARATOR + methodName;
      }

      private String qualifyEnumeration(String packagePath, String enumerationName) {
          return packagePath + CodeHandler.PACKAGE_SEPARATOR + enumerationName;
      }


}