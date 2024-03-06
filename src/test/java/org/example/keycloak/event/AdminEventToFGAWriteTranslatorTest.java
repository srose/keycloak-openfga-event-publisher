package org.example.keycloak.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.openfga.sdk.api.client.ApiResponse;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientReadResponse;
import dev.openfga.sdk.api.model.ReadResponse;
import dev.openfga.sdk.api.model.Tuple;
import dev.openfga.sdk.api.model.TupleKey;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import org.example.keycloak.support.KeycloakAdapter;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.api.client.model.ClientTupleKeyWithoutCondition;
import dev.openfga.sdk.api.client.model.ClientWriteRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.keycloak.events.admin.AdminEvent;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class AdminEventToFGAWriteTranslatorTest {

    private static Stream<Arguments> provideJsonFilesForTest() {
        return Stream.of(
                Arguments.of(
                        "src/test/resources/user/delete_user__remove_an_existing_user.json",
                        "src/test/resources/user/delete_user__remove_an_existing_user__tuples.json")
                // Add more files if needed
        );
    }

    @Test
    void testTranslate_createUserWithRolesAndGroups() throws IOException {

        // Arrange
        ObjectMapper objectMapper = initializeObjectMapper();
        AdminEvent adminEvent = readAdminEvent("src/test/resources/user/create_user_with_roles_and_groups.json", objectMapper);

        KeycloakAdapter keycloakAdapter = mock(KeycloakAdapter.class);
        OpenFgaClient openFgaClient = mock(OpenFgaClient.class);
        AdminEventToFGAWriteTranslator translator = new AdminEventToFGAWriteTranslator(keycloakAdapter, openFgaClient);

        //Act
        Optional<ClientWriteRequest> clientWriteRequest = translator.translate(adminEvent);

        //Assert
        ClientWriteRequest expectedClientWriteRequest = provideClientWriteRequest("src/test/resources/user/create_user_with_roles_and_groups__tuples.json", objectMapper);

        Assertions.assertTrue(clientWriteRequest.isPresent());
        assertTrue(new ClientWriteRequestComparator().areClientWriteRequestsEqual(expectedClientWriteRequest, clientWriteRequest.get()));
    }

    @Test
    void testTranslate_deleteUserWithRolesAndGroups() throws IOException, FgaInvalidParameterException {
        ObjectMapper objectMapper = initializeObjectMapper();

        // Arrange
        AdminEvent adminEvent = readAdminEvent("src/test/resources/user/delete_user__remove_an_existing_user.json", objectMapper);

        KeycloakAdapter keycloakAdapter = mock(KeycloakAdapter.class);
        OpenFgaClient openFgaClient = mock(OpenFgaClient.class);
        when(openFgaClient.read(any())).thenReturn(
                CompletableFuture.completedFuture(new ClientReadResponse(
                        getApiResponse(List.of(
                                new Tuple().key(new TupleKey().user("user:3228d3f0-f1d1-4022-a043-3d0e1ffa2002")._object("role:acme-user").relation("assignee")))))),
                CompletableFuture.completedFuture(new ClientReadResponse(
                        getApiResponse(List.of(
                                new Tuple().key(new TupleKey().user("user:3228d3f0-f1d1-4022-a043-3d0e1ffa2002")._object("group:acme-user").relation("member")),
                                new Tuple().key(new TupleKey().user("user:3228d3f0-f1d1-4022-a043-3d0e1ffa2002")._object("group:acme-subregion1").relation("member")))))));
        AdminEventToFGAWriteTranslator translator = new AdminEventToFGAWriteTranslator(keycloakAdapter, openFgaClient);

        // Act
        Optional<ClientWriteRequest> clientWriteRequest = translator.translate(adminEvent);

        // Assert
        ClientWriteRequest expectedClientWriteRequest = provideClientWriteRequest("src/test/resources/user/delete_user__remove_an_existing_user__tuples.json", objectMapper);

        Assertions.assertTrue(clientWriteRequest.isPresent());
        assertTrue(new ClientWriteRequestComparator().areClientWriteRequestsEqual(expectedClientWriteRequest, clientWriteRequest.get()));
    }

    private ApiResponse<ReadResponse> getApiResponse(List<Tuple> tuples) {
        return new ApiResponse<>(200, Collections.emptyMap(), "", new ReadResponse().tuples(tuples));
    }

    @Test
    void testTranslate_assignUserToRole() throws IOException {
        ObjectMapper objectMapper = initializeObjectMapper();

        // Arrange
        AdminEvent adminEvent = readAdminEvent("src/test/resources/role_mapping/create_realm_role_mapping__assign_role_to_user.json", objectMapper);

        KeycloakAdapter keycloakAdapter = mock(KeycloakAdapter.class);
        OpenFgaClient openFgaClient = mock(OpenFgaClient.class);
        AdminEventToFGAWriteTranslator translator = new AdminEventToFGAWriteTranslator(keycloakAdapter, openFgaClient);

        // Act
        Optional<ClientWriteRequest> clientWriteRequest = translator.translate(adminEvent);

        // Assert

        Assertions.assertTrue(clientWriteRequest.isPresent());

    }

    @Test
    void testTranslate_unassignUserFromRole() throws IOException {
        ObjectMapper objectMapper = initializeObjectMapper();

        // Arrange
        AdminEvent adminEvent = readAdminEvent("src/test/resources/role_mapping/delete_realm_role_mapping__remove_role_from_user.json", objectMapper);

        KeycloakAdapter keycloakAdapter = mock(KeycloakAdapter.class);
        OpenFgaClient openFgaClient = mock(OpenFgaClient.class);
        AdminEventToFGAWriteTranslator translator = new AdminEventToFGAWriteTranslator(keycloakAdapter, openFgaClient);

        // Act
        Optional<ClientWriteRequest> clientWriteRequest = translator.translate(adminEvent);

        // Assert

        Assertions.assertTrue(clientWriteRequest.isPresent());

    }

    @Test
    void testTranslate_createGroupMembershipPutUserInGroup() throws IOException {
        ObjectMapper objectMapper = initializeObjectMapper();

        // Arrange
        AdminEvent adminEvent = readAdminEvent("src/test/resources/group_membership/create_group_membership__put_user_in_group.json", objectMapper);

        KeycloakAdapter keycloakAdapter = mock(KeycloakAdapter.class);
        OpenFgaClient openFgaClient = mock(OpenFgaClient.class);
        AdminEventToFGAWriteTranslator translator = new AdminEventToFGAWriteTranslator(keycloakAdapter, openFgaClient);

        // Act
        Optional<ClientWriteRequest> clientWriteRequest = translator.translate(adminEvent);

        // Assert
        Assertions.assertTrue(clientWriteRequest.isPresent());
    }

    @Test
    void testTranslate_deleteGroupMembershipRemoveUserFromGroup() throws IOException {
        ObjectMapper objectMapper = initializeObjectMapper();

        // Arrange
        AdminEvent adminEvent = readAdminEvent("src/test/resources/group_membership/delete_group_membership__remove_user_from_group.json", objectMapper);

        KeycloakAdapter keycloakAdapter = mock(KeycloakAdapter.class);
        OpenFgaClient openFgaClient = mock(OpenFgaClient.class);
        AdminEventToFGAWriteTranslator translator = new AdminEventToFGAWriteTranslator(keycloakAdapter, openFgaClient);

        // Act
        Optional<ClientWriteRequest> clientWriteRequest = translator.translate(adminEvent);

        // Assert
        Assertions.assertTrue(clientWriteRequest.isPresent());
    }

    @Test
    void testTranslate_createGroupNewGroupInOtherGroupAkaSubgroup() throws IOException {
        ObjectMapper objectMapper = initializeObjectMapper();

        // Arrange
        AdminEvent adminEvent = readAdminEvent("src/test/resources/group/create_group__new_group_in_other_group_aka_subgroup.json", objectMapper);

        KeycloakAdapter keycloakAdapter = mock(KeycloakAdapter.class);
        when(keycloakAdapter.findGroupNameInRealm(anyString(), anyString())).thenReturn("acme-region1");
        OpenFgaClient openFgaClient = mock(OpenFgaClient.class);
        AdminEventToFGAWriteTranslator translator = new AdminEventToFGAWriteTranslator(keycloakAdapter, openFgaClient);

        // Act
        Optional<ClientWriteRequest> clientWriteRequest = translator.translate(adminEvent);

        // Assert
        Assertions.assertTrue(clientWriteRequest.isPresent());
    }

    @Test
    void testTranslate_createRealmRoleMappingAssignRoleToGroup() throws IOException {
        ObjectMapper objectMapper = initializeObjectMapper();

        // Arrange
        AdminEvent adminEvent = readAdminEvent("src/test/resources/role_mapping/create_realm_role_mapping__assign_realm_role_to_group.json", objectMapper);

        KeycloakAdapter keycloakAdapter = mock(KeycloakAdapter.class);
        OpenFgaClient openFgaClient = mock(OpenFgaClient.class);
        when(keycloakAdapter.findGroupNameInRealm(anyString(), anyString())).thenReturn("acme-group1");
        AdminEventToFGAWriteTranslator translator = new AdminEventToFGAWriteTranslator(keycloakAdapter, openFgaClient);

        // Act
        Optional<ClientWriteRequest> clientWriteRequest = translator.translate(adminEvent);

        // Assert
        Assertions.assertTrue(clientWriteRequest.isPresent());
    }

    @Test
    void testTranslate_deleteRealmRoleMappingRemoveRoleFromGroup() throws IOException {
        ObjectMapper objectMapper = initializeObjectMapper();

        // Arrange
        AdminEvent adminEvent = readAdminEvent("src/test/resources/role_mapping/delete_realm_role_mapping__remove_realm_role_from_group.json", objectMapper);

        KeycloakAdapter keycloakAdapter = mock(KeycloakAdapter.class);
        when(keycloakAdapter.findGroupNameInRealm(anyString(), anyString())).thenReturn("acme-group1");
        OpenFgaClient openFgaClient = mock(OpenFgaClient.class);
        AdminEventToFGAWriteTranslator translator = new AdminEventToFGAWriteTranslator(keycloakAdapter, openFgaClient);

        // Act
        Optional<ClientWriteRequest> clientWriteRequest = translator.translate(adminEvent);

        // Assert
        Assertions.assertTrue(clientWriteRequest.isPresent());
    }

    @Test
    void testTranslate_createRealmRoleCollectionAsCompositeForOtherRole() throws IOException {
        ObjectMapper objectMapper = initializeObjectMapper();

        // Arrange
        AdminEvent adminEvent = readAdminEvent("src/test/resources/role/create_realm_role__new_role_collection_as_composite_for_other_role.json", objectMapper);

        KeycloakAdapter keycloakAdapter = mock(KeycloakAdapter.class);
        when(keycloakAdapter.findGroupNameInRealm(anyString(), anyString())).thenReturn("acme-group1");
        OpenFgaClient openFgaClient = mock(OpenFgaClient.class);
        AdminEventToFGAWriteTranslator translator = new AdminEventToFGAWriteTranslator(keycloakAdapter, openFgaClient);

        // Act
        Optional<ClientWriteRequest> clientWriteRequest = translator.translate(adminEvent);

        // Assert
        Assertions.assertTrue(clientWriteRequest.isPresent());
    }

    @Test
    void testTranslate_createRealmRoleAdd1ToNRolesAsCompositeForOtherRole() throws IOException {
        ObjectMapper objectMapper = initializeObjectMapper();

        // Arrange
        AdminEvent adminEvent = readAdminEvent("src/test/resources/role/create_realm_role__single_role_add_1_to_n_roles_as_composite.json", objectMapper);

        KeycloakAdapter keycloakAdapter = mock(KeycloakAdapter.class);
        when(keycloakAdapter.findRoleNameInRealm(anyString(), anyString())).thenReturn("acme-user");
        OpenFgaClient openFgaClient = mock(OpenFgaClient.class);
        AdminEventToFGAWriteTranslator translator = new AdminEventToFGAWriteTranslator(keycloakAdapter, openFgaClient);

        // Act
        Optional<ClientWriteRequest> clientWriteRequest = translator.translate(adminEvent);

        // Assert
        Assertions.assertTrue(clientWriteRequest.isPresent());
    }

    @Test
    void testTranslate_deleteRealmRoleSingleRoleRemove1ToNRolesFromComposite() throws IOException {
        ObjectMapper objectMapper = initializeObjectMapper();

        // Arrange
        AdminEvent adminEvent = readAdminEvent("src/test/resources/role/delete_realm_role__single_role_remove_1_to_n_composite_roles.json", objectMapper);

        KeycloakAdapter keycloakAdapter = mock(KeycloakAdapter.class);
        when(keycloakAdapter.findRoleNameInRealm(anyString(), anyString())).thenReturn("acme-user");
        OpenFgaClient openFgaClient = mock(OpenFgaClient.class);



        AdminEventToFGAWriteTranslator translator = new AdminEventToFGAWriteTranslator(keycloakAdapter, openFgaClient);

        // Act
        Optional<ClientWriteRequest> clientWriteRequest = translator.translate(adminEvent);

        // Assert
        Assertions.assertTrue(clientWriteRequest.isPresent());
    }

    @Test
    void testTranslate_deleteRealmRoleCollectionAsCompositeForOtherRole() throws IOException, FgaInvalidParameterException {
        ObjectMapper objectMapper = initializeObjectMapper();

        // Arrange
        AdminEvent adminEvent = readAdminEvent("src/test/resources/role/delete_realm_role__remove_composite_collection.json", objectMapper);

        KeycloakAdapter keycloakAdapter = mock(KeycloakAdapter.class);
        OpenFgaClient openFgaClient = mock(OpenFgaClient.class);
        when(openFgaClient.read(any())).thenReturn(
                CompletableFuture.completedFuture(new ClientReadResponse(
                        getApiResponse(List.of(
                                new Tuple().key(new TupleKey().user("role:acme-user").relation("composite")._object("role:acme-admin")))))));
        AdminEventToFGAWriteTranslator translator = new AdminEventToFGAWriteTranslator(keycloakAdapter, openFgaClient);

        // Act
        Optional<ClientWriteRequest> clientWriteRequest = translator.translate(adminEvent);

        // Assert
        Assertions.assertTrue(clientWriteRequest.isPresent());
    }

    @Test
    void testTranslate_deleteRealmRoleRemoveRealmRoleAtRootLevel() throws IOException, FgaInvalidParameterException {
        ObjectMapper objectMapper = initializeObjectMapper();

        // Arrange
        AdminEvent adminEvent = readAdminEvent("src/test/resources/role/delete_realm_role__remove_role_at_root_level.json", objectMapper);

        KeycloakAdapter keycloakAdapter = mock(KeycloakAdapter.class);
        when(keycloakAdapter.getRoleNameFromCache(anyString(), anyString())).thenReturn("acme-user");

        OpenFgaClient openFgaClient = mock(OpenFgaClient.class);
        when(openFgaClient.read(any())).thenReturn(
                CompletableFuture.completedFuture(new ClientReadResponse(
                        getApiResponse(List.of(
                                new Tuple().key(new TupleKey().user("user:user1").relation("composite")._object("role:acme-user")))))),
                CompletableFuture.completedFuture(new ClientReadResponse(
                        getApiResponse(List.of(
                                new Tuple().key(new TupleKey().user("group:acme-user").relation("parent_group")._object("role:acme-user")))))),
                CompletableFuture.completedFuture(new ClientReadResponse(
                        getApiResponse(List.of(
                                new Tuple().key(new TupleKey().user("role:acme-user").relation("composite")._object("role:acme-admin")))))),
                CompletableFuture.completedFuture(new ClientReadResponse(
                        getApiResponse(List.of(
                                new Tuple().key(new TupleKey().user("role:acme-tester").relation("composite")._object("role:acme-user"))))))
                );
        AdminEventToFGAWriteTranslator translator = new AdminEventToFGAWriteTranslator(keycloakAdapter, openFgaClient);

        // Act
        Optional<ClientWriteRequest> clientWriteRequest = translator.translate(adminEvent);

        // Assert
        Assertions.assertTrue(clientWriteRequest.isPresent());
    }

    @Test
    void testTranslate_deleteGroupCanBeAnywhere() throws IOException, FgaInvalidParameterException {
        ObjectMapper objectMapper = initializeObjectMapper();

        // Arrange
        AdminEvent adminEvent = readAdminEvent("src/test/resources/group/delete_group__can_be_anywhere.json", objectMapper);

        KeycloakAdapter keycloakAdapter = mock(KeycloakAdapter.class);

        when(keycloakAdapter.getGroupNameFromCache(anyString(), anyString())).thenReturn("acme-user");

        OpenFgaClient openFgaClient = mock(OpenFgaClient.class);
        when(openFgaClient.read(any())).thenReturn(
                CompletableFuture.completedFuture(new ClientReadResponse(
                        getApiResponse(List.of(
                                new Tuple().key(new TupleKey().user("user:user1").relation("member")._object("group:acme-user")))))),
                CompletableFuture.completedFuture(new ClientReadResponse(
                        getApiResponse(List.of(
                                new Tuple().key(new TupleKey().user("group:acme-user").relation("parent_group")._object("role:acme-user")))))),
                CompletableFuture.completedFuture(new ClientReadResponse(
                        getApiResponse(List.of(
                                new Tuple().key(new TupleKey().user("role:acme-user").relation("composite")._object("role:acme-admin")))))),
                CompletableFuture.completedFuture(new ClientReadResponse(
                        getApiResponse(List.of(
                                new Tuple().key(new TupleKey().user("role:acme-tester").relation("composite")._object("role:acme-user"))))))
                );
        AdminEventToFGAWriteTranslator translator = new AdminEventToFGAWriteTranslator(keycloakAdapter, openFgaClient);

        // Act
        Optional<ClientWriteRequest> clientWriteRequest = translator.translate(adminEvent);

        // Assert
        Assertions.assertTrue(clientWriteRequest.isPresent());
    }

    private static ObjectMapper initializeObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ClientTupleKey.class, new ClientTupleKeyDeserializer());
        module.addDeserializer(ClientTupleKeyWithoutCondition.class, new ClientTupleKeyWithoutConditionDeserializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }

    private static ClientWriteRequest provideClientWriteRequest(String jsonFilePath, ObjectMapper objectMapper) throws IOException {
        String tuplesJsonFilePath = jsonFilePath;
        String tuplesJsonContent = new String(Files.readAllBytes(Paths.get(tuplesJsonFilePath)));
        ClientWriteRequest expectedClientWriteRequest = objectMapper.readValue(tuplesJsonContent, ClientWriteRequest.class);
        return expectedClientWriteRequest;
    }

    private static AdminEvent readAdminEvent(String jsonFilePath, ObjectMapper objectMapper) throws IOException {
        String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
        return objectMapper.readValue(jsonContent, AdminEvent.class);
    }

}