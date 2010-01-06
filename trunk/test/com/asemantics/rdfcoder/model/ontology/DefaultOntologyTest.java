/*
 * Copyright 2007-2008 Michele Mostarda ( michele.mostarda@gmail.com ).
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.asemantics.rdfcoder.model.ontology;

import com.asemantics.rdfcoder.model.CodeModel;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Test case for the {@link com.asemantics.rdfcoder.model.ontology.DefaultOntology} class.
 */
public class DefaultOntologyTest {

    private Ontology ontology;

    private final static String SUB_PREFIX = "sub_prefix_";
    private final static String OBJ_PREFIX = "obj_prefix_";
    private final static String PREDICATE  = "http://path.to.url/test_";

    private final static int SIZE = 100;

    @Before
    public void setUp() throws Exception {
        ontology = new DefaultOntology();
    }

    @After
    public void tearDown() throws Exception {
        ontology = null;
    }

    /**
     * Tests the {@link com.asemantics.rdfcoder.model.ontology.Ontology#defineRelation(String, java.net.URL, String)}
     * method.
     *
     * @throws OntologyException
     * @throws MalformedURLException
     */
    @Test
    public void testDefineRelation() throws OntologyException, MalformedURLException {
       for(int i = 0; i < SIZE; i++) {
           ontology.defineRelation(
                   SUB_PREFIX + i + CodeModel.PREFIX_SEPARATOR,
                   new URL(PREDICATE + (i % 2) ),
                   OBJ_PREFIX + i + CodeModel.PREFIX_SEPARATOR
           );
       }
       Assert.assertEquals(ontology.getRelationsCount(), SIZE);
    }

    /**
     * Tests the control on relation redefinition. 
     *
     * @throws MalformedURLException
     * @throws OntologyException
     */
    @Test
    public void testCheckRedefinition() throws MalformedURLException, OntologyException {
        final String sub = SUB_PREFIX;
        final URL   pred = new URL(PREDICATE);
        final String obj = OBJ_PREFIX;
        ontology.defineRelation(sub, pred, obj);
        try {
            ontology.defineRelation(sub, pred, obj);
            Assert.fail();
        } catch (OntologyException oe) {}
    }

    /**
     * Tests the {@link com.asemantics.rdfcoder.model.ontology.Ontology#printOntology(java.io.PrintStream)} method.
     *
     * @throws MalformedURLException
     * @throws OntologyException
     */
    @Test
    public void testPrint() throws MalformedURLException, OntologyException {
        testDefineRelation();

        final Counter counter = new Counter();
        ontology.printOntology(new PrintStream(System.out) {

            public void println() {
                super.println();
                counter.increment();
            }
        });

        Assert.assertEquals(SIZE, counter.count);
    }

    /**
     * Tests method {@link com.asemantics.rdfcoder.model.ontology.Ontology#validateTriple(String, String, String)}
     * on valid triples.
     *
     * @throws MalformedURLException
     * @throws OntologyException
     */
    @Test
    public void testPositiveValidation() throws MalformedURLException, OntologyException {
        testDefineRelation();
        for(int i = 0; i < SIZE; i++) {
            ontology.validateTriple(
                    SUB_PREFIX + i + CodeModel.PREFIX_SEPARATOR + "postfix",
                    PREDICATE  + (i % 2),
                    OBJ_PREFIX + i + CodeModel.PREFIX_SEPARATOR + "postfix2"
             );
        }
    }

    /**
     * Tests method {@link com.asemantics.rdfcoder.model.ontology.Ontology#validateTriple(String, String, String)}
     * on invalid triples.
     *
     * @throws MalformedURLException
     * @throws OntologyException
     */
    @Test
    public void testNegativeValidation() throws MalformedURLException, OntologyException {
        testDefineRelation();
        try {
            ontology.validateTriple(
                    SUB_PREFIX + 0 + CodeModel.PREFIX_SEPARATOR + "postfix",
                    PREDICATE + 1,
                    OBJ_PREFIX + 0 + CodeModel.PREFIX_SEPARATOR + "postfix2"
            );
            Assert.fail();
        } catch (OntologyException oe) {
            // Ok.
        }
    }

    /**
     * Tests the ontology accessor methods.
     *
     * @throws MalformedURLException
     * @throws OntologyException
     */
    @Test
    public void testAccess() throws MalformedURLException, OntologyException {
        testDefineRelation();

        Set<Object> found = new HashSet<Object>();
        final int count = ontology.getRelationsCount();

        for( int i = 0; i < count; i++) {
            if(found.contains(ontology.getRelationSubjectPrefix(i))) {
                Assert.fail("Cannot contain a subject prefix twice");
            }
            found.add(ontology.getRelationSubjectPrefix(i));
        }
        Assert.assertEquals(count, found.size());

        found.clear();
        for( int i = 0; i < count; i++) {
            if(found.contains(ontology.getRelationObjectPrefix(i))) {
                Assert.fail("Cannot contain an object prefix twice");
            }
            found.add(ontology.getRelationObjectPrefix(i));
        }
        Assert.assertEquals(count, found.size());

        found.clear();
        for( int i = 0; i < count; i++) {
            found.add( ontology.getRelationPredicate(i) );
        }
        Assert.assertEquals(2, found.size());

        Assert.assertEquals(SIZE, count);

        found.clear();
    }

    /**
     * Auxiliary class.
     */
    class Counter {
        int count = 0;

        void increment() {
            count++;
        }
    }

}
