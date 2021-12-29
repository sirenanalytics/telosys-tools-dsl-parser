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
package org.telosys.tools.dsl.parser2;

import org.telosys.tools.dsl.parser.commons.ParamValue;
import org.telosys.tools.dsl.parser.exceptions.ParsingError;
import org.telosys.tools.dsl.parser.model.DomainTag;

/**
 * Annotation parsing 
 * 
 * @author Laurent GUERIN
 */
public class TagProcessor extends AnnotationAndTagProcessor {

	/**
	 * Constructor
	 * 
	 * @param entityName
	 * @param fieldName
	 */
	public TagProcessor(String entityName, String fieldName) {
		super(entityName, fieldName);
	}
	
	/**
	 * Constructor
	 * 
	 * @param entityName
	 */
	public TagProcessor(String entityName) {
		super(entityName);
	}
	
	/**
	 * Parse the given raw tag, e.g. "#Foo" or "#Bar('abcd')"
	 * @param tagString
	 * @return
	 * @throws ParsingError
	 */
	public DomainTag parseTag(Element element) throws ParserError  {
		// get the tag name 
		String tagName = getName(element);

		// get the raw parameter value if any
		String rawParameterValue = getParameterValue(element);
		
		if ( rawParameterValue != null ) {
			ParamValue paramValue = buildTagParamValue(tagName, rawParameterValue);
			try {
				return new DomainTag(tagName, paramValue.getAsString());
			} catch (ParsingError e) {
				// TODO : replace ParsingError by SyntaxError ?
				throw newError(element.getLineNumber(), "'" + element.getContent() + "' : " + e.getErrorMessage() );
			}
		}
		else {
			return new DomainTag(tagName);
		}
	}	
}