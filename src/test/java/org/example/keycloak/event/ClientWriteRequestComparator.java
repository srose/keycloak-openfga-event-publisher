package org.example.keycloak.event;

import dev.openfga.sdk.api.client.model.ClientTupleKeyWithoutCondition;
import dev.openfga.sdk.api.client.model.ClientWriteRequest;
import dev.openfga.sdk.api.client.model.ClientTupleKey;

public class ClientWriteRequestComparator {

    public boolean areClientWriteRequestsEqual(ClientWriteRequest expected, ClientWriteRequest actual) {
        // Compare deletes
        if (expected.getDeletes().size() != actual.getDeletes().size()) {
            return false;
        }

        for (int i = 0; i < expected.getDeletes().size(); i++) {
            if (!areClientTupleKeysWithoutConditionEqual(expected.getDeletes().get(i), actual.getDeletes().get(i))) {
                return false;
            }
        }

        // Compare writes
        if (expected.getWrites().size() != actual.getWrites().size()) {
            return false;
        }

        for (int i = 0; i < expected.getWrites().size(); i++) {
            if (!areClientTupleKeysEqual(expected.getWrites().get(i), actual.getWrites().get(i))) {
                return false;
            }
        }

        return true;
    }

    private boolean areClientTupleKeysWithoutConditionEqual(ClientTupleKeyWithoutCondition expected, ClientTupleKeyWithoutCondition actual) {
        if (expected == null || actual == null) {
            return false;
        }

        if (expected.getUser() == null) {
            if (actual.getUser() != null) {
                return false;
            }
        } else if (!expected.getUser().equals(actual.getUser())) {
            return false;
        }

        if (expected.getRelation() == null) {
            if (actual.getRelation() != null) {
                return false;
            }
        } else if (!expected.getRelation().equals(actual.getRelation())) {
            return false;
        }

        if (expected.getObject() == null) {
            if (actual.getObject() != null) {
                return false;
            }
        } else if (!expected.getObject().equals(actual.getObject())) {
            return false;
        }

        return true;

    }

    private boolean areClientTupleKeysEqual(ClientTupleKey expected, ClientTupleKey actual) {
        if (expected == null || actual == null) {
            return false;
        }

        if (expected.getUser() == null) {
            if (actual.getUser() != null) {
                return false;
            }
        } else if (!expected.getUser().equals(actual.getUser())) {
            return false;
        }

        if (expected.getRelation() == null) {
            if (actual.getRelation() != null) {
                return false;
            }
        } else if (!expected.getRelation().equals(actual.getRelation())) {
            return false;
        }

        if (expected.getCondition() == null) {
            if (actual.getCondition() != null) {
                return false;
            }
        } else if (!expected.getCondition().equals(actual.getCondition())) {
            return false;
        }

        if (expected.getObject() == null) {
            if (actual.getObject() != null) {
                return false;
            }
        } else if (!expected.getObject().equals(actual.getObject())) {
            return false;
        }

        return true;
    }

}
