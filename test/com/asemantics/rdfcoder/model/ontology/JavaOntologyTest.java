package com.asemantics.rdfcoder.model.ontology;

import com.asemantics.rdfcoder.model.CodeHandler;
import com.asemantics.rdfcoder.model.CodeModelBase;
import com.asemantics.rdfcoder.model.CoderFactory;
import com.asemantics.rdfcoder.sourceparse.DirectoryParser;
import com.asemantics.rdfcoder.sourceparse.JavaBytecodeFileParser;
import com.asemantics.rdfcoder.sourceparse.ObjectsTable;
import com.asemantics.rdfcoder.sourceparse.JStatistics;
import com.asemantics.rdfcoder.storage.JenaCoderFactory;
import com.asemantics.rdfcoder.CoderUtils;
import junit.framework.TestCase;

import java.io.File;/*
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

public class JavaOntologyTest extends TestCase {


    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testApplyOntology() {
        final File dir = new File("./classes");
        if( ! dir.exists() ) {
            throw new RuntimeException(
                String.format("It is expected to find the classes dir at location %s", dir.getAbsolutePath())
            );
        }
        JStatistics statistics    = new JStatistics();
        CoderFactory coderFactory = new JenaCoderFactory();
        CodeModelBase codeModel   = coderFactory.createCodeModel();
        ValidatingCodeModel vcm   = new ValidatingCodeModel( codeModel, coderFactory.createCodeModelOntology() );
        CodeHandler codeHandler   = coderFactory.createHandlerOnModel(vcm);
        ObjectsTable objectsTable = new ObjectsTable();
        CodeHandler statisticsCodeHandler = statistics.createStatisticsCodeHandler( codeHandler );

        DirectoryParser directoryParser = new DirectoryParser( new JavaBytecodeFileParser(), new CoderUtils.JavaClassFilenameFilter() );
        directoryParser.initialize( statisticsCodeHandler, objectsTable );
        directoryParser.parseDirectory(dir.getName(), dir );

        assertTrue("Cannot find classes", statistics.getParsedClasses() > 0);

        System.out.println(statistics);
    }
}
