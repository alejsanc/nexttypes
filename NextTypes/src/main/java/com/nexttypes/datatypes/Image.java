/*
 * Copyright 2015-2020 Alejandro Sánchez <alex@nexttypes.com>
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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.nexttypes.datatypes.JSON.JSONObject;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.system.KeyWords;

@JsonPropertyOrder({ KeyWords.CONTENT, KeyWords.THUMBNAIL, KeyWords.CONTENT_TYPE })
public class Image extends File {
	private static final long serialVersionUID = 1L;
	public static final int THUMBNAIL_WIDTH = 200;

	protected byte[] thumbnail;
	protected BufferedImage image;
	
	public Image() {
		type = PT.IMAGE;
	}
	
	public Image(File file) {
		super(file);
		
		type = PT.IMAGE;
		
		image();
		thumbnail();
	}
	
	public Image(byte[] content) {
		this(null, content);
	}

	public Image(String name, byte[] content) {
		super(name, content);
		
		type = PT.IMAGE;

		image();
		thumbnail();
	}
	
	public Image(JSONObject image) {
		this(image.getBinary(KeyWords.CONTENT), image.getBinary(KeyWords.THUMBNAIL),
				image.getString(KeyWords.CONTENT_TYPE));
	}

	@JsonCreator
	public Image(@JsonProperty(KeyWords.CONTENT) byte[] content, @JsonProperty(KeyWords.THUMBNAIL) byte[] thumbnail,
			@JsonProperty(KeyWords.CONTENT_TYPE) String contentType) {
		super(content, contentType);
		
		type = PT.IMAGE;

		this.thumbnail = thumbnail;
	}

	protected void image() {
		try {
			image = ImageIO.read(new ByteArrayInputStream(content));
		} catch (IOException e) {
			throw new NXException(e);
		}
	}
	
	public BufferedImage getImage() {
		if (image == null) {
			image();
		}
		return image;
	}
	
	protected void thumbnail() {
		int imageWidth = image.getWidth();
		float thumbnailWidth = imageWidth > THUMBNAIL_WIDTH ? THUMBNAIL_WIDTH : imageWidth;
		float thumbnailHeight = image.getHeight() / (imageWidth / thumbnailWidth);
		thumbnail = binaryResize((int) thumbnailWidth, (int) thumbnailHeight);
	}
	
	@JsonProperty(KeyWords.THUMBNAIL)
	public byte[] getThumbnail() {
		return thumbnail;
	}
	
	public Image resize(int width, int height) {
		return new Image(binaryResize(width, height), thumbnail, contentType);
	}

	protected byte[] binaryResize(int width, int height) {
		if (image == null) {
			image();
		}

		BufferedImage resizedImage = Scalr.resize(image, Scalr.Mode.FIT_EXACT, width, height);

		try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
			ImageIO.write(resizedImage, "png", stream);
			stream.flush();
			return stream.toByteArray();
		} catch (IOException e) {
			throw new NXException(e);
		}
	}

	@Override
	public String getValue() {
		return "(" + hexEncode(content) + "," + hexEncode(thumbnail) + "," + contentType + ")";
	}

	@Override
	public void setValue(String value) {
		if (value != null && value.length() > 0) {
			int token1 = value.indexOf(',');
			int token2 = value.lastIndexOf(',');

			content = hexDecode(value.substring(1, token1));
			thumbnail = hexDecode(value.substring(token1 + 1, token2));
			contentType = value.substring(token2 + 1, value.length() - 1);
		}
	}
}