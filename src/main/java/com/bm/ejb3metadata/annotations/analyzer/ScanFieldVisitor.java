package com.bm.ejb3metadata.annotations.analyzer;

import org.hibernate.repackage.cglib.asm.FieldVisitor;

import com.bm.ejb3metadata.annotations.JField;
import com.bm.ejb3metadata.annotations.metadata.ClassAnnotationMetadata;
import com.bm.ejb3metadata.annotations.metadata.FieldAnnotationMetadata;

/**
 * This classes analyses a given field and build/fill meta data information.
 * 
 * @author Daniel Wiese
 */
public class ScanFieldVisitor extends
		ScanCommonVisitor<FieldAnnotationMetadata> implements FieldVisitor {

	/**
	 * Class generated by the visitor which correspond to meta data contained in
	 * the parsed field.
	 */
	private FieldAnnotationMetadata fieldAnnotationMetadata = null;

	/**
	 * Parent of field annotation meta data that are built by this visitor.
	 */
	private ClassAnnotationMetadata classAnnotationMetadata = null;

	/**
	 * Constructor.
	 * 
	 * @param jField
	 *            field object on which we set meta data.
	 * @param classAnnotationMetadata
	 *            the parent object on which add generated meta-data.
	 */
	public ScanFieldVisitor(final JField jField,
			final ClassAnnotationMetadata classAnnotationMetadata) {

		// object build and to fill
		this.fieldAnnotationMetadata = new FieldAnnotationMetadata(jField,
				classAnnotationMetadata);

		// parent
		this.classAnnotationMetadata = classAnnotationMetadata;

		// list of visitors to use
		initVisitors();
	}

	/**
	 * Build visitors used by this one.
	 */
	private void initVisitors() {
		super.initVisitors(fieldAnnotationMetadata);

	}

	/**
	 * Visits the end of the method. This method, which is the last one to be
	 * called, is used to inform the visitor that all the annotations and
	 * attributes of the method have been visited.
	 */
	@Override
	public void visitEnd() {
		classAnnotationMetadata
				.addFieldAnnotationMetadata(fieldAnnotationMetadata);
	}

}
