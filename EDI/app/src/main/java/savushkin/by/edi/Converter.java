package savushkin.by.edi;

import android.util.Base64;

import java.io.UnsupportedEncodingException;

public class Converter {
	// Sending side(Отправляющая сторона-Отправитель)Base64-позиционная система счисления с основанием 64. Используется для кодирования и декодирования строки текста типа String. Отправляем в нее данные.
	public static String getToBase64(String text) {
		String base64 = null;
		try {
			byte[] data = text.getBytes("UTF-8");
			base64 = Base64.encodeToString(data, Base64.DEFAULT);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return base64;
	}

	// Receiving side(Получающая сторона-Получатель) Получаем данные из Base64.
	public static String getFromBase64(String text) {
		String ret = null;
		try {
			byte[] data = Base64.decode(text, Base64.DEFAULT);
			ret = new String(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return ret;
	}
}
