package sf.ibu.netFileSearch.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import sf.ibu.netFileSearch.core.server.ServerHandler;

/**
 * this is socket server for receiving connection from terminal manager and
 * terminal. So in spring context there are 2 instances. one port is for
 * terminal manager and other for terminal
 * 
 * @author 01373525
 *
 */
public class MySocketServer {
	private ServerSocket server;
	private int port;
	private String name;
	private static final Logger logger = LoggerFactory.getLogger(MySocketServer.class);
	private ServerHandler oneOffConnectionHandler;

	/**
	 * Storing message from terminal or terminal manger
	 */
	public ConcurrentHashMap<String, JSONObject> msgMap = new ConcurrentHashMap<String, JSONObject>();

	public MySocketServer(int port, String name) throws Exception {
		this.name = name;
		this.port = port;
		init();
	}

	
	private void init() throws Exception {
		server = new ServerSocket(this.port);
	}

	public void startListener() {
		Thread thread = new Thread() {
			public void run() {
				while (true) {
					Socket socket = null;
					try {
						socket = server.accept();
						initClient(socket);
					} catch (Exception e) {
						logger.error("",e);
					}
				}
			}
		};
		thread.setName(port + " at " + name);
		thread.start();
	}

	private void initClient(final Socket socket) throws Exception {
		Thread thread = new Thread() {
			public void run() {
				try {
					String msgStr = Util.read(socket);
					if("".equals(msgStr)) {
						return;
					}
					if (oneOffConnectionHandler!=null) {
						oneOffConnectionHandler.handle(msgStr,socket);
					}
					System.out.println(name+":"+port+" receive: "+msgStr);
				} catch (Exception e) {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					String exceptionStack = sw.toString();
					try {
						Util.write(socket, exceptionStack);
					} catch (Exception e1) {
						logger.error("",e1);
					}
					logger.error("",e);
				}
			}
		};
		thread.setName("initClient");
		thread.start();
	}

	public ConcurrentHashMap<String, JSONObject> getMsgMap() {
		return msgMap;
	}


	public void setOneOffConnectionHandler(ServerHandler oneOffConnectionHandler) {
		this.oneOffConnectionHandler = oneOffConnectionHandler;
	}

	
}
