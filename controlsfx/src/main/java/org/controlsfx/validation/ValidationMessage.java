/**
 * Copyright (c) 2014, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.controlsfx.validation;

import java.util.Comparator;

import javafx.scene.control.Control;


/**
 * Interface to define basic contract for validation message  
 */
public interface ValidationMessage extends Comparable<ValidationMessage>{
	
	/**
	 * Message text
	 * @return message text
	 */
	String getText();
	
	/**
	 * Message {@link Severity} 
	 * @return message severity
	 */
	Severity getSeverity();
	

	/**
	 * Message target - {@link javafx.scene.Control} which message is related to . 
	 * @return message target
	 */
	Control getTarget();
	
	/**
	 * Factory method to create a simple error message 
	 * @param target message target
	 * @param text message text 
	 * @return error message
	 */
	static ValidationMessage error( Control target, String text ) {
		return new SimpleValidationMessage(target, text, Severity.ERROR);
	}
	
	/**
	 * Factory method to create a simple warning message 
	 * @param target message target
	 * @param text message text 
	 * @return warning message
	 */
	static ValidationMessage warning( Control target, String text ) {
		return new SimpleValidationMessage(target, text, Severity.WARNING);
	}
	
	static Comparator<ValidationMessage> COMPARATOR = new Comparator<ValidationMessage>() {

		@Override
		public int compare(ValidationMessage m1, ValidationMessage m2) {
			if ( m1 == null ) {
			    return m2 == null? 0: m2.compareTo(m1);
			} else {
				return m1.compareTo(m2);
			}
		}
	};
		
}