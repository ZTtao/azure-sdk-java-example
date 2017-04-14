package com.vianet.azure.sdk.billing;


import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.ComputeUsage;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.vianet.azure.sdk.Application;
import org.junit.Test;

import java.io.IOException;

public class ResourceUsage {

    @Test
    public void testResourceUsage() throws IOException {
        ApplicationTokenCredentials tokenCredentials = new ApplicationTokenCredentials(Application.CLIENT_ID, Application.TENTANT, Application.CLIENT_SECRET, AzureEnvironment.AZURE_CHINA).withDefaultSubscriptionId(Application.SUB_ID);
        String token = tokenCredentials.getToken();
        System.out.println("token" + token);

        Azure azure = Azure.authenticate(tokenCredentials).withDefaultSubscription();
        PagedList<ComputeUsage> results = azure.computeUsages().listByRegion(Region.CHINA_EAST);
        for(ComputeUsage computeUsage : results) {
            System.out.println(computeUsage.name().value());
            System.out.println(computeUsage.unit());
            System.out.println(computeUsage.currentValue());
            System.out.println(computeUsage.limit());
        }
    }

}
