package org.example.keycloak.service;

import dev.openfga.sdk.api.model.AuthorizationModel;
import dev.openfga.sdk.api.model.RelationReference;
import dev.openfga.sdk.api.model.TypeDefinition;
import dev.openfga.sdk.api.model.Userset;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class OpenFgaModel {

    private static final Logger LOG = Logger.getLogger(OpenFgaModel.class);
    private AuthorizationModel model;
    private final Map<String, String> modelTypeObjectAndRelation;

    public OpenFgaModel() {
        this.modelTypeObjectAndRelation = new HashMap<>();
    }

    public void loadModel(AuthorizationModel model) {
        this.model = model;
        this.loadModelAsTypeObjectRelationshipMap();
    }

    public Boolean isTypeDefinitionInModel(String eventObjectType) {
        return this.model.getTypeDefinitions().stream()
                .anyMatch(r -> r.getType().equalsIgnoreCase(eventObjectType));
    }

    public Boolean isRelationAvailableInModel(String typeDefinition, String objectType) {
        return this.modelTypeObjectAndRelation.containsKey(typeDefinition + objectType);
    }

    public String getRelationFromModel(String typeDefinition, String objectType){
        return this.modelTypeObjectAndRelation.get(typeDefinition + objectType); // Easy way to return the relation
    }


    private void loadModelAsTypeObjectRelationshipMap(){
        LOG.debugf("Loading internal model");
        for (TypeDefinition typeDef : this.model.getTypeDefinitions()) {
            for (Map.Entry<String, Userset> us : Objects.requireNonNull(typeDef.getRelations()).entrySet()) {
                if (typeDef.getMetadata() != null
                        && !Objects.requireNonNull(typeDef.getMetadata().getRelations()).isEmpty()
                        && typeDef.getMetadata().getRelations().containsKey(us.getKey())) {
                    for(RelationReference metadata: Objects.requireNonNull(typeDef.getMetadata().getRelations().get(us.getKey()).getDirectlyRelatedUserTypes())) {
                        this.modelTypeObjectAndRelation.put(typeDef.getType() + metadata.getType(), us.getKey());
                    }
                }
            }
        }
        LOG.debugf("Internal model as Map(TypeObject:Relation): %s",  this.modelTypeObjectAndRelation);
    }

}
