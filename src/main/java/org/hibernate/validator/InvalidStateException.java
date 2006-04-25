//$Id: InvalidStateException.java,v 1.1 2006/04/17 12:11:07 daniel_wiese Exp $
package org.hibernate.validator;

/**
 * Thrown when the bean has violated one or several of its constraints
 * You can get the violation details in getInvalidValues()
 *
 * @author Gavin King
 */
public class InvalidStateException extends RuntimeException {

	private final InvalidValue[] invalidValues;

	public InvalidStateException(InvalidValue[] invalidValues) {
		this( invalidValues, invalidValues[0].getBeanClass().getName() );
	}

	public InvalidStateException(InvalidValue[] invalidValues, String className) {
		super( "validation failed for: " + className );
		this.invalidValues = invalidValues;
	}

	public InvalidValue[] getInvalidValues() {
		return invalidValues;
	}

}
