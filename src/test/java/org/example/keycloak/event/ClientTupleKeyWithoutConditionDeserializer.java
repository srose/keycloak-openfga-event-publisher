package org.example.keycloak.event;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.api.client.model.ClientTupleKeyWithoutCondition;

import java.io.IOException;

public class ClientTupleKeyWithoutConditionDeserializer extends JsonDeserializer<ClientTupleKeyWithoutCondition> {

    @Override
    public ClientTupleKeyWithoutCondition deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        ClientTupleKeyWithoutCondition clientTupleKey = new ClientTupleKeyWithoutCondition();
        clientTupleKey.user(node.get("user").asText());
        clientTupleKey.relation(node.get("relation").asText());
        clientTupleKey._object(node.get("_object").asText());

        return clientTupleKey;
    }

}