package info.androidhive.webmobilegroupchat;

import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;


import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
@ServerEndpoint(value="/chat",configurator=HandskakeConfigurator.class)
public class SocketServer {
	private final static Logger logger = LoggerFactory.getLogger(SocketServer.class);
	
	final static String insertSql = "insert into chatschema(FROMENAME, MESSAGE, DATE)values(?,?,?)";
	
	final static String searchSql = "select * from chatschema where date > DATE_FORMAT(CURDATE(),'%Y-%m-%d %H:%i:%s')";
	
	private SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // set to store all the live sessions
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());

    // Mapping between session and person name
    private static final HashMap<String, String> nameSessionPair = new HashMap<String, String>();

    private JSONUtils jsonUtils = new JSONUtils();

    // Getting query params
    public static Map<String, String> getQueryMap(String query) {
        Map<String, String> map = Maps.newHashMap();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] nameval = param.split("=");
                map.put(nameval[0], nameval[1]);
            }
        }
        return map;
    }

    /**
     * Called when a socket connection opened
     * */
    @OnOpen
    public void onOpen(Session session) {

        logger.info(session.getId() + " 已经打开链接!");
        
        Map<String, String> queryParams = getQueryMap(session.getQueryString());

        String name = "";

        if (queryParams.containsKey("name")) {

            // Getting client name via query param
            name = queryParams.get("name");
            try {
                name = URLDecoder.decode(name, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // Mapping client name and session id
            nameSessionPair.put(session.getId(), name);
        }

        // Adding session to session list
        sessions.add(session);

        try {
            JDBCUtil dm = new JDBCUtil();
            // Sending session id to the client that just connected
            session.getBasicRemote().sendText(jsonUtils.getClientDetailsJson(session.getId(),dm.getResultData(null, null, searchSql)));
        } catch (IOException|SQLException e) {
            e.printStackTrace();
        }

        // Notifying all the clients about new person joined
        sendMessageToAll(session.getId(), name, " 已加入聊天室", true,
                false);

    }

    /**
     * method called when new message received from any client
     * 
     * @param message
     *            JSON message from client
     * */
    @OnMessage
    public void onMessage(String message, Session session) {

        logger.info("Message from " + session.getId() + ": " + message);

        String msg = null;
  
        // Parsing the json and getting message
        try {
            JSONObject jObj = new JSONObject(message);
            msg = jObj.getString("message");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Sending the message to all clients
        sendMessageToAll(session.getId(), nameSessionPair.get(session.getId()),
                msg, false, false);
    }
        @OnError
        public void onError(Session session, Throwable error){
        	if(error instanceof EOFException)
        		logger.info("EOFException");
        	else
        		logger.error("服务器发生异常："+ error.toString());
        	
        }
    /**
     * Method called when a connection is closed
     * */
    @OnClose
    public void onClose(Session session) {

        logger.info("Session " + session.getId() + " has ended");

        // Getting the client name that exited
        String name = nameSessionPair.get(session.getId());

        // removing the session from sessions list
        sessions.remove(session);

        // Notifying all the clients about person exit
        sendMessageToAll(session.getId(), name, " 已经离开聊天室", false,
                true);

    }

    /**
     * Method to send message to all clients
     * 
     * @param sessionId
     * @param message
     *            message to be sent to clients
     * @param isNewClient
     *            flag to identify that message is about new person joined
     * @param isExit
     *            flag to identify that a person left the conversation
     * */
    private void sendMessageToAll(String sessionId, String name,
            String message, boolean isNewClient, boolean isExit) {
    	JDBCUtil util = new JDBCUtil();String date = f.format(new Date());
        // Looping through all the sessions and sending the message individually
    	if(!isNewClient&&!isExit&&!" ".equals(message)) {
    	    String[] coulmn = new String[]{name,  message, date};
            int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP};
            
            try {
                boolean flag = util.updateOrAdd(coulmn, type, insertSql);
                if(flag)
                    logger.info("插入成功");
            } catch (SQLException e) {
                e.printStackTrace();
            }
    	}
        for (Session s : sessions) {
            String json = null;

            // Checking if the message is about new client joined
            if (isNewClient) {
            	json = jsonUtils.getNewClientJson(sessionId, name, message,sessions.size());
            	
            } else if (isExit) {
                // Checking if the person left the conversation
                json = jsonUtils.getClientExitJson(sessionId, name, message,
                        sessions.size());
            } else {
                // Normal chat conversation message
            	
                json = jsonUtils
                        .getSendAllMessageJson(sessionId, name, message,date);
            }

            try {
                logger.info("Sending Message To: " + sessionId + ", "
                        + json);

                s.getBasicRemote().sendText(json);
            } catch (IOException e) {
                logger.info("error in sending. " + s.getId() + ", "
                        + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
