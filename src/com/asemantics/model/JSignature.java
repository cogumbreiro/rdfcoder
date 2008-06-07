/**
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


package com.asemantics.model;

/**
 * Represents a method signature.
 *
 * @author Michele Mostarda (michele.mostarda@gmail.com)
 */
public class JSignature extends JBase {

    /**
     * The parameter types of the signature.
     */
    CodeModel.JType[] paramTypes;

    /**
     * The return type of the signature.
     */
    CodeModel.JType returnType;

    /**
     * Check wether a signaure exists.
     * @param qm
     * @param pathToSignature
     * @return
     */
    public static boolean exists( QueryModel qm, String pathToSignature) {
        return qm.signatureExists(pathToSignature);
    }

    protected JSignature(
            QueryModel queryModel,
            String[] sections,
            CodeModel.JType[] paramTypes,
            CodeModel.JType returnType
    ) throws QueryModelException {
        super(queryModel, sections);
        if(paramTypes == null || returnType == null) {
            throw new IllegalArgumentException();
        }

        this.paramTypes = paramTypes;
        this.returnType = returnType;
    }

    protected JSignature(
            QueryModel queryModel,
            String pathToSignature,
            CodeModel.JType[] paramTypes,
            CodeModel.JType returnType
    ) throws QueryModelException {
        super(queryModel, pathToSignature);
        if(paramTypes == null || returnType == null) {
            throw new IllegalArgumentException();
        }

        this.paramTypes = paramTypes;
        this.returnType = returnType;
    }

    protected JSignature(
        QueryModel queryModel,
        String[] sections
    ) throws QueryModelException {
        super(queryModel, sections);
        String pathToSignature = concatenate(sections, sections.length -1);
        paramTypes = queryModel.getParameters(pathToSignature);
        returnType = queryModel.getReturnType(pathToSignature);
    }

    protected JSignature(
        QueryModel queryModel,
        String pathToSignature
    ) throws QueryModelException {
        super(queryModel, pathToSignature);
        paramTypes = queryModel.getParameters(pathToSignature);
        returnType = queryModel.getReturnType(pathToSignature);
    }

    CodeModel.JType[] getParameters() {
        return paramTypes.clone();
    }

    CodeModel.JType getReturnType() {
        return returnType.cloneType();
    }


    public boolean exists(final String[] name, int index) {
        return exists( getQueryModel(), concatenate(name, index) );
    }

    protected String getHyerarchyElemType() {
        return this.getClass().getSimpleName();
    }
}