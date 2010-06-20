package com.asemantics.rdfcoder.sourceparse.javadoc;

import com.asemantics.rdfcoder.model.Identifier;
import com.asemantics.rdfcoder.model.IdentifierReader;
import com.asemantics.rdfcoder.model.java.JavadocHandler;
import com.asemantics.rdfcoder.sourceparse.JavadocEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Test case for {@link com.asemantics.rdfcoder.sourceparse.javadoc.JavadocHandlerSerializer}.
 *
 * @author Michele Mostarda (michele.mostarda@gmail.com)
 */
public class JavadocHandlerSerializerTest implements Serializable {

    private JavadocHandlerSerializer javadocHandlerSerializer;

    @Before
    public void setUp() {
        javadocHandlerSerializer = new JavadocHandlerSerializer();
    }

    @After
    public void tearDown() {
        javadocHandlerSerializer = null;
    }

    @Test
    public void testSerializationDeserializationFlow() throws Exception {
        // Data.
        final JavadocHandler jh = javadocHandlerSerializer.getHandler();
        final JavadocEntry javadocEntry = new JavadocEntry(
            "short",
            "long",
            new HashMap<String, List<String>>(){ { put("p1", null); } },
            1, 2
        );
        final Identifier fakeIdentifier = IdentifierReader.readFullyQualifiedClass("path.to.Clazz");

        // Serialization.
        jh.startParsing("test-lib-name", "test-lib-location");
        jh.classJavadoc(
                javadocEntry,
                fakeIdentifier
        );
        jh.endParsing();
        File tmpFile = File.createTempFile("pre", "suff");
        javadocHandlerSerializer.serialize(tmpFile);

        // Deserialization.
        JavadocHandler mockJH = mock(JavadocHandler.class);
        javadocHandlerSerializer.deserialize(
                tmpFile,
                mockJH
        );

        // Verification.
        verify(mockJH).startParsing("test-lib-name", "test-lib-location");
        verify(mockJH).classJavadoc(javadocEntry, fakeIdentifier);
        verify(mockJH).endParsing();
    }


}
