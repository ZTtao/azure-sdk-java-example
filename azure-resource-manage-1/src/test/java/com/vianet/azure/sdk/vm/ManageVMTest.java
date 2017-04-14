package com.vianet.azure.sdk.vm;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.credentials.UserTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.*;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.vianet.azure.sdk.Application;

import java.util.Date;
import java.util.Map;
import java.util.function.Consumer;


public class ManageVMTest {

    public static void main(String[] args) throws Exception {
        ApplicationTokenCredentials tokenCredentials = new ApplicationTokenCredentials(Application.CLIENT_ID, Application.TENTANT, Application.CLIENT_SECRET, AzureEnvironment.AZURE_CHINA).withDefaultSubscriptionId(Application.SUB_ID);
        String token = tokenCredentials.getToken();
        System.out.println("token" + token);

        Azure azure = Azure.authenticate(tokenCredentials).withDefaultSubscription();
        ManageVMTest testAdal = new ManageVMTest();
        testAdal.listDiskList(azure);

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
                .withAdminUsername(userName)
                .withAdminPassword(password)
                .withNewDataDisk(10)
                .withSize(VirtualMachineSizeTypes.STANDARD_DS1)
                .create();
    }


    public void listDiskList(Azure azure) {
        VirtualMachine windowsVM = azure.virtualMachines().getById("/subscriptions/e0fbea86-6cf2-4b2d-81e2-9c59f4f96bcb/resourceGroups/yuvmtest/providers/Microsoft.Compute/virtualMachines/yuvmtest");
        System.out.println(windowsVM.id());
        System.out.println(windowsVM.vmId());
        System.out.println(windowsVM.computerName());
        System.out.println(windowsVM.name());
        System.out.println(windowsVM.type());
        windowsVM.networkInterfaceIds().forEach(new Consumer<String>() {
            @Override
            public void accept(String id) {
                NetworkInterface networkInterface = azure.networkInterfaces().getById(id);
                System.out.println(networkInterface.name());
            }
        });
        PagedList<VirtualMachineSize> virtualMachineSizes = azure.virtualMachines().sizes().listByRegion("chinanorth");
        virtualMachineSizes.forEach(new Consumer<VirtualMachineSize>() {
            @Override
            public void accept(VirtualMachineSize virtualMachineSize) {
                System.out.println(virtualMachineSize.name());
            }
        });
//        PagedList<VirtualMachineImage> imageReferences = azure.virtualMachineImages().listByRegion("chinanorth");
//        imageReferences.forEach(new Consumer<VirtualMachineImage>() {
//            @Override
//            public void accept(VirtualMachineImage virtualMachineImage) {
//                System.out.println(virtualMachineImage.publisherName());
//            }
//        });
    }

}
