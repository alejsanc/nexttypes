/*
 * Copyright 2015-2021 Alejandro Sánchez <alex@nexttypes.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nexttypes.security;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

public class Security {
	public static final int BCRYPT_COST = 12;
	public static final int BCRYPT_MAX_PASSWORD_LENGTH = 72;
	public static final String HIDDEN_PASSWORD = "••••••••";
	public static final String PASSWORD_STRENGTH_CHECK = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$";
	protected static final String HASH = "$2y$12$TFhXz5DvNAl0/jUg7hd56eLT52bZXiuBsDrapQ1Tm8LDaB7qaO3o6";

	public static String randomString() {
		SecureRandom random = new SecureRandom();
		return new BigInteger(256, random).toString(32);
	}

	public static boolean checkPassword(String hash, String password) {
		boolean result = false;

		if (hash == null) {
			OpenBSDBCrypt.checkPassword(HASH, password.toCharArray());
		} else {
			result = OpenBSDBCrypt.checkPassword(hash, password.toCharArray());
		}

		return result;
	}

	public static boolean checkPasswordStrength(String password) {
		return password == null || password.matches(PASSWORD_STRENGTH_CHECK);
	}

	public static boolean passwordsMatch(String password, String passwordRepeat) {
		return (password == null && passwordRepeat == null)
				|| (password != null && password.equals(passwordRepeat));
	}

	public static String passwordHash(String password) {
		if (password == null) {
			return null;
		}

		SecureRandom random = new SecureRandom();
		byte[] salt = new BigInteger(127, random).toByteArray();
		return OpenBSDBCrypt.generate(password.toCharArray(), salt, BCRYPT_COST);
	}
}