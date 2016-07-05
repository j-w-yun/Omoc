package com.jaewanyun.omoc.net;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/*
 * The MIT License
 *
 * Copyright (c) 2016 Jaewan Yun <jay50@pitt.edu>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * A utility to create buffered streams for the server and client
 *
 * @author Jaewan Yun <jay50@pitt.edu>
 */
public final class StreamUtil {

	/*
	 * Create output stream from connection
	 */
	public static ObjectOutputStream createOutputStream(Socket socket) {
		try {
			/*
			 * InputStream constructor blocks until the corresponding OutputStream has flushed
			 * Always instantiate OutputStream first at both ends and flush
			 */
			return new ObjectOutputStream(
					new BufferedOutputStream(
							socket.getOutputStream()));
		} catch (IOException ioe) {
			// TODO: Handle
			System.out.println("Read failed");
			System.exit(-1);
		}
		return null;
	}

	/*
	 * Create input stream from connection
	 */
	public static ObjectInputStream createInputStream(Socket socket) {
		try {
			/*
			 * InputStream constructor blocks until the corresponding OutputStream has flushed
			 * Always instantiate OutputStream first at both ends and flush
			 */
			return new ObjectInputStream(
					new BufferedInputStream(
							socket.getInputStream()));
		} catch (IOException ioe) {
			// TODO: Handle
			System.out.println("Read failed");
			System.exit(-1);
		}
		return null;
	}

	/*
	 * Create compressed output stream from connection
	 */
	public static ObjectOutputStream createOutputZipStream(Socket socket) {
		try {
			/*
			 * InputStream constructor blocks until the corresponding OutputStream has flushed
			 * Always instantiate OutputStream first at both ends and flush
			 */
			return new ObjectOutputStream(
					new BufferedOutputStream(
							new CompressedBlockOutputStream(
									socket.getOutputStream(), 1024)));
		} catch (IOException ioe) {
			// TODO: Handle
			System.out.println("Read failed");
			System.exit(-1);
		}
		return null;
	}

	/*
	 * Create compressed input stream from connection
	 */
	public static ObjectInputStream createInputZipStream(Socket socket) {
		try {
			/*
			 * InputStream constructor blocks until the corresponding OutputStream has flushed
			 * Always instantiate OutputStream first at both ends and flush
			 */
			return new ObjectInputStream(
					new BufferedInputStream(
							new CompressedBlockInputStream(
									socket.getInputStream())));
		} catch (IOException ioe) {
			// TODO: Handle
			System.out.println("Read failed");
			System.exit(-1);
		}
		return null;
	}
}
