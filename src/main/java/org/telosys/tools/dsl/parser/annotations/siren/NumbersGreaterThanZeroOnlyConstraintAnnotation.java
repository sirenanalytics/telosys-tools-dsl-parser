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
package org.telosys.tools.dsl.parser.annotations.siren;

import java.util.List;

import org.telosys.tools.dsl.model.DslModel;
import org.telosys.tools.dsl.model.DslModelAttribute;
import org.telosys.tools.dsl.model.DslModelEntity;
import org.telosys.tools.dsl.parser.annotation.AnnotationDefinition;
import org.telosys.tools.dsl.parser.annotation.AnnotationParamType;
import org.telosys.tools.dsl.parser.annotation.AnnotationScope;
import org.telosys.tools.dsl.parser.commons.ParamError;
import org.telosys.tools.dsl.parser.model.DomainAnnotation;
import org.telosys.tools.generic.model.SirenParams;

/**
 * 'Cascade' annotation
 * Examples :   Cascade(MERGE)   Cascade(M)   Cascade(MERGE, REMOVE)
 *  
 * @author Laurent Guerin
 *
 */
public class NumbersGreaterThanZeroOnlyConstraintAnnotation extends AnnotationDefinition {

	public NumbersGreaterThanZeroOnlyConstraintAnnotation() {
		super(SirenParams.NumbersGreaterThanZeroOnlyConstraint, AnnotationParamType.NONE, AnnotationScope.ATTRIBUTE);
	}
	
	@Override
	public void apply(DslModel model, DslModelEntity entity, DslModelAttribute attribute, Object paramValue) throws ParamError {
		checkParamValue(entity, attribute, paramValue);
		//List<String> list = getListOfParameters(paramValue);
		
		attribute.setSirenParam(SirenParams.NumbersGreaterThanZeroOnlyConstraint, SirenParams.Exists, true);
	}

}
