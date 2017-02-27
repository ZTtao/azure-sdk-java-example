package com.vianet.azure.sdk.vm;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.vianet.azure.sdk.Application;

import java.util.Date;


public class ManageVMTest {

    public static void main(String[] args) throws Exception {
        ApplicationTokenCredentials tokenCredentials = new ApplicationTokenCredentials(Application.CLIENT_ID, Application.TENTANT, Application.CLIENT_SECRET, AzureEnvironment.AZURE_CHINA).withDefaultSubscriptionId(Application.SUB_ID);
        Azure azure = Azure.authenticate(tokenCredentials).withDefaultSubscription();
        ManageVMTest testAdal = new ManageVMTest();
        testAdal.retrieveResource(azure);
        testAdal.vmCreate(azure);

        System.exit(1);
    }

    public void retrieveResource(Azure azure) {
        PagedList<ResourceGroup> groups = azure.resourceGroups().list();
        for (ResourceGroup group : groups) {
            System.out.println(group.name());
        }
    }

    public void vmCreate(Azure azure) throws Exception{
        Date t1 = new Date();
        String resourceGroupName = "kevin-group";
        String windowVmName = "window-" + t1.getTime();
        String userName = "kevin";
        String password = "Chenrui@123456";


        VirtualMachine windowsVM = azure.virtualMachines().define(windowVmName)
                .withRegion(Region.CHINA_EAST)
                .withNewResourceGroup(resourceGroupName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withoutPrimaryPublicIpAddress()
                .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                .withAdminUserName(userName)
                .withPassword(password)
                .withNewDataDisk(10)
                .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                .create();
    }

}
