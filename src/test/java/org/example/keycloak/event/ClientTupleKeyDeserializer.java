package org.example.keycloak.event;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import dev.openfga.sdk.api.client.model.ClientTupleKey;

import java.io.IOException;

public class ClientTupleKeyDeserializer extends JsonDeserializer<ClientTupleKey> {

    @Override
    public ClientTupleKey deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        ClientTupleKey clientTupleKey = new ClientTupleKey();
        clientTupleKey.user(node.get("user").asText());
        clientTupleKey.relation(node.get("relation").asText());
        clientTupleKey._object(node.get("_object").asText());

        return clientTupleKey;
    }

}