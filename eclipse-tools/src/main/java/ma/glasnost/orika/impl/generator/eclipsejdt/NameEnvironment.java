/*
 * Orika - simpler, better and faster Java bean mapping
 *
 * Copyright (C) 2011-2013 Orika authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ma.glasnost.orika.impl.generator.eclipsejdt;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class NameEnvironment implements INameEnvironment {

    private static final int END_OF_STREAM = -1;
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    
	private final ClassLoader classLoader;

	public NameEnvironment(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public NameEnvironmentAnswer findType(char[][] compoundTypeName) {
		StringBuilder result = new StringBuilder();

		String sep = "";

		for (char[] chars : compoundTypeName) {
			result.append(sep);
			result.append(chars);
			sep = ".";
		}

		return findType(result.toString());
	}

	public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
		StringBuilder result = new StringBuilder();

		String sep = "";

		for (char[] chars : packageName) {
			result.append(sep);
			result.append(chars);
			sep = ".";
		}

		result.append(sep);
		result.append(typeName);
		NameEnvironmentAnswer answer = findType(result.toString());
		if (answer == null && Character.isUpperCase(typeName[0])) {
		    result.setLength(0);
		    result.append(typeName);
		    answer = findType(result.toString());
		}
		return answer;
	}

	public NameEnvironmentAnswer findType(String className) {
		try {
			String resourceName = className.replace('.', '/') + ".class";

			InputStream is = classLoader.getResourceAsStream(resourceName);

			if (is == null) {
				return null;

			} else {
				try {
					final ByteArrayOutputStream output = new ByteArrayOutputStream();
					final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

					int n = 0;
					while ((n = is.read(buffer)) != END_OF_STREAM) {
						output.write(buffer, 0, n);
					}

					byte[] classBytes = output.toByteArray();
					char[] fileName = className.toCharArray();

					ClassFileReader classFileReader = new ClassFileReader(
					        classBytes, fileName, true);

					return new NameEnvironmentAnswer(classFileReader, null);
				} finally {
					is.close();
				}
			}
		} catch (IOException | ClassFormatException e) {
			return null;
		}
    }

	private boolean isPackage(String result) {
		String resourceName = "/" + result.replace('.', '/') + ".class";
		InputStream is = classLoader.getResourceAsStream(resourceName);
		return is == null;
	}

	public boolean isPackage(char[][] parentPackageName, char[] packageName) {
		StringBuilder result = new StringBuilder();

		String sep = "";

		if (parentPackageName != null) {
			for (char[] chars : parentPackageName) {
				result.append(sep);
				result.append(chars);
				sep = ".";
			}
		}

		if (Character.isUpperCase(packageName[0])) {
			return false;
		}

		result.append(sep);
		result.append(packageName);

		return isPackage(result.toString());
	}

	public void cleanup() {
		// nothing to do
	}
}