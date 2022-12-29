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
package org.telosys.tools.dsl.model.dbmodel;

import java.util.List;

import org.telosys.tools.commons.NamingStyleConverter;
import org.telosys.tools.commons.dbcfg.yaml.DatabaseDefinition;
import org.telosys.tools.dsl.model.DslModel;
import org.telosys.tools.dsl.model.DslModelEntity;
import org.telosys.tools.dsl.model.DslModelForeignKey;
import org.telosys.tools.dsl.model.DslModelLink;
import org.telosys.tools.generic.model.Attribute;
import org.telosys.tools.generic.model.Entity;
import org.telosys.tools.generic.model.ForeignKey;
import org.telosys.tools.generic.model.enums.Cardinality;

/**
 * DB-MODEL to DSL-MODEL : Links builder
 * 
 * @author Laurent GUERIN
 * 
 */

public class LinksBuilder {
	
	private final NamingStyleConverter nameConverter = new NamingStyleConverter();
	
	private final DatabaseDefinition databaseDefinition;

	/**
	 * Constructor
	 * @param databaseDefinition
	 */
	public LinksBuilder(DatabaseDefinition databaseDefinition) {
		super();
		this.databaseDefinition = databaseDefinition;
	}

	/**
	 * Create links from foreign keys for all entities
	 * @param model
	 * @return
	 */
	public void createLinks(DslModel model) {
		for ( Entity entity : model.getEntities() ) {
			if ( isJoinEntity(entity) ) {
				createManyToManyLinks(model, (DslModelEntity) entity);
			}
			else {
				createLinks(model, (DslModelEntity) entity);
			}
		}
	}

	private boolean isJoinEntity(Entity entity) {
		//--- Check if entity has 2 Foreign Keys
		if ( entity.getForeignKeys().size() != 2 ) {
			return false;
		}
		//--- Check if all attributes are in the PK and in a FK
		for ( Attribute attribute : entity.getAttributes() ) {
			if ( ! attribute.isKeyElement() ) { 
				return false ; // at least one attribute is not in PK 
			}
			if ( ! attribute.isFK() ) {
				return false ; // at least one attribute is not in FK
			}
		}
		//--- all conditions are met : this is a "Join Entity"
		return true ;
	}
	
	/**
	 * Create links from foreign keys for the given entity
	 * @param model
	 * @param entity
	 * @return
	 */
	protected void createLinks(DslModel model, DslModelEntity entity) {
		for ( ForeignKey fk : entity.getForeignKeys() ) {
			createLinks(model, entity, (DslModelForeignKey) fk);
		}
	}

	protected void createLinks(DslModel model, DslModelEntity entity, DslModelForeignKey fk) {
		if ( databaseDefinition.isLinksManyToOne() ) {
			createLinkManyToOne(entity, fk);
		}
		if ( databaseDefinition.isLinksOneToMany() ) {
			createLinkOneToMany(model, fk);
		}
	}

	/**
	 * Creates a ManyToOne owning side link for the given FK
	 * @param entity
	 * @param fk
	 */
	protected void createLinkManyToOne(DslModelEntity entity, DslModelForeignKey fk) {
		String referencedEntityName = fk.getReferencedEntityName();
		String fieldName = buildFieldNameManyToOne(referencedEntityName, entity);
		// create link
		DslModelLink link = new DslModelLink(fieldName);
		link.setReferencedEntityName(referencedEntityName);
		link.setCardinality(Cardinality.MANY_TO_ONE);
		link.setForeignKeyName(fk.getName());
		link.setBasedOnForeignKey(true);
		link.setOwningSide(true);
		link.setInverseSide(false);
		// add link in entity
		entity.addLink(link);
	}
	private String buildFieldNameManyToOne(String referencedEntityName, DslModelEntity entity) {	
		// ref entity "Person" --> field "person"
		String basicFieldName = nameConverter.toCamelCase(referencedEntityName);
		return getNonDuplicateFieldName(basicFieldName, entity) ; 
	}

