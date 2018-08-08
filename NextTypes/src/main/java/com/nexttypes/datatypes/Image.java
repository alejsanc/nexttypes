/*
 * Copyright 2015-2018 Alejandro Sánchez <alex@nexttypes.com>
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

import com.nexttypes.exceptions.NXException;
import com.nexttypes.system.Constants;
import com.nexttypes.system.Utils;

@JsonPropertyOrder({ Constants.CONTENT, Constants.THUMBNAIL, Constants.CONTENT_TYPE })
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

	@JsonCreator
	public Image(@JsonProperty(Constants.CONTENT) byte[] content, @JsonProperty(Constants.THUMBNAIL) byte[] thumbnail,
			@JsonProperty(Constants.CONTENT_TYPE) String contentType) {
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
	
	@JsonProperty(Constants.THUMBNAIL)
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
		return "(" + Utils.hexEncode(content) + "," + Utils.hexEncode(thumbnail) + "," + contentType + ")";
	}

	@Override
	public void setValue(String value) {
		if (value != null && value.length() > 0) {
			int token1 = value.indexOf(',');
			int token2 = value.lastIndexOf(',');

			content = Utils.hexDecode(value.substring(1, token1));
			thumbnail = Utils.hexDecode(value.substring(token1 + 1, token2));
			contentType = value.substring(token2 + 1, value.length() - 1);
		}
	}
}