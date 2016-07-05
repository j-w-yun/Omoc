package com.jaewanyun.omoc.net;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

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
 * A server with bidirectional communication capability with or without data compression
 *
 * @author Jaewan Yun <jay50@pitt.edu>
 */
public class Server implements Runnable {

	private volatile int port;
	private volatile HashMap<Integer, ConnectionTask> connections;
	private volatile ArrayList<Integer> ids;
	private volatile boolean usingCompression;
	private volatile ServerSocket serverSocket;
	private volatile Thread connectionListener;
	private volatile static HashMap<Integer, Server> serverList; // Servers cannot share a same port

	/*
	 * Prevent default constructor call
	 */
	private Server() {throw new UnsupportedOperationException();}

	/*
	 * Private constructor to be called from the static factory
	 */
	private Server(int port, boolean usingCompression) {
		this.port = port;
		this.usingCompression = usingCompression;

		connections = new HashMap<>();
		ids = new ArrayList<>();

		serverList.put(port, this);

		createSocket();
		start();
	}

	/**
	 * A blocking static factory
	 *
	 * @param port The port number to use
	 * @return A singleton server with respect to port number that does not use compression
	 */
	public static Server getServer(int port) {
		return getServer(port, false);
	}

	/**
	 * A blocking static factory
	 *
	 * @param port The port number to use
	 * @param usingCompression
	 * @return A singleton server with respect to port number with an option to use compression
	 */
	public static synchronized Server getServer(int port, boolean usingCompression) {
		if(serverList != null) {
			return serverList.containsKey(port) ? serverList.get(port) : new Server(port, usingCompression);
		} else {
			serverList = new HashMap<>();
			return new Server(port, usingCompression);
		}
	}

	/**
	 * Pauses listening to new connections
	 */
	public synchronized void pause() {
		connectionListener = null;
	}

	/**
	 * Listens for a client to connect
	 */
	public synchronized void start() {
		if(connectionListener == null) {
			connectionListener = new Thread(this);
			// Create streams from the client in another thread upon connecting
			connectionListener.start();
		}
	}

	/**
	 * Get the identification numbers of all clients connected to the server at that instant
	 *
	 * @return An array of identification numbers
	 */
	public synchronized Integer[] getId() {
		return ids.toArray(new Integer[ids.size()]);
	}

	//	/**
	//	 * Close all connections
	//	 */
	//	synchronized void closeAll() {
	//		for(int j = 0; j < ids.size(); j++) {
	//			close(ids.get(j));
	//		}
	//	}

	/**
	 * Closes a specific connection by identification number
	 *
	 * @param id The identification number associated with a connection
	 */
	public synchronized void close(int id) {
		ConnectionTask taskToClose = connections.get(id);
		if(taskToClose != null)
			taskToClose.close();
	}

	/**
	 * Checks to see if all sockets that need to be closed are closed
	 *
	 * @return True if the integrity of the server is sound
	 */
	public synchronized boolean isStateValid() {
		return ids.size() - 1 == connections.size();
	}

