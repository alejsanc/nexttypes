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

package com.nexttypes.datatypes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.system.Constants;

public class QRCode {
	protected BufferedImage qrcode;

	public QRCode(String text, int size, ErrorCorrectionLevel errorCorrectionLevel) {
		Hashtable<EncodeHintType, Object> hintMap = new Hashtable<EncodeHintType, Object>();
		hintMap.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel);
		hintMap.put(EncodeHintType.CHARACTER_SET, Constants.UTF_8_CHARSET);
		hintMap.put(EncodeHintType.MARGIN, 1);

		QRCodeWriter qrcodeWriter = new QRCodeWriter();

		BitMatrix bitMatrix = null;
		try {
			bitMatrix = qrcodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size, hintMap);
		} catch (WriterException e) {
			throw new NXException(e);
		}
		int imageSize = bitMatrix.getWidth();
		qrcode = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
		qrcode.createGraphics();

		Graphics2D graphics = (Graphics2D) qrcode.getGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, imageSize, imageSize);
		graphics.setColor(Color.BLACK);

		for (int x = 0; x < imageSize; x++) {
			for (int y = 0; y < imageSize; y++) {
				if (bitMatrix.get(x, y)) {
					graphics.fillRect(x, y, 1, 1);
				}
			}
		}
	}

	public byte[] getBytes() {
		byte[] bytes = null;
		try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
			ImageIO.write(qrcode, "png", stream);
			bytes = stream.toByteArray();
		} catch (IOException e) {
			throw new NXException(e);
		}
		return bytes;
	}

	public String getBase64() {
		return "data:image/png;base64," + Base64.encodeBase64String(getBytes());
	}
}