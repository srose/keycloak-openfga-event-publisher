package org.example.keycloak.resolver;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientReadRequest;
import dev.openfga.sdk.api.client.model.ClientReadResponse;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.api.model.Tuple;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import org.keycloak.events.admin.AdminEvent;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.stream.Stream;

public abstract class AbstractTupleResolver implements TupleResolver {
    private final AdminEvent adminEvent;
    private final Matcher matcher;

    public static final String USER_TYPE_USER = "user";
    public static final String USER_TYPE_ROLE = "role";
    public static final String USER_TYPE_GROUP = "group";

    public static final String OBJECT_TYPE_ROLE = "role";
    public static final String OBJECT_TYPE_GROUP = "group";

    private static final Map<String, String> modelTypType2Relation = Map.of(
            USER_TYPE_USER + OBJECT_TYPE_ROLE, "assignee",
            USER_TYPE_USER + OBJECT_TYPE_GROUP, "member",
            USER_TYPE_GROUP + OBJECT_TYPE_GROUP, "subgroup",
            USER_TYPE_ROLE + OBJECT_TYPE_ROLE, "composite",
            USER_TYPE_GROUP + OBJECT_TYPE_ROLE, "parent_group"
    );

    public AbstractTupleResolver(AdminEvent adminEvent, Matcher matcher) {
        this.adminEvent = adminEvent;
        this.matcher = matcher;
    }

    private static String getRelationType(String typeConcat) {
        return modelTypType2Relation.getOrDefault(typeConcat, "unknown");
    }

    protected ClientTupleKey user_assignee_role(String userId, String role) {
        return new ClientTupleKey()
            .user(USER_TYPE_USER + ":" + userId)
            .relation(getRelationType(USER_TYPE_USER + OBJECT_TYPE_ROLE))
            ._object(OBJECT_TYPE_ROLE + ":" + role);
    }

    protected Stream<ClientTupleKey> existing_user_assignee_role(OpenFgaClient openFgaClient, String userId) {
        return retrieve_tuples_with_userId(openFgaClient, USER_TYPE_USER, userId, OBJECT_TYPE_ROLE);
    }

    protected Stream<ClientTupleKey> existing_user_assignee_role_for_roleName(OpenFgaClient openFgaClient, String roleName) {
        return retrieve_tuples_with_objectId(openFgaClient, USER_TYPE_USER, OBJECT_TYPE_ROLE, roleName);
    }

    protected ClientTupleKey user_member_group(String userId, String group) {
        return new ClientTupleKey()
            .user(USER_TYPE_USER + ":" + userId)
            .relation(getRelationType(USER_TYPE_USER + OBJECT_TYPE_GROUP))
            ._object(OBJECT_TYPE_GROUP + ":" + group);
    }

    protected Stream<ClientTupleKey> existing_user_member_group_for_userId(OpenFgaClient openFgaClient, String userId) {
        return retrieve_tuples_with_userId(openFgaClient, USER_TYPE_USER, userId, OBJECT_TYPE_GROUP);
    }

    protected Stream<ClientTupleKey> existing_user_member_group_for_groupName(OpenFgaClient openFgaClient, String groupName) {
        return retrieve_tuples_with_objectId(openFgaClient, USER_TYPE_USER, OBJECT_TYPE_GROUP, groupName);
    }


    protected ClientTupleKey group_subgroup_group(String group, String subgroup) {
        return new ClientTupleKey()
            .user(USER_TYPE_GROUP + ":" + subgroup)
            .relation(getRelationType(USER_TYPE_GROUP + OBJECT_TYPE_GROUP))
            ._object(OBJECT_TYPE_GROUP + ":" + group);
    }

    protected Stream<ClientTupleKey> existing_group_subgroup_group_for_groupName(OpenFgaClient openFgaClient, String groupName) {
        return retrieve_tuples_with_objectId(openFgaClient, USER_TYPE_GROUP, OBJECT_TYPE_GROUP, groupName);
    }

