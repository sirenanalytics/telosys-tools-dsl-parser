/**
 *  Copyright (C) 2008-2017  Telosys project org. ( http://www.telosys.org/ )
 *
 *  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.gnu.org/licenses/lgpl.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.telosys.tools.dsl;

/**
 * DSL Parser Exception 
 * 
 * @author Laurent GUERIN
 *
 */
public class DslParserException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DslParserException(String message) {
        super(message);
    }
    public DslParserException(String message, int lineNumber) {
    	// TODO : build message
        super(message);
    }
}
