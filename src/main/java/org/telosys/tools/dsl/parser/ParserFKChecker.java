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
package org.telosys.tools.dsl.parser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.telosys.tools.dsl.parser.exceptions.EntityParsingError;
import org.telosys.tools.dsl.parser.model.DomainEntity;
import org.telosys.tools.dsl.parser.model.DomainFK;
import org.telosys.tools.dsl.parser.model.DomainField;
import org.telosys.tools.dsl.parser.model.DomainModel;



public class ParserFKChecker {

	private class FullFK {
		String entityName ;
		String fieldName ;
		DomainFK fk;
		
		public FullFK(DomainEntity entity, DomainField field, DomainFK fk) {
			super();
			this.entityName = entity.getName();
			this.fieldName = field.getName();
			this.fk = fk;
		}

		public String getEntityName() {
			return entityName;
		}
		public String getFieldName() {
			return fieldName;
		}
		public DomainFK getFk() {
			return fk;
		}
	}

	public void checkNoDuplicateFK(DomainModel model, List<EntityParsingError> errors) {
		Map<String,List<FullFK>> map = buildFKMap(model);
		for (String fkName : map.keySet() ) {
			List<FullFK> list = map.get(fkName);
			if (list.size() > 1 ) {
				// duplicated FK name found
				for (FullFK fullFK : list) {
					
					EntityParsingError err = new EntityParsingError(
							fullFK.getEntityName(), 
							fullFK.getFieldName() 
							  + " : Duplicated FK name '" + fkName + "' ");
					errors.add(err);
				}
			}
		}
	}
	
	private Map<String,List<FullFK>> buildFKMap(DomainModel model) {
		Map<String,List<FullFK>> map = new HashMap<>();
		for (DomainEntity entity : model.getEntities()) {
			for (DomainField field : entity.getFields() ) {
				for (DomainFK fk : field.getFKDeclarations() ) {
					FullFK fullFK = new FullFK(entity, field, fk);
					String fkName = fk.getFkName();
					List<FullFK> list = map.get(fkName);
					if ( list == null ) {
						// First FK with this name => new list
						list = new LinkedList<>();
						list.add(fullFK);
						map.put(fkName, list);
					}
					else {
						// Another FK with this name : add if duplicated
						if ( isDuplicatedFK(fullFK, list) ) {
							list.add(fullFK);
						}
					}
				}
			}
		}
		return map;
	}	

	private boolean isDuplicatedFK(FullFK fullFK, List<FullFK> list) {
		for (FullFK e : list) {
			// not in the same entity => duplicated
			if ( ! fullFK.getEntityName().equals(e.getEntityName()) ) {
				return true ;
			}
			// in the same entity but not referencing the same entity => duplicated
			if ( ! fullFK.getFk().getReferencedEntityName().equals(e.getFk().getReferencedEntityName()) ) {
				return true ;
			}
		}
		return false ; // no real duplicate FK found
	}

//	private String buildFKOrigin(DomainEntity entity, DomainField field, DomainFK fk) {
//		StringBuilder sb = new StringBuilder();
//		sb.append(entity.getName());
//		sb.append(".");
//		sb.append(field.getName());
//		sb.append(":");
//		sb.append(fk.getFkName());
//		return sb.toString();
//	}
}
