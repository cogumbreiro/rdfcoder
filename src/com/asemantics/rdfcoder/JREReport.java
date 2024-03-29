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


package com.asemantics.rdfcoder;

import com.asemantics.rdfcoder.parser.JStatistics;

import java.io.File;

/**
 * Defines a report on activities concerning parsing of the
 * <i>Java Runtime Environment</i>.
 */
public class JREReport {

    /**
     * The <i>JRE</i> location.
     */
    private File jreLocation;

    /**
     * The Java parser statistics.
     */
    private JStatistics statistics;

    /**
     * Constructor.
     *
     * @param l
     * @param s
     */
    protected JREReport(File l, JStatistics s) {
        jreLocation = l;
        statistics  = s;
    }

    JStatistics getStatistics() {
        return statistics;
    }

    File getJRELocation() {
        return jreLocation;
    }

    @Override
    public String toString() {
        return String.format(
                "%s[%s]\n%s",
                this.getClass().getName(),
                jreLocation.getAbsolutePath(),
                statistics.toStringReport()
        );
    }
}
