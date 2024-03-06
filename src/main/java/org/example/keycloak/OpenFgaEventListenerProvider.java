package org.example.keycloak;

import org.example.keycloak.event.AdminEventToFGAWriteTranslator;
import org.example.keycloak.service.OpenFgaClientHandler;
import org.example.keycloak.support.KeycloakAdapterDefault;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;

public class OpenFgaEventListenerProvider implements EventListenerProvider {
	private static final Logger LOG = Logger.getLogger(OpenFgaEventListenerProvider.class);
	private OpenFgaClientHandler client;
	private AdminEventToFGAWriteTranslator translator;

	public OpenFgaEventListenerProvider(OpenFgaClientHandler client, KeycloakSession session) {
		this.client = client;
		this.translator = new AdminEventToFGAWriteTranslator(new KeycloakAdapterDefault(session), client.getFgaClient());
	}

	@Override
	public void onEvent(Event event) {
		LOG.debug("Discarding onEvent() type: " + event.getType().toString());
	}

	@Override
	public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
		LOG.debugf("Admin event received onEvent(): %s ", adminEvent.toString());

		try {
			translator.translate(adminEvent)
					.ifPresent(request -> client.publish(adminEvent.getId(), request));
		} catch (IllegalArgumentException e) {
			LOG.warn(e.getMessage());
		}
	}

	@Override
	public void close() {
		// ignore
	}
}
