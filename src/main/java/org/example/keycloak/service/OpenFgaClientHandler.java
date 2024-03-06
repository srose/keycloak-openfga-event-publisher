package org.example.keycloak.service;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientReadAuthorizationModelsResponse;
import dev.openfga.sdk.api.client.model.ClientWriteRequest;
import dev.openfga.sdk.api.configuration.ClientConfiguration;
import dev.openfga.sdk.api.configuration.ClientWriteOptions;
import dev.openfga.sdk.api.model.AuthorizationModel;
import dev.openfga.sdk.api.model.ListStoresResponse;
import dev.openfga.sdk.api.model.ReadAuthorizationModelsResponse;
import dev.openfga.sdk.api.model.Store;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.utils.StringUtil;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

public class OpenFgaClientHandler {

    protected final Config.Scope config;

    public OpenFgaClient getFgaClient() {
        return fgaClient;
    }

    private final OpenFgaClient fgaClient;
    private final OpenFgaModel fgaHelper;
    private Boolean isClientInitialized = false;

    private final ClientWriteOptions clientWriteOptions;

    protected static final String OPENFGA_API_URL = "openfgaApiUrl";
    protected static final String OPENFGA_STORE_ID = "openfgaStoreId";
    protected static final String OPENFGA_AUTHORIZATION_MODEL_ID = "openfgaAuthorizationModelId";

    private static final Logger LOG = Logger.getLogger(OpenFgaClientHandler.class);

    public OpenFgaClientHandler(Config.Scope config ) throws FgaInvalidParameterException {
        this.config = config;

        ClientConfiguration configuration = new ClientConfiguration()
                .apiUrl(getOpenFgaApiUrl())
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(5));

        if(StringUtil.isNotBlank(getOpenFgaOpenStoreId())
                && StringUtil.isNotBlank(getOpenFgaAuthorizationModelId())) {
            configuration.storeId(getOpenFgaOpenStoreId());
            configuration.authorizationModelId(getOpenFgaAuthorizationModelId());
            this.isClientInitialized = true;
        };

        this.fgaHelper = new OpenFgaModel();
        this.clientWriteOptions = new ClientWriteOptions();
        this.clientWriteOptions.disableTransactions(false);
        this.fgaClient = new OpenFgaClient(configuration);

        if(this.isClientInitialized) {
            ClientReadAuthorizationModelsResponse modelResponse = null;
            try {
                modelResponse = this.fgaClient.readAuthorizationModels().get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
            this.fgaHelper.loadModel(modelResponse.getAuthorizationModels().get(0));
        }
    }

    public void publish(String eventId , ClientWriteRequest request) {
        try {
            if(!this.isClientInitialized && !this.discoverClientConfiguration()) {
                LOG.errorf("Unable to initialized OpenFga client. Discarding  event %s",  eventId);
            } else {
                LOG.debugf("Publishing event id %", eventId);
                var response = fgaClient.write(request, this.clientWriteOptions).get();
                LOG.debugf("Successfully sent tuple key to OpenFga, response: %s", response);
            }
        } catch (FgaInvalidParameterException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



    private boolean discoverClientConfiguration() throws FgaInvalidParameterException, ExecutionException, InterruptedException {
        LOG.info("Discover store and authorization model");
        ListStoresResponse stores = fgaClient.listStores().get();
        if(!stores.getStores().isEmpty()) {
           Store store = stores.getStores().get(0);
           LOG.infof("Found store id: %s", store.getId());
           this.fgaClient.setStoreId(store.getId());
           ReadAuthorizationModelsResponse authorizationModels = fgaClient.readAuthorizationModels().get();
           if(!authorizationModels.getAuthorizationModels().isEmpty()) {
               AuthorizationModel model = authorizationModels.getAuthorizationModels().get(0);
               LOG.infof("Found authorization model id: %s", model.getId());
               this.fgaClient.setAuthorizationModelId(model.getId());
               this.fgaHelper.loadModel(model);
               this.isClientInitialized = true;
           }
        }
        return this.isClientInitialized;
    }

    public String getOpenFgaApiUrl() {
        return config.get(OPENFGA_API_URL) != null ? config.get(OPENFGA_API_URL) : "http://openfga:8080";
    }

    public String getOpenFgaOpenStoreId() {
        return config.get(OPENFGA_STORE_ID) != null ? config.get(OPENFGA_STORE_ID) : "";
    }

    public String getOpenFgaAuthorizationModelId() {
        return config.get(OPENFGA_AUTHORIZATION_MODEL_ID) != null? config.get(OPENFGA_AUTHORIZATION_MODEL_ID) : "";
    }
}
