/*
 * =============================================================================
 *
 *   Copyright (c) 2011-2014, The THYMELEAF team (http://www.thymeleaf.org)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 * =============================================================================
 */
package org.thymeleaf.aurora.engine;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.util.Validate;

/**
 *
 * @author Daniel Fern&aacute;ndez
 * @since 3.0.0
 *
 */
final class ElementAttribute {


    static final String DEFAULT_OPERATOR = "=";

    /*
     * Note: An Attribute should not be made responsible for converting non-String values to String, or computing
     * the boolean-ness of attributes or their representation. All these should be the responsibility of the
     * diverse processors being executed. This class is a raw representation of what appears/will appear on markup.
     */


    AttributeDefinition definition = null;
    String name = null;
    String operator = DEFAULT_OPERATOR;
    String value = null;
    ElementAttributes.ValueQuotes valueQuotes = null;
    int line = -1;
    int col = -1;



    ElementAttribute() {
        super();
    }


    // No public constructor or getters! Attribute instances are package-protected, and should always be created
    // and maintained from the corresponding Attributes instances, because these are responsible for setting the
    // right attribute definition, name, etc.



    // Used internally, only from the engine
    void setElementAttribute(
            final AttributeDefinition definition,
            final String name, final String operator, final String value,
            final ElementAttributes.ValueQuotes valueQuotes,
            final int line, final int col) {

        this.definition = definition;
        this.name = name;
        this.operator = (operator == null && value != null? ElementAttribute.DEFAULT_OPERATOR : operator);
        this.value = value;
        this.valueQuotes = (valueQuotes == null? ElementAttributes.ValueQuotes.DOUBLE : valueQuotes);
        this.line = line;
        this.col = col;

    }



    // Used internally, only from the engine
    void setElementAttribute(
            final String name, final String operator, final String value,
            final ElementAttributes.ValueQuotes valueQuotes,
            final int line, final int col) {

        this.name = name;

        if (operator != null) {
            // Coming operator will be used unless the value is null, in which case operator should be too
            this.operator = (value == null? null : operator);
        } else if (this.operator == null) {
            // No existing operator, and we are not specifically setting any either
            this.operator = (this.value == null && value != null ? ElementAttribute.DEFAULT_OPERATOR : null);
        } else if (value != null && value.length() == 0) {
            // We cannot respect the existing operator if they was none and we are setting an empty string value
            this.operator = (this.operator == null? ElementAttribute.DEFAULT_OPERATOR : this.operator);
        }

        if (valueQuotes != null) {
            // Coming quotes will be used unless the value is null, in which case quotes should be too
            this.valueQuotes = (value == null? null : valueQuotes);
        } else if (this.valueQuotes == null) {
            // No existing quotes, and we are not specifically setting any either
            this.valueQuotes = (this.value == null && value != null? ElementAttributes.ValueQuotes.DOUBLE : null);
        } else if (value != null && value.length() == 0) {
            // We cannot respect the existing quotes if they were none and we are setting an empty string value
            this.valueQuotes = (ElementAttributes.ValueQuotes.NONE.equals(this.valueQuotes)? ElementAttributes.ValueQuotes.DOUBLE : this.valueQuotes);
        }

        this.value = value;
        this.line = line;
        this.col = col;

    }




    void write(final Writer writer) throws IOException {

        Validate.notNull(writer, "Writer cannot be null");

        /*
         * How an attribute will be written:
         *    - If value == null : only the attribute name will be written.
         *    - If value != null : the attribute will be written according to its operator and quotes
         */

        writer.write(this.name);
        if (this.operator != null && this.value != null) {
            writer.write(this.operator);
            if (this.valueQuotes == null) {
                writer.write(this.value);
            } else {
                switch (this.valueQuotes) {
                    case DOUBLE:
                        writer.write('"');
                        writer.write(this.value);
                        writer.write('"');
                        break;
                    case SINGLE:
                        writer.write('\'');
                        writer.write(this.value);
                        writer.write('\'');
                        break;
                    case NONE:
                        writer.write(this.value);
                        break;
                }
            }
        }

    }


    ElementAttribute cloneElementAttribute() {
        final ElementAttribute clone = new ElementAttribute();
        clone.definition = this.definition;
        clone.name = this.name;
        clone.operator = this.operator;
        clone.value = this.value;
        clone.valueQuotes = this.valueQuotes;
        clone.line = this.line;
        clone.col = this.col;
        return clone;
    }


    public String toString() {
        final StringWriter stringWriter = new StringWriter();
        try {
            write(stringWriter);
        } catch (final IOException e) {
            // Should never happen!
            throw new TemplateProcessingException("Error computing attribute representation", e);
        }
        return stringWriter.toString();
    }



}