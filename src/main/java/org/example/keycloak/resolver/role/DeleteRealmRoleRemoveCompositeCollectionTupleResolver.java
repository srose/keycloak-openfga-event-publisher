package org.example.keycloak.resolver.role;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import org.example.keycloak.resolver.AbstractTupleResolver;
import org.example.keycloak.resolver.TupleResolver;
import org.example.keycloak.support.KeycloakAdapter;
import org.keycloak.events.admin.AdminEvent;

import java.util.regex.Matcher;
import java.util.stream.Stream;

public class DeleteRealmRoleRemoveCompositeCollectionTupleResolver extends AbstractTupleResolver implements TupleResolver {

    public static final String REGEX = "roles/(.*)/composites";

    private final String roleName;

    public DeleteRealmRoleRemoveCompositeCollectionTupleResolver(AdminEvent adminEvent, Matcher matcher) {
        super(adminEvent, matcher);

        this.roleName = matcher.group(1);
    }

    @Override
    public Stream<ClientTupleKey> resolve(KeycloakAdapter keycloakAdapter, OpenFgaClient openFgaClient) {
        return existing_role_composite_role_for_roleName(openFgaClient, this.roleName);
    }

}
