package com.jaewanyun.omoc.net;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

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
 * A client with bidirectional communication capability with or without data compression
 *
 * @author Jaewan Yun <jay50@pitt.edu>
 */
public class Client implements Runnable {

	private Socket socket;
	private String serverName;
	private int port;
	private int id;
	private Thread connectionListener;
	private boolean usingCompression;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;

	/*
	 * Prevent default constructor call
	 */
	@SuppressWarnings("unused")
	private Client() {throw new UnsupportedOperationException();}

	public Client(String serverName, int port) {
		this(serverName, port, false);
	}

	/**
	 * A client constructor with an option to use compression
	 *
	 * @param serverName Name of the server to connect to
	 * @param port Port to request connection from
	 * @param usingCompression Request to compress data. Both server and client need the same value
	 */
	public Client(String serverName, int port, boolean usingCompression) {
		this.serverName = serverName;
		this.port = port;
		this.usingCompression = usingCompression;
		this.id = -1;

		start();
	}

	/**
	 * Check if the client is still connected to the server
	 *
	 * @return True if at least one stream is open
	 */
	public boolean isConnected() {
		if((outputStream != null || inputStream != null) && id != -1) {
			return true;
		}
		return false;
	}

	/**
	 * Close a connection
	 *
	 * @param id Identification number of a connection to close
	 */
	public void close() {
		// Close ObjectOutputStream
		if(outputStream != null) {
			try {
				outputStream.flush();
			} catch (IOException ioe) {
				// TODO: Handle
				System.out.println("Possible loss of data while closing");
			}
			try {
				outputStream.close();
			} catch (IOException ioe2) {
				// TODO: Handle
				System.out.println("Error closing output stream");
			}
		}

		// Close ObjectInputStream
		if(inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException ioe) {
				// TODO: Handle
				System.out.println("Error closing input stream");
			}
		}

		// Close Socket
		if(socket != null) {
			try {
				socket.close();
			} catch (IOException ioe) {
				// TODO: Handle
				System.out.println("Error closing socket");
			}
		}

		System.out.println("Closed connection: " + id);
	}

	/**
	 * Get the identification number of this connection
	 *
	 * @return An identification number of this client assigned by the server
	 */
	public int id() {
		return id;
	}

	/**
	 * Retrieves all identifications that are connected to the server
	 *
	 * @return All identification numbers of connected clients
	 */
	@SuppressWarnings("unchecked")
	public Integer[] serverIds() {
		JayList<String> jayList = new JayList<>();
		jayList.addLast("getid");
		try {
			outputStream.writeObject(jayList);
			outputStream.flush();
		} catch (IOException ioe) {
			// TODO: Handle
			System.out.println("Could not write request");
		}
		try {
			Object temp = inputStream.readObject();
			JayList<Integer> list = (JayList<Integer>) temp;
			return list.toArray(new Integer[list.size()]);

		} catch (IOException ioe) {
			// TODO: Handle
			System.out.println("Could not read request 1");
		} catch (ClassNotFoundException cnfe) {
			// TODO: Handle
			System.out.println("Could not read request 2");
		}
		return null;
	}

	/**
	 * Severs the connection between the server and the identifications passed into this method
	 *
	 * @param idsToQuit The identification numbers to quit
	 */
	public void serverKick(int[] idsToQuit) {
		JayList<String> jayList = new JayList<>();
		jayList.addLast("kick");

		for(int j = 0; j < idsToQuit.length; j++) {
			jayList.addLast(Integer.toString(idsToQuit[j]));
		}

		try {
			outputStream.writeObject(jayList);
			outputStream.flush();
		} catch (IOException ioe) {
			// TODO: Handle
			System.out.println("Could not write request: kick");
		}
	}

	/**
	 * All commands without a parameter or a return type are send through this method
	 * Supported commands: pause
	 *
	 * @param command The command to send to the server
	 */
	public void serverCommand(String command) {
		JayList<String> jayList = new JayList<>();

		if(!(command.equals("pause")))
			throw new IllegalArgumentException();

		jayList.addLast(command);
		try {
			outputStream.writeObject(jayList);
			outputStream.flush();
			System.out.println(command);
		} catch (IOException ioe) {
			// TODO: Handle
			System.out.println("Could not write request: " + command);
		}
	}

	public void tether(JayList<Object> outQueue, JayList<Object> inQueue) {
		new Thread(() -> {
			try {
				while(true) {
					while(!outQueue.isEmpty()) {
						outputStream.writeObject(outQueue.removeFirst());
						outputStream.flush();
						outputStream.reset();
					}
					Thread.sleep(10);
				}
			} catch (IOException ioe) {
				// TODO: Handle
				System.out.println("Writing output from tether failed");
			} catch (InterruptedException ie) {
				// Do nothing
			} finally {
				close();
			}
		}).start();

		new Thread(() -> {
			try {
				while(true) {
					inQueue.addLast(inputStream.readObject());
					Thread.sleep(10);
				}
			} catch (IOException ioe) {
				// TODO: Handle
				System.out.println("Reading input to tether failed");
			} catch (ClassNotFoundException cnfe) {
				// TODO: Handle
				System.out.println("Reading input failed");
			} catch (InterruptedException ie) {
				// Do nothing
			} finally {
				close();
			}
		}).start();
	}

	@Override
	public void run() {
		// Create socket
		makeSocket();

		// Create output stream
		outputStream = usingCompression ? StreamUtil.createOutputZipStream(socket) : StreamUtil.createOutputStream(socket);

		// Flush output
		try {
			outputStream.flush();
		} catch (IOException ioe) {
			// TODO: Handle
			System.out.println("Error with flushing: " + id);
		}

		// Create input stream
		inputStream = usingCompression ? StreamUtil.createInputZipStream(socket) : StreamUtil.createInputStream(socket);

		// Read ID
		try {
			Object readFirst = inputStream.readObject();
			if(readFirst instanceof Integer)
				id = (int) readFirst;
			else
				throw new ClassNotFoundException();
		} catch (ClassNotFoundException cnfe) {
			// TODO: Handle
			System.out.println("Unable to read from: " + id);
		} catch (IOException ioe) {
			// TODO: Handle
			System.out.println("Error with client: " + id);
		}
	}

	/**
	 * Creates a socket requesting for a connection to the specified port
	 */
	private void start() {
		if(connectionListener == null) {
			connectionListener = new Thread(this);
			connectionListener.start();
		}
	}

	/*
	 * Creates a socket from name of server and the port provided
	 */
	private void makeSocket() {
		try {
			socket = new Socket(serverName, port);
		} catch (UnknownHostException uhe) {
			// TODO: Handle
			System.out.println("Host unknown: " + uhe.getMessage());
			System.exit(-1);
		} catch (IOException ioe) {
			// TODO: Handle
			System.out.println("Could not listen on: " + serverName + " : " + port);
			System.exit(-1);
		}
	}
}
