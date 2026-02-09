/*
 * Copyright 2015-2026 Alejandro Sánchez <alex@nexttypes.com>
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

package com.nexttypes.antivirus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import com.nexttypes.exceptions.NXException;
import com.nexttypes.settings.Settings;
import com.nexttypes.system.Context;
import com.nexttypes.system.KeyWords;

public class ClamAV extends Antivirus {
	
	protected static final String OK = "stream: OK";
	protected static final byte[] INSTREAM = "zINSTREAM\0".getBytes();
	protected static final byte[] END = new byte[]{0,0,0,0};
	
	protected Settings settings;
	protected String host;
	protected int port;
	
	public ClamAV(Context context) {
		settings = context.getSettings(Settings.CLAMAV_SETTINGS);
		host = settings.getString(KeyWords.HOST);
		port = settings.getInt32(KeyWords.PORT);
	}
	
	@Override
	public String scan(byte[] data) {
		
		try (
				Socket socket = new Socket(host, port);
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				OutputStream output = socket.getOutputStream();
		) {
			byte[] size = ByteBuffer.allocate(4).putInt(data.length).array();
			
			output.write(INSTREAM);
			output.write(size);
			output.write(data);
			output.write(END);
			output.flush();
			
			String result = input.readLine().trim();
										        
		    if (OK.equals(result)) {
		    	
		    	result = null;
		    	
		    } else {
		    	
		    	result = result.substring(8, result.length()-6);
		  
		    } 
		    
		    return result;
		    
		} catch (IOException e) {
			throw new NXException(e);
		} 
	}
}