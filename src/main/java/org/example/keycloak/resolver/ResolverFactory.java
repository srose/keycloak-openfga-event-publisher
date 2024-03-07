package org.example.keycloak.resolver;

import org.example.keycloak.resolver.group.CreateGroupNewGroupAtRootLevelWithOptionalSubgroupsTupleResolver;
import org.example.keycloak.resolver.group.CreateGroupNewGroupInOtherGroupAkaSubgroupTupleResolver;
import org.example.keycloak.resolver.role_mapping.CreateRealmRoleMappingAssignRealmRoleToGroupTupleResolver;
import org.example.keycloak.resolver.role.CreateRealmRoleNewRoleAtRootLevelTupleResolver;
import org.example.keycloak.resolver.role.CreateRealmRoleNewRoleCollectionAsCompositeForOtherRoleTupleResolver;
import org.example.keycloak.resolver.role.CreateRealmRoleSingleRoleAdd1ToNRolesAsCompositeTupleResolver;
import org.example.keycloak.resolver.role_mapping.DeleteRealmRoleMappingRemoveRealmRoleFromGroupTupleResolver;
import org.example.keycloak.resolver.role.DeleteRealmRoleRemoveCompositeCollectionTupleResolver;
import org.example.keycloak.resolver.role.DeleteRealmRoleSingleRoleRemove1ToNCompositeRolesTupleResolver;
import org.example.keycloak.resolver.group.DeleteGroupCanBeAnywhereTupleResolver;
import org.example.keycloak.resolver.role.DeleteRealmRoleRemoveRoleAtRootLevelTupleResolver;
import org.example.keycloak.resolver.group_membership.CreateGroupMembershipPutUserInGroupTupleResolver;
import org.example.keycloak.resolver.role_mapping.CreateRealmRoleMappingAssignRoleToUserTupleResolver;
import org.example.keycloak.resolver.user.CreateUserWithRolesAndGroupsTupleResolver;
import org.example.keycloak.resolver.group_membership.DeleteGroupMembershipRemoveUserFromGroupTupleResolver;
import org.example.keycloak.resolver.role_mapping.DeleteRealmRoleMappingRemoveRoleFromUserTupleResolver;
import org.example.keycloak.resolver.user.DeleteUserRemoveAnExistingUserTupleResolver;
import org.keycloak.events.admin.AdminEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ResolverFactory {
    public static final String ADMIN_EVENT_OPERATION_TYPE_CREATE = "CREATE";
    public static final String ADMIN_EVENT_OPERATION_TYPE_DELETE = "DELETE";

    private static ResolverFactory instance = null;

    private final Map<String, Map<String, Map<Pattern, BiFunction<AdminEvent, Matcher, TupleResolver>>>> regexMap = new HashMap<>();

    public ResolverFactory() {
        {
            // For resourceType "GROUP"

            Map<String, Map<Pattern, BiFunction<AdminEvent, Matcher, TupleResolver>>> groupOperationTypePatternCommandFunctionMap = new HashMap<>();

            Map<Pattern, BiFunction<AdminEvent, Matcher, TupleResolver>> groupCreatePatternCommandFunctionMap = new HashMap<>();
            groupCreatePatternCommandFunctionMap.put(Pattern.compile(CreateGroupNewGroupAtRootLevelWithOptionalSubgroupsTupleResolver.REGEX), CreateGroupNewGroupAtRootLevelWithOptionalSubgroupsTupleResolver::new);
            groupCreatePatternCommandFunctionMap.put(Pattern.compile("groups/(.*)/children"), CreateGroupNewGroupInOtherGroupAkaSubgroupTupleResolver::new);
            groupOperationTypePatternCommandFunctionMap.put(ADMIN_EVENT_OPERATION_TYPE_CREATE, groupCreatePatternCommandFunctionMap);

            Map<Pattern, BiFunction<AdminEvent, Matcher, TupleResolver>> groupDeletePatternCommandFunctionMap = new HashMap<>();
            groupDeletePatternCommandFunctionMap.put(Pattern.compile(DeleteGroupCanBeAnywhereTupleResolver.REGEX), DeleteGroupCanBeAnywhereTupleResolver::new); // compiled regex derived from resourcePath of delete_group__can_be_anywhere.json
            groupOperationTypePatternCommandFunctionMap.put(ADMIN_EVENT_OPERATION_TYPE_DELETE, groupDeletePatternCommandFunctionMap);

            regexMap.put("GROUP", groupOperationTypePatternCommandFunctionMap);
        }

        {
            // For resourceType "GROUP_MEMBERSHIP"
            Map<String, Map<Pattern, BiFunction<AdminEvent, Matcher, TupleResolver>>> groupMembershipOperationTypePatternCommandFunctionMap = new HashMap<>();

            Map<Pattern, BiFunction<AdminEvent, Matcher, TupleResolver>> groupMembershipCreatePatternCommandFunctionMap = new HashMap<>();
            groupMembershipCreatePatternCommandFunctionMap.put(Pattern.compile("users/(.*)/groups/(.*)"), CreateGroupMembershipPutUserInGroupTupleResolver::new);
            groupMembershipOperationTypePatternCommandFunctionMap.put(ADMIN_EVENT_OPERATION_TYPE_CREATE, groupMembershipCreatePatternCommandFunctionMap);

            Map<Pattern, BiFunction<AdminEvent, Matcher, TupleResolver>> groupMembershipDeletePatternCommandFunctionMap = new HashMap<>();
            groupMembershipDeletePatternCommandFunctionMap.put(Pattern.compile("users/(.*)/groups/(.*)"), DeleteGroupMembershipRemoveUserFromGroupTupleResolver::new);
            groupMembershipOperationTypePatternCommandFunctionMap.put(ADMIN_EVENT_OPERATION_TYPE_DELETE, groupMembershipDeletePatternCommandFunctionMap);

            regexMap.put("GROUP_MEMBERSHIP", groupMembershipOperationTypePatternCommandFunctionMap);
        }

        {
            // For resourceType "REALM_ROLE"
            Map<String, Map<Pattern, BiFunction<AdminEvent, Matcher, TupleResolver>>> realmRoleOperationTypePatternCommandFunctionMap = new HashMap<>();

            Map<Pattern, BiFunction<AdminEvent, Matcher, TupleResolver>> realmRoleCreatePatternCommandFunctionMap = new HashMap<>();
            realmRoleCreatePatternCommandFunctionMap.put(Pattern.compile(CreateRealmRoleNewRoleAtRootLevelTupleResolver.REGEX), CreateRealmRoleNewRoleAtRootLevelTupleResolver::new); // compiled regex derived from resourcePath of create_realm_role__new_role_at_root_level.json
            realmRoleCreatePatternCommandFunctionMap.put(Pattern.compile(CreateRealmRoleNewRoleCollectionAsCompositeForOtherRoleTupleResolver.REGEX), CreateRealmRoleNewRoleCollectionAsCompositeForOtherRoleTupleResolver::new); // compiled regex derived from resourcePath of create_realm_role__new_role_collection_as_composite_for_other_role.json
            realmRoleCreatePatternCommandFunctionMap.put(Pattern.compile(CreateRealmRoleSingleRoleAdd1ToNRolesAsCompositeTupleResolver.REGEX), CreateRealmRoleSingleRoleAdd1ToNRolesAsCompositeTupleResolver::new); // compiled regex derived from resourcePath of create_realm_role__single_role_add_1_to_n_roles_as_composite.json
            realmRoleOperationTypePatternCommandFunctionMap.put(ADMIN_EVENT_OPERATION_TYPE_CREATE, realmRoleCreatePatternCommandFunctionMap);

            Map<Pattern, BiFunction<AdminEvent, Matcher, TupleResolver>> realmRoleDeletePatternCommandFunctionMap = new HashMap<>();
            realmRoleDeletePatternCommandFunctionMap.put(Pattern.compile(DeleteRealmRoleRemoveCompositeCollectionTupleResolver.REGEX), DeleteRealmRoleRemoveCompositeCollectionTupleResolver::new); // compiled regex derived from resourcePath of delete_realm_role__remove_composite_collection.json
            realmRoleDeletePatternCommandFunctionMap.put(Pattern.compile(DeleteRealmRoleSingleRoleRemove1ToNCompositeRolesTupleResolver.REGEX), DeleteRealmRoleSingleRoleRemove1ToNCompositeRolesTupleResolver::new);
            realmRoleDeletePatternCommandFunctionMap.put(Pattern.compile(DeleteRealmRoleRemoveRoleAtRootLevelTupleResolver.REGEX), DeleteRealmRoleRemoveRoleAtRootLevelTupleResolver::new); // compiled regex derived from resourcePath of delete_realm_role__remove_role_at_root_level.json
            realmRoleOperationTypePatternCommandFunctionMap.put(ADMIN_EVENT_OPERATION_TYPE_DELETE, realmRoleDeletePatternCommandFunctionMap);

            regexMap.put("REALM_ROLE", realmRoleOperationTypePatternCommandFunctionMap);
        }

        {
            // For resourceType "REALM_ROLE_MAPPING"
            Map<String, Map<Pattern, BiFunction<AdminEvent, Matcher, TupleResolver>>> realmRoleMappingOperationTypePatternCommandFunctionMap = new HashMap<>();
            Map<Pattern, BiFunction<AdminEvent, Matcher, TupleResolver>> realmRoleMappingCreatePatternCommandFunctionMap = new HashMap<>();
            realmRoleMappingCreatePatternCommandFunctionMap.put(Pattern.compile(CreateRealmRoleMappingAssignRealmRoleToGroupTupleResolver.REGEX), CreateRealmRoleMappingAssignRealmRoleToGroupTupleResolver::new); // compiled regex derived from resourcePath of create_realm_role_mapping__assign_realm_role_to_group.json
            realmRoleMappingCreatePatternCommandFunctionMap.put(Pattern.compile(CreateRealmRoleMappingAssignRoleToUserTupleResolver.REGEX), CreateRealmRoleMappingAssignRoleToUserTupleResolver::new); // compiled regex derived from resourcePath of create_realm_role_mapping__assign_role_to_user.json
            realmRoleMappingOperationTypePatternCommandFunctionMap.put(ADMIN_EVENT_OPERATION_TYPE_CREATE, realmRoleMappingCreatePatternCommandFunctionMap);
            Map<Pattern, BiFunction<AdminEvent, Matcher, TupleResolver>> realmRoleMappingDeletePatternCommandFunctionMap = new HashMap<>();
            realmRoleMappingDeletePatternCommandFunctionMap.put(Pattern.compile(DeleteRealmRoleMappingRemoveRealmRoleFromGroupTupleResolver.REGEX), DeleteRealmRoleMappingRemoveRealmRoleFromGroupTupleResolver::new); // compiled regex derived from resourcePath of delete_realm_role_mapping__remove_realm_role_from_group.json
            realmRoleMappingDeletePatternCommandFunctionMap.put(Pattern.compile(DeleteRealmRoleMappingRemoveRoleFromUserTupleResolver.REGEX), DeleteRealmRoleMappingRemoveRoleFromUserTupleResolver::new); // compiled regex derived from resourcePath of delete_realm_role_mapping__remove_role_from_user.json
            realmRoleMappingOperationTypePatternCommandFunctionMap.put(ADMIN_EVENT_OPERATION_TYPE_DELETE, realmRoleMappingDeletePatternCommandFunctionMap);
            regexMap.put("REALM_ROLE_MAPPING", realmRoleMappingOperationTypePatternCommandFunctionMap);
        }

        {
            // For resourceType "USER"
            Map<String, Map<Pattern, BiFunction<AdminEvent, Matcher, TupleResolver>>> userOperationTypePatternCommandFunctionMap = new HashMap<>();
            Map<Pattern, BiFunction<AdminEvent, Matcher, TupleResolver>> userCreatePatternCommandFunctionMap = new HashMap<>();
            userCreatePatternCommandFunctionMap.put(Pattern.compile(CreateUserWithRolesAndGroupsTupleResolver.REGEX), CreateUserWithRolesAndGroupsTupleResolver::new); // compiled regex derived from resourcePath of create_user_with_roles_and_groups.json
            userOperationTypePatternCommandFunctionMap.put(ADMIN_EVENT_OPERATION_TYPE_CREATE, userCreatePatternCommandFunctionMap);
            Map<Pattern, BiFunction<AdminEvent, Matcher, TupleResolver>> userDeletePatternCommandFunctionMap = new HashMap<>();
            userDeletePatternCommandFunctionMap.put(Pattern.compile(DeleteUserRemoveAnExistingUserTupleResolver.REGEX), DeleteUserRemoveAnExistingUserTupleResolver::new); // compiled regex derived from resourcePath of delete_user__remove_an_existing_user.json
            userOperationTypePatternCommandFunctionMap.put(ADMIN_EVENT_OPERATION_TYPE_DELETE, userDeletePatternCommandFunctionMap);
            regexMap.put("USER", userOperationTypePatternCommandFunctionMap);
        }
    }

    public static ResolverFactory getInstance() {
        if(instance == null) {
            instance = new ResolverFactory();
        }
        return instance;
    }

    public Stream<TupleResolver> tupleResolver(AdminEvent adminEvent) {
        String operationType = adminEvent.getOperationType().toString();
        String resourceType = adminEvent.getResourceType().toString();
        String resourcePath = adminEvent.getResourcePath();

        Map<Pattern, BiFunction<AdminEvent, Matcher, TupleResolver>> patternFunctionMap = regexMap.getOrDefault(resourceType, Collections.emptyMap()).getOrDefault(operationType, Collections.emptyMap());
        return patternFunctionMap.keySet().stream().map(pattern -> {
            Matcher matcher = pattern.matcher(resourcePath);
            if (matcher.matches()) {
                return patternFunctionMap.get(pattern).apply(adminEvent, matcher);
            }
            return TupleResolver.VOID_TUPLE_RESOLVER;
        });
    }
}