	/**
	 * Creates a OneToMany inverse side link for the given FK
	 * @param model
	 * @param fk
	 */
	protected void createLinkOneToMany(DslModel model, DslModelForeignKey fk) {
		String referencedEntityName = fk.getReferencedEntityName();
		DslModelEntity referencedEntity = (DslModelEntity) model.getEntityByClassName(fk.getReferencedEntityName());
		if ( referencedEntity == null ) {
			throw new IllegalStateException("FK "+fk.getName()+ ": invalid referenced entity " + referencedEntityName);
		}
		String originEntityName = fk.getOriginEntityName();
		String fieldName = buildFieldNameOneToMany(originEntityName, referencedEntity);
		// create link
		DslModelLink link = new DslModelLink(fieldName);
		link.setReferencedEntityName(originEntityName);
		link.setCardinality(Cardinality.ONE_TO_MANY);
		link.setOwningSide(false);
		link.setInverseSide(true);
		// add link in entity
		referencedEntity.addLink(link);
	}

	private String buildFieldNameOneToMany(String originEntityName, DslModelEntity entity) {
		// entity "Person" --ref--> Other entity => inverse side field = "personList"
		String basicFieldName = nameConverter.toCamelCase(originEntityName)+"List";
		return getNonDuplicateFieldName(basicFieldName, entity) ; 
	}
	
	private String getNonDuplicateFieldName(String basicFieldName, DslModelEntity entity) {
		String fieldName = basicFieldName ;
		int n = 1;
		while ( fieldExistsInEntity(fieldName, entity) ) {
			n++;
			fieldName = basicFieldName + n;
		}
		return fieldName;
	}
	
	private boolean fieldExistsInEntity(String fieldName, DslModelEntity entity) {
		if ( entity.getAttributeByName(fieldName) != null ) {
			return true;
		}
		if ( entity.getLinkByFieldName(fieldName) != null ) {
			return true;
		}
		return false;
	}
	
	protected void createManyToManyLinks(DslModel model, DslModelEntity entity) {
		List<ForeignKey> foreignKeys = entity.getForeignKeys();
		if ( foreignKeys.size() == 2 ) {
			DslModelForeignKey fk1 = (DslModelForeignKey) foreignKeys.get(0);
			DslModelForeignKey fk2 = (DslModelForeignKey) foreignKeys.get(1);
			DslModelEntity referencedEntity1 = getReferencedEntity(model, fk1);
			DslModelEntity referencedEntity2 = getReferencedEntity(model, fk2);
			createLinkManyToMany(referencedEntity1, referencedEntity2.getClassName()) ;
			createLinkManyToMany(referencedEntity2, referencedEntity1.getClassName()) ;
		}
	}
	
	private void createLinkManyToMany(DslModelEntity entity, String referencedEntityName) {
		String linkFieldName = buildCollectionFieldName(entity, referencedEntityName);
		// create link
		DslModelLink link = new DslModelLink(linkFieldName);
		link.setReferencedEntityName(referencedEntityName);
		link.setCardinality(Cardinality.MANY_TO_MANY);
		link.setOwningSide(false); // no matter
		link.setInverseSide(false); // no matter
		// add link in entity
		entity.addLink(link);
	}
	
	private DslModelEntity getReferencedEntity(DslModel model, DslModelForeignKey fk) {
		String referencedEntityName = fk.getReferencedEntityName();
		DslModelEntity referencedEntity = (DslModelEntity) model.getEntityByClassName(referencedEntityName);
		if ( referencedEntity == null ) {
			throw new IllegalStateException("FK "+fk.getName()+ ": invalid referenced entity " + referencedEntityName);
		}
		return referencedEntity;
	}
	
	/**
	 * Build the link field name for a collection
	 * @param entity the entity in which the field will be added
	 * @param entityInCollection eg "Person"
	 * @return the collection name, eg "personList"
	 */
	private String buildCollectionFieldName(DslModelEntity entity, String entityInCollection) {
		// entity "Person" --ref--> Other entity => inverse side field = "personList"
		String basicFieldName = nameConverter.toCamelCase(entityInCollection)+"List";
		return getNonDuplicateFieldName(basicFieldName, entity) ; 
	}
}