	/*
	 * Creates a socket listening on the specified port
	 */
	synchronized private void createSocket() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException ioe) {
			// TODO: Handle
			System.out.println("Could not listen on port: " + port);
			System.exit(-1);
		}
	}

	/*
	 * This blocks until a connection is made
	 * Listen for a client to connect
	 */
	synchronized private Socket getSocket() {
		try {
			return serverSocket.accept();
		} catch (IOException ioe) {
			// TODO: Handle
			System.out.println("Accept failed on port: " + port);
			System.exit(-1);
		}
		return null;
	}

	/*
	 * Returns a unique ID
	 * Does not put the unique ID in the bank
	 */
	synchronized private int uniqueID() {
		// Create a unique ID number
		Random random = new Random();
		int unique = random.nextInt(Integer.MAX_VALUE);
		while(ids.contains(unique)) {
			unique = random.nextInt(Integer.MAX_VALUE);
		}
		return unique;
	}

	/**
	 * Creates streams from the client in another thread upon connecting
	 */
	@Override
	public void run() {
		while(connectionListener != null) {
			/*
			 * Upon connection, add ID and connectionTask to a HashMap
			 */
			int id = uniqueID();
			ConnectionTask connection = new ConnectionTask(getSocket(), id);
			ids.add(id); // Put the id in the bank after connecting
			connections.put(id, connection);

			// Create a bidirectional stream from the accepted connection
			new Thread(connection).start();
		}
	}

	/*
	 * Reads from the input of its dedicated stream and sends the collected input to relevant connections
	 */
	private class ConnectionTask implements Runnable {

		private volatile int id;
		private Socket socket;
		private ObjectOutputStream outputStream;
		private ObjectInputStream inputStream;

		/*
		 * Prevent default constructor call
		 */
		private ConnectionTask() {throw new UnsupportedOperationException();}

		/**
		 * The only constructor available for this class
		 *
		 * @param socket The socket which this object will listen to
		 * @param id The identification number associated with the socket
		 */
		private ConnectionTask(Socket socket, Integer id) {
			this.socket = socket;
			this.id = id;
		}

		/**
		 * Close all streams from the connection
		 *
		 * @param id Identification number of a connection to close
		 */
		private synchronized void close() {
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
					outputStream = null;
				} catch (IOException ioe2) {
					// TODO: Handle
					System.out.println("Error closing output stream");
				}
			}

			// Close ObjectInputStream
			if(inputStream != null) {
				try {
					inputStream.close();
					inputStream = null;
				} catch (IOException ioe) {
					// TODO: Handle
					System.out.println("Error closing input stream");
				}
			}

			// Close Socket
			if(socket != null) {
				try {
					socket.close();
					socket = null;
				} catch (IOException ioe) {
					// TODO: Handle
					System.out.println("Error closing socket");
				}
			}

			// Remove the references from the collection
			try {
				Server.this.connections.remove(id);
				Server.this.ids.remove(Server.this.ids.indexOf(id));
			} catch (ArrayIndexOutOfBoundsException aiobe) {
				// Do nothing
			}

			// TODO: Handle
			System.out.println("Closed connection to: " + id);
		}

		/*
		 * Return null if no messages are to be read
		 * Else return a JayList containing the requested information
		 * And/or modify this field so that whatever this particular client sends only goes to those specified
		 */
		@SuppressWarnings("rawtypes")
		private JayList parseRequest(Object rebound) {
			if(!(rebound instanceof JayList))
				return null;

			String request = "";

			JayList list = (JayList) rebound;
			if(!list.isEmpty()) {
				Object readFirst = list.getFirst();
				if(readFirst instanceof String) {
					request = (String) readFirst;
				} else {
					return null;
				}
			}

			/*
			 * Check what commands are to be run
			 */
			if(request.equals("getid")) { // Returns the list of clients connected; returns JayList<>
				System.out.println("Getting IDs for client: " + id);
				return new JayList<>(ids.toArray(new Integer[ids.size()]));

			} else if(request.equals("selected")) { // Talk to only those selected; returns null
				// Parse the rest of the Strings in the list into integers signifying IDs

			} else if(request.equals("everyone")) { // Talk to everyone; returns null
				// Set the field so objects read from this client is sent to everyone

			} else if(request.equals("kick")) { // Close id; returns null
				System.out.println("Kicking ID");
				list.removeFirst(); // Remove the message
				while(!list.isEmpty()) {
					Object readValue = list.removeFirst();
					if(readValue instanceof String) { // Should be String unless type is a String superclass
						try {
							int idToClose = Integer.parseInt((String) readValue);
							ConnectionTask connectionToClose = connections.get(idToClose);
							if(connectionToClose == null) // Could not find ID in hash map
								throw new IllegalArgumentException();
							connectionToClose.close();
							System.out.println("Kicked ID: " + idToClose);
						} catch (NumberFormatException nfe) {
							// TODO: Handle
							System.out.println("Close failed. ID could not be read: " + (String) readValue);
						} catch (IllegalArgumentException iae) {
							// TODO: Handle
							System.out.println("Close failed. ID does not exist: " + (String) readValue);
						}
					}
				}

			} else if(request.equals("pause")) {
				System.out.println("Pausing");
				pause();
				System.out.println("Paused");

			}

			// Handle message validity check from client side
			return null;
		}

		/**
		 * Creates input and output streams
		 * Runs in an infinite loop to send a received input to all other open connections
		 * Does not send to other connections if received input is a request to the server
		 */
		@Override
		public void run() {
			/*
			 * Create output stream
			 */
			outputStream = Server.this.usingCompression ? StreamUtil.createOutputZipStream(socket) :
				StreamUtil.createOutputStream(socket);

			/*
			 * Give client its ID
			 */
			try {
				outputStream.writeObject(new Integer(id));
				outputStream.flush();
			} catch (IOException ioe) {
				// TODO: Handle
				System.out.println("Unable to give client id: " + id);
			}

			/*
			 * Create input stream
			 */
			inputStream = Server.this.usingCompression ? StreamUtil.createInputZipStream(socket) :
				StreamUtil.createInputStream(socket);

			/*
			 * Read streams until the particular connection is closed off
			 * Prevent memory leak by closing off streams from severed connections
			 */
			try {
				while(true) {
					/*
					 * Parse rebound and send its information only to relevant connections
					 */
					Object rebound = inputStream.readObject();
					boolean confidential = false;

					/*
					 * Send to a parser to find out if the client is asking for anything
					 */
					@SuppressWarnings("rawtypes")
					JayList serverResponse = parseRequest(rebound);
					if(serverResponse != null) { // If non-null, then it was a request to the server
						outputStream.writeObject(serverResponse);
						outputStream.flush();
						confidential = true;
					}

					/*
					 * Send rebound to each client if the message was not for the server
					 */
					if(!confidential) {
						for(int j = 0; j < Server.this.ids.size(); j++) {
							ConnectionTask current = Server.this.connections.get(Server.this.ids.get(j));
							/*
							 * Watch for null in case the hash map or id bank gets corrupted
							 * This will cut off every connection there is in every thread
							 */
							if(current != null) {
								current.outputStream.writeObject(rebound);
								current.outputStream.flush();
							}
						}
					}
				}
			} catch (IOException ioe) {
				// TODO: Handle
				System.out.println("Error at connection: " + id);
			} catch (ClassNotFoundException cnfe) {
				// TODO: Handle
				System.out.println("Error reading from: " + id);
			} catch (NullPointerException npe) {
				// TODO: Handle
				System.out.println("Null pointer from: " + id);
			} finally {
				close();
			}
		}
	}
}
