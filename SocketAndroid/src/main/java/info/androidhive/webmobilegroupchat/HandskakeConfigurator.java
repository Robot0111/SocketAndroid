package info.androidhive.webmobilegroupchat;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

public class HandskakeConfigurator extends Configurator {
	@Override
	 public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
    }
	
	public HandskakeConfigurator() {
		super();
	}


}
