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


package com.asemantics.model;

import java.util.Date;

/**
 * Represents a model to query {@link com.asemantics.model.CodeModel}s.
 */
public interface QueryModel {

    /**
     * Returns the Code Model asset.
     *
     * @return
     */
    public Asset getAsset();

    /**
     * Returns the asset libraries.
     *
     * @return
     */
    public String[] getLibraries();

    /**
     * Returns the library location.
     *
     * @return
     */
    public String getLibraryLocation(String library);

    /**
     * Returns the library datetime.
     *
     * @param library
     * @return
     */
    public Date getLibraryDateTime(String library);
    
}
