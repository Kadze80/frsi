/*
package util;

import com.sun.xml.internal.bind.marshaller.CharacterEscapeHandler;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class XmlCharacterHandler implements CharacterEscapeHandler {

	@Override
	public void escape(char[] chars, int start, int len, boolean isAttValue, Writer writer) throws IOException {

		StringWriter buffer = new StringWriter();
		for (int i = start; i < start + len; i++) buffer.write(chars[i]);
		String st = buffer.toString();

		if (!st.contains("CDATA")) {
			st = buffer.toString().replace("&", "&amp;").replace("<", "&lt;")
				.replace(">", "&gt;").replace("'", "&apos;").replace("\"", "&quot;");
		}
		writer.write(st);
	}
}
*/