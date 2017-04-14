package com.lab309.general;

/**
 * Class containing static methods for transferring data to a byte array and vice-versa
 *
 * Created by Vitor Andrade dos Santos on 3/28/17.
 */

public abstract class ByteArrayConverter {
	/*METHODS*/
	//TO ARRAY
	public static byte[] shortToArray (short hd, byte[] array, int offset) {
		for (int i = 0; i < SizeConstants.sizeOfShort; i++) {
			array[offset+i] = (byte)(hd >> i*Byte.SIZE);
		}
		return array;
	}

	public static byte[] intToArray (int d, byte[] array, int offset) {
		for (int i = 0; i < SizeConstants.sizeOfInt; i++) {
			array[offset+i] = (byte)(d >> i*Byte.SIZE);
		}
		return array;
	}

	public static byte[] longToArray (long ld, byte[] array, int offset) {
		for (int i = 0; i < SizeConstants.sizeOfLong; i++) {
			array[offset+i] = (byte)(ld >> i*Byte.SIZE);
		}
		return array;
	}

	public static byte[] floatToArray (float f, byte[] array, int offset) {
		int bits = Float.floatToIntBits(f);
		for (int i = 0; i < SizeConstants.sizeOfInt; i++) {
			array[offset+i] = (byte)(bits >> i*Byte.SIZE);
		}
		return array;
	}

	public static byte[] doubleToArray (double lf, byte[] array, int offset) {
		long bits = Double.doubleToLongBits(lf);
		for (int i = 0; i < SizeConstants.sizeOfLong; i++) {
			array[offset+i] = (byte)(bits >> i*Byte.SIZE);
		}
		return array;
	}

	public static byte[] booleanToArray (boolean b, byte[] array, int offset) {
		array[offset] = (byte)(b ? 1 : 0);
		return array;
	}

	public static void charToArray (char c, byte[] array, int offset) {
		for (int i = 0; i < SizeConstants.sizeOfChar; i++) {
			array[offset+i] = (byte)(c >> i*Byte.SIZE);
		}
	}

	public static byte[] latinCharToArray (char c, byte[] array, int offset) {
		array[offset] = (byte)c;
		return array;
	}

	public static byte[] stringToArray (String string, byte[] array, int offset) {
		int i, j, byteI;

		byteI = offset;
		for (i = 0; i < string.length(); i++) {
			for (j = 0; j < SizeConstants.sizeOfChar; j++) {
				array[byteI] = (byte)(string.charAt(i) >> j*Byte.SIZE);
				byteI++;
			}
		}

		for (j = 0; j < SizeConstants.sizeOfChar; j++) {
			array[byteI] = (byte)0;
			byteI++;
		}
		return array;
	}

	public static byte[] latinStringToArray (String string, byte[] array, int offset) {
		int i;
		for (i = 0; i < string.length(); i++) {
			array[offset+i] = (byte)string.charAt(i);
		}
		array[offset+i] = (byte)0;

		return array;
	}

	//FROM ARRAY
	public static short shortFromArray (byte[] array, int offset) {
		short hd = 0;

		for (int i = 0; i < SizeConstants.sizeOfShort; i++) {
			hd |= (0xFF & array[offset+i]) << i*Byte.SIZE;
		}

		return hd;
	}

	public static int intFromArray (byte[] array, int offset) {
		int d = 0;

		for (int i = 0; i < SizeConstants.sizeOfInt; i++) {
			d |= (0xFF & array[offset+i]) << i*Byte.SIZE;
		}

		return d;
	}

	public static long longFromArray (byte[] array, int offset) {
		long ld = 0;

		for (int i = 0; i < SizeConstants.sizeOfLong; i++) {
			ld |= (0xFF & array[offset+i]) << i*Byte.SIZE;
		}

		return ld;
	}

	public static float floatFromArray (byte[] array, int offset) {
		int bits = 0;

		for (int i = 0; i < SizeConstants.sizeOfInt; i++) {
			bits |= (0xFF & array[offset+i]) << i*Byte.SIZE;
		}

		return Float.intBitsToFloat(bits);
	}

	public static double doubleFromArray (byte[] array, int offset) {
		long bits = 0;

		for (int i = 0; i < SizeConstants.sizeOfLong; i++) {
			bits |= (0xFF & array[offset+i]) << i*Byte.SIZE;
		}

		return Double.longBitsToDouble(bits);
	}

	public static boolean booleanFromArray (byte[] array, int offset) {
		return array[offset] == 1;
	}

	public static char charFromArray (byte[] array, int offset) {
		char c = 0;

		for (int i = 0; i < SizeConstants.sizeOfChar; i++) {
			c |= (0xFF & array[offset+i]) << i*Byte.SIZE;
		}

		return c;
	}

	public static char latinCharFromArray (byte[] array, int offset) {
		return (char)(0xFF & array[offset]);
	}

	public static String stringFromArray (byte[] array, int offset) {
		int byteI, j;
		char read;
		StringBuilder builder = new StringBuilder();

		byteI = offset;
		read = 0;
		for (j = 0; j < SizeConstants.sizeOfChar && byteI < array.length; j++) {
			read |= (0xFF & array[byteI]) << j*Byte.SIZE;
			byteI++;
		}

		while (read != 0 && byteI < array.length) {
			builder.append(read);
			read = 0;
			for (j = 0; j < SizeConstants.sizeOfChar && byteI < array.length; j++) {
				read |= (0xFF & array[byteI]) << j*Byte.SIZE;
				byteI++;
			}

		}

		return new String(builder);
	}

	public static String latinStringFromArray (byte[] array, int offset) {
		int byteI;
		char read;
		StringBuilder builder = new StringBuilder();

		byteI = offset;
		read = (char)(0xFF & array[byteI]);
		byteI++;
		while (read != 0 && byteI < array.length) {
			builder.append(read);
			read = (char)(0xFF & array[byteI]);
			byteI++;
		}

		return new String(builder);
	}

	/*ARRAY METHODS*/
	public static byte[] copyArrayTo (byte[] input, int beginning, int end, byte[] output, int outputOffset) {
		int i, limit;
		limit = end-beginning;
		for (i = 0; i < limit; i++) {
			output[i+outputOffset] = input[i+beginning];
		}
		return output;
	}

}
