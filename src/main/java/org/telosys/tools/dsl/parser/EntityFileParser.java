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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.telosys.tools.dsl.DslModelUtil;
import org.telosys.tools.dsl.DslParserException;
import org.telosys.tools.dsl.parser.model.DomainEntity;

/**
 * Telosys DSL entity parser
 *
 * @author Laurent Guerin
 * 
 */
public class EntityFileParser {

	private static final char SPACE = 32;

	private final File entityFile;
	private final String entityNameFromFileName;

	private final List<Field> fieldsParsed = new LinkedList<>();
	private Field currentField = null ;

	private boolean inFields      = false ;
	private boolean inAnnotations = false ;
	
	
	private void log(String message) {
		System.out.println("LOG:" + message);
	}

	private void throwParsingException(String message) {
		String errorMessage = entityNameFromFileName + " : " + message;
		throw new DslParserException(errorMessage);
	}
	private void throwParsingException(String message, int lineNumber) {
		String errorMessage = entityNameFromFileName + " : " + message + " [line " + lineNumber + "]";
		throw new DslParserException(errorMessage);
	}

	public EntityFileParser(String fileName) {
		this.entityFile = new File(fileName);
		this.entityNameFromFileName = DslModelUtil.getEntityName(entityFile);
	}

	/**
	 * Parse entity from the given file
	 * 
	 * @param file
	 * @return
	 */
	public DomainEntity parse() {

		DomainEntity domainEntity = new DomainEntity(entityNameFromFileName);

		List<Field> fields = parseFile() ; // rename parseFile()
		// TODO : parseAllFields(domainEntity);
		return domainEntity;
	}
	
	protected List<Field> parseFile() {
		if (!entityFile.exists()) {
			throwParsingException("File not found");
		}
		log("parse() : File : " + entityFile.getAbsolutePath());
		
		int lineNumber = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(entityFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				lineNumber++;
				processLine(line, lineNumber);
				// read next line
			}
		} catch (IOException e) {
			throwParsingException("IOException");
		}
		return this.fieldsParsed ;
	}
	
	private void processLine(String line, int lineNumber) {
		System.out.println("\n-------------------------------------------");
		System.out.println("#" + lineNumber + " : " + line);
		
		if ( inFields ) {
			processLineFieldLevel(line, lineNumber);
			System.out.println("processLineFieldLevel return : Field = " + currentField );
		}
		else {
			String s = processLineEntityLevel(line, lineNumber);
			if ( s != null ) {
				System.out.println("\n=== ENTITY : " + s);
			}
		}
	}

	/**
	 * Process the given line for a current level = ENTITY LEVEL
	 * @param line
	 * @param lineNumber
	 * @return the entity name if found or null if none
	 */
	protected String processLineEntityLevel(String line, int lineNumber) {
		StringBuilder sb = new StringBuilder();
		char previous = 0;
		for (char c : line.toCharArray()) {
			System.out.print( "[" + c+ "]");
			if (c > SPACE) {
				switch (c) {

				case '{':
					inFields = true ;
					break;
				
				case '}':
					inFields = false ;
					break;
				
				case '/':
					if (previous == '/') {
						// comment 
						return currentValue(sb);
					}
					break;
				
				default:
					
					// TODO : check invalid characters
					sb.append(c);
				}
				previous = c;
			}
		}
		return currentValue(sb);
	}
	
	private void resetCurrentField(int lineNumber) {
		if ( currentField == null ) {
			currentField = new Field(lineNumber);
		}
		else {
			if ( currentField.isVoid() ) {
				currentField = new Field(lineNumber);
			}
		}
	}

	/**
	 * Process the given line for a current level = FIELD LEVEL
	 * @param line
	 * @param lineNumber
	 * @return
	 */
	protected void processLineFieldLevel(String line, int lineNumber) {
		
		boolean inSingleQuote = false ;
		boolean inDoubleQuote = false ;
		
		char previous = 0;
		resetCurrentField(lineNumber);
		System.out.println("processLineFieldLevel : #" + lineNumber + " : '" + line + "'");
		System.out.println("processLineFieldLevel : #" + lineNumber + " : Field : " + currentField );
		
		// parse all chararcters in the given line
		for (char c : line.toCharArray()) {
			System.out.print( "[" + c + "]");
			if (c >= SPACE) { // if not a void char
				
				switch (c) {
				case SPACE :   // end of field 
					if ( inSingleQuote || inDoubleQuote ) {
						keepChar(c);
					}
					break;

				case ';':   // end of field 
					if ( inAnnotations ) {
						if ( inSingleQuote || inDoubleQuote ) {
							keepChar(c);
						}
						else {
							throwParsingException("Unexpected ';'", lineNumber) ;
						}
					}
					else if ( inFields ) {
						endOfCurrentField() ;
					}
					else {
						throwParsingException("Unexpected ';'", lineNumber) ;
					}
					break;
					
				case '{':
					if ( inAnnotations ) {
						if ( inSingleQuote || inDoubleQuote ) {
							keepChar(c);
						}
						else {
							throwParsingException("Unexpected '{'", lineNumber) ;
						}
					}
					else if ( inFields ) {
						inAnnotations = true ;
						currentField.setInAnnotations(true);
					}
					else {
						throwParsingException("Unexpected '{'", lineNumber) ;
					}
					
					break;
					
				case '}':
					if ( inAnnotations ) {
						if ( inSingleQuote || inDoubleQuote ) {
							keepChar(c);
						}
						else {
							inAnnotations = false ; // End of annotations
							currentField.setInAnnotations(false);
						}
					}
					else if ( inFields ) {
						inFields = false ; // End of fields
					}
					else {
						throwParsingException("Unexpected '}'", lineNumber) ;
					}
					break;
					
				case '/':
					if ( inSingleQuote || inDoubleQuote ) {
						keepChar(c);
					}
					else {
						if (previous == '/') {
							// comment => end of current line
							return ;
						}
					}
					break;
					
				case '\'': // single quote char (open/close)
					if ( inAnnotations ) {
						if ( ! inDoubleQuote ) {
							inSingleQuote = ! inSingleQuote ; // toggle quote flag
						}
						keepChar(c);
					}
					else {
						throw new DslParserException("Unexpected single quote");
					}
					break;
					
				case '"': // double quote char (open/close)
					if ( inAnnotations ) {
						if ( ! inSingleQuote ) {
							inDoubleQuote = ! inDoubleQuote ; // toggle quote flag
						}
						keepChar(c);
					}
					else {
						throw new DslParserException("Unexpected double quote");
					}
					break;

				default:
					keepChar(c);
				}
				previous = c;
			}
		}
	}
	
	private void keepChar(char c) {
		System.out.print( "+" );
		currentField.append(c);
	}
//	private void keepCharOnlyIfInQuote(char c) {
//		System.out.print( "+" );
//		currentField.append(c);
//	}

	private void endOfCurrentField() {
		currentField.finished();
		// TODO : check field
		// if invalid => ERROR unexpected ';'
		System.out.println("\n\n=== FINISHED : " + currentField);
		fieldsParsed.add(currentField);
	//	currentField = null ; // no current field
		currentField = new Field(-1) ; // no current field
		inAnnotations = false ;
	}
	
	private String currentValue(StringBuilder sb) {
		String s = sb.toString();
		return ( s.length() > 0 ? s : null );
	}
}