    protected Stream<ClientTupleKey> existing_group_subgroup_group_for_subGroupName(OpenFgaClient openFgaClient, String subGroupName) {
        return retrieve_tuples_with_userId(openFgaClient, USER_TYPE_GROUP, subGroupName, OBJECT_TYPE_GROUP);
    }


    protected ClientTupleKey group_parent_group_role(String groupName, String roleName) {
        return new ClientTupleKey()
                .user(USER_TYPE_GROUP + ":" + groupName)
                .relation(getRelationType(USER_TYPE_GROUP + OBJECT_TYPE_ROLE))
                ._object(OBJECT_TYPE_ROLE + ":" + roleName);
    }

    protected Stream<ClientTupleKey> existing_group_parent_group_role(OpenFgaClient openFgaClient, String groupName) {
        return retrieve_tuples_with_userId(openFgaClient, USER_TYPE_GROUP, groupName, OBJECT_TYPE_ROLE);
    }

    protected Stream<ClientTupleKey> existing_group_parent_group_role_for_roleName(OpenFgaClient openFgaClient, String roleName) {
        return retrieve_tuples_with_objectId(openFgaClient, USER_TYPE_GROUP, OBJECT_TYPE_ROLE, roleName);
    }

    protected ClientTupleKey role_composite_role(String roleName, String compositeRoleName) {
        return new ClientTupleKey()
                .user(USER_TYPE_ROLE + ":" + roleName)
                .relation(getRelationType(USER_TYPE_ROLE + OBJECT_TYPE_ROLE))
                ._object(OBJECT_TYPE_ROLE + ":" + compositeRoleName);
    }

    protected Stream<ClientTupleKey> existing_role_composite_role(OpenFgaClient openFgaClient, String compositeRoleName) {
        return retrieve_tuples_with_userId(openFgaClient, USER_TYPE_ROLE, compositeRoleName, OBJECT_TYPE_ROLE);
    }

    protected Stream<ClientTupleKey> existing_role_composite_role_for_roleName(OpenFgaClient openFgaClient, String roleName) {
        return retrieve_tuples_with_objectId(openFgaClient, USER_TYPE_ROLE, OBJECT_TYPE_ROLE, roleName);
    }

    @Override
    public OperationType getOperationType() {
        switch(adminEvent.getOperationType()) {
            case DELETE:
                return OperationType.DELETE;
            case UPDATE:
            case CREATE:
                return OperationType.WRITE;
        }
        return OperationType.NONE;
    }

    protected Stream<ClientTupleKey> retrieve_tuples_with_userId(OpenFgaClient openFgaClient, String userType, String userId, String objectType) {
        try {
            return openFgaClient.read(new ClientReadRequest()
                            .user(userType + ":" + userId)
                            ._object(objectType + ":"))
                    .thenApply(ClientReadResponse::getTuples)
                    .thenApply(
                            tuples -> tuples.stream()
                                    .map(Tuple::getKey)
                                    .map(tupleKey -> new ClientTupleKey().user(tupleKey.getUser())._object(tupleKey.getObject()).relation(tupleKey.getRelation()))
                    ).get();
        } catch (InterruptedException | FgaInvalidParameterException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    protected Stream<ClientTupleKey> retrieve_tuples_with_objectId(OpenFgaClient openFgaClient, String userType, String objectType, String objectId) {
        try {
            return openFgaClient.read(new ClientReadRequest()
                            //.user(userType + ":") works but I am not sure if it is correct
                            .relation(getRelationType(userType + objectType))
                            ._object(objectType + ":" + objectId))
                    .thenApply(ClientReadResponse::getTuples)
                    .thenApply(
                            tuples -> tuples.stream()
                                    .map(Tuple::getKey)
                                    .map(tupleKey -> new ClientTupleKey().user(tupleKey.getUser())._object(tupleKey.getObject()).relation(tupleKey.getRelation()))
                    ).get();
        } catch (InterruptedException | ExecutionException | FgaInvalidParameterException e) {
            throw new RuntimeException(e);
        }
    }
}
