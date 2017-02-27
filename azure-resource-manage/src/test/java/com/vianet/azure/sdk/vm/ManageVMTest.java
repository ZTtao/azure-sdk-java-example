package com.vianet.azure.sdk.vm;


import com.microsoft.azure.management.compute.ComputeManagementClient;
import com.microsoft.azure.management.compute.ComputeManagementService;
import com.microsoft.azure.management.compute.models.*;
import com.microsoft.azure.management.network.NetworkResourceProviderClient;
import com.microsoft.azure.management.network.NetworkResourceProviderService;
import com.microsoft.azure.management.network.models.*;
import com.microsoft.azure.utility.AuthHelper;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;
import com.vianet.azure.sdk.Application;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class ManageVMTest {

    public static void main(String[] args) throws Exception {
        ManageVMTest testVM091 = new ManageVMTest();
        testVM091.vmCreate();
        System.exit(1);
    }

    public void listPublish() throws Exception {
        Configuration config = createConfiguration();
        ComputeManagementClient computeManagementClient = ComputeManagementService.create(config);
        VirtualMachineImageListParameters parameters = new VirtualMachineImageListParameters();
        parameters.setLocation("chinanorth");
        VirtualMachineImageResourceList virtualMachineImageResourceList = computeManagementClient.getVirtualMachineImagesOperations().listPublishers(parameters);
        virtualMachineImageResourceList.getResources().forEach(new Consumer<VirtualMachineImageResource>() {
            @Override
            public void accept(VirtualMachineImageResource virtualMachineImageResource) {
                System.out.println("==================================");
                System.out.println(virtualMachineImageResource.getId());
                System.out.println(virtualMachineImageResource.getLocation());
                System.out.println(virtualMachineImageResource.getName());
            }
        });
    }

    public void vmCreate() throws Exception{
        try {
            Date t = new Date();
            String region = "chinanorth";
            String resourceGroupName = "kevingroup";
            String storageAccountName = "kevintest1";
            String container = "vhds";
            String vmName = "kevin-" + t.getTime();
            String adminUserName = "kevin";
            String adminPassword = "Chenrui@123456";

            String nicName = "kevin-nic-" + t.getTime();
            String ipConfigName = "kevin-ip-" + t.getTime();
            String vnetName = "kevin-net-" + t.getTime();

            Configuration config = createConfiguration();
            ComputeManagementClient computeManagementClient = ComputeManagementService.create(config);
            NetworkResourceProviderClient networkResourceProviderClient = NetworkResourceProviderService.create(config);

            String vhdContainer = getVhdContainerUrl(storageAccountName, container);
            String osVhduri = vhdContainer + String.format("/os%s.vhd", "osvhd-" + t.getTime());

            VirtualMachine vm = new VirtualMachine(region);
            vm.setName(vmName);
            vm.setType("Microsoft.Compute/virtualMachines");

            //set hardware profile
            HardwareProfile hwProfile = new HardwareProfile();
            hwProfile.setVirtualMachineSize(VirtualMachineSizeTypes.STANDARD_A0);
            vm.setHardwareProfile(hwProfile);

            //set storage profile
            StorageProfile sto = new StorageProfile();
            sto.setImageReference(getWindowsServerDefaultImage(computeManagementClient, region));

            VirtualHardDisk vhardDisk = new VirtualHardDisk();
            vhardDisk.setUri(osVhduri);
            OSDisk osDisk = new OSDisk("osdisk", vhardDisk, DiskCreateOptionTypes.FROMIMAGE);
            osDisk.setCaching(CachingTypes.NONE);
            sto.setOSDisk(osDisk);
            vm.setStorageProfile(sto);

            //set network profile
            VirtualNetwork virtualNetwork = createVNET(networkResourceProviderClient, region, resourceGroupName, vnetName);
            NetworkInterface networkInterface = createNIC(networkResourceProviderClient, region, resourceGroupName, nicName, ipConfigName, virtualNetwork.getSubnets().get(0));
            NetworkProfile networkProfile = new NetworkProfile();
            ArrayList<NetworkInterfaceReference> nirs = new ArrayList<NetworkInterfaceReference>(1);
            NetworkInterfaceReference nir = new NetworkInterfaceReference();
            nir.setReferenceUri(networkInterface.getId());
            nirs.add(nir);
            networkProfile.setNetworkInterfaces(nirs);
            vm.setNetworkProfile(networkProfile);

            //set os profile
            OSProfile osProfile = new OSProfile();
            osProfile.setAdminPassword(adminPassword);
            osProfile.setAdminUsername(adminUserName);
            osProfile.setAdminPassword(adminPassword);
            osProfile.setComputerName("keivn-window" );
            vm.setOSProfile(osProfile);

            VirtualMachineCreateOrUpdateResponse vmCreationResponse = computeManagementClient.getVirtualMachinesOperations().beginCreatingOrUpdating(resourceGroupName, vm);

        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public ImageReference getWindowsServerDefaultImage(ComputeManagementClient computeManagementClient, String location)
            throws ServiceException, IOException, URISyntaxException {
        return getDefaultVMImage(computeManagementClient, location, "MicrosoftWindowsServer", "WindowsServer", "2012-R2-Datacenter");
    }

    public ImageReference getUbuntuServerDefaultImage(
            ComputeManagementClient computeManagementClient, String location)
            throws ServiceException, IOException, URISyntaxException {
        return getDefaultVMImage(computeManagementClient, location, "Canonical", "UbuntuServer", "15.04");
    }

    public ImageReference getDefaultVMImage(
            ComputeManagementClient computeManagementClient, String location, String publisher,
            String offer, String sku)
            throws IOException, ServiceException, URISyntaxException {
        ArrayList<VirtualMachineImageResource> queryResult = queryVMImage(
                computeManagementClient, location, publisher, offer, sku, "$top=1");
        if (queryResult.size() < 1) {
            throw new IllegalArgumentException(
                    String.format("no image found for %s, %s, %s, %s", location, publisher, offer, sku));
        }

        VirtualMachineImageResource image = queryResult.get(0);
        ImageReference defaultImage = new ImageReference();
        defaultImage.setOffer(offer);
        defaultImage.setPublisher(publisher);
        defaultImage.setSku(sku);
        defaultImage.setVersion("latest");

        return defaultImage;
    }

    public ArrayList<VirtualMachineImageResource> queryVMImage(
            ComputeManagementClient computeManagementClient, String location, String publisher, String offer,
            String sku, String filterExpression)
            throws ServiceException, IOException, URISyntaxException {
        VirtualMachineImageListParameters param = new VirtualMachineImageListParameters();
        param.setLocation(location);
        param.setPublisherName(publisher);
        param.setOffer(offer);
        param.setSkus(sku);
        param.setFilterExpression(filterExpression);
        VirtualMachineImageResourceList images = computeManagementClient.getVirtualMachineImagesOperations().list(param);
        return images.getResources();
    }

    private NetworkInterface createNIC(NetworkResourceProviderClient networkResourceProviderClient, String location, String resourceGroup, String nicName, String ipConfigName, Subnet subnet) throws IOException, ServiceException {
        NetworkInterface nic = new NetworkInterface(location);
        nic.setName(nicName);

        //set ipconfiguration
        NetworkInterfaceIpConfiguration nicConfig = new NetworkInterfaceIpConfiguration();
        nicConfig.setName(ipConfigName);
        nicConfig.setPrivateIpAllocationMethod(IpAllocationMethod.DYNAMIC);
        nicConfig.setSubnet(subnet);
        ArrayList<NetworkInterfaceIpConfiguration> ipConfigs = new ArrayList<NetworkInterfaceIpConfiguration>(1);
        ipConfigs.add(nicConfig);
        nic.setIpConfigurations(ipConfigs);

        try
        {
            AzureAsyncOperationResponse response = networkResourceProviderClient.getNetworkInterfacesOperations().createOrUpdate(resourceGroup, nicName, nic);
        } catch (ExecutionException ee) {
            ee.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        NetworkInterface createdNic = networkResourceProviderClient.getNetworkInterfacesOperations()
                .get(resourceGroup, nicName)
                .getNetworkInterface();
        return createdNic;
    }

    private VirtualNetwork createVNET(NetworkResourceProviderClient networkResourceProviderClient, String location, String resourceGroup, String vnetName) throws IOException, ServiceException {
        VirtualNetwork vnet = new VirtualNetwork(location);
        String subnetName = vnetName + "-Subnet";

        // set AddressSpace
        AddressSpace asp = new AddressSpace();
        ArrayList<String> addrPrefixes = new ArrayList<String>(1);
        addrPrefixes.add("10.0.0.0/16");
        asp.setAddressPrefixes(addrPrefixes);
        vnet.setAddressSpace(asp);

        // set DhcpOptions
        DhcpOptions dop = new DhcpOptions();
        ArrayList<String> dnsServers = new ArrayList<String>(2);
        dnsServers.add("10.1.1.1");
        dop.setDnsServers(dnsServers);
        vnet.setDhcpOptions(dop);

        // set subNet
        Subnet subnet = new Subnet("10.0.0.0/24");
        subnet.setName(subnetName);
        ArrayList<Subnet> subNets = new ArrayList<Subnet>(1);
        subNets.add(subnet);
        vnet.setSubnets(subNets);

        try
        {
            AzureAsyncOperationResponse response = networkResourceProviderClient.getVirtualNetworksOperations()
                    .createOrUpdate(resourceGroup, vnetName, vnet);
        } catch (ExecutionException ee) {
            ee.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        VirtualNetwork createdVnet = networkResourceProviderClient.getVirtualNetworksOperations()
                .get(resourceGroup, vnetName)
                .getVirtualNetwork();
        return createdVnet;
    }


    public String getVhdContainerUrl(String storageAccountName, String containerName) {
        return String.format("https://%s.blob.core.chinacloudapi.cn/%s", storageAccountName, containerName);
    }

    public Configuration createConfiguration() throws Exception {
        Configuration config = ManagementConfiguration.configure(
                null,
                new URI(Application.MANAGEMENT_EBDPOINT),
                Application.SUB_ID,
                AuthHelper.getAccessTokenFromServicePrincipalCredentials(Application.MANAGEMENT_EBDPOINT, Application.AUTHORITY, Application.TENTANT, Application.CLIENT_ID, Application.CLIENT_SECRET).getAccessToken());
        config.setProperty(ManagementConfiguration.URI, new URI(Application.MANAGEMENT_EBDPOINT));
        return config;
    }

}
