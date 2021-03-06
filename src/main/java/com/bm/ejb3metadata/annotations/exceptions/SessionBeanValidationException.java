package com.bm.ejb3metadata.annotations.exceptions;

/**
 * Exception thrown if there is a failure in a bean.
 * 
 * @author Daniel Wiese
 */
public class SessionBeanValidationException extends RuntimeException {

	/**
	 * Id for serializable class.
	 */
	private static final long serialVersionUID = 9155236573751243404L;

	/**
	 * Constructs a new runtime exception with <code>null</code> as its detail
	 * message. The cause is not initialized, and may subsequently be
	 * initialized by a call to {@link #initCause}.
	 */
	public SessionBeanValidationException() {
		super();
	}

	/**
	 * Constructs a new runtime exception with the specified detail message. The
	 * cause is not initialized, and may subsequently be initialized by a call
	 * to {@link #initCause}.
	 * 
	 * @param message
	 *            the detail message. The detail message is saved for later
	 *            retrieval by the {@link #getMessage()} method.
	 */
	public SessionBeanValidationException(final String message) {
		super(message);
	}

	/**
	 * Constructs a new runtime exception with the specified detail message and
	 * cause.
	 * <p>
	 * Note that the detail message associated with <code>cause</code> is
	 * <i>not</i> automatically incorporated in this runtime exception's detail
	 * message.
	 * 
	 * @param message
	 *            the detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method).
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method). (A <tt>null</tt> value is
	 *            permitted, and indicates that the cause is nonexistent or
	 *            unknown.)
	 */
	public SessionBeanValidationException(final String message,
			final Throwable cause) {
		super(message, cause);
	}

}
